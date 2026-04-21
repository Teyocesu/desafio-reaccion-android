package com.uade.desafioreaccion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uade.desafioreaccion.model.FeedbackType
import com.uade.desafioreaccion.model.GameUiState
import com.uade.desafioreaccion.model.RoundPhase
import com.uade.desafioreaccion.model.StimulusCategory
import com.uade.desafioreaccion.model.StimulusUi
import com.uade.desafioreaccion.ui.theme.Amber
import com.uade.desafioreaccion.ui.theme.Cyan
import com.uade.desafioreaccion.ui.theme.Emerald
import com.uade.desafioreaccion.ui.theme.RedAccent
import java.util.Locale

@Composable
fun GameScreen(
    state: GameUiState,
    onReact: () -> Unit,
    onAbandon: () -> Unit
) {
    val stimulus = state.currentStimulus
    val targetStimulus = state.targetStimulus ?: stimulus

    val progress = if (stimulus == null) {
        0f
    } else {
        ((state.remainingMs.toFloat() / 1000f) / currentRoundSeconds(state)).coerceIn(0f, 1f)
    }

    val phaseTitle = when (state.roundPhase) {
        RoundPhase.IDLE -> "Esperando"
        RoundPhase.PREPARING -> "Observá la secuencia"
        RoundPhase.STREAMING -> "Observá la secuencia"
        RoundPhase.TARGET_VISIBLE -> "Observá la secuencia"
        RoundPhase.FEEDBACK -> "Resultado"
        RoundPhase.LEVEL_TRANSITION -> "Subiendo de nivel"
        RoundPhase.FINISHED -> "Partida terminada"
    }

    val phaseMessage = when (state.roundPhase) {
        RoundPhase.IDLE -> "Todavía no comenzó la partida."
        RoundPhase.PREPARING -> "Leé la regla de esta ronda y preparate."
        RoundPhase.STREAMING -> "Mirá con atención y seguí la regla mostrada arriba."
        RoundPhase.TARGET_VISIBLE -> "Mirá con atención y seguí la regla mostrada arriba."
        RoundPhase.FEEDBACK -> state.feedbackMessage.ifBlank { "Procesando resultado..." }
        RoundPhase.LEVEL_TRANSITION -> "Comienza un nivel más rápido y exigente."
        RoundPhase.FINISHED -> state.feedbackMessage.ifBlank { "La sesión terminó." }
    }

    val phaseColor = when (state.roundPhase) {
        RoundPhase.STREAMING,
        RoundPhase.TARGET_VISIBLE,
        RoundPhase.PREPARING -> Amber

        RoundPhase.FEEDBACK -> when (state.feedbackType) {
            FeedbackType.SUCCESS -> Emerald
            FeedbackType.ERROR -> RedAccent
            FeedbackType.NEUTRAL -> Amber
        }

        RoundPhase.FINISHED -> if (state.won) Emerald else RedAccent
        else -> Cyan
    }

    val instructionText = buildRoundInstructionText(state, targetStimulus)
    val instructionChipColor = instructionChipBackground(state, targetStimulus)
    val cardBackgroundColor = stimulusCardBackground(stimulus)

    val canTapCard = state.inProgress &&
            stimulus != null &&
            state.roundPhase != RoundPhase.FEEDBACK &&
            state.roundPhase != RoundPhase.LEVEL_TRANSITION &&
            state.roundPhase != RoundPhase.FINISHED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Desafío de Reacción",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )

                PhaseBadge(
                    title = phaseTitle,
                    background = phaseColor.copy(alpha = 0.18f),
                    content = phaseColor
                )

                Text(
                    text = phaseMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Nivel",
                value = "${state.level}/${state.totalLevels}",
                accent = Cyan
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Vidas",
                value = state.lives.toString(),
                accent = RedAccent
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Puntaje",
                value = state.score.toString(),
                accent = Emerald
            )
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Ronda ${state.roundInLevel} de ${state.config.iterationsPerLevel}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )

                Text(
                    text = "Tiempo restante: ${(state.remainingMs / 1000.0).coerceAtLeast(0.0).formatSeconds()} s",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = instructionChipColor
                ) {
                    Text(
                        text = instructionText,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable(
                    enabled = canTapCard,
                    onClick = onReact
                ),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                if (stimulus == null) {
                    Text(
                        text = "Preparando ronda...",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.45f)
                        ) {
                            Text(
                                text = when (stimulus.category) {
                                    StimulusCategory.COLORS -> "COLOR"
                                    StimulusCategory.NUMBERS -> "NUMERO"
                                    StimulusCategory.WORDS -> "PALABRA"
                                },
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = stimulus.label,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        if (state.feedbackMessage.isNotBlank() && state.roundPhase == RoundPhase.FEEDBACK) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (state.feedbackType) {
                        FeedbackType.SUCCESS -> Emerald.copy(alpha = 0.12f)
                        FeedbackType.ERROR -> RedAccent.copy(alpha = 0.12f)
                        FeedbackType.NEUTRAL -> Amber.copy(alpha = 0.14f)
                    }
                )
            ) {
                Text(
                    text = state.feedbackMessage,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onAbandon
        ) {
            Text("Volver al inicio")
        }
    }
}

@Composable
private fun PhaseBadge(
    title: String,
    background: Color,
    content: Color
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = background
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = content,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    accent: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = accent.copy(alpha = 0.14f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
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

private fun buildRoundInstructionText(
    state: GameUiState,
    targetStimulus: StimulusUi?
): String {
    return targetStimulus?.ruleText
        ?: state.currentStimulus?.ruleText
        ?: "Preparando objetivo..."
}

@Composable
private fun instructionChipBackground(
    state: GameUiState,
    targetStimulus: StimulusUi?
): Color {
    if (targetStimulus == null) {
        return MaterialTheme.colorScheme.surfaceVariant
    }

    if (state.config.reverseMode) {
        return MaterialTheme.colorScheme.surfaceVariant
    }

    return when (targetStimulus.category) {
        StimulusCategory.COLORS -> Color(targetStimulus.accentColorHex).copy(alpha = 0.22f)
        StimulusCategory.NUMBERS,
        StimulusCategory.WORDS -> MaterialTheme.colorScheme.surfaceVariant
    }
}

@Composable
private fun stimulusCardBackground(stimulus: StimulusUi?): Color {
    if (stimulus == null) return MaterialTheme.colorScheme.surfaceVariant

    return when (stimulus.category) {
        StimulusCategory.COLORS -> Color(stimulus.accentColorHex).copy(alpha = 0.50f)
        StimulusCategory.NUMBERS -> MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
        StimulusCategory.WORDS -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)
    }
}

private fun currentRoundSeconds(state: GameUiState): Float {
    val base = state.config.maxReactionTimeSeconds
    return when (state.level) {
        1 -> base.toFloat()
        2 -> (base - 2).coerceAtLeast(2).toFloat()
        else -> (base - 4).coerceAtLeast(2).toFloat()
    }
}

private fun Double.formatSeconds(): String {
    return String.format(Locale.US, "%.2f", this)
}