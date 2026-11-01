package com.tuapp.rutina.presentation.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Text
import com.tuapp.rutina.presentation.UpdateStatus
import com.tuapp.rutina.presentation.components.RutinaCard
import com.tuapp.rutina.presentation.components.RutinaList
import com.tuapp.rutina.presentation.components.ScreenTitle
import com.tuapp.rutina.presentation.theme.Accent
import com.tuapp.rutina.presentation.theme.CardBackground
import com.tuapp.rutina.presentation.theme.Teal
import com.tuapp.rutina.presentation.theme.TextPrimary
import com.tuapp.rutina.presentation.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    lastUpdate: Long?,
    updateStatus: UpdateStatus,
    onUpdateClick: () -> Unit,
) {
    val loading = updateStatus == UpdateStatus.Loading
    RutinaList {
        item { ScreenTitle("Ajustes") }
        item {
            Button(
                onClick = onUpdateClick,
                enabled = !loading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Accent,
                    contentColor = TextPrimary,
                    disabledContainerColor = CardBackground,
                    disabledContentColor = TextSecondary,
                ),
            ) {
                Text(
                    if (loading) "Actualizando…" else "Actualizar rutina",
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        when (updateStatus) {
            is UpdateStatus.Error -> item {
                StatusText("Error: ${updateStatus.message}\nSe conserva la versión anterior.", Accent)
            }
            UpdateStatus.Success -> item { StatusText("Rutina actualizada", Teal) }
            else -> Unit
        }
        item {
            RutinaCard {
                Text("Última actualización", color = TextSecondary, fontSize = 13.sp)
                Text(
                    lastUpdate?.let { formatDate(it) } ?: "Nunca (versión incluida)",
                    color = TextPrimary,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
private fun StatusText(text: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        text,
        color = color,
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun formatDate(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(millis))
