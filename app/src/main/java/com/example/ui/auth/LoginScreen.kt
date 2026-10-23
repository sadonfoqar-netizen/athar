package com.example.ui.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AtharDarkCard
import com.example.ui.theme.AtharDarkInputBg
import com.example.ui.theme.AtharDividerDark
import com.example.ui.theme.AtharGold
import com.example.ui.theme.AtharGoldLight
import com.example.ui.theme.AtharMilkyCream
import com.example.ui.theme.AtharMilkyCreamMuted
import com.example.ui.theme.AtharMilkyCreamSubtle
import com.example.ui.theme.AtharOliveLight
import com.example.ui.theme.AtharOlivePrimary
import com.example.ui.theme.AtharPitchBlack
import com.example.ui.theme.CairoFontFamily

@Composable
fun LoginScreen(
  viewModel: AuthViewModel,
  onNavigateToSignUp: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var showForgotDialog by remember { mutableStateOf(false) }

  if (showForgotDialog) {
    ForgotPasswordDialog(
      onDismiss = { showForgotDialog = false },
      onSendReset = { resetEmail ->
        viewModel.sendPasswordReset(resetEmail) { success, msg ->
          Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
          showForgotDialog = false
        }
      }
    )
  }

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(AtharPitchBlack)
        .statusBarsPadding()
        .navigationBarsPadding()
        .testTag("login_screen"),
      contentAlignment = Alignment.Center
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 22.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {

        // Main Dark Container Card
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp)),
          shape = RoundedCornerShape(26.dp),
          colors = CardDefaults.cardColors(containerColor = AtharDarkCard),
          border = null
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {

            // Top Emblem (No outer circular frames or borders)
            Image(
              painter = painterResource(id = R.drawable.img_app_logo),
              contentDescription = stringResource(id = R.string.app_name),
              modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp)),
              contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Welcome Title (Milky Cream color)
            Text(
              text = "مرحباً بك مجدداً",
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 24.sp,
              color = AtharMilkyCream,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle Description (Olive / Gold accent)
            Text(
              text = "سجّل دخولك لتستمر في جمع الحسنات والأثر الطيب",
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Normal,
              fontSize = 13.sp,
              color = AtharOliveLight,
              textAlign = TextAlign.Center,
              lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(26.dp))

            // Email Input
            Text(
              text = "البريد الإلكتروني",
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.sp,
              color = AtharMilkyCream,
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
            )

            OutlinedTextField(
              value = email,
              onValueChange = { email = it },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("email_input"),
              shape = RoundedCornerShape(14.dp),
              placeholder = {
                Text(
                  text = "أدخل بريدك الإلكتروني",
                  fontFamily = CairoFontFamily,
                  fontSize = 13.sp,
                  color = AtharMilkyCreamSubtle
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
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = AtharMilkyCream,
                unfocusedTextColor = AtharMilkyCream,
                focusedBorderColor = AtharGold,
                unfocusedBorderColor = AtharDividerDark,
                focusedContainerColor = AtharDarkInputBg,
                unfocusedContainerColor = AtharDarkInputBg
              )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Input
            Text(
              text = "كلمة المرور",
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.sp,
              color = AtharMilkyCream,
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
            )

            OutlinedTextField(
              value = password,
              onValueChange = { password = it },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("password_input"),
              shape = RoundedCornerShape(14.dp),
              placeholder = {
                Text(
                  text = "أدخل كلمة المرور",
                  fontFamily = CairoFontFamily,
                  fontSize = 13.sp,
                  color = AtharMilkyCreamSubtle
                )
              },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.Lock,
                  contentDescription = null,
                  tint = AtharGold
                )
              },
              trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                  Icon(
                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = AtharGold
                  )
                }
              },
              visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = AtharMilkyCream,
                unfocusedTextColor = AtharMilkyCream,
                focusedBorderColor = AtharGold,
                unfocusedBorderColor = AtharDividerDark,
                focusedContainerColor = AtharDarkInputBg,
                unfocusedContainerColor = AtharDarkInputBg
              )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Forgot Password Link
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.Start
            ) {
              Text(
                text = "نسيت كلمة المرور؟",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.5.sp,
                color = AtharOliveLight,
                modifier = Modifier
                  .clickable { showForgotDialog = true }
                  .padding(vertical = 4.dp)
                  .testTag("forgot_password_link")
              )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Login Button (Clean, no outer circles/frames)
            Button(
              onClick = {
                if (email.isBlank() || password.isBlank()) {
                  Toast.makeText(context, "يرجى إدخال البريد الإلكتروني وكلمة المرور أو المتابعة باستخدام Google", Toast.LENGTH_SHORT).show()
                } else {
                  val webClientId = context.getString(R.string.default_web_client_id)
                  viewModel.signInWithGoogle(context, webClientId)
                }
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("login_button"),
              colors = ButtonDefaults.buttonColors(
                containerColor = AtharOlivePrimary,
                contentColor = AtharMilkyCream
              ),
              shape = RoundedCornerShape(12.dp)
            ) {
              Text(
                text = "تسجيل الدخول",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Subtle divider
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = AtharDividerDark,
                thickness = 1.dp
              )
              Text(
                text = " أو عبر ",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.5.sp,
                color = AtharMilkyCreamSubtle,
                modifier = Modifier.padding(horizontal = 10.dp)
              )
              HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = AtharDividerDark,
                thickness = 1.dp
              )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Free-floating Minimal Flat Icons (NO backgrounds, NO circles, NO boxes)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.spacedBy(36.dp, Alignment.CenterHorizontally),
              verticalAlignment = Alignment.CenterVertically
            ) {

              // 1. Apple Icon (Floating freely, transparent, no circle)
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clickable {
                    Toast.makeText(context, "تسجيل Apple مخصص لأجهزة iOS، يُمكنك المتابعة عبر Google أو كضيف", Toast.LENGTH_LONG).show()
                  }
                  .testTag("apple_signin_button"),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  painter = painterResource(id = R.drawable.ic_apple_logo),
                  contentDescription = "المتابعة باستخدام Apple",
                  tint = AtharMilkyCream,
                  modifier = Modifier.size(28.dp)
                )
              }

              // 2. Google Icon (Floating freely, transparent, no circle)
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clickable {
                    val webClientId = context.getString(R.string.default_web_client_id)
                    viewModel.signInWithGoogle(context, webClientId)
                  }
                  .testTag("google_signin_button"),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  painter = painterResource(id = R.drawable.ic_google_logo),
                  contentDescription = "المتابعة باستخدام Google",
                  tint = Color.Unspecified,
                  modifier = Modifier.size(28.dp)
                )
              }

              // 3. Guest Icon (Floating freely, transparent, no circle)
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clickable {
                    viewModel.continueAsGuest()
                  }
                  .testTag("guest_signin_button"),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = "الدخول كضيف",
                  tint = AtharGold,
                  modifier = Modifier.size(30.dp)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Footer Link
        Row(
          modifier = Modifier.padding(bottom = 12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "ليس لديك حساب؟ ",
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 13.5.sp,
            color = AtharMilkyCreamMuted
          )
          Text(
            text = "إنشاء حساب جديد",
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.5.sp,
            color = AtharOliveLight,
            modifier = Modifier
              .clickable { onNavigateToSignUp() }
              .padding(4.dp)
              .testTag("navigate_to_signup_link")
          )
        }
      }
    }
  }
}
