package com.example.ui.ai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ai.AiChatRepository
import com.example.data.ai.ChatMessage
import com.example.data.ai.ChatSession
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.AtharCreamCardElevated
import com.example.ui.theme.AtharCreamDivider
import com.example.ui.theme.AtharCreamInputBg
import com.example.ui.theme.AtharDeepBlack
import com.example.ui.theme.AtharGold
import com.example.ui.theme.AtharGoldLight
import com.example.ui.theme.AtharMilkyCreamBg
import com.example.ui.theme.AtharMilkyCreamSubtle
import com.example.ui.theme.AtharOliveContainer
import com.example.ui.theme.AtharOliveLight
import com.example.ui.theme.AtharOlivePrimary
import com.example.ui.theme.AtharOnOliveContainer
import com.example.ui.theme.AtharTextMuted
import com.example.ui.theme.CairoFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
  repository: AiChatRepository,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val focusManager = LocalFocusManager.current

  // Hardware back returns to previous tab
  BackHandler {
    onBackClick()
  }

  val activeSession by repository.activeSession.collectAsStateWithLifecycle()
  val sessions by repository.sessions.collectAsStateWithLifecycle()
  val isGenerating by repository.isGenerating.collectAsStateWithLifecycle()

  var inputText by rememberSaveable { mutableStateOf("") }
  var showHistorySheet by remember { mutableStateOf(false) }

  val listState = rememberLazyListState()
  val messages = activeSession?.messages ?: emptyList()

  // Track already revealed messages to stream only newly generated assistant responses
  val revealedMessageIds = remember(activeSession?.id) {
    mutableSetOf<String>().apply {
      activeSession?.messages?.forEach { add(it.id) }
    }
  }

  // Auto scroll to bottom smoothly when new messages arrive
  LaunchedEffect(messages.size, isGenerating) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  fun copyToClipboard(text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Athar AI", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "تم نسخ النص بنجاح", Toast.LENGTH_SHORT).show()
  }

  fun shareText(text: String) {
    val sendIntent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_TEXT, "$text\n\n[من تطبيق أثر - المساعد الإيماني]")
      type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "مشاركة الفائدة"))
  }

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(AtharMilkyCreamBg)
        .statusBarsPadding()
        .testTag("ai_chat_screen")
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .imePadding()
      ) {

        // 1. Top Header Bar
        AiChatTopHeaderBar(
          onBackClick = onBackClick,
          onOpenHistory = { showHistorySheet = true }
        )

        HorizontalDivider(color = AtharCreamDivider, thickness = 0.8.dp)

        // 2. Chat View (Messages List / Clean Empty State)
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        ) {
          if (messages.isEmpty() && !isGenerating) {
            // Completely clean serene empty state with transparent Zolavor / ZR emblem in the center
            AiChatSereneCenterEmblem()
          } else {
            LazyColumn(
              state = listState,
              contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
              verticalArrangement = Arrangement.spacedBy(14.dp),
              modifier = Modifier.fillMaxSize()
            ) {
              items(messages, key = { it.id }) { msg ->
                val isNew = msg.id !in revealedMessageIds
                val visibleState = remember(msg.id) {
                  MutableTransitionState(false).apply { targetState = true }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                  AnimatedVisibility(
                    visibleState = visibleState,
                    enter = fadeIn(animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)) +
                      slideInVertically(
                        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                        initialOffsetY = { it / 3 }
                      )
                  ) {
                    ChatMessageBubble(
                      message = msg,
                      isNewMessage = isNew,
                      onTypingComplete = {
                        revealedMessageIds.add(msg.id)
                      },
                      onCopy = { copyToClipboard(msg.content) },
                      onShare = { shareText(msg.content) }
                    )
                  }
                }
              }

              if (isGenerating) {
                item(key = "generating_indicator") {
                  val generatingVisibleState = remember {
                    MutableTransitionState(false).apply { targetState = true }
                  }
                  Column(modifier = Modifier.fillMaxWidth()) {
                    AnimatedVisibility(
                      visibleState = generatingVisibleState,
                      enter = fadeIn(tween(250)) + slideInVertically(initialOffsetY = { it / 4 })
                    ) {
                      GeneratingBubbleIndicator()
                    }
                  }
                }
              }
            }
          }
        }

        HorizontalDivider(color = AtharCreamDivider, thickness = 0.8.dp)

        // 3. Bottom Input Area
        AiChatBottomInputArea(
          text = inputText,
          onTextChanged = { inputText = it },
          isGenerating = isGenerating,
          onSend = {
            val q = inputText.trim()
            if (q.isNotEmpty()) {
              inputText = ""
              focusManager.clearFocus()
              scope.launch {
                repository.sendMessage(q)
              }
            }
          }
        )
      }

      // History & Saved Conversations Bottom Sheet
      if (showHistorySheet) {
        AiChatHistoryBottomSheet(
          sessions = sessions,
          activeSessionId = activeSession?.id,
          onSelectSession = { session ->
            repository.selectSession(session.id)
            showHistorySheet = false
          },
          onNewChat = {
            repository.createNewSession()
            showHistorySheet = false
          },
          onDeleteSession = { session ->
            repository.deleteSession(session.id)
          },
          onDismiss = { showHistorySheet = false }
        )
      }
    }
  }
}

