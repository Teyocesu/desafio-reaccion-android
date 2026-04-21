package com.uade.desafioreaccion.data

import com.uade.desafioreaccion.model.StimulusCategory
import com.uade.desafioreaccion.model.StimulusMode
import com.uade.desafioreaccion.model.StimulusUi
import kotlin.random.Random

data class StimulusRoundDefinition(
    val selectedMode: StimulusMode,
    val ruleText: String,
    val sequence: List<StimulusUi>,
    val targetStimulus: StimulusUi
)

object StimulusFactory {

    fun create(mode: StimulusMode, reverseMode: Boolean, level: Int): StimulusUi {
        return createRoundDefinition(
            mode = mode,
            reverseMode = reverseMode,
            level = level
        ).targetStimulus
    }

    fun createRoundDefinition(
        mode: StimulusMode,
        reverseMode: Boolean,
        level: Int
    ): StimulusRoundDefinition {
        val selectedMode = resolveMode(mode)

        return when (selectedMode) {
            StimulusMode.COLORS -> createColorRound(reverseMode, level)
            StimulusMode.NUMBERS -> createNumberRound(reverseMode, level)
            StimulusMode.WORDS -> createWordRound(reverseMode, level)
            StimulusMode.MIXED -> createColorRound(reverseMode, level)
        }
    }

    private fun resolveMode(mode: StimulusMode): StimulusMode {
        return if (mode == StimulusMode.MIXED) {
            when (Random.nextInt(3)) {
                0 -> StimulusMode.COLORS
                1 -> StimulusMode.NUMBERS
                else -> StimulusMode.WORDS
            }
        } else {
            mode
        }
    }

    private fun createColorRound(reverseMode: Boolean, level: Int): StimulusRoundDefinition {
        val pool = when (level) {
            1 -> colorPool.take(4)
            2 -> colorPool.take(6)
            else -> colorPool
        }

        val distractorCount = randomDistractorCount(level)

        return if (reverseMode) {
            val forbiddenColor = pool.random()
            val allowedPool = pool.filter { it.name != forbiddenColor.name }
            val targetColor = allowedPool.random()
            val ruleText =
                "No toques cuando aparezca el color: ${forbiddenColor.name}. Toca el primer color permitido."

            val distractors = List(distractorCount) {
                forbiddenColor.toStimulus(
                    ruleText = ruleText,
                    shouldReact = false,
                    level = level,
                    isTarget = false,
                    sequenceIndex = 0
                )
            }

            val target = targetColor.toStimulus(
                ruleText = ruleText,
                shouldReact = true,
                level = level,
                isTarget = true,
                sequenceIndex = 0
            )

            val sequence = buildSequenceWithRandomTarget(distractors, target)
            val finalTarget = sequence.first { it.isTarget }

            StimulusRoundDefinition(
                selectedMode = StimulusMode.COLORS,
                ruleText = ruleText,
                sequence = sequence,
                targetStimulus = finalTarget
            )
        } else {
            val targetColor = pool.random()
            val distractorPool = pool.filter { it.name != targetColor.name }
            val ruleText = "Toca cuando aparezca el color: ${targetColor.name}"

            val distractors = List(distractorCount) {
                distractorPool.random().toStimulus(
                    ruleText = ruleText,
                    shouldReact = false,
                    level = level,
                    isTarget = false,
                    sequenceIndex = 0
                )
            }

            val target = targetColor.toStimulus(
                ruleText = ruleText,
                shouldReact = true,
                level = level,
                isTarget = true,
                sequenceIndex = 0
            )

            val sequence = buildSequenceWithRandomTarget(distractors, target)
            val finalTarget = sequence.first { it.isTarget }

            StimulusRoundDefinition(
                selectedMode = StimulusMode.COLORS,
                ruleText = ruleText,
                sequence = sequence,
                targetStimulus = finalTarget
            )
        }
    }

    private fun createNumberRound(reverseMode: Boolean, level: Int): StimulusRoundDefinition {
        val maxValue = when (level) {
            1 -> 20
            2 -> 60
            else -> 150
        }

        val distractorCount = randomDistractorCount(level)

        return if (reverseMode) {
            val forbiddenValue = Random.nextInt(1, maxValue + 1)
            var targetValue: Int
            do {
                targetValue = Random.nextInt(1, maxValue + 1)
            } while (targetValue == forbiddenValue)

            val ruleText =
                "No toques cuando aparezca el numero: $forbiddenValue. Toca el primer numero permitido."

            val distractors = List(distractorCount) {
                StimulusUi(
                    label = forbiddenValue.toString(),
                    category = StimulusCategory.NUMBERS,
                    ruleText = ruleText,
                    shouldReact = false,
                    accentColorHex = 0xFF7C3AED,
                    level = level,
                    isTarget = false,
                    sequenceIndex = 0
                )
            }

            val target = StimulusUi(
                label = targetValue.toString(),
                category = StimulusCategory.NUMBERS,
                ruleText = ruleText,
                shouldReact = true,
                accentColorHex = 0xFF4F46E5,
                level = level,
                isTarget = true,
                sequenceIndex = 0
            )

            val sequence = buildSequenceWithRandomTarget(distractors, target)
            val finalTarget = sequence.first { it.isTarget }

            StimulusRoundDefinition(
                selectedMode = StimulusMode.NUMBERS,
                ruleText = ruleText,
                sequence = sequence,
                targetStimulus = finalTarget
            )
        } else {
            val targetValue = Random.nextInt(1, maxValue + 1)
            val ruleText = "Toca cuando aparezca el numero: $targetValue"

            val distractors = List(distractorCount) {
                var value: Int
                do {
                    value = Random.nextInt(1, maxValue + 1)
                } while (value == targetValue)

                StimulusUi(
                    label = value.toString(),
                    category = StimulusCategory.NUMBERS,
                    ruleText = ruleText,
                    shouldReact = false,
                    accentColorHex = 0xFF7C3AED,
                    level = level,
                    isTarget = false,
                    sequenceIndex = 0
                )
            }

            val target = StimulusUi(
                label = targetValue.toString(),
                category = StimulusCategory.NUMBERS,
                ruleText = ruleText,
                shouldReact = true,
                accentColorHex = 0xFF4F46E5,
                level = level,
                isTarget = true,
                sequenceIndex = 0
            )

            val sequence = buildSequenceWithRandomTarget(distractors, target)
            val finalTarget = sequence.first { it.isTarget }

            StimulusRoundDefinition(
                selectedMode = StimulusMode.NUMBERS,
                ruleText = ruleText,
                sequence = sequence,
                targetStimulus = finalTarget
            )
        }
    }

