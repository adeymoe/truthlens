package uk.ac.tees.mad.e4615842.ui.home

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import uk.ac.tees.mad.e4615842.BuildConfig
import uk.ac.tees.mad.e4615842.api.RetrofitInstance
import uk.ac.tees.mad.e4615842.data.ScanEntity
import uk.ac.tees.mad.e4615842.data.ScanRepository
import uk.ac.tees.mad.e4615842.model.ClassResult
import uk.ac.tees.mad.e4615842.model.HiveInput
import uk.ac.tees.mad.e4615842.model.HiveRequest
import uk.ac.tees.mad.e4615842.utils.uriToBase64
import uk.ac.tees.mad.e4615842.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class DetectionDetail(
    val label: String, val confidence: Int, val isAi: Boolean,
    val generator: String, val deepfakeRisk: String, val deepfakeScore: Int
)

sealed class DetectionResult {
    object Idle : DetectionResult()
    object Loading : DetectionResult()
    data class Success(val detail: DetectionDetail) : DetectionResult()
    data class Error(val message: String) : DetectionResult()
}

fun resolveGenerator(classes: List<ClassResult>): String {
    val map = mapOf("midjourney" to "Midjourney", "stablediffusion" to "Stable Diffusion",
        "dalle" to "DALL·E", "firefly" to "Adobe Firefly", "imagen" to "Google Imagen",
        "other_image_generators" to "Other Generator")
    val best = classes.filter { map.containsKey(it.className.lowercase()) }.maxByOrNull { it.score }
    return if (best != null && best.score > 0.05) map[best.className.lowercase()] ?: "Unknown" else "None detected"
}

fun resolveDeepfakeRisk(score: Double) = when {
    score >= 0.7 -> "High"; score >= 0.3 -> "Medium"; else -> "Low"
}

