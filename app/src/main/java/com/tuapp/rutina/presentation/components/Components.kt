package com.tuapp.rutina.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.tuapp.rutina.data.Exercise
import com.tuapp.rutina.presentation.theme.Accent
import com.tuapp.rutina.presentation.theme.Background
import com.tuapp.rutina.presentation.theme.CardBackground
import com.tuapp.rutina.presentation.theme.CardBorder
import com.tuapp.rutina.presentation.theme.Teal
import com.tuapp.rutina.presentation.theme.TextPrimary
import com.tuapp.rutina.presentation.theme.TextSecondary

/** Pantalla base: ScreenScaffold + ScalingLazyColumn (corona rotatoria incluida). */
@Composable
fun RutinaList(
    state: ScalingLazyListState = rememberScalingLazyListState(),
    content: ScalingLazyListScope.() -> Unit,
) {
    ScreenScaffold(scrollState = state) { contentPadding ->
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Background),
            state = state,
            contentPadding = contentPadding,
            content = content,
        )
    }
}

@Composable
fun RutinaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CardBackground)
            .border(1.dp, CardBorder, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        content = content,
    )
}

@Composable
fun ScreenTitle(title: String, subtitle: String? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun SectionHeader(label: String) {
    Text(
        text = label.uppercase(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 8.dp, bottom = 2.dp),
        color = Accent,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
    )
}

/** Badge "nuevo" (naranja) o "progreso" (turquesa); nada si el ejercicio no tiene ninguno. */
@Composable
fun ExerciseBadge(exercise: Exercise, modifier: Modifier = Modifier) {
    val (text, color) = when {
        exercise.isNew -> "nuevo" to Accent
        exercise.isProgress -> "progreso" to Teal
        else -> return
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color)
            .padding(PaddingValues(horizontal = 8.dp, vertical = 1.dp)),
    ) {
        Text(text = text, color = Background, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CenteredMessage(text: String) {
    RutinaList {
        item {
            Text(
                text = text,
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
