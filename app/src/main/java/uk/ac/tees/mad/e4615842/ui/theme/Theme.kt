package uk.ac.tees.mad.e4615842.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── TruthLens Design Tokens ───────────────────────────────────────────────────
val TealPrimary       = Color(0xFF00D4AA)
val TealVariant       = Color(0xFF00A884)
val DeepBackground    = Color(0xFF0D1117)
val SurfaceDark       = Color(0xFF161B22)
val SurfaceElevated   = Color(0xFF1E2530)
val CardSurface       = Color(0xFF1A2332)
val BorderSubtle      = Color(0xFF2A3548)
val TextPrimary       = Color(0xFFEAEEF4)
val TextSecondary     = Color(0xFF8896A7)
val TextMuted         = Color(0xFF4A5568)
val AiRed             = Color(0xFFFF4757)
val AiRedBg           = Color(0xFF1F0A0D)
val AiRedBorder       = Color(0xFF4A1520)
val RealGreen         = Color(0xFF2ED573)
val RealGreenBg       = Color(0xFF061510)
val RealGreenBorder   = Color(0xFF0D3320)
val WarningAmber      = Color(0xFFFFB347)
val WarningBg         = Color(0xFF1A1200)

private val DarkColorScheme = darkColorScheme(
    primary          = TealPrimary,
    onPrimary        = Color(0xFF001A14),
    primaryContainer = Color(0xFF003D2E),
    secondary        = Color(0xFF4A9EFF),
    background       = DeepBackground,
    surface          = SurfaceDark,
    surfaceVariant   = SurfaceElevated,
    onBackground     = TextPrimary,
    onSurface        = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline          = BorderSubtle,
    error            = AiRed,
    onError          = Color.White
)

@Composable
fun TruthLensTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = Typography(),
        content     = content
    )
}