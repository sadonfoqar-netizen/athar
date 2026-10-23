package com.example.data.games

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val PREFS_GAMES = "athar_games_prefs"
private const val KEY_SCORE = "key_total_games_score"

class GameRepository(
  private val context: Context,
  private val scope: CoroutineScope
) {
  private val aiService = GameAiService()
  private val prefs = context.getSharedPreferences(PREFS_GAMES, Context.MODE_PRIVATE)

  private val _score = MutableStateFlow(prefs.getInt(KEY_SCORE, 0))
  val score: StateFlow<Int> = _score.asStateFlow()

  // Track counts per game
  private val _availableCounts = MutableStateFlow(
    mapOf(
      GameType.FILL_BLANK to getAvailableCount(GameType.FILL_BLANK),
      GameType.TRUE_FALSE to getAvailableCount(GameType.TRUE_FALSE),
      GameType.MULTIPLE_CHOICE to getAvailableCount(GameType.MULTIPLE_CHOICE),
      GameType.ORDERING to getAvailableCount(GameType.ORDERING)
    )
  )
  val availableCounts: StateFlow<Map<GameType, Int>> = _availableCounts.asStateFlow()

  private val _isReplenishing = MutableStateFlow(false)
  val isReplenishing: StateFlow<Boolean> = _isReplenishing.asStateFlow()

  fun isOnline(): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
  }

  suspend fun getQuestionsForGame(type: GameType): List<GameQuestion> = withContext(Dispatchers.IO) {
    val baseList = GameLocalPoolProvider.getInitialQuestions(type)
    val consumedIds = getConsumedIds(type)
    val remaining = baseList.filterNot { consumedIds.contains(it.id) }
    if (remaining.isNotEmpty()) remaining else baseList
  }

  fun consumeQuestion(type: GameType, questionId: String, isCorrect: Boolean) {
    val consumed = getConsumedIds(type).toMutableSet()
    consumed.add(questionId)
    saveConsumedIds(type, consumed)

    // Update score
    if (isCorrect) {
      val newScore = _score.value + 10
      _score.value = newScore
      prefs.edit().putInt(KEY_SCORE, newScore).apply()
    }

    val count = getAvailableCount(type)
    _availableCounts.update { it + (type to count) }

    // If online, trigger background replenishment to replenish back to 100!
    if (isOnline() && count < 100) {
      triggerBackgroundReplenishment(type)
    }
  }

  private fun triggerBackgroundReplenishment(type: GameType) {
    scope.launch(Dispatchers.IO) {
      _isReplenishing.value = true
      val newQ = aiService.generateReplenishmentQuestion(type)
      if (newQ != null) {
        // Replenishment successful: remove an old consumed id to restore count
        val consumed = getConsumedIds(type).toMutableSet()
        if (consumed.isNotEmpty()) {
          val first = consumed.first()
          consumed.remove(first)
          saveConsumedIds(type, consumed)
        }
      }
      val count = getAvailableCount(type)
      _availableCounts.update { it + (type to count) }
      _isReplenishing.value = false
    }
  }

  suspend fun evaluateAnswer(
    userAnswer: String,
    expectedAnswer: String,
    prompt: String
  ): EvaluationResult {
    return aiService.evaluateAnswer(userAnswer, expectedAnswer, prompt)
  }

  fun resetPoolForGame(type: GameType) {
    saveConsumedIds(type, emptySet())
    val count = getAvailableCount(type)
    _availableCounts.update { it + (type to count) }
  }

  private fun getAvailableCount(type: GameType): Int {
    val initialSize = 100
    val consumedCount = getConsumedIds(type).size
    val available = initialSize - consumedCount
    return if (available > 0) available else 100
  }

  private fun getConsumedIds(type: GameType): Set<String> {
    val raw = prefs.getString("consumed_${type.name}", "") ?: ""
    return if (raw.isBlank()) emptySet() else raw.split(",").toSet()
  }

  private fun saveConsumedIds(type: GameType, ids: Set<String>) {
    prefs.edit().putString("consumed_${type.name}", ids.joinToString(",")).apply()
  }
}
