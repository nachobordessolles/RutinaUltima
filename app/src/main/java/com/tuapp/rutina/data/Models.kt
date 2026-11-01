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
