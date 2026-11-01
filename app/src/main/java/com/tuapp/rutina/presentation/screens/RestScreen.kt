package com.tuapp.rutina.presentation.screens

import android.content.Context
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Text
import com.tuapp.rutina.presentation.theme.Accent
import com.tuapp.rutina.presentation.theme.Background
import com.tuapp.rutina.presentation.theme.CardBackground
import com.tuapp.rutina.presentation.theme.CardBorder
import com.tuapp.rutina.presentation.theme.TextPrimary
import com.tuapp.rutina.presentation.theme.TextSecondary
import kotlinx.coroutines.delay

/**
 * Cuenta atrás de descanso. [endAt] es un instante de SystemClock.elapsedRealtime(), así el tiempo
 * es correcto aunque la UI se recomponga o la app pase a segundo plano un momento.
 */
@Composable
fun RestScreen(
    endAt: Long,
    totalSeconds: Int,
    label: String,
    onAddTime: () -> Unit,
    onFinish: (vibrate: Boolean) -> Unit,
) {
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    val finish by rememberUpdatedState(onFinish)

    KeepScreenOn()

    LaunchedEffect(endAt) {
        while (true) {
            now = SystemClock.elapsedRealtime()
            if (now >= endAt) {
                finish(true)
                break
            }
            delay(200)
        }
    }

    val remainingMs = (endAt - now).coerceAtLeast(0)
    val remaining = ((remainingMs + 999) / 1000).toInt()
    val progress = if (totalSeconds > 0) remainingMs / (totalSeconds * 1000f) else 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center,
    ) {
        TimerRing(progress, Accent)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("DESCANSO", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Text(
                formatSeconds(remaining),
                color = TextPrimary,
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                label,
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.padding(horizontal = 36.dp),
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SmallButton("+15 s", primary = false, onClick = onAddTime)
                SmallButton("Saltar", primary = true) { finish(false) }
            }
        }
    }
}

/** Mantiene la pantalla encendida mientras este composable esté visible. */
@Composable
fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}

/** Anillo de progreso alrededor de la pantalla redonda. [progress] entre 0 y 1. */
@Composable
fun TimerRing(progress: Float, color: Color) {
    Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
        val stroke = 8.dp.toPx()
        val inset = stroke / 2
        val arcSize = Size(size.width - stroke, size.height - stroke)
        drawArc(CardBorder, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
        drawArc(
            color, -90f, 360f * progress.coerceIn(0f, 1f), false,
            Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round),
        )
    }
}

@Composable
fun SmallButton(text: String, primary: Boolean, primaryColor: Color = Accent, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.width(92.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (primary) primaryColor else CardBackground,
            contentColor = TextPrimary,
        ),
    ) {
        Text(text, fontSize = 13.sp, maxLines = 1, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

fun formatSeconds(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)

/** Tres vibraciones para avisar que terminó un temporizador. */
fun vibrateRestEnd(context: Context) = vibrate(context, longArrayOf(0, 300, 150, 300, 150, 500))

/** Una vibración corta (arranque del ejercicio). */
fun vibrateShort(context: Context) = vibrate(context, longArrayOf(0, 200))

/** Dos vibraciones cortas (mínimo alcanzado). */
fun vibrateDouble(context: Context) = vibrate(context, longArrayOf(0, 150, 120, 150))

private fun vibrate(context: Context, pattern: LongArray) {
    val vibrator = context.getSystemService(Vibrator::class.java) ?: return
    if (!vibrator.hasVibrator()) return
    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
}
