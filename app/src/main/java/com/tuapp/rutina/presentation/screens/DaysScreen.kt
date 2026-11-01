package com.tuapp.rutina.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Text
import com.tuapp.rutina.data.weekday
import com.tuapp.rutina.presentation.theme.CardBorder
import com.tuapp.rutina.presentation.theme.Teal
import java.time.LocalDate
import com.tuapp.rutina.data.Progression
import com.tuapp.rutina.presentation.components.RutinaCard
import com.tuapp.rutina.presentation.components.RutinaList
import com.tuapp.rutina.presentation.components.ScreenTitle
import com.tuapp.rutina.presentation.theme.Accent
import com.tuapp.rutina.presentation.theme.TextPrimary
import com.tuapp.rutina.presentation.theme.TextSecondary

@Composable
fun DaysScreen(
    progression: Progression,
    onDayClick: (Int) -> Unit,
    isCompleted: (Int) -> Boolean = { false },
    onProgressionsClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
) {
    val today = LocalDate.now().dayOfWeek
    val weekdays = progression.days.map { it.weekday() }
    val todayIndex = weekdays.indexOf(today)
    // Si hoy no toca entrenar, arrancar en el próximo día de la semana.
    val startIndex = todayIndex.takeIf { it >= 0 }
        ?: weekdays.indexOfFirst { it != null && it > today }.takeIf { it >= 0 }
        ?: 0
    val listState = rememberScalingLazyListState(initialCenterItemIndex = startIndex + 1)

    RutinaList(state = listState) {
        item { ScreenTitle(progression.name, progression.weeks) }
        items(progression.days.size) { index ->
            val day = progression.days[index]
            val isToday = index == todayIndex
            val done = isCompleted(index)
            RutinaCard(
                modifier = Modifier.alpha(if (done && !isToday) 0.5f else 1f),
                onClick = { onDayClick(index) },
                borderColor = if (isToday) Accent else CardBorder,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (done) "✓" else (index + 1).toString(),
                        color = if (done) Teal else Accent,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.width(28.dp),
                    )
                    Column {
                        if (isToday) {
                            Text(
                                if (done) "HOY · HECHO" else "HOY",
                                color = if (done) Teal else Accent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                            )
                        }
                        Text(day.title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text(day.sub, color = TextSecondary, fontSize = 13.sp)
                        Text("${day.allExercises.size} ejercicios", color = Accent, fontSize = 13.sp)
                    }
                }
            }
        }
        onProgressionsClick?.let { item { TextCard("Otras progresiones", it) } }
        onSettingsClick?.let { item { TextCard("Ajustes", it) } }
    }
}

@Composable
private fun TextCard(text: String, onClick: () -> Unit) {
    RutinaCard(onClick = onClick) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(text, color = TextPrimary, fontSize = 15.sp)
        }
    }
}
