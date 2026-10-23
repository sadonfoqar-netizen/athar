package com.example.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AtharDarkCard
import com.example.ui.theme.AtharDarkInputBg
import com.example.ui.theme.AtharDividerDark
import com.example.ui.theme.AtharGold
import com.example.ui.theme.AtharMilkyCream
import com.example.ui.theme.AtharMilkyCreamMuted
import com.example.ui.theme.AtharOlivePrimary
import com.example.ui.theme.CairoFontFamily

@Composable
fun ForgotPasswordDialog(
  onDismiss: () -> Unit,
  onSendReset: (String) -> Unit
) {
  var email by remember { mutableStateOf("") }

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    AlertDialog(
      onDismissRequest = onDismiss,
      containerColor = AtharDarkCard,
      shape = RoundedCornerShape(20.dp),
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.LockReset,
            contentDescription = null,
            tint = AtharGold,
            modifier = Modifier.size(26.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "استعادة كلمة المرور",
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = AtharMilkyCream
          )
        }
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "أدخل بريدك الإلكتروني المسجل وسنرسل لك رابطاً لإعادة تعيين كلمة المرور بكل سهولة وأمان.",
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 13.5.sp,
            color = AtharMilkyCreamMuted,
            lineHeight = 22.sp
          )
          Spacer(modifier = Modifier.height(16.dp))
          OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("reset_email_input"),
            shape = RoundedCornerShape(12.dp),
            placeholder = {
              Text(
                text = "أدخل بريدك الإلكتروني",
                fontFamily = CairoFontFamily,
                fontSize = 13.sp,
                color = AtharMilkyCreamMuted.copy(alpha = 0.6f)
              )
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                tint = AtharGold
              )
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = AtharMilkyCream,
              unfocusedTextColor = AtharMilkyCream,
              focusedBorderColor = AtharGold,
              unfocusedBorderColor = AtharDividerDark,
              focusedContainerColor = AtharDarkInputBg,
              unfocusedContainerColor = AtharDarkInputBg
            )
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (email.isNotBlank()) {
              onSendReset(email)
            }
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = AtharOlivePrimary,
            contentColor = AtharMilkyCream
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("send_reset_button")
        ) {
          Text(
            text = "إرسال الرابط",
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.5.sp
          )
        }
      },
      dismissButton = {
        TextButton(onClick = onDismiss) {
          Text(
            text = "إلغاء",
            fontFamily = CairoFontFamily,
            color = AtharMilkyCreamMuted,
            fontSize = 13.5.sp
          )
        }
      }
    )
  }
}
