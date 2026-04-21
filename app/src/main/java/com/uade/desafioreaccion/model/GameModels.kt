package com.uade.desafioreaccion.model

enum class Difficulty(val label: String, val defaultSeconds: Int, val multiplier: Double) {
    TRAINING("Entrenamiento", 20, 0.0),
    EASY("Fácil", 20, 1.0),
    MEDIUM("Medio", 15, 1.4),
    HARD("Difícil", 10, 1.8)
}

enum class StimulusMode(val label: String) {
    COLORS("Colores"),
    NUMBERS("Números"),
    WORDS("Palabras"),
    MIXED("Mixto")
}

enum class StimulusCategory {
    COLORS,
    NUMBERS,
    WORDS
}

enum class FeedbackType {
    NEUTRAL,
    SUCCESS,
    ERROR
}

enum class RoundPhase {
    IDLE,
    PREPARING,
    STREAMING,
    TARGET_VISIBLE,
    FEEDBACK,
    LEVEL_TRANSITION,
    FINISHED
}

data class GameConfig(
    val playerName: String = "",
    val difficulty: Difficulty = Difficulty.EASY,
    val stimulusMode: StimulusMode = StimulusMode.MIXED,
    val iterationsPerLevel: Int = 20,
    val maxReactionTimeSeconds: Int = Difficulty.EASY.defaultSeconds,
    val reverseMode: Boolean = false,
    val soundsEnabled: Boolean = true
)

data class StimulusUi(
    val label: String,
    val category: StimulusCategory,
    val ruleText: String,
    val shouldReact: Boolean,
    val accentColorHex: Long,
    val level: Int,
    val isTarget: Boolean = false,
    val sequenceIndex: Int = 0
)

data class ScoreRecord(
    val playerName: String,
    val difficulty: Difficulty,
    val stimulusMode: StimulusMode,
    val reverseMode: Boolean,
    val score: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val timeoutCount: Int,
    val averageReactionMs: Long?,
    val bestReactionMs: Long?,
    val won: Boolean,
    val playedAt: Long,
    val falseStartCount: Int = 0
)

data class GameUiState(
    val config: GameConfig = GameConfig(),
    val inProgress: Boolean = false,
    val isSessionFinished: Boolean = false,
    val won: Boolean = false,
    val level: Int = 1,
    val totalLevels: Int = 3,
    val roundInLevel: Int = 1,
    val score: Int = 0,
    val lives: Int = 3,

    val roundPhase: RoundPhase = RoundPhase.IDLE,

    val currentStimulus: StimulusUi? = null,
    val targetStimulus: StimulusUi? = null,

    val remainingMs: Long = 0L,
    val stimulusStartedAtMs: Long = 0L,
    val targetShownAtMs: Long? = null,

    val feedbackMessage: String = "",
    val feedbackType: FeedbackType = FeedbackType.NEUTRAL,

    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val timeoutCount: Int = 0,
    val falseStartCount: Int = 0,

    val totalReactionMs: Long = 0L,
    val measuredReactionCount: Int = 0,
    val bestReactionMs: Long? = null,
    val lastReactionMs: Long? = null,

    val sequenceLength: Int = 0,
    val currentSequenceIndex: Int = 0,

    val tapLockedForCurrentStimulus: Boolean = false,
    val reactionAlreadyHandledForCurrentStimulus: Boolean = false,

    val bestResults: List<ScoreRecord> = emptyList(),
    val recentResults: List<ScoreRecord> = emptyList(),
    val resultsSaved: Boolean = false
) {
    val averageReactionMs: Long?
        get() = if (measuredReactionCount == 0) null else totalReactionMs / measuredReactionCount

    val totalRoundsPlanned: Int
        get() = totalLevels * config.iterationsPerLevel

    val completedRounds: Int
        get() = ((level - 1) * config.iterationsPerLevel) + (roundInLevel - 1)

    val currentGlobalRound: Int
        get() = completedRounds + 1
}