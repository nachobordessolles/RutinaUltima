package com.tuapp.rutina.presentation.screens

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import com.tuapp.rutina.presentation.theme.Accent
import com.tuapp.rutina.presentation.theme.Background
import com.tuapp.rutina.presentation.theme.Teal
import com.tuapp.rutina.presentation.theme.TextPrimary
import com.tuapp.rutina.presentation.theme.TextSecondary
import kotlinx.coroutines.delay

/** Segundos para ponerse en posición antes de que arranque el tiempo del ejercicio. */
const val PREP_SECONDS = 5

/**
 * Temporizador de un ejercicio con tiempo (plancha, hollow hold…).
 *
 * Fases: preparación ([PREP_SECONDS]) → hasta el mínimo de [range] → (si es un rango) hasta el máximo.
 * Vibra al empezar, al llegar al mínimo y al llegar al máximo; al llegar al máximo termina solo.
 * [startAt] es un instante de SystemClock.elapsedRealtime().
 */
@Composable
fun WorkTimerScreen(
    startAt: Long,
    range: IntRange,
    exerciseName: String,
    setLabel: String,
    onSkipPrep: () -> Unit,
    onFinish: () -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    val finish by rememberUpdatedState(onFinish)
    val prepMs = PREP_SECONDS * 1000L
    val minMs = range.first * 1000L
    val maxMs = range.last * 1000L
    val isRange = maxMs > minMs

    KeepScreenOn()

    LaunchedEffect(startAt) {
        val initialWork = SystemClock.elapsedRealtime() - startAt - prepMs
        // Si la pantalla se recrea a mitad del ejercicio, no repetir las vibraciones ya hechas.
        var startedVibrated = initialWork > 500
        var minVibrated = initialWork >= minMs
        while (true) {
            now = SystemClock.elapsedRealtime()
            val work = now - startAt - prepMs
            if (!startedVibrated && work >= 0) {
                vibrateShort(context)
                startedVibrated = true
            }
            if (isRange && !minVibrated && work >= minMs) {
                vibrateDouble(context)
                minVibrated = true
            }
            if (work >= maxMs) {
                vibrateRestEnd(context)
                finish()
                break
            }
            delay(100)
        }
    }

    val elapsed = now - startAt
    val work = elapsed - prepMs
    val preparing = work < 0
    val minReached = !preparing && work >= minMs

    val header: String
    val remainingMs: Long
    val progress: Float
    val color = if (minReached) Teal else Accent
    val subtitle: String
    when {
        preparing -> {
            header = "PREPARATE"
            remainingMs = -work
            progress = remainingMs / prepMs.toFloat()
            subtitle = exerciseName
        }
        !minReached -> {
            header = setLabel.uppercase()
            remainingMs = minMs - work
            progress = work / minMs.toFloat()
            subtitle = if (isRange) "Mín ${range.first} s · máx ${range.last} s" else exerciseName
        }
        else -> {
            header = "¡MÍNIMO HECHO!"
            remainingMs = (maxMs - work).coerceAtLeast(0)
            progress = (work - minMs) / (maxMs - minMs).toFloat()
            subtitle = "Sigue hasta ${range.last} s o termina"
        }
    }
    val seconds = ((remainingMs + 999) / 1000).toInt()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center,
    ) {
        TimerRing(progress, color)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(header, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Text(
                if (preparing) seconds.toString() else formatSeconds(seconds),
                color = TextPrimary,
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                subtitle,
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.padding(horizontal = 36.dp),
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SmallButton("Cancelar", primary = false, onClick = onCancel)
                if (preparing) {
                    SmallButton("Ya", primary = true, onClick = onSkipPrep)
                } else {
                    // Ignorar toques del primer segundo: "Ya" está en el mismo lugar y un toque
                    // tardío terminaría la serie sin querer.
                    SmallButton("Terminar", primary = true, primaryColor = color) {
                        if (SystemClock.elapsedRealtime() - startAt - prepMs >= 1000) finish()
                    }
                }
            }
        }
    }
}
