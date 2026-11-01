package com.tuapp.rutina.presentation.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import com.tuapp.rutina.data.Day
import com.tuapp.rutina.data.setCount
import com.tuapp.rutina.presentation.components.ExerciseBadge
import com.tuapp.rutina.presentation.components.RutinaCard
import com.tuapp.rutina.presentation.components.RutinaList
import com.tuapp.rutina.presentation.components.ScreenTitle
import com.tuapp.rutina.presentation.components.SectionHeader
import com.tuapp.rutina.presentation.theme.Accent
import com.tuapp.rutina.presentation.theme.Teal
import com.tuapp.rutina.presentation.theme.TextPrimary
import com.tuapp.rutina.presentation.theme.TextSecondary

@Composable
fun ExercisesScreen(
    day: Day,
    setsDone: List<Int> = emptyList(),
    onExerciseClick: (Int) -> Unit,
) {
    RutinaList {
        item { ScreenTitle(day.title, day.sub) }
        // Índice plano dentro del día, para que el detalle recorra todos los ejercicios en orden.
        var flatIndex = 0
        day.sections.forEach { section ->
            item { SectionHeader(section.lbl) }
            section.exs.forEach { ex ->
                val index = flatIndex++
                item {
                    RutinaCard(onClick = { onExerciseClick(index) }) {
                        Text(ex.n, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        if (ex.isNew || ex.isProgress) {
                            Spacer(Modifier.height(2.dp))
                            ExerciseBadge(ex)
                        }
                        Text(
                            "${ex.s} series · ${ex.r} · Desc: ${ex.d}",
                            color = TextSecondary,
                            fontSize = 13.sp,
                        )
                        val done = setsDone.getOrElse(index) { 0 }
                        val total = ex.setCount()
                        when {
                            done >= total -> Text("✓ Hecho", color = Teal, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            done > 0 -> Text("$done / $total series", color = Accent, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
