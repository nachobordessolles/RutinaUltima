package com.tuapp.rutina.data

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class Progression(
    val id: Int,
    val name: String = "",
    val weeks: String = "",
    val offset: Int = 0,
    val days: List<Day> = emptyList(),
)

@Serializable
data class Day(
    val title: String = "",
    val sub: String = "",
    val sections: List<Section> = emptyList(),
) {
    /** Todos los ejercicios del día en orden, ignorando las secciones. Se calcula una sola vez. */
    val allExercises: List<Exercise> by lazy { sections.flatMap { it.exs } }
}

@Serializable
data class Section(
    val lbl: String = "",
    val exs: List<Exercise> = emptyList(),
)

@Serializable
data class Exercise(
    val n: String = "",
    val s: String = "",
    val r: String = "",
    val d: String = "",
    val t: String = "",
    val e: List<String> = emptyList(),
    val resp: String = "",
    val isNew: Boolean = false,
    val isProgress: Boolean = false,
)

enum class ProgressionStatus { ACTIVE, COMPLETED, UPCOMING }

val BASE_DATE: LocalDate = LocalDate.of(2026, 4, 27)

fun Progression.startDate(): LocalDate = BASE_DATE.plusDays(offset.toLong())

/** La progresión está en curso entre su inicio y el inicio de la siguiente (en orden de lista). */
fun List<Progression>.statusOf(index: Int, today: LocalDate = LocalDate.now()): ProgressionStatus {
    val start = this[index].startDate()
    val nextStart = getOrNull(index + 1)?.startDate()
    val active = !today.isBefore(start) && (nextStart == null || today.isBefore(nextStart))
    return when {
        active -> ProgressionStatus.ACTIVE
        today.isAfter(start) -> ProgressionStatus.COMPLETED
        else -> ProgressionStatus.UPCOMING
    }
}

/** Índice de la progresión a mostrar al abrir la app: la en curso, si no la próxima, si no la última. */
fun List<Progression>.currentIndex(today: LocalDate = LocalDate.now()): Int {
    if (isEmpty()) return -1
    val statuses = indices.map { statusOf(it, today) }
    return statuses.indexOf(ProgressionStatus.ACTIVE).takeIf { it >= 0 }
        ?: statuses.indexOf(ProgressionStatus.UPCOMING).takeIf { it >= 0 }
        ?: lastIndex
}

private val NUMBER = Regex("""\d+""")

/** Cantidad de series ("3" → 3, "1-2" → 2). Mínimo 1. */
fun Exercise.setCount(): Int =
    NUMBER.findAll(s).mapNotNull { it.value.toIntOrNull() }.maxOrNull()?.coerceAtLeast(1) ?: 1

/** Descanso en segundos ("30 s" → 30, "2-3 min" → 120). 0 si no hay. */
fun Exercise.restSeconds(): Int {
    val first = NUMBER.find(d)?.value?.toIntOrNull() ?: return 0
    return if (d.contains("min", ignoreCase = true)) first * 60 else first
}

private val WEEKDAYS = mapOf(
    "lunes" to java.time.DayOfWeek.MONDAY,
    "martes" to java.time.DayOfWeek.TUESDAY,
    "miercoles" to java.time.DayOfWeek.WEDNESDAY,
    "jueves" to java.time.DayOfWeek.THURSDAY,
    "viernes" to java.time.DayOfWeek.FRIDAY,
    "sabado" to java.time.DayOfWeek.SATURDAY,
    "domingo" to java.time.DayOfWeek.SUNDAY,
)

/** Día de la semana según el subtítulo ("Lunes - …" → MONDAY), o null si no lo indica. */
fun Day.weekday(): java.time.DayOfWeek? {
    val word = java.text.Normalizer.normalize(sub.trim().substringBefore(' ').substringBefore('-'), java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{M}"), "")
        .lowercase()
    return WEEKDAYS[word]
}

/** True si todas las series de todos los ejercicios del día están hechas. */
fun Day.isComplete(setsDone: List<Int>): Boolean =
    allExercises.isNotEmpty() && allExercises.withIndex().all { (i, ex) -> setsDone.getOrElse(i) { 0 } >= ex.setCount() }

private val TIMED = Regex("""^(\d+)\s*(?:-\s*(\d+))?\s*(s|seg|segundos?|min|minutos?)\b""", RegexOption.IGNORE_CASE)

/**
 * Si las repeticiones son un tiempo ("40 s", "30-40 s", "90-120 s", "1 min"), devuelve el rango en
 * segundos; si son repeticiones normales, null.
 */
fun Exercise.timeRange(): IntRange? {
    val m = TIMED.find(r.trim()) ?: return null
    val mult = if (m.groupValues[3].startsWith("min", ignoreCase = true)) 60 else 1
    val min = m.groupValues[1].toInt() * mult
    val max = (m.groupValues[2].toIntOrNull() ?: m.groupValues[1].toInt()) * mult
    return if (min <= 0) null else minOf(min, max)..maxOf(min, max)
}
