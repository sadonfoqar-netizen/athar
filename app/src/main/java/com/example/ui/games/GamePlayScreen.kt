package com.example.ui.games

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.games.EvaluationResult
import com.example.data.games.GameQuestion
import com.example.data.games.GameRepository
import com.example.data.games.GameType
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.AtharDeepBlack
import com.example.ui.theme.AtharGold
import com.example.ui.theme.AtharGoldDark
import com.example.ui.theme.AtharMilkyCreamBg
import com.example.ui.theme.AtharOliveContainer
import com.example.ui.theme.AtharOlivePrimary
import com.example.ui.theme.AtharTextMuted
import com.example.ui.theme.CairoFontFamily
import kotlinx.coroutines.launch

@Composable
fun GamePlayScreen(
  gameType: GameType,
  repository: GameRepository,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler(onBack = onBack)

  val scope = rememberCoroutineScope()
  val totalScore by repository.score.collectAsState()
  val isOnline = remember { repository.isOnline() }

  var questions by remember { mutableStateOf<List<GameQuestion>>(emptyList()) }
  var currentIndex by remember { mutableIntStateOf(0) }
  var isLoading by remember { mutableStateOf(true) }

  // Game Play State
  var selectedChoice by remember { mutableStateOf<String?>(null) }
  var fillBlankText by remember { mutableStateOf("") }
  var orderingItems by remember { mutableStateOf<List<String>>(emptyList()) }

  var isSubmitted by remember { mutableStateOf(false) }
  var isEvaluating by remember { mutableStateOf(false) }
  var evaluationResult by remember { mutableStateOf<EvaluationResult?>(null) }

  val scrollState = rememberScrollState()

  // Load questions for this game type
  LaunchedEffect(gameType) {
    isLoading = true
    questions = repository.getQuestionsForGame(gameType)
    isLoading = false
  }

  val currentQuestion = questions.getOrNull(currentIndex)

  // Initialize per-question state
  LaunchedEffect(currentIndex, currentQuestion) {
    selectedChoice = null
    fillBlankText = ""
    isSubmitted = false
    evaluationResult = null
    if (currentQuestion != null && gameType == GameType.ORDERING) {
      orderingItems = currentQuestion.options.ifEmpty { currentQuestion.correctOrder.shuffled() }
    }
  }

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(AtharMilkyCreamBg)
        .statusBarsPadding()
        .navigationBarsPadding()
        .testTag("game_play_screen_${gameType.name.lowercase()}")
    ) {
      if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator(color = AtharOlivePrimary)
        }
      } else if (currentQuestion == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
          ) {
            Text(
              text = "اكتملت جميع أسئلة هذا القسم!",
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 20.sp,
              color = AtharDeepBlack
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "يمكنك إعادة تحميل المخزون لـ 100 سؤال جديد.",
              fontFamily = CairoFontFamily,
              fontSize = 13.5.sp,
              color = AtharTextMuted
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
              onClick = {
                repository.resetPoolForGame(gameType)
                scope.launch {
                  questions = repository.getQuestionsForGame(gameType)
                  currentIndex = 0
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = AtharOlivePrimary)
            ) {
              Text("إعادة بدء الأسئلة", fontFamily = CairoFontFamily)
            }
          }
        }
      } else {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 20.dp)
        ) {

          // 1. Top Action Bar
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(onClick = onBack, modifier = Modifier.testTag("game_back_button")) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "رجوع",
                  tint = AtharDeepBlack,
                  modifier = Modifier.size(24.dp)
                )
              }
              Spacer(modifier = Modifier.width(6.dp))
              Column {
                Text(
                  text = gameType.title,
                  fontFamily = CairoFontFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 17.sp,
                  color = AtharDeepBlack
                )
                Text(
                  text = "السؤال ${currentIndex + 1} من ${questions.size}",
                  fontFamily = CairoFontFamily,
                  fontSize = 11.5.sp,
                  color = AtharOlivePrimary
                )
              }
            }

            // Score Pill & Connection Status Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
              // Online / Offline Status Badge
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (isOnline) AtharOliveContainer else Color(0xFFF2ECE1))
                  .padding(horizontal = 8.dp, vertical = 5.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = if (isOnline) AtharOlivePrimary else AtharTextMuted,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = if (isOnline) "سحابي ذكي" else "محلي",
                    fontFamily = CairoFontFamily,
                    fontSize = 10.5.sp,
                    color = if (isOnline) AtharOlivePrimary else AtharTextMuted
                  )
                }
              }

              Spacer(modifier = Modifier.width(8.dp))

              // Score
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color.White)
                  .border(1.dp, AtharGold.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                  .padding(horizontal = 10.dp, vertical = 5.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = AtharGold,
                    modifier = Modifier.size(15.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "$totalScore نقطة",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    color = AtharGoldDark
                  )
                }
              }
            }
          }

          HorizontalDivider(color = AtharGold.copy(alpha = 0.2f), thickness = 0.8.dp)

          // 2. Question Board
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 18.dp, vertical = 14.dp)
          ) {

            // Source tag
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(AtharOliveContainer)
                .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Text(
                text = currentQuestion.source,
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = AtharOlivePrimary
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Question Prompt
            Text(
              text = currentQuestion.prompt,
              fontFamily = AmiriFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 22.sp,
              lineHeight = 32.sp,
              color = AtharDeepBlack,
              modifier = Modifier.testTag("question_prompt_text")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Game Type Specific Interactive Component
            when (gameType) {
              GameType.FILL_BLANK -> {
                GameFillBlankComponent(
                  currentText = fillBlankText,
                  onTextChange = { fillBlankText = it },
                  suggestedOptions = currentQuestion.options,
                  isEnabled = !isSubmitted
                )
              }

              GameType.TRUE_FALSE -> {
                TrueFalseChoiceComponent(
                  selectedChoice = selectedChoice,
                  onSelect = { if (!isSubmitted) selectedChoice = it },
                  isSubmitted = isSubmitted,
                  correctAnswer = currentQuestion.correctAnswer
                )
              }

              GameType.MULTIPLE_CHOICE -> {
                MultipleChoiceComponent(
                  options = currentQuestion.options,
                  selectedChoice = selectedChoice,
                  onSelect = { if (!isSubmitted) selectedChoice = it },
                  isSubmitted = isSubmitted,
                  correctAnswer = currentQuestion.correctAnswer
                )
              }

              GameType.ORDERING -> {
                GameOrderingComponent(
                  items = orderingItems,
                  onMoveUp = { idx ->
                    if (idx > 0) {
                      val list = orderingItems.toMutableList()
                      val item = list.removeAt(idx)
                      list.add(idx - 1, item)
                      orderingItems = list
                    }
                  },
                  onMoveDown = { idx ->
                    if (idx < orderingItems.size - 1) {
                      val list = orderingItems.toMutableList()
                      val item = list.removeAt(idx)
                      list.add(idx + 1, item)
                      orderingItems = list
                    }
                  },
                  isAnswerSubmitted = isSubmitted,
                  correctOrder = currentQuestion.correctOrder
                )
              }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Submit / Next Button
            if (!isSubmitted) {
              val isReadyToSubmit = when (gameType) {
                GameType.FILL_BLANK -> fillBlankText.trim().isNotEmpty()
                GameType.TRUE_FALSE, GameType.MULTIPLE_CHOICE -> selectedChoice != null
                GameType.ORDERING -> orderingItems.isNotEmpty()
              }

              Button(
                onClick = {
                  if (isReadyToSubmit) {
                    isEvaluating = true
                    scope.launch {
                      val result = when (gameType) {
                        GameType.FILL_BLANK -> {
                          repository.evaluateAnswer(
                            userAnswer = fillBlankText,
                            expectedAnswer = currentQuestion.correctAnswer,
                            prompt = currentQuestion.prompt
                          )
                        }
                        GameType.TRUE_FALSE, GameType.MULTIPLE_CHOICE -> {
                          val correct = selectedChoice == currentQuestion.correctAnswer
                          EvaluationResult(
                            isCorrect = correct,
                            feedbackTitle = if (correct) "إجابة صحيحة! أحسنت" else "إجابة غير صحيحة",
                            explanation = currentQuestion.explanation,
                            recognizedAnswer = currentQuestion.correctAnswer
                          )
                        }
                        GameType.ORDERING -> {
                          val correct = orderingItems == currentQuestion.correctOrder
                          EvaluationResult(
                            isCorrect = correct,
                            feedbackTitle = if (correct) "ترتيب سليم ومتقن 100%!" else "الترتيب غير مكتمل",
                            explanation = currentQuestion.explanation,
                            recognizedAnswer = currentQuestion.correctOrder.joinToString(" ← ")
                          )
                        }
                      }

                      evaluationResult = result
                      isSubmitted = true
                      isEvaluating = false
                      repository.consumeQuestion(gameType, currentQuestion.id, result.isCorrect)
                    }
                  }
                },
                enabled = isReadyToSubmit && !isEvaluating,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = AtharOlivePrimary,
                  contentColor = Color.White
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(52.dp)
                  .testTag("submit_answer_button")
              ) {
                if (isEvaluating) {
                  CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                } else {
                  Text(
                    text = "تأكيد الإجابة",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                  )
                }
              }
            } else {
              Button(
                onClick = {
                  if (currentIndex < questions.size - 1) {
                    currentIndex++
                  } else {
                    onBack()
                  }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = AtharOlivePrimary,
                  contentColor = Color.White
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(52.dp)
                  .testTag("next_question_button")
              ) {
                Text(
                  text = if (currentIndex < questions.size - 1) "السؤال التالي" else "إنهاء والعودة للرئيسية",
                  fontFamily = CairoFontFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp
                )
              }
            }

            // 5. Feedback Sheet Card (Immediate explanation upon submit)
            AnimatedVisibility(
              visible = isSubmitted && evaluationResult != null,
              enter = fadeIn() + slideInVertically { it / 2 }
            ) {
              val result = evaluationResult ?: return@AnimatedVisibility
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 18.dp)
                  .testTag("feedback_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (result.isCorrect) AtharOliveContainer else Color(0xFFFDF2E9)
                ),
                border = null
              ) {
                Column(modifier = Modifier.padding(16.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = if (result.isCorrect) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                      contentDescription = null,
                      tint = if (result.isCorrect) AtharOlivePrimary else Color(0xFFC0392B),
                      modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = result.feedbackTitle,
                      fontFamily = CairoFontFamily,
                      fontWeight = FontWeight.Bold,
                      fontSize = 16.sp,
                      color = if (result.isCorrect) AtharOlivePrimary else Color(0xFFC0392B)
                    )
                  }

                  Spacer(modifier = Modifier.height(10.dp))

                  // Scientific / Islamic benefit box
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(12.dp))
                      .background(Color.White)
                      .padding(12.dp)
                  ) {
                    Column {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                          imageVector = Icons.Default.Lightbulb,
                          contentDescription = null,
                          tint = AtharGold,
                          modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                          text = "الفائدة العلمية والمعلومة الموثقة:",
                          fontFamily = CairoFontFamily,
                          fontWeight = FontWeight.Bold,
                          fontSize = 12.5.sp,
                          color = AtharDeepBlack
                        )
                      }
                      Spacer(modifier = Modifier.height(6.dp))
                      Text(
                        text = result.explanation.ifBlank { currentQuestion.explanation },
                        fontFamily = CairoFontFamily,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = AtharDeepBlack,
                        textAlign = TextAlign.Justify
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun TrueFalseChoiceComponent(
  selectedChoice: String?,
  onSelect: (String) -> Unit,
  isSubmitted: Boolean,
  correctAnswer: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    listOf("صح", "خطأ").forEach { choice ->
      val isSelected = selectedChoice == choice
      val isTrue = choice == "صح"

      val borderColor = when {
        isSubmitted && choice == correctAnswer -> AtharOlivePrimary
        isSubmitted && isSelected && choice != correctAnswer -> Color(0xFFC0392B)
        isSelected -> AtharOlivePrimary
        else -> AtharGold.copy(alpha = 0.35f)
      }

      val bgColor = when {
        isSubmitted && choice == correctAnswer -> AtharOliveContainer
        isSubmitted && isSelected && choice != correctAnswer -> Color(0xFFFDEDEC)
        isSelected -> AtharOliveContainer.copy(alpha = 0.5f)
        else -> Color.White
      }

      Box(
        modifier = Modifier
          .weight(1f)
          .height(64.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(bgColor)
          .border(2.dp, borderColor, RoundedCornerShape(16.dp))
          .clickable { onSelect(choice) }
          .testTag("choice_tf_$choice"),
        contentAlignment = Alignment.Center
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (isTrue) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (isTrue) AtharOlivePrimary else Color(0xFFC0392B),
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = choice,
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = AtharDeepBlack
          )
        }
      }
    }
  }
}

@Composable
private fun MultipleChoiceComponent(
  options: List<String>,
  selectedChoice: String?,
  onSelect: (String) -> Unit,
  isSubmitted: Boolean,
  correctAnswer: String
) {
  val letters = listOf("أ", "ب", "ج", "د")
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    options.forEachIndexed { index, option ->
      val letter = letters.getOrElse(index) { "${index + 1}" }
      val isSelected = selectedChoice == option

      val borderColor = when {
        isSubmitted && option == correctAnswer -> AtharOlivePrimary
        isSubmitted && isSelected && option != correctAnswer -> Color(0xFFC0392B)
        isSelected -> AtharOlivePrimary
        else -> AtharGold.copy(alpha = 0.35f)
      }

      val bgColor = when {
        isSubmitted && option == correctAnswer -> AtharOliveContainer
        isSubmitted && isSelected && option != correctAnswer -> Color(0xFFFDEDEC)
        isSelected -> AtharOliveContainer.copy(alpha = 0.5f)
        else -> Color.White
      }

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(bgColor)
          .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
          .clickable { onSelect(option) }
          .padding(horizontal = 14.dp, vertical = 12.dp)
          .testTag("choice_mc_$index")
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(if (isSelected || (isSubmitted && option == correctAnswer)) AtharOlivePrimary else AtharGold.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = letter,
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = if (isSelected || (isSubmitted && option == correctAnswer)) Color.White else AtharDeepBlack
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Text(
            text = option,
            fontFamily = CairoFontFamily,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.5.sp,
            color = AtharDeepBlack,
            modifier = Modifier.weight(1f)
          )
        }
      }
    }
  }
}
