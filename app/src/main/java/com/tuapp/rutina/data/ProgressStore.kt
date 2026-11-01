package com.tuapp.rutina.data

import android.content.Context
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** Guarda qué días se completaron. Un día cuenta como hecho si se completó desde el lunes de esta semana. */
class ProgressStore(context: Context) {

    private val prefs = context.getSharedPreferences("progreso", Context.MODE_PRIVATE)

    fun markDone(key: String, today: LocalDate = LocalDate.now()) {
        prefs.edit().putLong(key, today.toEpochDay()).apply()
    }

    fun unmark(key: String) {
        prefs.edit().remove(key).apply()
    }

    fun doneThisWeek(today: LocalDate = LocalDate.now()): Set<String> {
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toEpochDay()
        return prefs.all.filterValues { (it as? Long ?: -1L) >= monday }.keys
    }

    companion object {
        fun key(progression: Progression, dayIndex: Int) = "${progression.id}/$dayIndex"
    }
}
