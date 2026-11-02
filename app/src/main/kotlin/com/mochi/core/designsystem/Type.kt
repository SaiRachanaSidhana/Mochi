package com.mochi.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.mochi.app.R

private val googleFontProvider =
    GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.mochi_gms_font_certs,
    )

private val fredoka = GoogleFont("Fredoka")

val FredokaFamily =
    FontFamily(
        Font(fredoka, googleFontProvider, FontWeight.Normal),
        Font(fredoka, googleFontProvider, FontWeight.Medium),
        Font(fredoka, googleFontProvider, FontWeight.SemiBold),
        Font(fredoka, googleFontProvider, FontWeight.Bold),
    )

private val base = Typography()

val MochiTypography =
    base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = FredokaFamily, fontWeight = FontWeight.SemiBold),
        displayMedium = base.displayMedium.copy(fontFamily = FredokaFamily, fontWeight = FontWeight.SemiBold),
        displaySmall = base.displaySmall.copy(fontFamily = FredokaFamily, fontWeight = FontWeight.SemiBold),
        headlineLarge = base.headlineLarge.copy(fontFamily = FredokaFamily, fontWeight = FontWeight.SemiBold),
        headlineMedium = base.headlineMedium.copy(fontFamily = FredokaFamily, fontWeight = FontWeight.SemiBold),
        headlineSmall = base.headlineSmall.copy(fontFamily = FredokaFamily, fontWeight = FontWeight.SemiBold),
        titleLarge = base.titleLarge.copy(fontFamily = FredokaFamily, fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.copy(fontFamily = FredokaFamily, fontWeight = FontWeight.Medium),
        labelLarge = base.labelLarge.copy(fontFamily = FredokaFamily, fontWeight = FontWeight.Medium),
    )
