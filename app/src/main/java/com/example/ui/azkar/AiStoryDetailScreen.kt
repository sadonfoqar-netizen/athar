package com.example.ui.azkar

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.azkar.AiDailyStory
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.AtharDarkCard
import com.example.ui.theme.AtharDarkCardElevated
import com.example.ui.theme.AtharDividerDark
import com.example.ui.theme.AtharGold
import com.example.ui.theme.AtharGoldLight
import com.example.ui.theme.AtharMilkyCream
import com.example.ui.theme.AtharMilkyCreamMuted
import com.example.ui.theme.AtharMilkyCreamSubtle
import com.example.ui.theme.AtharOliveContainer
import com.example.ui.theme.AtharOliveLight
import com.example.ui.theme.AtharOlivePrimary
import com.example.ui.theme.AtharPitchBlack
import com.example.ui.theme.CairoFontFamily

@Composable
fun AiStoryDetailScreen(
  story: AiDailyStory,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  BackHandler {
    onBack()
  }

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(AtharPitchBlack)
        .statusBarsPadding()
        .testTag("ai_story_detail_screen")
    ) {
      Column(modifier = Modifier.fillMaxSize()) {

        // Top Navigation Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = onBack,
              modifier = Modifier.testTag("story_back_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "رجوع",
                tint = AtharMilkyCream,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
              Text(
                text = "مجالس العبرة",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AtharMilkyCream
              )
              Text(
                text = story.category,
                fontFamily = CairoFontFamily,
                fontSize = 11.5.sp,
                color = AtharOliveLight
              )
            }
          }

          // Action Icons (Copy & Share)
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(
                  "قصة وعبرة",
                  "${story.title}\n\n${story.content}\n\nالعبرة المستفادة:\n${story.lesson}\n\nتطبيق أثر"
                )
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "تم نسخ القصة والعبرة", Toast.LENGTH_SHORT).show()
              }
            ) {
              Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "نسخ القصة",
                tint = AtharMilkyCreamSubtle,
                modifier = Modifier.size(20.dp)
              )
            }

            IconButton(
              onClick = {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                  type = "text/plain"
                  putExtra(
                    Intent.EXTRA_TEXT,
                    "*${story.title}*\n\n${story.content}\n\n*العبرة والدرس المستفاد:*\n${story.lesson}\n\nمن مجالس العبرة في تطبيق أثر"
                  )
                }
                context.startActivity(Intent.createChooser(sendIntent, "مشاركة القصة"))
              }
            ) {
              Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "مشاركة",
                tint = AtharOliveLight,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }

        HorizontalDivider(color = AtharDividerDark, thickness = 0.8.dp)

        // Story Narrative Scroll Content
        Column(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {

          // Category Badge
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(AtharOliveContainer)
              .padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = AtharGold,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = story.category,
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.5.sp,
                color = AtharGoldLight
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Title in bold Amiri Gold script
          Text(
            text = story.title,
            fontFamily = AmiriFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 38.sp,
            color = AtharGold
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "وقت القراءة المقدر: دقيقتان • موثقة ومحققة",
            fontFamily = CairoFontFamily,
            fontSize = 11.5.sp,
            color = AtharMilkyCreamSubtle
          )

          Spacer(modifier = Modifier.height(20.dp))

          // Full Story Narrative Body
          val paragraphs = story.content.split("\n\n")
          paragraphs.forEach { paragraph ->
            if (paragraph.isNotBlank()) {
              Text(
                text = paragraph.trim(),
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 28.sp,
                color = AtharMilkyCream,
                textAlign = TextAlign.Justify,
                modifier = Modifier.fillMaxWidth()
              )
              Spacer(modifier = Modifier.height(14.dp))
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Bottom Highlighted Golden/Olive Box: "العبرة والدروس المستفادة"
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, AtharGold.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = AtharOliveContainer.copy(alpha = 0.5f))
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Lightbulb,
                  contentDescription = null,
                  tint = AtharGold,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "العبرة والدروس المستفادة",
                  fontFamily = CairoFontFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = AtharGoldLight
                )
              }

              Spacer(modifier = Modifier.height(10.dp))

              Text(
                text = story.lesson,
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                lineHeight = 25.sp,
                color = AtharMilkyCream
              )
            }
          }

          Spacer(modifier = Modifier.height(36.dp))
        }
      }
    }
  }
}