    private fun createWordRound(reverseMode: Boolean, level: Int): StimulusRoundDefinition {
        val pool = when (level) {
            1 -> wordPool.take(4)
            2 -> wordPool.take(6)
            else -> wordPool
        }

        val distractorCount = randomDistractorCount(level)

        return if (reverseMode) {
            val forbiddenWord = pool.random()
            val allowedPool = pool.filter { it != forbiddenWord }
            val targetWord = allowedPool.random()
            val ruleText =
                "No toques cuando aparezca la palabra: $forbiddenWord. Toca la primera palabra permitida."

            val distractors = List(distractorCount) {
                StimulusUi(
                    label = forbiddenWord,
                    category = StimulusCategory.WORDS,
                    ruleText = ruleText,
                    shouldReact = false,
                    accentColorHex = 0xFFEF4444,
                    level = level,
                    isTarget = false,
                    sequenceIndex = 0
                )
            }

            val target = StimulusUi(
                label = targetWord,
                category = StimulusCategory.WORDS,
                ruleText = ruleText,
                shouldReact = true,
                accentColorHex = 0xFFFF8A00,
                level = level,
                isTarget = true,
                sequenceIndex = 0
            )

            val sequence = buildSequenceWithRandomTarget(distractors, target)
            val finalTarget = sequence.first { it.isTarget }

            StimulusRoundDefinition(
                selectedMode = StimulusMode.WORDS,
                ruleText = ruleText,
                sequence = sequence,
                targetStimulus = finalTarget
            )
        } else {
            val targetWord = pool.random()
            val distractorPool = pool.filter { it != targetWord }
            val ruleText = "Toca cuando aparezca la palabra: $targetWord"

            val distractors = List(distractorCount) {
                StimulusUi(
                    label = distractorPool.random(),
                    category = StimulusCategory.WORDS,
                    ruleText = ruleText,
                    shouldReact = false,
                    accentColorHex = 0xFFFFB84D,
                    level = level,
                    isTarget = false,
                    sequenceIndex = 0
                )
            }

            val target = StimulusUi(
                label = targetWord,
                category = StimulusCategory.WORDS,
                ruleText = ruleText,
                shouldReact = true,
                accentColorHex = 0xFFFF8A00,
                level = level,
                isTarget = true,
                sequenceIndex = 0
            )

            val sequence = buildSequenceWithRandomTarget(distractors, target)
            val finalTarget = sequence.first { it.isTarget }

            StimulusRoundDefinition(
                selectedMode = StimulusMode.WORDS,
                ruleText = ruleText,
                sequence = sequence,
                targetStimulus = finalTarget
            )
        }
    }

    private fun buildSequenceWithRandomTarget(
        distractors: List<StimulusUi>,
        target: StimulusUi
    ): List<StimulusUi> {
        val targetPosition = Random.nextInt(0, distractors.size + 1)
        val mutableSequence = distractors.toMutableList()
        mutableSequence.add(targetPosition, target)

        return mutableSequence.mapIndexed { index, stimulus ->
            stimulus.copy(
                sequenceIndex = index,
                isTarget = index == targetPosition,
                shouldReact = index == targetPosition
            )
        }
    }

    private fun randomDistractorCount(level: Int): Int {
        return when (level) {
            1 -> Random.nextInt(2, 4)
            2 -> Random.nextInt(3, 5)
            else -> Random.nextInt(4, 7)
        }
    }

    private fun NamedColor.toStimulus(
        ruleText: String,
        shouldReact: Boolean,
        level: Int,
        isTarget: Boolean,
        sequenceIndex: Int
    ): StimulusUi {
        return StimulusUi(
            label = name,
            category = StimulusCategory.COLORS,
            ruleText = ruleText,
            shouldReact = shouldReact,
            accentColorHex = hex,
            level = level,
            isTarget = isTarget,
            sequenceIndex = sequenceIndex
        )
    }

    private data class NamedColor(val name: String, val hex: Long)

    private val colorPool = listOf(
        NamedColor("ROJO", 0xFFE53935),
        NamedColor("VERDE", 0xFF43A047),
        NamedColor("AZUL", 0xFF1E88E5),
        NamedColor("AMARILLO", 0xFFFDD835),
        NamedColor("NARANJA", 0xFFFB8C00),
        NamedColor("MORADO", 0xFF8E24AA),
        NamedColor("CIAN", 0xFF00ACC1)
    )

    private val wordPool = listOf(
        "GO",
        "LISTO",
        "PLAY",
        "FLASH",
        "TOQUE",
        "RAPIDO",
        "STOP"
    )
}