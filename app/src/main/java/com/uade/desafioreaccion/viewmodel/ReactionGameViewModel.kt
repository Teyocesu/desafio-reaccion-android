package com.uade.desafioreaccion.viewmodel

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.uade.desafioreaccion.data.ScoreRepository
import com.uade.desafioreaccion.data.StimulusFactory
import com.uade.desafioreaccion.data.StimulusRoundDefinition
import com.uade.desafioreaccion.model.Difficulty
import com.uade.desafioreaccion.model.FeedbackType
import com.uade.desafioreaccion.model.GameConfig
import com.uade.desafioreaccion.model.GameUiState
import com.uade.desafioreaccion.model.RoundPhase
import com.uade.desafioreaccion.model.ScoreRecord
import com.uade.desafioreaccion.model.StimulusMode
import com.uade.desafioreaccion.model.StimulusUi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

class ReactionGameViewModel(
    private val repository: ScoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)

    private var timerJob: Job? = null
    private var transitionJob: Job? = null
    private var sequenceJob: Job? = null
    private var preparationJob: Job? = null

    private var roundDurationMs: Long = 0L
    private var targetShownAt: Long = 0L
    private var currentRoundDefinition: StimulusRoundDefinition? = null

    private var selectedDifficultyBeforeSession: Difficulty = Difficulty.EASY
    private var selectedMaxReactionTimeBeforeSession: Int = Difficulty.EASY.defaultSeconds

    init {
        refreshRanking()
    }

    fun updatePlayerName(name: String) {
        _uiState.update {
            it.copy(config = it.config.copy(playerName = name.take(25)))
        }
    }

    fun updateDifficulty(difficulty: Difficulty) {
        _uiState.update {
            it.copy(
                config = it.config.copy(
                    difficulty = difficulty,
                    maxReactionTimeSeconds = difficulty.defaultSeconds
                )
            )
        }
    }

    fun updateStimulusMode(mode: StimulusMode) {
        _uiState.update {
            it.copy(config = it.config.copy(stimulusMode = mode))
        }
    }

    fun updateIterations(delta: Int) {
        _uiState.update {
            val newValue = (it.config.iterationsPerLevel + delta).coerceIn(5, 50)
            it.copy(config = it.config.copy(iterationsPerLevel = newValue))
        }
    }

    fun updateMaxReactionTime(delta: Int) {
        _uiState.update {
            val newValue = (it.config.maxReactionTimeSeconds + delta).coerceIn(1, 30)
            it.copy(config = it.config.copy(maxReactionTimeSeconds = newValue))
        }
    }

    fun updateReverseMode(enabled: Boolean) {
        _uiState.update {
            it.copy(config = it.config.copy(reverseMode = enabled))
        }
    }

    fun updateSounds(enabled: Boolean) {
        _uiState.update {
            it.copy(config = it.config.copy(soundsEnabled = enabled))
        }
    }

    fun startGame() {
        cancelAllJobs()

        val currentConfig = _uiState.value.config
        selectedDifficultyBeforeSession = currentConfig.difficulty
        selectedMaxReactionTimeBeforeSession = currentConfig.maxReactionTimeSeconds

        val playerName = currentConfig.playerName.trim().ifEmpty { "Jugador" }
        val startLevel = startingLevelForDifficulty(selectedDifficultyBeforeSession)
        val effectiveDifficulty = effectiveDifficultyForLevel(
            baseDifficulty = selectedDifficultyBeforeSession,
            level = startLevel
        )

        _uiState.update {
            it.copy(
                config = it.config.copy(
                    playerName = playerName,
                    difficulty = effectiveDifficulty,
                    maxReactionTimeSeconds = selectedMaxReactionTimeBeforeSession
                ),
                inProgress = true,
                isSessionFinished = false,
                won = false,
                level = startLevel,
                roundInLevel = 1,
                score = 0,
                lives = 3,
                currentStimulus = null,
                targetStimulus = null,
                remainingMs = 0L,
                stimulusStartedAtMs = 0L,
                targetShownAtMs = null,
                feedbackMessage = "",
                feedbackType = FeedbackType.NEUTRAL,
                roundPhase = RoundPhase.PREPARING,
                correctCount = 0,
                incorrectCount = 0,
                timeoutCount = 0,
                falseStartCount = 0,
                totalReactionMs = 0L,
                measuredReactionCount = 0,
                bestReactionMs = null,
                lastReactionMs = null,
                sequenceLength = 0,
                currentSequenceIndex = 0,
                tapLockedForCurrentStimulus = true,
                reactionAlreadyHandledForCurrentStimulus = false,
                resultsSaved = false
            )
        }

        launchRound()
    }

    fun onStimulusTapped() {
        val state = _uiState.value
        val stimulus = state.currentStimulus ?: return

        if (!state.inProgress || state.isSessionFinished) return
        if (state.tapLockedForCurrentStimulus || state.reactionAlreadyHandledForCurrentStimulus) return

        _uiState.update {
            it.copy(
                tapLockedForCurrentStimulus = true,
                reactionAlreadyHandledForCurrentStimulus = true
            )
        }

        cancelRoundJobs()

        if (stimulus.shouldReact) {
            val reactionBase = state.targetShownAtMs ?: SystemClock.elapsedRealtime()
            val reactionMs = max(1L, SystemClock.elapsedRealtime() - reactionBase)
            val points = calculatePoints(
                reactionMs = reactionMs,
                availableMs = roundDurationMs,
                difficulty = state.config.difficulty
            )

            _uiState.update {
                it.copy(
                    score = it.score + points,
                    correctCount = it.correctCount + 1,
                    totalReactionMs = it.totalReactionMs + reactionMs,
                    measuredReactionCount = it.measuredReactionCount + 1,
                    bestReactionMs = minOfNullable(it.bestReactionMs, reactionMs),
                    lastReactionMs = reactionMs,
                    feedbackMessage = "Correcto. Tiempo: ${reactionMs} ms (+$points puntos)",
                    feedbackType = FeedbackType.SUCCESS,
                    roundPhase = RoundPhase.FEEDBACK,
                    remainingMs = 0L
                )
            }

            playSuccessSoundIfEnabled()
            scheduleAdvance()
        } else {
            val remainingLives = (state.lives - 1).coerceAtLeast(0)

            _uiState.update {
                it.copy(
                    lives = remainingLives,
                    incorrectCount = it.incorrectCount + 1,
                    falseStartCount = it.falseStartCount + 1,
                    feedbackMessage = "Te adelantaste. Ese no era el estímulo correcto.",
                    feedbackType = FeedbackType.ERROR,
                    roundPhase = RoundPhase.FEEDBACK,
                    remainingMs = 0L,
                    lastReactionMs = null
                )
            }

            playErrorSoundIfEnabled()
            scheduleAdvance()
        }
    }

    fun abandonGame() {
        cancelAllJobs()
        currentRoundDefinition = null
        targetShownAt = 0L

        _uiState.update {
            it.copy(
                config = it.config.copy(
                    difficulty = selectedDifficultyBeforeSession,
                    maxReactionTimeSeconds = selectedMaxReactionTimeBeforeSession
                ),
                inProgress = false,
                isSessionFinished = false,
                currentStimulus = null,
                targetStimulus = null,
                remainingMs = 0L,
                stimulusStartedAtMs = 0L,
                targetShownAtMs = null,
                sequenceLength = 0,
                currentSequenceIndex = 0,
                tapLockedForCurrentStimulus = false,
                reactionAlreadyHandledForCurrentStimulus = false,
                roundPhase = RoundPhase.IDLE,
                feedbackMessage = "Partida cancelada.",
                feedbackType = FeedbackType.NEUTRAL
            )
        }
    }

    fun resetToHomeState() {
        cancelAllJobs()
        currentRoundDefinition = null
        targetShownAt = 0L

        _uiState.update {
            it.copy(
                config = it.config.copy(
                    difficulty = selectedDifficultyBeforeSession,
                    maxReactionTimeSeconds = selectedMaxReactionTimeBeforeSession
                ),
                inProgress = false,
                isSessionFinished = false,
                currentStimulus = null,
                targetStimulus = null,
                remainingMs = 0L,
                stimulusStartedAtMs = 0L,
                targetShownAtMs = null,
                sequenceLength = 0,
                currentSequenceIndex = 0,
                tapLockedForCurrentStimulus = false,
                reactionAlreadyHandledForCurrentStimulus = false,
                roundPhase = RoundPhase.IDLE,
                feedbackMessage = "",
                feedbackType = FeedbackType.NEUTRAL
            )
        }

        refreshRanking()
    }

    fun refreshRanking() {
        _uiState.update {
            it.copy(
                bestResults = repository.getBestResultsByPlayer(),
                recentResults = repository.getAllSessions().take(10)
            )
        }
    }

    private fun launchRound() {
        cancelRoundJobs()

        val currentState = _uiState.value
        val roundDefinition = StimulusFactory.createRoundDefinition(
            mode = currentState.config.stimulusMode,
            reverseMode = currentState.config.reverseMode,
            level = currentState.level
        )

        currentRoundDefinition = roundDefinition
        roundDurationMs = computeRoundDurationMs(currentState.config, currentState.level)
        targetShownAt = 0L

        startPreparation(roundDefinition)
    }

    private fun startPreparation(roundDefinition: StimulusRoundDefinition) {
        val target = roundDefinition.targetStimulus

        _uiState.update {
            it.copy(
                currentStimulus = null,
                targetStimulus = target,
                remainingMs = roundDurationMs,
                stimulusStartedAtMs = 0L,
                targetShownAtMs = null,
                sequenceLength = 0,
                currentSequenceIndex = 0,
                tapLockedForCurrentStimulus = true,
                reactionAlreadyHandledForCurrentStimulus = false,
                roundPhase = RoundPhase.PREPARING,
                feedbackMessage = buildRoundInstruction(target),
                feedbackType = FeedbackType.NEUTRAL,
                lastReactionMs = null
            )
        }

        preparationJob = viewModelScope.launch {
            delay(3_000)

            if (!_uiState.value.inProgress || _uiState.value.isSessionFinished) return@launch

            startActiveRound(roundDefinition)
        }
    }

    private fun startActiveRound(roundDefinition: StimulusRoundDefinition) {
        targetShownAt = 0L

        val firstStimulus = roundDefinition.sequence.firstOrNull() ?: roundDefinition.targetStimulus
        showStimulus(
            stimulus = firstStimulus,
            index = 0,
            sequenceSize = roundDefinition.sequence.size
        )

        if (!firstStimulus.shouldReact) {
            startStimulusSequence(roundDefinition)
        }
    }

    private fun startRoundTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val elapsed = SystemClock.elapsedRealtime() - targetShownAt
                val pending = (roundDurationMs - elapsed).coerceAtLeast(0L)

                _uiState.update { state ->
                    state.copy(remainingMs = pending)
                }

                if (pending <= 0L) break
                delay(50)
            }

            resolveTimeout()
        }
    }

    private fun startStimulusSequence(roundDefinition: StimulusRoundDefinition) {
        if (roundDefinition.sequence.size <= 1) return

        val delays = buildDistractorDelays(
            sequenceSize = roundDefinition.sequence.size
        )

        sequenceJob = viewModelScope.launch {
            val lastIndex = roundDefinition.sequence.lastIndex

            for (index in 0 until lastIndex) {
                val delayMs = delays.getOrElse(index) { 650L }
                delay(delayMs)

                if (!_uiState.value.inProgress || _uiState.value.isSessionFinished) return@launch
                if (_uiState.value.reactionAlreadyHandledForCurrentStimulus) return@launch

                val nextIndex = index + 1
                val nextStimulus = roundDefinition.sequence[nextIndex]

                showStimulus(nextStimulus, nextIndex, roundDefinition.sequence.size)

                if (nextStimulus.shouldReact) {
                    return@launch
                }
            }
        }
    }

    private fun showStimulus(
        stimulus: StimulusUi,
        index: Int,
        sequenceSize: Int
    ) {
        val now = SystemClock.elapsedRealtime()
        val isTarget = stimulus.shouldReact
        val target = currentRoundDefinition?.targetStimulus

        if (isTarget && targetShownAt == 0L) {
            targetShownAt = now
        }

        _uiState.update {
            it.copy(
                currentStimulus = stimulus,
                targetStimulus = target,
                stimulusStartedAtMs = now,
                targetShownAtMs = if (targetShownAt == 0L) null else targetShownAt,
                sequenceLength = sequenceSize,
                currentSequenceIndex = index + 1,
                tapLockedForCurrentStimulus = false,
                reactionAlreadyHandledForCurrentStimulus = false,
                roundPhase = if (isTarget) RoundPhase.TARGET_VISIBLE else RoundPhase.STREAMING,
                feedbackMessage = buildActiveRoundMessage(),
                feedbackType = FeedbackType.NEUTRAL,
                remainingMs = if (isTarget) roundDurationMs else roundDurationMs
            )
        }

        playStimulusSoundIfEnabled()

        if (isTarget) {
            sequenceJob?.cancel()
            sequenceJob = null
            startRoundTimer()
        }
    }

    private fun resolveTimeout() {
        val state = _uiState.value
        if (!state.inProgress || state.isSessionFinished) return
        if (state.reactionAlreadyHandledForCurrentStimulus) return

        cancelRoundJobs()

        _uiState.update {
            it.copy(
                tapLockedForCurrentStimulus = true,
                reactionAlreadyHandledForCurrentStimulus = true
            )
        }

        val remainingLives = (state.lives - 1).coerceAtLeast(0)

        _uiState.update {
            it.copy(
                lives = remainingLives,
                incorrectCount = it.incorrectCount + 1,
                timeoutCount = it.timeoutCount + 1,
                feedbackMessage = "Se terminó el tiempo. No reaccionaste cuando apareció el objetivo.",
                feedbackType = FeedbackType.ERROR,
                roundPhase = RoundPhase.FEEDBACK,
                remainingMs = 0L,
                lastReactionMs = null
            )
        }

        playErrorSoundIfEnabled()
        scheduleAdvance()
    }

    private fun scheduleAdvance() {
        cancelRoundJobs()

        transitionJob?.cancel()
        transitionJob = viewModelScope.launch {
            delay(900)
            advanceOrFinish()
        }
    }

    private fun advanceOrFinish() {
        val state = _uiState.value

        if (state.lives <= 0) {
            finishGame(won = false)
            return
        }

        val lastRoundInLevel = state.roundInLevel >= state.config.iterationsPerLevel
        val lastLevel = state.level >= state.totalLevels

        if (lastRoundInLevel && lastLevel) {
            finishGame(won = true)
            return
        }

        if (lastRoundInLevel) {
            val nextLevel = state.level + 1
            val nextDifficulty = effectiveDifficultyForLevel(
                baseDifficulty = selectedDifficultyBeforeSession,
                level = nextLevel
            )

            _uiState.update {
                it.copy(
                    level = nextLevel,
                    roundInLevel = 1,
                    roundPhase = RoundPhase.LEVEL_TRANSITION,
                    config = it.config.copy(
                        difficulty = nextDifficulty,
                        maxReactionTimeSeconds = computeDisplayedBaseSecondsForLevel(nextLevel)
                    )
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    roundInLevel = it.roundInLevel + 1,
                    roundPhase = RoundPhase.PREPARING
                )
            }
        }

        launchRound()
    }

    private fun finishGame(won: Boolean) {
        cancelAllJobs()
        currentRoundDefinition = null
        targetShownAt = 0L

        _uiState.update {
            it.copy(
                inProgress = false,
                isSessionFinished = true,
                won = won,
                remainingMs = 0L,
                currentStimulus = null,
                targetStimulus = null,
                tapLockedForCurrentStimulus = true,
                reactionAlreadyHandledForCurrentStimulus = true,
                roundPhase = RoundPhase.FINISHED,
                feedbackMessage = if (won) {
                    "¡Completaste todos los niveles!"
                } else {
                    "Perdiste la partida."
                },
                feedbackType = if (won) FeedbackType.SUCCESS else FeedbackType.ERROR
            )
        }

        saveCurrentSessionIfNeeded()
        playFinalSoundIfEnabled(won)
    }

    private fun saveCurrentSessionIfNeeded() {
        val state = _uiState.value
        if (state.resultsSaved) return

        val record = ScoreRecord(
            playerName = state.config.playerName,
            difficulty = state.config.difficulty,
            stimulusMode = state.config.stimulusMode,
            reverseMode = state.config.reverseMode,
            score = state.score,
            correctCount = state.correctCount,
            incorrectCount = state.incorrectCount,
            timeoutCount = state.timeoutCount,
            averageReactionMs = state.averageReactionMs,
            bestReactionMs = state.bestReactionMs,
            won = state.won,
            playedAt = System.currentTimeMillis(),
            falseStartCount = state.falseStartCount
        )

        repository.saveSession(record)
        _uiState.update { it.copy(resultsSaved = true) }
        refreshRanking()
    }

    private fun computeRoundDurationMs(config: GameConfig, level: Int): Long {
        if (selectedDifficultyBeforeSession == Difficulty.TRAINING) {
            return config.maxReactionTimeSeconds * 1_000L
        }

        val seconds = computeDisplayedBaseSecondsForLevel(level)
        return seconds * 1_000L
    }

    private fun computeDisplayedBaseSecondsForLevel(level: Int): Int {
        if (selectedDifficultyBeforeSession == Difficulty.TRAINING) {
            return selectedMaxReactionTimeBeforeSession.coerceAtLeast(1)
        }

        val startLevel = startingLevelForDifficulty(selectedDifficultyBeforeSession)
        val levelOffset = (level - startLevel).coerceAtLeast(0)
        return (selectedMaxReactionTimeBeforeSession - (levelOffset * 5)).coerceAtLeast(2)
    }

    private fun buildDistractorDelays(sequenceSize: Int): List<Long> {
        val distractorCount = (sequenceSize - 1).coerceAtLeast(0)
        if (distractorCount == 0) return emptyList()

        return List(distractorCount) {
            Random.nextLong(450L, 1_251L)
        }
    }

    private fun calculatePoints(
        reactionMs: Long,
        availableMs: Long,
        difficulty: Difficulty
    ): Int {
        if (difficulty == Difficulty.TRAINING) return 0

        val speedFactor = 1.0 - (reactionMs.toDouble() / availableMs.toDouble()).coerceIn(0.0, 1.0)
        val base = 60 + (speedFactor * 90.0)
        return (base * difficulty.multiplier).roundToInt()
    }

    private fun buildRoundInstruction(target: StimulusUi?): String {
        val label = target?.label ?: "el objetivo"
        return "Tocá solo cuando aparezca $label. La ronda empieza en 3 segundos."
    }

    private fun buildActiveRoundMessage(): String {
        return "Observá la secuencia."
    }

    private fun playStimulusSoundIfEnabled() {
        if (_uiState.value.config.soundsEnabled) {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 70)
        }
    }

    private fun playSuccessSoundIfEnabled() {
        if (_uiState.value.config.soundsEnabled) {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 120)
        }
    }

    private fun playErrorSoundIfEnabled() {
        if (_uiState.value.config.soundsEnabled) {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 180)
        }
    }

    private fun playFinalSoundIfEnabled(won: Boolean) {
        if (_uiState.value.config.soundsEnabled) {
            val tone = if (won) {
                ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
            } else {
                ToneGenerator.TONE_PROP_NACK
            }
            toneGenerator.startTone(tone, 250)
        }
    }

    private fun startingLevelForDifficulty(difficulty: Difficulty): Int {
        return when (difficulty) {
            Difficulty.TRAINING,
            Difficulty.EASY -> 1

            Difficulty.MEDIUM -> 2
            Difficulty.HARD -> 3
        }
    }

    private fun effectiveDifficultyForLevel(
        baseDifficulty: Difficulty,
        level: Int
    ): Difficulty {
        if (baseDifficulty == Difficulty.TRAINING) return Difficulty.TRAINING

        return when (level.coerceIn(1, 3)) {
            1 -> Difficulty.EASY
            2 -> Difficulty.MEDIUM
            else -> Difficulty.HARD
        }
    }

    private fun minOfNullable(current: Long?, candidate: Long): Long {
        return current?.let { minOf(it, candidate) } ?: candidate
    }

    private fun cancelRoundJobs() {
        timerJob?.cancel()
        timerJob = null

        sequenceJob?.cancel()
        sequenceJob = null

        preparationJob?.cancel()
        preparationJob = null
    }

    private fun cancelAllJobs() {
        cancelRoundJobs()

        transitionJob?.cancel()
        transitionJob = null
    }

    override fun onCleared() {
        super.onCleared()
        cancelAllJobs()
        toneGenerator.release()
    }

    class Factory(
        private val repository: ScoreRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReactionGameViewModel::class.java)) {
                return ReactionGameViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}