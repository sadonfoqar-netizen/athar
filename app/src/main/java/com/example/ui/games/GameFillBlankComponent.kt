package com.example.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AtharDeepBlack
import com.example.ui.theme.AtharGold
import com.example.ui.theme.AtharOliveContainer
import com.example.ui.theme.AtharOlivePrimary
import com.example.ui.theme.AtharTextMuted
import com.example.ui.theme.CairoFontFamily

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameFillBlankComponent(
  currentText: String,
  onTextChange: (String) -> Unit,
  suggestedOptions: List<String>,
  isEnabled: Boolean,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {

    // Text Input Field
    OutlinedTextField(
      value = currentText,
      onValueChange = onTextChange,
      enabled = isEnabled,
      placeholder = {
        Text(
          text = "اكتب الكلمة المفقودة هنا أو اختر من المقترحات...",
          fontFamily = CairoFontFamily,
          fontSize = 13.5.sp,
          color = AtharTextMuted
        )
      },
      shape = RoundedCornerShape(16.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AtharOlivePrimary,
        unfocusedBorderColor = AtharGold.copy(alpha = 0.5f),
        focusedTextColor = AtharDeepBlack,
        unfocusedTextColor = AtharDeepBlack,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White
      ),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("fill_blank_input_field")
    )

    if (suggestedOptions.isNotEmpty() && isEnabled) {
      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "أو اختر الكلمة بنقرة واحدة:",
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        color = AtharOlivePrimary
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Word suggestion chips
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        suggestedOptions.forEach { word ->
          val isSelected = currentText.trim() == word.trim()
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(if (isSelected) AtharOlivePrimary else AtharOliveContainer)
              .border(
                1.dp,
                if (isSelected) AtharOlivePrimary else AtharGold.copy(alpha = 0.4f),
                RoundedCornerShape(12.dp)
              )
              .clickable { onTextChange(word) }
              .padding(horizontal = 14.dp, vertical = 7.dp)
              .testTag("suggestion_chip_$word")
          ) {
            Text(
              text = word,
              fontFamily = CairoFontFamily,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              fontSize = 13.sp,
              color = if (isSelected) Color.White else AtharDeepBlack
            )
          }
        }
      }
    }
  }
}
