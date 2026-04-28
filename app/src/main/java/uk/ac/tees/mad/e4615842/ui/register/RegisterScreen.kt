package uk.ac.tees.mad.e4615842.ui.register

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.ac.tees.mad.e4615842.ui.theme.*

@Composable
fun RegisterScreen(
    onRegisterClick: (String, String, (Boolean, String) -> Unit) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword    by remember { mutableStateOf(false) }
    var isLoading       by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf("") }

    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) { alpha.animateTo(1f, animationSpec = tween(700)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(listOf(TealPrimary.copy(0.08f), Color.Transparent))
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .alpha(alpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(72.dp))

            Text("🔍", fontSize = 52.sp)
            Spacer(Modifier.height(10.dp))
            Text("TruthLens", fontSize = 30.sp, fontWeight = FontWeight.Bold,
                color = TextPrimary, letterSpacing = (-0.5).sp)
            Spacer(Modifier.height(4.dp))
            Text("CREATE ACCOUNT", fontSize = 10.sp, color = TealPrimary,
                letterSpacing = 3.sp, fontWeight = FontWeight.Medium)

            Spacer(Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(CardSurface)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Text("Get Started", fontSize = 22.sp,
                        fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Sign up to start detecting AI images", fontSize = 13.sp,
                        color = TextSecondary, modifier = Modifier.padding(bottom = 24.dp, top = 2.dp))

                    TLTextField(value = email, onValueChange = { email = it; errorMessage = "" },
                        label = "Email address", icon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email)

                    Spacer(Modifier.height(12.dp))

                    TLTextField(value = password, onValueChange = { password = it; errorMessage = "" },
                        label = "Password", icon = Icons.Default.Lock,
                        keyboardType = KeyboardType.Password,
                        isPassword = true, showPassword = showPassword,
                        onTogglePassword = { showPassword = !showPassword })

                    Spacer(Modifier.height(12.dp))

                    TLTextField(value = confirmPassword,
                        onValueChange = { confirmPassword = it; errorMessage = "" },
                        label = "Confirm password", icon = Icons.Default.Lock,
                        keyboardType = KeyboardType.Password,
                        isPassword = true, showPassword = false)

                    // Password mismatch indicator
                    if (confirmPassword.isNotEmpty() && confirmPassword != password) {
                        Spacer(Modifier.height(6.dp))
                        Text("Passwords do not match", color = AiRed, fontSize = 12.sp)
                    }

                    if (errorMessage.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(AiRedBg)
                                .border(1.dp, AiRedBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚠", fontSize = 14.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(errorMessage, color = AiRed, fontSize = 13.sp)
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    TLPrimaryButton(
                        text = "Create Account",
                        isLoading = isLoading,
                        onClick = {
                            when {
                                email.isBlank() || password.isBlank() ->
                                    errorMessage = "Please fill in all fields."
                                password != confirmPassword ->
                                    errorMessage = "Passwords do not match."
                                password.length < 6 ->
                                    errorMessage = "Password must be at least 6 characters."
                                else -> {
                                    isLoading = true
                                    onRegisterClick(email.trim(), password) { success, msg ->
                                        isLoading = false
                                        if (!success) errorMessage = msg
                                    }
                                }
                            }
                        }
                    )

                    Spacer(Modifier.height(20.dp))
                    Divider(color = BorderSubtle)
                    Spacer(Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center) {
                        Text("Already have an account? ", fontSize = 14.sp, color = TextSecondary)
                        TextButton(onClick = onNavigateToLogin,
                            contentPadding = PaddingValues(0.dp)) {
                            Text("Sign In", fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold, color = TealPrimary)
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}