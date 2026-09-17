package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TaxiColorScheme =
  darkColorScheme(
    primary = TaxiYellow,
    onPrimary = Color.Black,
    primaryContainer = TaxiDarkCard,
    onPrimaryContainer = TaxiYellow,
    secondary = TaxiYellowDark,
    onSecondary = Color.Black,
    tertiary = TaxiYellowLight,
    onTertiary = Color.Black,
    background = TaxiBlackDark,
    onBackground = TaxiTextPrimary,
    surface = TaxiDarkSurface,
    onSurface = TaxiTextPrimary,
    surfaceVariant = TaxiDarkCard,
    onSurfaceVariant = TaxiTextSecondary,
    outline = TaxiBorder,
    error = TaxiErrorRed,
    onError = Color.White
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = TaxiColorScheme,
    typography = Typography,
    content = content
  )
}

