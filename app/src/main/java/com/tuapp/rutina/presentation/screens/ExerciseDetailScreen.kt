package com.tuapp.rutina.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Text
import android.os.SystemClock
import com.tuapp.rutina.data.Day
import com.tuapp.rutina.data.restSeconds
import com.tuapp.rutina.data.setCount
import com.tuapp.rutina.data.timeRange
import com.tuapp.rutina.presentation.components.CenteredMessage
import com.tuapp.rutina.presentation.components.ExerciseBadge
import com.tuapp.rutina.presentation.components.RutinaCard
import com.tuapp.rutina.presentation.components.RutinaList
import com.tuapp.rutina.presentation.theme.Accent
import com.tuapp.rutina.presentation.theme.CardBackground
import com.tuapp.rutina.presentation.theme.Teal
import com.tuapp.rutina.presentation.theme.TextPrimary
import com.tuapp.rutina.presentation.theme.TextSecondary

@Composable
fun ExerciseDetailScreen(
    day: Day,
    initialIndex: Int,
    setsDone: List<Int>,
    onSetsChange: (List<Int>) -> Unit,
) {
    val exercises = day.allExercises
    if (exercises.isEmpty()) {
        CenteredMessage("Sin ejercicios")
        return
    }
    var index by rememberSaveable { mutableIntStateOf(initialIndex.coerceIn(exercises.indices)) }
    val ex = exercises[index]
    val listState = rememberScalingLazyListState(initialCenterItemIndex = 0)
    val context = LocalContext.current

    // Descanso en curso: instante de fin (0 = sin descanso), duración total y si al terminar se pasa al siguiente.
    var restEndAt by rememberSaveable { mutableLongStateOf(0L) }
    var restTotal by rememberSaveable { mutableIntStateOf(0) }
    var advanceAfterRest by rememberSaveable { mutableStateOf(false) }
    // Ejercicio con tiempo en curso: instante en que se tocó "Empezar" (0 = no está corriendo).
    var workStartAt by rememberSaveable { mutableLongStateOf(0L) }

    LaunchedEffect(index) { listState.scrollToItem(0) }

    val total = ex.setCount()
    val done = setsDone.getOrElse(index) { 0 }.coerceAtMost(total)
    val timeRange = ex.timeRange()

    fun completeSet() {
        val newDone = (done + 1).coerceAtMost(total)
        onSetsChange(withSets(setsDone, exercises.size, index, newDone))
        val isLast = newDone == total
        val hasNext = index < exercises.lastIndex
        val rest = ex.restSeconds()
        when {
            rest > 0 && (!isLast || hasNext) -> {
                restTotal = rest
                restEndAt = SystemClock.elapsedRealtime() + rest * 1000L
                advanceAfterRest = isLast
            }
            isLast && hasNext -> index++
        }
    }

    fun undoSet() {
        onSetsChange(withSets(setsDone, exercises.size, index, (done - 1).coerceAtLeast(0)))
    }

    if (workStartAt > 0L && timeRange != null) {
        WorkTimerScreen(
            startAt = workStartAt,
            range = timeRange,
            exerciseName = ex.n,
            setLabel = "Serie ${done + 1} de $total",
            onSkipPrep = { workStartAt = SystemClock.elapsedRealtime() - PREP_SECONDS * 1000L },
            onFinish = {
                workStartAt = 0L
                completeSet()
            },
            onCancel = { workStartAt = 0L },
        )
        return
    }

    if (restEndAt > 0L) {
        val label = if (advanceAfterRest) {
            "Sigue: ${exercises.getOrNull(index + 1)?.n.orEmpty()}"
        } else {
            "Sigue serie ${done + 1} de $total"
        }
        RestScreen(
            endAt = restEndAt,
            totalSeconds = restTotal,
            label = label,
            onAddTime = {
                restEndAt += 15_000
                restTotal += 15
            },
            onFinish = { vibrate ->
                if (vibrate) vibrateRestEnd(context)
                restEndAt = 0L
                if (advanceAfterRest && index < exercises.lastIndex) index++
                advanceAfterRest = false
            },
        )
        return
    }

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
            SetsBlock(
                done = done,
                total = total,
                timed = timeRange != null,
                onDone = {
                    if (timeRange != null) workStartAt = SystemClock.elapsedRealtime() else completeSet()
                },
                onUndo = ::undoSet,
            )
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

private fun withSets(current: List<Int>, size: Int, index: Int, value: Int): List<Int> =
    List(size) { i -> if (i == index) value else current.getOrElse(i) { 0 } }

@Composable
private fun SetsBlock(done: Int, total: Int, timed: Boolean, onDone: () -> Unit, onUndo: () -> Unit) {
    val finished = done >= total
    RutinaCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "SERIES",
                color = Accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
            )
            Text(
                "$done / $total",
                color = if (finished) Teal else TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (finished) {
                Text(
                    "Completado",
                    color = Teal,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f).align(Alignment.CenterVertically),
                )
            } else {
                NavButton(if (timed) "▶ Empezar serie ${done + 1}" else "Serie ${done + 1} hecha", enabled = true, modifier = Modifier.weight(1f), onClick = onDone)
            }
            if (done > 0) {
                NavButton("↺", enabled = true, modifier = Modifier.width(48.dp), onClick = onUndo)
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
