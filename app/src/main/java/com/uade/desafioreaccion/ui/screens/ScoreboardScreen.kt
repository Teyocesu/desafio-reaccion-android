package com.uade.desafioreaccion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun ScoreboardScreen(
    state: GameUiState,
    onRefresh: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionTitle(
                    title = "Ranking local",
                    subtitle = "Se muestra el mejor resultado guardado por jugador, ordenado de mayor a menor puntaje."
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RankingInfoCard(
                        modifier = Modifier.weight(1f),
                        title = "Jugadores",
                        value = state.bestResults.size.toString(),
                        accentColor = Emerald
                    )

                    RankingInfoCard(
                        modifier = Modifier.weight(1f),
                        title = "Partidas",
                        value = state.recentResults.size.toString(),
                        accentColor = Cyan
                    )

                    RankingInfoCard(
                        modifier = Modifier.weight(1f),
                        title = "Mejor puntaje",
                        value = state.bestResults.firstOrNull()?.score?.toString() ?: "-",
                        accentColor = Amber
                    )
                }
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.bestResults.isEmpty()) {
                    item {
                        EmptyRankingCard()
                    }
                } else {
                    item {
                        SectionChip(
                            text = "Mejores resultados por jugador",
                            accentColor = Emerald
                        )
                    }

                    itemsIndexed(state.bestResults) { index, record ->
                        ScoreRecordCard(index = index + 1, record = record)
                    }
                }

                if (state.recentResults.isNotEmpty()) {
                    item {
                        SectionChip(
                            text = "Últimas partidas guardadas",
                            accentColor = Cyan
                        )
                    }

                    itemsIndexed(state.recentResults.take(5)) { _, record ->
                        ScoreRecordCard(record = record)
                    }
                }
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onBack
        ) {
            Text("Volver")
        }
    }
}

@Composable
private fun RankingInfoCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    accentColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.14f)
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
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SectionChip(
    text: String,
    accentColor: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = accentColor.copy(alpha = 0.10f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun EmptyRankingCard() {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Todavía no hay resultados competitivos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Jugá una partida en fácil, medio o difícil para empezar a poblar el ranking local y comparar puntajes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}