@Composable
fun HomeScreen(
    scanRepository: ScanRepository,
    onViewHistory: () -> Unit,
    onViewProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    var imageUri        by remember { mutableStateOf<Uri?>(null) }
    var detectionResult by remember { mutableStateOf<DetectionResult>(DetectionResult.Idle) }

    var photoFile by remember {
        mutableStateOf(File(context.cacheDir,
            "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"))
    }
    var cameraUri by remember {
        mutableStateOf(androidx.core.content.FileProvider.getUriForFile(
            context, "${context.packageName}.provider", photoFile))
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) { imageUri = cameraUri; detectionResult = DetectionResult.Idle }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { imageUri = uri; detectionResult = DetectionResult.Idle }
    }

    Scaffold(
        containerColor = DeepBackground,
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                tonalElevation = 0.dp,
                modifier = Modifier.border(
                    width = 1.dp,
                    color = BorderSubtle,
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
                )
            ) {
                NavigationBarItem(selected = true, onClick = {},
                    icon = { Text("🔍", fontSize = 20.sp) },
                    label = { Text("Detect", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TealPrimary,
                        selectedTextColor = TealPrimary,
                        indicatorColor = TealPrimary.copy(alpha = 0.12f)
                    ))
                NavigationBarItem(selected = false, onClick = onViewHistory,
                    icon = { Icon(Icons.Default.History, null) },
                    label = { Text("History", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = Color.Transparent
                    ))
                NavigationBarItem(selected = false, onClick = onViewProfile,
                    icon = { Icon(Icons.Default.AccountCircle, null) },
                    label = { Text("Profile", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = Color.Transparent
                    ))
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(pad)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Header ────────────────────────────────────────────
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("TruthLens", fontSize = 24.sp,
                        fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("AI IMAGE DETECTOR", fontSize = 9.sp,
                        color = TealPrimary, letterSpacing = 2.sp)
                }
                FilledTonalButton(
                    onClick = {
                        photoFile = File(context.cacheDir,
                            "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg")
                        cameraUri = androidx.core.content.FileProvider.getUriForFile(
                            context, "${context.packageName}.provider", photoFile)
                        cameraLauncher.launch(cameraUri)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = SurfaceElevated,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(Icons.Default.CameraAlt, null,
                        modifier = Modifier.size(16.dp), tint = TealPrimary)
                    Spacer(Modifier.width(6.dp))
                    Text("Camera", fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Image preview ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardSurface)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Selected image",
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    // overlay badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("IMAGE LOADED", fontSize = 9.sp,
                            color = TealPrimary, letterSpacing = 1.sp)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceElevated)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) { Text("🖼️", fontSize = 28.sp) }
                        Spacer(Modifier.height(12.dp))
                        Text("No image selected", fontSize = 15.sp,
                            fontWeight = FontWeight.Medium, color = TextSecondary)
                        Spacer(Modifier.height(4.dp))
                        Text("Select from gallery or capture with camera",
                            fontSize = 12.sp, color = TextMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Gallery + Analyse ─────────────────────────────────
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) {
                    Icon(Icons.Default.PhotoLibrary, null,
                        modifier = Modifier.size(16.dp), tint = TealPrimary)
                    Spacer(Modifier.width(6.dp))
                    Text("Gallery", fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        if (imageUri == null) {
                            detectionResult = DetectionResult.Error("Please select or capture an image first.")
                            return@Button
                        }
                        val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE)
                                as android.net.ConnectivityManager
                        val caps = cm.getNetworkCapabilities(cm.activeNetwork)
                        if (caps?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) != true) {
                            detectionResult = DetectionResult.Error("No internet connection. Please check your network.")
                            return@Button
                        }
                        scope.launch {
                            detectionResult = DetectionResult.Loading
                            try {
                                val base64 = uriToBase64(context, imageUri!!)
                                if (base64 == null) {
                                    detectionResult = DetectionResult.Error("Could not read image.")
                                    return@launch
                                }
                                val response = RetrofitInstance.api.detectImage(
                                    "Bearer ${BuildConfig.HIVE_API_KEY}",
                                    HiveRequest(listOf(HiveInput("data:image/jpeg;base64,$base64")))
                                )
                                if (response.isSuccessful) {
                                    val classes = response.body()?.output?.firstOrNull()?.classes
                                    if (classes.isNullOrEmpty()) {
                                        detectionResult = DetectionResult.Error("No results returned.")
                                        return@launch
                                    }
                                    val imgClasses = classes.filter {
                                        it.className == "ai_generated" || it.className == "not_ai_generated"
                                    }
                                    val best = imgClasses.maxByOrNull { it.score } ?: run {
                                        detectionResult = DetectionResult.Error("Could not interpret response.")
                                        return@launch
                                    }
                                    val isAi       = best.className == "ai_generated"
                                    val label      = if (isAi) "AI GENERATED" else "LIKELY REAL"
                                    val confidence = (best.score * 100).toInt()
                                    val generator  = if (isAi) resolveGenerator(classes) else "N/A"
                                    val dfScore    = classes.find { it.className == "deepfake" }?.score ?: 0.0
                                    detectionResult = DetectionResult.Success(
                                        DetectionDetail(label, confidence, isAi, generator,
                                            resolveDeepfakeRisk(dfScore), (dfScore * 100).toInt())
                                    )
                                    scanRepository.insertScan(
                                        ScanEntity(
                                            imageUri   = imageUri.toString(),
                                            label      = label,
                                            confidence = confidence,
                                            isAi       = isAi
                                        ))
                                } else {
                                    detectionResult = when (response.code()) {
                                        401 -> DetectionResult.Error("Unauthorised — check your API key.")
                                        429 -> DetectionResult.Error("Rate limit reached. Please wait.")
                                        500 -> DetectionResult.Error("Server error. Try again later.")
                                        else -> DetectionResult.Error("API Error: ${response.code()}")
                                    }
                                }
                            } catch (e: java.net.UnknownHostException) {
                                detectionResult = DetectionResult.Error("No internet connection.")
                            } catch (e: java.net.SocketTimeoutException) {
                                detectionResult = DetectionResult.Error("Request timed out.")
                            } catch (e: Exception) {
                                detectionResult = DetectionResult.Error("Error: ${e.message}")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = detectionResult !is DetectionResult.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TealPrimary,
                        contentColor   = Color(0xFF001A14),
                        disabledContainerColor = TealPrimary.copy(0.3f),
                        disabledContentColor = Color.Black.copy(0.3f)
                    )
                ) {
                    Icon(Icons.Default.Search, null,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Analyse", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Results ───────────────────────────────────────────
            when (val r = detectionResult) {
                is DetectionResult.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CardSurface)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = TealPrimary, strokeWidth = 2.dp,
                                modifier = Modifier.size(36.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("ANALYSING IMAGE", fontSize = 11.sp,
                                color = TealPrimary, letterSpacing = 2.sp)
                            Text("Powered by Hive AI", fontSize = 12.sp,
                                color = TextMuted, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }

                is DetectionResult.Success -> {
                    val d = r.detail
                    val accentColor = if (d.isAi) AiRed else RealGreen
                    val bgColor     = if (d.isAi) AiRedBg else RealGreenBg
                    val borderColor = if (d.isAi) AiRedBorder else RealGreenBorder

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(bgColor)
                            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            // Top badge
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(accentColor.copy(alpha = 0.15f))
                                        .border(1.dp, accentColor.copy(0.3f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        if (d.isAi) "⚠  ${d.label}" else "✓  ${d.label}",
                                        fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                        color = accentColor, letterSpacing = 1.sp
                                    )
                                }
                                Spacer(Modifier.weight(1f))
                                Text("${d.confidence}%", fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold, color = accentColor)
                            }

                            Spacer(Modifier.height(12.dp))

                            // Confidence bar
                            Text("CONFIDENCE", fontSize = 9.sp, color = accentColor.copy(0.6f),
                                letterSpacing = 2.sp)
                            Spacer(Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(accentColor.copy(0.1f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(d.confidence / 100f)
                                        .fillMaxHeight()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(accentColor.copy(0.6f), accentColor)
                                            )
                                        )
                                )
                            }

                            Spacer(Modifier.height(20.dp))
                            Divider(color = borderColor)
                            Spacer(Modifier.height(14.dp))

                            // Details
                            if (d.isAi) {
                                ResultDetailRow("Generator", d.generator, accentColor)
                                Spacer(Modifier.height(10.dp))
                            }

                            val dfColor = when (d.deepfakeRisk) {
                                "High" -> AiRed; "Medium" -> WarningAmber; else -> RealGreen
                            }
                            ResultDetailRow("Deepfake Risk",
                                "${d.deepfakeRisk}  •  ${d.deepfakeScore}%", dfColor)

                            Spacer(Modifier.height(16.dp))
                            Divider(color = borderColor)
                            Spacer(Modifier.height(14.dp))

                            // Share
                            OutlinedButton(
                                onClick = {
                                    val txt = buildString {
                                        appendLine("🔍 TruthLens Detection Result")
                                        appendLine()
                                        appendLine("Verdict: ${d.label}")
                                        appendLine("Confidence: ${d.confidence}%")
                                        if (d.isAi) appendLine("Generator: ${d.generator}")
                                        appendLine("Deepfake Risk: ${d.deepfakeRisk} (${d.deepfakeScore}%)")
                                        appendLine()
                                        appendLine("Detected using TruthLens — AI Image Detection App")
                                    }
                                    context.startActivity(Intent.createChooser(
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, txt)
                                        }, "Share Result"
                                    ))
                                },
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor)
                            ) {
                                Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Share Result", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }

                            Spacer(Modifier.height(8.dp))
                            Text("Saved to history", fontSize = 11.sp,
                                color = accentColor.copy(0.5f),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center)
                        }
                    }
                }

                is DetectionResult.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(WarningBg)
                            .border(1.dp, WarningAmber.copy(0.3f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚠", fontSize = 18.sp)
                            Spacer(Modifier.width(12.dp))
                            Text(r.message, color = WarningAmber, fontSize = 13.sp)
                        }
                    }
                }

                is DetectionResult.Idle -> {}
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun ResultDetailRow(label: String, value: String, valueColor: Color) {
    Row(Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(label.uppercase(), fontSize = 10.sp,
            color = TextMuted, letterSpacing = 1.sp)
        Text(value, fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}