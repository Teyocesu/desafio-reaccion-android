package com.uade.desafioreaccion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uade.desafioreaccion.model.GameUiState
import com.uade.desafioreaccion.ui.theme.Amber
import com.uade.desafioreaccion.ui.theme.Cyan
import com.uade.desafioreaccion.ui.theme.Emerald
import com.uade.desafioreaccion.ui.theme.RedAccent

@Composable
fun ResultScreen(
    state: GameUiState,
    onPlayAgain: () -> Unit,
    onGoHome: () -> Unit,
    onOpenRanking: () -> Unit
) {
    val headerColor = if (state.won) Emerald else RedAccent
    val headerTitle = if (state.won) "¡Partida superada!" else "Fin de la partida"
    val headerSubtitle = if (state.won) {
        "Terminaste todos los niveles y tu resultado ya quedó guardado localmente."
    } else {
        "Se terminó la sesión. Revisá tu rendimiento y volvé a intentarlo."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = headerColor.copy(alpha = 0.12f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = headerColor.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = if (state.won) "Victoria" else "Derrota",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        color = headerColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Text(
                    text = headerTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = headerSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Jugador: ${state.config.playerName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (state.feedbackMessage.isNotBlank()) {
                    Text(
                        text = state.feedbackMessage,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ResultMetricCard(
                modifier = Modifier.weight(1f),
                title = "Puntaje",
                value = state.score.toString(),
                accent = Emerald
            )
            ResultMetricCard(
                modifier = Modifier.weight(1f),
                title = "Aciertos",
                value = state.correctCount.toString(),
                accent = Cyan
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ResultMetricCard(
                modifier = Modifier.weight(1f),
                title = "Promedio",
                value = state.averageReactionMs?.let { "$it ms" } ?: "Sin dato",
                accent = Amber
            )
            ResultMetricCard(
                modifier = Modifier.weight(1f),
                title = "Mejor tiempo",
                value = state.bestReactionMs?.let { "$it ms" } ?: "Sin dato",
                accent = RedAccent
            )
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Resumen de desempeño",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                ResultDetailRow("Dificultad", state.config.difficulty.label)
                ResultDetailRow("Estímulos", state.config.stimulusMode.label)
                ResultDetailRow("Modo inverso", if (state.config.reverseMode) "Sí" else "No")
                ResultDetailRow("Errores", state.incorrectCount.toString())
                ResultDetailRow("Timeouts", state.timeoutCount.toString())
                ResultDetailRow("Falsas salidas", state.falseStartCount.toString())
                ResultDetailRow("Resultados guardados", if (state.resultsSaved) "Sí" else "No")
            }
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Lectura rápida de la partida",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = buildSummaryText(state),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onPlayAgain
        ) {
            Text("Jugar otra vez")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onOpenRanking
        ) {
            Text("Ver ranking")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onGoHome
        ) {
            Text("Volver al inicio")
        }
    }
}

@Composable
private fun ResultMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    accent: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = accent.copy(alpha = 0.14f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ResultDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun buildSummaryText(state: GameUiState): String {
    return when {
        state.won && state.bestReactionMs != null ->
            "Hiciste una muy buena partida. Completaste todos los niveles, lograste ${state.correctCount} aciertos y tu mejor reacción fue de ${state.bestReactionMs} ms."
        state.won ->
            "Completaste todos los niveles y cerraste la partida con un muy buen rendimiento general."
        !state.won && state.falseStartCount > 0 ->
            "La partida terminó antes de tiempo. Hubo ${state.falseStartCount} falsas salidas, así que te conviene esperar un poco más antes de tocar cuando aparecen los distractores."
        !state.won && state.timeoutCount > 0 ->
            "La sesión tuvo varios tiempos agotados. Te conviene reaccionar un poco más rápido cuando finalmente aparece el objetivo."
        else ->
            "La partida ya terminó y tus estadísticas quedaron listas para comparar en el ranking."
    }
}