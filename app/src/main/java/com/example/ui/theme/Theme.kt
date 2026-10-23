package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AtharColorScheme = lightColorScheme(
  primary = AtharOlivePrimary,
  onPrimary = Color.White,
  primaryContainer = AtharOliveContainer,
  onPrimaryContainer = AtharOnOliveContainer,
  secondary = AtharGold,
  onSecondary = Color.White,
  tertiary = AtharGoldDark,
  onTertiary = Color.White,
  background = AtharMilkyCreamBg,
  onBackground = AtharDeepBlack,
  surface = AtharCreamSurface,
  onSurface = AtharDeepBlack,
  surfaceVariant = AtharCreamCard,
  onSurfaceVariant = AtharTextMuted,
  outline = AtharCreamDivider,
  outlineVariant = AtharCreamDivider
)

@Composable
fun AtharTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = AtharColorScheme,
    typography = AtharTypography,
    content = content
  )
}
