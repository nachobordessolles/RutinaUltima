package com.tuapp.rutina.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
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
) {
    RutinaList {
        item { ScreenTitle(progression.name, progression.weeks) }
        items(progression.days.size) { index ->
            val day = progression.days[index]
            RutinaCard(onClick = { onDayClick(index) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = (index + 1).toString(),
                        color = Accent,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.width(28.dp),
                    )
                    Column {
                        Text(day.title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text(day.sub, color = TextSecondary, fontSize = 13.sp)
                        Text("${day.allExercises.size} ejercicios", color = Accent, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
