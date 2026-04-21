package com.uade.desafioreaccion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.uade.desafioreaccion.data.ScoreRepository
import com.uade.desafioreaccion.ui.screens.GameScreen
import com.uade.desafioreaccion.ui.screens.ResultScreen
import com.uade.desafioreaccion.ui.screens.ScoreboardScreen
import com.uade.desafioreaccion.ui.screens.SetupScreen
import com.uade.desafioreaccion.ui.theme.ReactionGameTheme
import com.uade.desafioreaccion.viewmodel.ReactionGameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = ScoreRepository(applicationContext)

        setContent {
            ReactionGameTheme {
                val viewModel: ReactionGameViewModel = viewModel(
                    factory = ReactionGameViewModel.Factory(repository)
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val navController = rememberNavController()

                AppNavigation(
                    navController = navController,
                    viewModel = viewModel,
                    uiState = uiState
                )
            }
        }
    }
}

private object Routes {
    const val SETUP = "setup"
    const val GAME = "game"
    const val RESULT = "result"
    const val SCORES = "scores"
}

@androidx.compose.runtime.Composable
private fun AppNavigation(
    navController: NavHostController,
    viewModel: ReactionGameViewModel,
    uiState: com.uade.desafioreaccion.model.GameUiState
) {
    LaunchedEffect(uiState.isSessionFinished) {
        if (uiState.isSessionFinished && navController.currentDestination?.route != Routes.RESULT) {
            navController.navigate(Routes.RESULT) {
                popUpTo(Routes.GAME) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SETUP
    ) {
        composable(Routes.SETUP) {
            SetupScreen(
                state = uiState,
                onPlayerNameChange = viewModel::updatePlayerName,
                onDifficultySelected = viewModel::updateDifficulty,
                onStimulusModeSelected = viewModel::updateStimulusMode,
                onIterationsChange = viewModel::updateIterations,
                onReactionTimeChange = viewModel::updateMaxReactionTime,
                onReverseModeChange = viewModel::updateReverseMode,
                onSoundsChange = viewModel::updateSounds,
                onStartGame = {
                    viewModel.startGame()
                    navController.navigate(Routes.GAME)
                },
                onOpenRanking = {
                    viewModel.refreshRanking()
                    navController.navigate(Routes.SCORES)
                }
            )
        }

        composable(Routes.GAME) {
            GameScreen(
                state = uiState,
                onReact = viewModel::onStimulusTapped,
                onAbandon = {
                    viewModel.abandonGame()
                    navController.popBackStack(Routes.SETUP, inclusive = false)
                }
            )
        }

        composable(Routes.RESULT) {
            ResultScreen(
                state = uiState,
                onPlayAgain = {
                    viewModel.startGame()
                    navController.navigate(Routes.GAME) {
                        popUpTo(Routes.RESULT) { inclusive = true }
                    }
                },
                onGoHome = {
                    viewModel.resetToHomeState()
                    navController.navigate(Routes.SETUP) {
                        popUpTo(Routes.SETUP) { inclusive = false }
                    }
                },
                onOpenRanking = {
                    viewModel.refreshRanking()
                    navController.navigate(Routes.SCORES)
                }
            )
        }

        composable(Routes.SCORES) {
            ScoreboardScreen(
                state = uiState,
                onRefresh = viewModel::refreshRanking,
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.SETUP)
                    }
                }
            )
        }
    }
}
