package uk.ac.tees.mad.e4615842.ui.login

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import uk.ac.tees.mad.e4615842.ui.theme.*

@Composable
fun LoginScreen(
    onLoginClick: (String, String, (Boolean, String) -> Unit) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading    by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) { alpha.animateTo(1f, animationSpec = tween(700)) }

    val biometricManager = BiometricManager.from(context)
    val canUseBiometric  = biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
    ) == BiometricManager.BIOMETRIC_SUCCESS

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
    ) {
        // Subtle top gradient glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(TealPrimary.copy(alpha = 0.08f), Color.Transparent)
                    )
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
            Spacer(Modifier.height(80.dp))

            // Logo
            Text("🔍", fontSize = 52.sp)
            Spacer(Modifier.height(10.dp))
            Text(
                "TruthLens",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "AI • DETECTION • VERIFIED",
                fontSize = 10.sp,
                color = TealPrimary,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(48.dp))

            // Form card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(CardSurface)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        "Sign In",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        "Welcome back",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 24.dp, top = 2.dp)
                    )

                    // Email
                    TLTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = "" },
                        label = "Email address",
                        icon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(Modifier.height(12.dp))

                    // Password
                    TLTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = "" },
                        label = "Password",
                        icon = Icons.Default.Lock,
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        showPassword = showPassword,
                        onTogglePassword = { showPassword = !showPassword }
                    )

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

                    // Sign in button
                    TLPrimaryButton(
                        text = "Sign In",
                        isLoading = isLoading,
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Please enter your email and password."
                                return@TLPrimaryButton
                            }
                            isLoading = true
                            onLoginClick(email.trim(), password) { success, msg ->
                                isLoading = false
                                if (!success) errorMessage = msg
                            }
                        }
                    )

                    // Biometric
                    if (canUseBiometric) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                val activity = context as? FragmentActivity ?: return@OutlinedButton
                                val executor = ContextCompat.getMainExecutor(context)
                                val prompt = BiometricPrompt(activity, executor,
                                    object : BiometricPrompt.AuthenticationCallback() {
                                        override fun onAuthenticationSucceeded(r: BiometricPrompt.AuthenticationResult) {
                                            errorMessage = "Biometric verified ✓ Enter credentials to complete sign-in."
                                        }
                                        override fun onAuthenticationError(code: Int, msg: CharSequence) {
                                            errorMessage = "Biometric error: $msg"
                                        }
                                        override fun onAuthenticationFailed() {
                                            errorMessage = "Biometric failed. Please try again."
                                        }
                                    })
                                prompt.authenticate(
                                    BiometricPrompt.PromptInfo.Builder()
                                        .setTitle("TruthLens Biometric Login")
                                        .setSubtitle("Verify your identity")
                                        .setAllowedAuthenticators(
                                            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
                                        ).build()
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                        ) {
                            Text("🔒  Use Biometric Login", fontSize = 14.sp)
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Divider(color = BorderSubtle)
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Don't have an account? ", fontSize = 14.sp, color = TextSecondary)
                        TextButton(onClick = onNavigateToRegister, contentPadding = PaddingValues(0.dp)) {
                            Text("Register", fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold, color = TealPrimary)
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}