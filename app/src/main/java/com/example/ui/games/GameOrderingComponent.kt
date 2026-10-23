package com.example.ui.games

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AtharDeepBlack
import com.example.ui.theme.AtharGold
import com.example.ui.theme.AtharMilkyCreamBg
import com.example.ui.theme.AtharOliveContainer
import com.example.ui.theme.AtharOlivePrimary
import com.example.ui.theme.CairoFontFamily

@Composable
fun GameOrderingComponent(
  items: List<String>,
  onMoveUp: (Int) -> Unit,
  onMoveDown: (Int) -> Unit,
  isAnswerSubmitted: Boolean,
  correctOrder: List<String>,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    items.forEachIndexed { index, item ->
      val isItemInCorrectPos = if (isAnswerSubmitted && index < correctOrder.size) {
        item == correctOrder[index]
      } else null

      val borderColor = when (isItemInCorrectPos) {
        true -> AtharOlivePrimary
        false -> Color(0xFFC0392B)
        null -> AtharGold.copy(alpha = 0.45f)
      }

      val bgColor = when (isItemInCorrectPos) {
        true -> AtharOliveContainer.copy(alpha = 0.6f)
        false -> Color(0xFFFDEDEC)
        null -> Color.White
      }

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(bgColor)
          .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
          .padding(horizontal = 14.dp, vertical = 10.dp)
          .testTag("ordering_item_$index")
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {

          // Item Rank and Text
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isItemInCorrectPos == true) AtharOlivePrimary else AtharGold),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "${index + 1}",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
              text = item,
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Medium,
              fontSize = 14.5.sp,
              color = AtharDeepBlack
            )
          }

          // Move Up & Down Controls (Disabled after submit)
          if (!isAnswerSubmitted) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(
                onClick = { onMoveUp(index) },
                enabled = index > 0,
                modifier = Modifier.size(36.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.KeyboardArrowUp,
                  contentDescription = "تحريك للأعلى",
                  tint = if (index > 0) AtharOlivePrimary else Color.LightGray
                )
              }

              IconButton(
                onClick = { onMoveDown(index) },
                enabled = index < items.size - 1,
                modifier = Modifier.size(36.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.KeyboardArrowDown,
                  contentDescription = "تحريك للأسفل",
                  tint = if (index < items.size - 1) AtharOlivePrimary else Color.LightGray
                )
              }
            }
          }
        }
      }
    }
  }
}
