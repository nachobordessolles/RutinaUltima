package com.tuapp.rutina.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

const val RUTINA_URL =
    "https://raw.githubusercontent.com/nachobordessolles/RutinaUltima/main/app/src/main/assets/rutina.json"

private const val FILE_NAME = "rutina.json"
private const val TIMEOUT_MS = 15_000

class RutinaRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val localFile: File get() = File(context.filesDir, FILE_NAME)

    /** Usa la copia descargada si existe y es válida; si no, la incluida en assets. */
    suspend fun load(): List<Progression> = withContext(Dispatchers.IO) {
        val file = localFile
        if (file.exists()) {
            runCatching { parse(file.readText()) }.getOrNull()?.let { return@withContext it }
        }
        parse(context.assets.open(FILE_NAME).bufferedReader().use { it.readText() })
    }

    /** Fecha (epoch millis) de la última descarga exitosa, o null si nunca se actualizó. */
    fun lastUpdate(): Long? = localFile.takeIf { it.exists() }?.lastModified()

    /**
     * Descarga, valida y guarda la rutina. Si algo falla, la copia anterior queda intacta
     * y se devuelve el error.
     */
    suspend fun update(): Result<List<Progression>> = withContext(Dispatchers.IO) {
        runCatching {
            require(RUTINA_URL.startsWith("http")) { "URL no configurada" }
            val body = download(RUTINA_URL)
            val progressions = parse(body)
            val tmp = File(context.filesDir, "$FILE_NAME.tmp")
            tmp.writeText(body)
            if (!tmp.renameTo(localFile)) {
                tmp.delete()
                error("No se pudo guardar el archivo")
            }
            progressions
        }
    }

    private fun parse(text: String): List<Progression> {
        val list = json.decodeFromString<List<Progression>>(text)
        require(list.isNotEmpty()) { "La rutina está vacía" }
        return list
    }

    private fun download(url: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = TIMEOUT_MS
            conn.readTimeout = TIMEOUT_MS
            val code = conn.responseCode
            if (code !in 200..299) error("Error HTTP $code")
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }
}
