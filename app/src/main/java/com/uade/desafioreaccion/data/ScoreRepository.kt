package com.uade.desafioreaccion.data

import android.content.Context
import com.uade.desafioreaccion.model.Difficulty
import com.uade.desafioreaccion.model.ScoreRecord
import com.uade.desafioreaccion.model.StimulusMode
import org.json.JSONArray
import org.json.JSONObject

class ScoreRepository(context: Context) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveSession(record: ScoreRecord) {
        val array = JSONArray(preferences.getString(KEY_SESSIONS, "[]") ?: "[]")
        array.put(record.toJson())
        preferences.edit().putString(KEY_SESSIONS, array.toString()).apply()
    }

    fun getAllSessions(): List<ScoreRecord> {
        val raw = preferences.getString(KEY_SESSIONS, "[]") ?: "[]"
        val array = JSONArray(raw)
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                runCatching { add(item.toScoreRecord()) }
            }
        }.sortedByDescending { it.playedAt }
    }

    fun getBestResultsByPlayer(): List<ScoreRecord> {
        return getAllSessions()
            .filter { it.difficulty != Difficulty.TRAINING }
            .groupBy { it.playerName.trim().lowercase() }
            .values
            .mapNotNull { records ->
                records.maxWithOrNull(
                    compareBy<ScoreRecord> { it.score }
                        .thenBy { it.correctCount }
                        .thenBy { -(it.averageReactionMs ?: Long.MAX_VALUE) }
                        .thenBy { it.playedAt }
                )
            }
            .sortedWith(
                compareByDescending<ScoreRecord> { it.score }
                    .thenByDescending { it.correctCount }
                    .thenBy { it.averageReactionMs ?: Long.MAX_VALUE }
                    .thenByDescending { it.playedAt }
            )
    }

    private fun ScoreRecord.toJson(): JSONObject = JSONObject().apply {
        put("playerName", playerName)
        put("difficulty", difficulty.name)
        put("stimulusMode", stimulusMode.name)
        put("reverseMode", reverseMode)
        put("score", score)
        put("correctCount", correctCount)
        put("incorrectCount", incorrectCount)
        put("timeoutCount", timeoutCount)
        if (averageReactionMs != null) put("averageReactionMs", averageReactionMs) else put("averageReactionMs", JSONObject.NULL)
        if (bestReactionMs != null) put("bestReactionMs", bestReactionMs) else put("bestReactionMs", JSONObject.NULL)
        put("won", won)
        put("playedAt", playedAt)
    }

    private fun JSONObject.toScoreRecord(): ScoreRecord {
        return ScoreRecord(
            playerName = optString("playerName", "Jugador"),
            difficulty = enumValueOfOrDefault(optString("difficulty"), Difficulty.EASY),
            stimulusMode = enumValueOfOrDefault(optString("stimulusMode"), StimulusMode.MIXED),
            reverseMode = optBoolean("reverseMode", false),
            score = optInt("score", 0),
            correctCount = optInt("correctCount", 0),
            incorrectCount = optInt("incorrectCount", 0),
            timeoutCount = optInt("timeoutCount", 0),
            averageReactionMs = if (isNull("averageReactionMs")) null else optLong("averageReactionMs"),
            bestReactionMs = if (isNull("bestReactionMs")) null else optLong("bestReactionMs"),
            won = optBoolean("won", false),
            playedAt = optLong("playedAt", System.currentTimeMillis())
        )
    }

    private inline fun <reified T : Enum<T>> enumValueOfOrDefault(name: String, default: T): T {
        return enumValues<T>().firstOrNull { it.name == name } ?: default
    }

    companion object {
        private const val PREFS_NAME = "reaction_game_prefs"
        private const val KEY_SESSIONS = "stored_sessions"
    }
}
