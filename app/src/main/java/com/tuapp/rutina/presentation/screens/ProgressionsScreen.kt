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
import androidx.wear.compose.material3.Text
import com.tuapp.rutina.data.Progression
import com.tuapp.rutina.data.ProgressionStatus
import com.tuapp.rutina.data.startDate
import com.tuapp.rutina.data.statusOf
import com.tuapp.rutina.presentation.components.RutinaCard
import com.tuapp.rutina.presentation.components.RutinaList
import com.tuapp.rutina.presentation.components.ScreenTitle
import com.tuapp.rutina.presentation.theme.Accent
import com.tuapp.rutina.presentation.theme.TextPrimary
import com.tuapp.rutina.presentation.theme.TextSecondary
import java.time.format.DateTimeFormatter

private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
fun ProgressionsScreen(
    progressions: List<Progression>,
    onProgressionClick: (Int) -> Unit,
    onSettingsClick: () -> Unit,
) {
    RutinaList {
        item { ScreenTitle("Progresiones") }
        items(progressions.size) { index ->
            val p = progressions[index]
            val status = progressions.statusOf(index)
            val completed = status == ProgressionStatus.COMPLETED
            RutinaCard(
                modifier = Modifier.alpha(if (completed) 0.45f else 1f),
                onClick = { onProgressionClick(index) },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = p.id.toString(),
                        color = if (completed) TextSecondary else Accent,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.width(34.dp),
                    )
                    Column {
                        Text(p.name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Desde ${p.startDate().format(DATE_FORMAT)}",
                            color = TextSecondary,
                            fontSize = 13.sp,
                        )
                        when (status) {
                            ProgressionStatus.ACTIVE -> Text("En curso", color = Accent, fontSize = 13.sp)
                            ProgressionStatus.COMPLETED -> Text("Completada", color = TextSecondary, fontSize = 13.sp)
                            ProgressionStatus.UPCOMING -> Unit
                        }
                    }
                }
            }
        }
        item {
            RutinaCard(onClick = onSettingsClick) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text("Ajustes", color = TextPrimary, fontSize = 15.sp)
                }
            }
        }
    }
}
