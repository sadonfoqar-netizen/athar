package com.example.ui.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AtharDarkCardElevated
import com.example.ui.theme.AtharDarkInputBg
import com.example.ui.theme.AtharDividerDark
import com.example.ui.theme.AtharGold
import com.example.ui.theme.AtharMilkyCream
import com.example.ui.theme.AtharMilkyCreamSubtle
import com.example.ui.theme.AtharOlivePrimary
import com.example.ui.theme.CairoFontFamily

@Composable
fun EditProfileDialog(
  currentProfileData: UserProfileData,
  onSave: (UserProfileData) -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var displayName by remember { mutableStateOf(currentProfileData.displayName) }
  var username by remember { mutableStateOf(currentProfileData.username) }
  var bio by remember { mutableStateOf(currentProfileData.bio) }

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Dialog(onDismissRequest = onDismiss) {
      Surface(
        shape = RoundedCornerShape(22.dp),
        color = AtharDarkCardElevated,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("edit_profile_dialog")
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "تعديل الملف الشخصي",
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = AtharMilkyCream
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Display Name
          Text(
            text = "الاسم الكامل المعروض:",
            fontFamily = CairoFontFamily,
            fontSize = 12.5.sp,
            color = AtharGold,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = editColors()
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Username
          Text(
            text = "اسم المستخدم (Username):",
            fontFamily = CairoFontFamily,
            fontSize = 12.5.sp,
            color = AtharGold,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = editColors()
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Bio
          Text(
            text = "الوصف الشخصي (Bio):",
            fontFamily = CairoFontFamily,
            fontSize = 12.5.sp,
            color = AtharGold,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = editColors()
          )

          Spacer(modifier = Modifier.height(20.dp))

          // Actions
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = {
                if (displayName.isBlank()) {
                  Toast.makeText(context, "يرجى كتابة الاسم", Toast.LENGTH_SHORT).show()
                  return@Button
                }
                val cleanUser = if (username.startsWith("@")) username else "@$username"
                onSave(
                  currentProfileData.copy(
                    displayName = displayName.trim(),
                    username = cleanUser.trim(),
                    bio = bio.trim()
                  )
                )
                Toast.makeText(context, "تم حفظ التعديلات بنجاح", Toast.LENGTH_SHORT).show()
                onDismiss()
              },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = AtharOlivePrimary),
              modifier = Modifier.weight(1f)
            ) {
              Text(text = "حفظ التعديلات", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = AtharMilkyCream)
            }

            Button(
              onClick = onDismiss,
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = AtharDividerDark),
              modifier = Modifier.weight(0.7f)
            ) {
              Text(text = "إلغاء", fontFamily = CairoFontFamily, color = AtharMilkyCreamSubtle)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun editColors() = OutlinedTextFieldDefaults.colors(
  focusedTextColor = AtharMilkyCream,
  unfocusedTextColor = AtharMilkyCream,
  focusedBorderColor = AtharGold,
  unfocusedBorderColor = AtharDividerDark,
  focusedContainerColor = AtharDarkInputBg,
  unfocusedContainerColor = AtharDarkInputBg
)