/**
 * Top Header Bar:
 * - Return back button to previous container
 * - Centered "أَثَـر" Brand logo
 * - Three dots (⋮) menu button for Firebase saved chats
 */
@Composable
private fun AiChatTopHeaderBar(
  onBackClick: () -> Unit,
  onOpenHistory: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 10.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {

    // Right (RTL): Back Button
    IconButton(
      onClick = onBackClick,
      modifier = Modifier
        .size(42.dp)
        .testTag("ai_chat_back_button")
    ) {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = "رجوع",
        tint = AtharDeepBlack,
        modifier = Modifier.size(24.dp)
      )
    }

    // Dead Center: "أَثَـر" Logo in deep black and gold
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = null,
          tint = AtharGold,
          modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
          text = "أَثَـر",
          fontFamily = AmiriFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 24.sp,
          color = AtharDeepBlack
        )
      }
      Text(
        text = "المساعد الإيماني الذكي",
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        color = AtharGold
      )
    }

    // Left (RTL): Three dots menu for previous conversations
    IconButton(
      onClick = onOpenHistory,
      modifier = Modifier
        .size(42.dp)
        .testTag("ai_chat_history_button")
    ) {
      Icon(
        imageVector = Icons.Default.MoreVert,
        contentDescription = "المحادثات السابقة",
        tint = AtharDeepBlack,
        modifier = Modifier.size(24.dp)
      )
    }
  }
}

/**
 * Serene, pristine center emblem when chat is empty:
 * Zero placeholders, zero prompt chips, zero cards.
 * Transparent elegant Zolavor / ZR emblem in soft gold and deep black.
 */
