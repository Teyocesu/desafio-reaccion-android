package com.uade.desafioreaccion.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uade.desafioreaccion.model.Difficulty
import com.uade.desafioreaccion.model.GameUiState
import com.uade.desafioreaccion.model.StimulusMode

@Composable
fun SetupScreen(
    state: GameUiState,
    onPlayerNameChange: (String) -> Unit,
    onDifficultySelected: (Difficulty) -> Unit,
    onStimulusModeSelected: (StimulusMode) -> Unit,
    onIterationsChange: (Int) -> Unit,
    onReactionTimeChange: (Int) -> Unit,
    onReverseModeChange: (Boolean) -> Unit,
    onSoundsChange: (Boolean) -> Unit,
    onStartGame: () -> Unit,
    onOpenRanking: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        SectionTitle(
            title = "Desafío de Reacción y Atención",
            subtitle = "Configurá una partida local, jugá por niveles y guardá los mejores resultados por jugador."
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Cómo se juega",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Cuando aparece un estímulo, tocá la tarjeta lo más rápido posible. Si activás el modo inverso, algunos estímulos deben ignorarse hasta que termine el tiempo.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        OutlinedTextField(
            value = state.config.playerName,
            onValueChange = onPlayerNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nombre del jugador") },
            singleLine = true,
            supportingText = { Text("Si lo dejás vacío, se usará 'Jugador'.") }
        )

        OptionChipGroup(
            label = "Dificultad",
            options = Difficulty.entries.map { it.label },
            selected = state.config.difficulty.label,
            onSelected = { selected ->
                Difficulty.entries.firstOrNull { it.label == selected }?.let(onDifficultySelected)
            }
        )

        OptionChipGroup(
            label = "Tipo de estímulos",
            options = StimulusMode.entries.map { it.label },
            selected = state.config.stimulusMode.label,
            onSelected = { selected ->
                StimulusMode.entries.firstOrNull { it.label == selected }?.let(onStimulusModeSelected)
            }
        )

        CounterSelector(
            label = "Iteraciones por nivel",
            value = state.config.iterationsPerLevel.toString(),
            onMinus = { onIterationsChange(-1) },
            onPlus = { onIterationsChange(1) },
            helper = "Valor recomendado: 20. Se juega en 3 niveles progresivos."
        )

        CounterSelector(
            label = "Tiempo máximo de respuesta",
            value = "${state.config.maxReactionTimeSeconds} segundos",
            onMinus = { onReactionTimeChange(-1) },
            onPlus = { onReactionTimeChange(1) },
            helper = "Nunca supera los 30 segundos. El sistema acelera el tiempo en los niveles 2 y 3."
        )

        SettingsSwitchRow(
            title = "Modo inverso",
            description = "Ejemplos: ignorar ROJO, números primos o la palabra STOP.",
            checked = state.config.reverseMode,
            onCheckedChange = onReverseModeChange
        )

        SettingsSwitchRow(
            title = "Sonidos",
            description = "Activa señales cortas al mostrar estímulos, acertar, fallar y terminar la partida.",
            checked = state.config.soundsEnabled,
            onCheckedChange = onSoundsChange
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Resumen actual", fontWeight = FontWeight.Bold)
                Text(
                    text = buildString {
                        append("Jugador: ")
                        append(state.config.playerName.ifBlank { "Jugador" })
                        append("\nDificultad: ${state.config.difficulty.label}")
                        append("\nModo: ${state.config.stimulusMode.label}")
                        append("\nIteraciones por nivel: ${state.config.iterationsPerLevel}")
                        append("\nTiempo máximo: ${state.config.maxReactionTimeSeconds} s")
                        append("\nModo inverso: ${if (state.config.reverseMode) "Sí" else "No"}")
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = onStartGame
            ) {
                Text("Comenzar")
            }
            Button(
                modifier = Modifier.weight(1f),
                onClick = onOpenRanking
            ) {
                Text("Ranking")
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = title, fontWeight = FontWeight.SemiBold)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
        }
    }
}
