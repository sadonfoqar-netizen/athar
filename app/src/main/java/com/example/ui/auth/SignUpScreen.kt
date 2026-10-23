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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import com.example.ui.theme.AtharMilkyCream
import com.example.ui.theme.AtharMilkyCreamMuted
import com.example.ui.theme.AtharMilkyCreamSubtle
import com.example.ui.theme.AtharOliveLight
import com.example.ui.theme.AtharOlivePrimary
import com.example.ui.theme.AtharPitchBlack
import com.example.ui.theme.CairoFontFamily

@Composable
fun SignUpScreen(
  viewModel: AuthViewModel,
  onNavigateToLogin: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var fullName by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var confirmPasswordVisible by remember { mutableStateOf(false) }
  var agreeToTerms by remember { mutableStateOf(false) }

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(AtharPitchBlack)
        .statusBarsPadding()
        .navigationBarsPadding()
        .testTag("signup_screen"),
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

            // Title
            Text(
              text = "إنشاء حساب جديد",
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 24.sp,
              color = AtharMilkyCream,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle
            Text(
              text = "انضم إلى أثر لتوثيق حسناتك وصدقاتك الجارية بإذن الله",
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Normal,
              fontSize = 13.sp,
              color = AtharOliveLight,
              textAlign = TextAlign.Center,
              lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Full Name Input
            Text(
              text = "الاسم الكامل",
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.sp,
              color = AtharMilkyCream,
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
            )

            OutlinedTextField(
              value = fullName,
              onValueChange = { fullName = it },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("full_name_input"),
              shape = RoundedCornerShape(14.dp),
              placeholder = {
                Text(
                  text = "أدخل اسمك الكريم",
                  fontFamily = CairoFontFamily,
                  fontSize = 13.sp,
                  color = AtharMilkyCreamSubtle
                )
              },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.Person,
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

            Spacer(modifier = Modifier.height(14.dp))

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
                .testTag("signup_email_input"),
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

            Spacer(modifier = Modifier.height(14.dp))

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
                .testTag("signup_password_input"),
              shape = RoundedCornerShape(14.dp),
              placeholder = {
                Text(
                  text = "أنشئ كلمة مرور قوية",
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

            Spacer(modifier = Modifier.height(14.dp))

            // Confirm Password Input
            Text(
              text = "تأكيد كلمة المرور",
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.sp,
              color = AtharMilkyCream,
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
            )

            OutlinedTextField(
              value = confirmPassword,
              onValueChange = { confirmPassword = it },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("signup_confirm_password_input"),
              shape = RoundedCornerShape(14.dp),
              placeholder = {
                Text(
                  text = "أعد إدخال كلمة المرور",
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
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                  Icon(
                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = AtharGold
                  )
                }
              },
              visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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

            Spacer(modifier = Modifier.height(14.dp))

            // Terms Checkbox
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { agreeToTerms = !agreeToTerms }
                .padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Checkbox(
                checked = agreeToTerms,
                onCheckedChange = { agreeToTerms = it },
                colors = CheckboxDefaults.colors(
                  checkedColor = AtharOlivePrimary,
                  checkmarkColor = AtharMilkyCream
                ),
                modifier = Modifier.testTag("terms_checkbox")
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "أوافق على شروط الاستخدام وسياسة الخصوصية",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.5.sp,
                color = AtharMilkyCream
              )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Create Account Button
            Button(
              onClick = {
                if (fullName.isBlank()) {
                  Toast.makeText(context, "يرجى إدخال الاسم الكريم", Toast.LENGTH_SHORT).show()
                } else if (email.isBlank()) {
                  Toast.makeText(context, "يرجى إدخال البريد الإلكتروني", Toast.LENGTH_SHORT).show()
                } else if (password.length < 6) {
                  Toast.makeText(context, "يجب أن تتكون كلمة المرور من 6 أحرف على الأقل", Toast.LENGTH_SHORT).show()
                } else if (password != confirmPassword) {
                  Toast.makeText(context, "كلمتا المرور غير متطابقتين", Toast.LENGTH_SHORT).show()
                } else if (!agreeToTerms) {
                  Toast.makeText(context, "يرجى الموافقة على شروط الاستخدام أولاً", Toast.LENGTH_SHORT).show()
                } else {
                  val webClientId = context.getString(R.string.default_web_client_id)
                  viewModel.signInWithGoogle(context, webClientId)
                }
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("create_account_button"),
              colors = ButtonDefaults.buttonColors(
                containerColor = AtharOlivePrimary,
                contentColor = AtharMilkyCream
              ),
              shape = RoundedCornerShape(12.dp)
            ) {
              Text(
                text = "إنشاء الحساب",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Return to Login Link
        Row(
          modifier = Modifier.padding(bottom = 12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "لديك حساب بالفعل؟ ",
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 13.5.sp,
            color = AtharMilkyCreamMuted
          )
          Text(
            text = "تسجيل الدخول",
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.5.sp,
            color = AtharOliveLight,
            modifier = Modifier
              .clickable { onNavigateToLogin() }
              .padding(4.dp)
              .testTag("navigate_to_login_link")
          )
        }
      }
    }
  }
}