@Composable
private fun AiChatSereneCenterEmblem(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .testTag("ai_chat_empty_state"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Subtle transparent double-ring circular frame
      Box(
        modifier = Modifier
          .size(86.dp)
          .clip(CircleShape)
          .border(1.2.dp, AtharGold.copy(alpha = 0.35f), CircleShape)
          .padding(5.dp)
          .border(0.8.dp, AtharOliveLight.copy(alpha = 0.20f), CircleShape)
          .background(AtharMilkyCreamSubtle.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        // Pure Gold Three Sparkles Icon
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = null,
          tint = AtharGold,
          modifier = Modifier.size(36.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Main Calligraphic Athar Title
      Text(
        text = "أَثَـر",
        fontFamily = AmiriFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        color = AtharDeepBlack
      )

      Spacer(modifier = Modifier.height(4.dp))

      // Zolavor / ZR subtle brand signature
      Text(
        text = "ZOLAVOR • ZR",
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.5.sp,
        letterSpacing = 2.sp,
        color = AtharGold
      )
    }
  }
}

/**
 * Individual Chat Message Bubble with:
 * - Fade & Slide Up Entrance Animation
 * - Typewriter Stream Effect (word-by-word streaming) for AI assistant responses
 * - Instant tap-to-complete
 * - Copy and share actions revealed upon completion
 */
@Composable
private fun ChatMessageBubble(
  message: ChatMessage,
  isNewMessage: Boolean,
  onTypingComplete: () -> Unit,
  onCopy: () -> Unit,
  onShare: () -> Unit
) {
  val isUser = message.role == "user"

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isUser) Arrangement.Start else Arrangement.End
  ) {
    if (isUser) {
      // User Message Card (RTL Start)
      Card(
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
        colors = CardDefaults.cardColors(containerColor = AtharOliveContainer),
        border = null,
        modifier = Modifier.fillMaxWidth(0.85f)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = message.content,
            fontFamily = CairoFontFamily,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            color = AtharOnOliveContainer
          )
        }
      }
    } else {
      // Assistant Message Card with Typewriter streaming effect
      val words = remember(message.content) {
        // Tokenize words while preserving spaces and line breaks
        message.content.split(Regex("(?<=\\s)|(?<=\\n)"))
      }

      var displayedWordsCount by remember(message.id) {
        mutableIntStateOf(if (isNewMessage) 0 else words.size)
      }
      var isTypingComplete by remember(message.id) {
        mutableStateOf(!isNewMessage)
      }

      LaunchedEffect(message.id, isNewMessage) {
        if (isNewMessage && !isTypingComplete) {
          for (i in 1..words.size) {
            displayedWordsCount = i
            delay(22L) // Fast, realistic word streaming
          }
          isTypingComplete = true
          onTypingComplete()
        }
      }

      val displayedText = if (isTypingComplete) {
        message.content
      } else {
        words.take(displayedWordsCount).joinToString("")
      }

      Card(
        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
        colors = CardDefaults.cardColors(containerColor = AtharCreamCardElevated),
        border = androidx.compose.foundation.BorderStroke(0.6.dp, AtharCreamDivider),
        modifier = Modifier
          .fillMaxWidth(0.92f)
          .clickable {
            // Tap to reveal immediately if streaming
            if (!isTypingComplete) {
              isTypingComplete = true
              displayedWordsCount = words.size
              onTypingComplete()
            }
          }
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          // Assistant badge
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = AtharGold,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "أثر الذكي",
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 11.5.sp,
              color = AtharGold
            )
          }

          // Message Text with typing cursor during streaming
          Text(
            text = if (isTypingComplete) displayedText else "$displayedText ▌",
            fontFamily = CairoFontFamily,
            fontSize = 13.5.sp,
            lineHeight = 23.sp,
            color = AtharDeepBlack,
            textAlign = TextAlign.Justify
          )

          // Action buttons: Copy & Share (fades in once typing completes)
          AnimatedVisibility(
            visible = isTypingComplete,
            enter = fadeIn(tween(250))
          ) {
            Column {
              Spacer(modifier = Modifier.height(10.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
              ) {
                IconButton(
                  onClick = onCopy,
                  modifier = Modifier.size(30.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "نسخ الإجابة",
                    tint = AtharMilkyCreamSubtle,
                    modifier = Modifier.size(16.dp)
                  )
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                  onClick = onShare,
                  modifier = Modifier.size(30.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "مشاركة",
                    tint = AtharMilkyCreamSubtle,
                    modifier = Modifier.size(16.dp)
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

/**
 * Generating State Indicator
 */
@Composable
private fun GeneratingBubbleIndicator() {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = AtharCreamCardElevated),
    border = androidx.compose.foundation.BorderStroke(0.6.dp, AtharCreamDivider),
    modifier = Modifier.padding(vertical = 4.dp)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      CircularProgressIndicator(
        color = AtharGold,
        strokeWidth = 2.dp,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = "جاري استحضار الإجابة الإيمانية...",
        fontFamily = CairoFontFamily,
        fontSize = 12.sp,
        color = AtharTextMuted
      )
    }
  }
}

/**
 * Bottom Input Bar:
 * Flat, elegant, transparent input field with Olive Green & Gold send button
 */
@Composable
private fun AiChatBottomInputArea(
  text: String,
  onTextChanged: (String) -> Unit,
  isGenerating: Boolean,
  onSend: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(AtharMilkyCreamBg)
      .navigationBarsPadding()
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = text,
        onValueChange = onTextChanged,
        placeholder = {
          Text(
            text = "اكتب استفسارك الإيماني...",
            fontFamily = CairoFontFamily,
            fontSize = 12.5.sp,
            color = AtharMilkyCreamSubtle.copy(alpha = 0.75f)
          )
        },
        maxLines = 4,
        enabled = !isGenerating,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = AtharCreamInputBg,
          unfocusedContainerColor = AtharCreamInputBg,
          focusedBorderColor = AtharOliveLight,
          unfocusedBorderColor = AtharCreamDivider,
          focusedTextColor = AtharDeepBlack,
          unfocusedTextColor = AtharDeepBlack
        ),
        shape = RoundedCornerShape(24.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(onSend = { onSend() }),
        modifier = Modifier
          .weight(1f)
          .testTag("ai_chat_input_field")
      )

      Spacer(modifier = Modifier.width(8.dp))

      // Send Button (Olive Green & Gold)
      val hasText = text.isNotBlank() && !isGenerating
      IconButton(
        onClick = onSend,
        enabled = hasText,
        modifier = Modifier
          .size(46.dp)
          .clip(CircleShape)
          .background(if (hasText) AtharOlivePrimary else AtharOliveContainer.copy(alpha = 0.5f))
          .testTag("ai_chat_send_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Send,
          contentDescription = "إرسال",
          tint = if (hasText) AtharGoldLight else AtharGold.copy(alpha = 0.45f),
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

/**
 * Bottom Sheet displaying saved conversations synchronized with Firebase Firestore
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiChatHistoryBottomSheet(
  sessions: List<ChatSession>,
  activeSessionId: String?,
  onSelectSession: (ChatSession) -> Unit,
  onNewChat: () -> Unit,
  onDeleteSession: (ChatSession) -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val dateFormat = remember { SimpleDateFormat("d MMMM • h:mm a", Locale.forLanguageTag("ar")) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = AtharCreamCardElevated,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 18.dp, vertical = 14.dp)
        .navigationBarsPadding()
        .testTag("ai_chat_history_sheet")
    ) {

      // Header row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Forum,
            contentDescription = null,
            tint = AtharGold,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "المحادثات السابقة",
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = AtharDeepBlack
          )
        }

        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "إغلاق",
            tint = AtharMilkyCreamSubtle,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // [ + محادثة جديدة ] Button
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = AtharOliveContainer,
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onNewChat() }
          .testTag("ai_new_chat_button")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = AtharOlivePrimary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "بدء محادثة إيمانية جديدة",
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = AtharOlivePrimary
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
      HorizontalDivider(color = AtharCreamDivider, thickness = 0.7.dp)
      Spacer(modifier = Modifier.height(8.dp))

      // Saved Sessions List
      if (sessions.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "لا توجد محادثات سابقة محفوظة",
            fontFamily = CairoFontFamily,
            fontSize = 13.sp,
            color = AtharTextMuted
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(sessions, key = { it.id }) { session ->
            val isCurrent = session.id == activeSessionId
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isCurrent) AtharCreamInputBg else AtharCreamCardElevated,
              border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.dp, AtharOliveLight) else androidx.compose.foundation.BorderStroke(0.6.dp, AtharCreamDivider),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectSession(session) }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = session.title,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = AtharDeepBlack,
                    maxLines = 1
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = dateFormat.format(Date(session.updatedAt)),
                    fontFamily = CairoFontFamily,
                    fontSize = 10.5.sp,
                    color = AtharMilkyCreamSubtle
                  )
                }

                IconButton(
                  onClick = { onDeleteSession(session) },
                  modifier = Modifier.size(28.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "حذف المحادثة",
                    tint = AtharTextMuted,
                    modifier = Modifier.size(17.dp)
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
