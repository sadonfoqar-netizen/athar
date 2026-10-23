package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Warm Milky Cream Palette (Soft, calming, warm background identity)
val AtharMilkyCreamBg = Color(0xFFF9F6EE)
val AtharCreamSurface = Color(0xFFFAF7F0)
val AtharCreamCard = Color(0xFFFFFFFF)
val AtharCreamCardElevated = Color(0xFFFFFFFF)
val AtharCreamInputBg = Color(0xFFF1ECE2)
val AtharCreamDivider = Color(0xFFE5DDD0)

// Deep Pitch Black Typography (Highest legibility, contrast, and clarity)
val AtharDeepBlack = Color(0xFF121212)
val AtharTextPrimary = Color(0xFF121212)
val AtharTextSecondary = Color(0xFF333333)
val AtharTextMuted = Color(0xFF5C5549)
val AtharTextSubtle = Color(0xFF7A7367)

// Spiritual Accents: Elegant Olive Green & Radiant Gold
val AtharOlivePrimary = Color(0xFF2E6338)
val AtharOliveLight = Color(0xFF3A7D47)
val AtharOliveDark = Color(0xFF1E4526)
val AtharOliveContainer = Color(0xFFE2EFE4)
val AtharOnOliveContainer = Color(0xFF1B4021)

val AtharGold = Color(0xFFB8860B)
val AtharGoldLight = Color(0xFFD4AF37)
val AtharGoldDark = Color(0xFF8C6507)

// Backward-compatible bindings ensuring all existing screens instantly reflect the milky cream identity:
// Screen backgrounds:
val AtharPitchBlack = AtharMilkyCreamBg
val AtharDarkSurface = AtharCreamSurface
val AtharDarkCard = AtharCreamCard
val AtharDarkCardElevated = AtharCreamCardElevated
val AtharDarkInputBg = AtharCreamInputBg
val AtharDividerDark = AtharCreamDivider
val AtharDividerSubtle = AtharCreamDivider

// Screen typography (Text & Headings become deep pitch black):
val AtharMilkyCream = AtharDeepBlack
val AtharMilkyCreamVariant = AtharTextSecondary
val AtharMilkyCreamMuted = AtharTextMuted
val AtharMilkyCreamSubtle = AtharTextSubtle
