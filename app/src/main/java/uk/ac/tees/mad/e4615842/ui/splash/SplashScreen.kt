package uk.ac.tees.mad.e4615842.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import kotlinx.coroutines.delay
import uk.ac.tees.mad.e4615842.ui.theme.*

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    val alpha  = remember { Animatable(0f) }
    val scale  = remember { Animatable(0.6f) }
    val ring1  = remember { Animatable(0f) }
    val ring2  = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        ring1.animateTo(1f, animationSpec = tween(1000, easing = FastOutSlowInEasing))
        ring2.animateTo(1f, animationSpec = tween(800, easing = FastOutSlowInEasing))
        scale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        alpha.animateTo(1f, animationSpec = tween(400))
        delay(1000)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground),
        contentAlignment = Alignment.Center
    ) {
        // Animated ring background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            drawCircle(
                color = TealPrimary.copy(alpha = 0.04f * ring1.value),
                radius = 320.dp.toPx() * ring1.value,
                center = Offset(cx, cy),
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = TealPrimary.copy(alpha = 0.07f * ring2.value),
                radius = 200.dp.toPx() * ring2.value,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(TealPrimary.copy(alpha = 0.08f), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = 240.dp.toPx()
                ),
                radius = 240.dp.toPx(),
                center = Offset(cx, cy)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(
                        Brush.radialGradient(listOf(TealPrimary.copy(0.2f), Color.Transparent)),
                        androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("🔍", fontSize = 44.sp)
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "TruthLens",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                letterSpacing = (-1).sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "AI • DETECTION • VERIFIED",
                fontSize = 11.sp,
                color = TealPrimary,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Bottom tag
        Text(
            "Powered by Hive AI",
            fontSize = 11.sp,
            color = TextMuted,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .alpha(alpha.value)
        )
    }
}