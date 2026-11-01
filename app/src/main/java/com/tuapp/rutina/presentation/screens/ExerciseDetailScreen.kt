package com.tuapp.rutina.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Text
import com.tuapp.rutina.data.Day
import com.tuapp.rutina.presentation.components.CenteredMessage
import com.tuapp.rutina.presentation.components.ExerciseBadge
import com.tuapp.rutina.presentation.components.RutinaCard
import com.tuapp.rutina.presentation.components.RutinaList
import com.tuapp.rutina.presentation.theme.Accent
import com.tuapp.rutina.presentation.theme.CardBackground
import com.tuapp.rutina.presentation.theme.TextPrimary
import com.tuapp.rutina.presentation.theme.TextSecondary

@Composable
fun ExerciseDetailScreen(
    day: Day,
    initialIndex: Int,
) {
    val exercises = day.allExercises
    if (exercises.isEmpty()) {
        CenteredMessage("Sin ejercicios")
        return
    }
    var index by rememberSaveable { mutableIntStateOf(initialIndex.coerceIn(exercises.indices)) }
    val ex = exercises[index]
    val listState = rememberScalingLazyListState(initialCenterItemIndex = 0)

    LaunchedEffect(index) { listState.scrollToItem(0) }

    RutinaList(state = listState) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    ex.n,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                if (ex.isNew || ex.isProgress) {
                    Spacer(Modifier.height(2.dp))
                    ExerciseBadge(ex)
                }
            }
        }
        item {
            InfoBlock("Volumen") {
                VolumeRow("Series", ex.s)
                VolumeRow("Reps/tiempo", ex.r)
                VolumeRow("Descanso", ex.d)
            }
        }
        if (ex.t.isNotBlank()) item { InfoBlock("Técnica") { BodyText(ex.t) } }
        if (ex.resp.isNotBlank()) item { InfoBlock("Respiración") { BodyText(ex.resp) } }
        if (ex.e.isNotEmpty()) {
            item {
                InfoBlock("Errores comunes") {
                    ex.e.forEach { BodyText("! $it") }
                }
            }
        }
        item {
            Text(
                "Ejercicio ${index + 1} de ${exercises.size}",
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                NavButton("Anterior", enabled = index > 0, modifier = Modifier.weight(1f)) { index-- }
                NavButton("Siguiente", enabled = index < exercises.lastIndex, modifier = Modifier.weight(1f)) { index++ }
            }
        }
    }
}

@Composable
private fun InfoBlock(title: String, content: @Composable () -> Unit) {
    RutinaCard {
        Text(
            title.uppercase(),
            color = Accent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
        )
        Spacer(Modifier.height(4.dp))
        content()
    }
}

@Composable
private fun VolumeRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, fontSize = 13.sp)
        Text(
            value,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f, fill = false),
        )
    }
}

@Composable
private fun BodyText(text: String) {
    Text(text, color = TextPrimary, fontSize = 14.sp, lineHeight = 18.sp)
}

@Composable
private fun NavButton(text: String, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Accent,
            contentColor = TextPrimary,
            disabledContainerColor = CardBackground,
            disabledContentColor = TextSecondary,
        ),
    ) {
        Text(
            text,
            fontSize = 13.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
