package uk.ac.tees.mad.e4615842.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import uk.ac.tees.mad.e4615842.model.HiveInput
import uk.ac.tees.mad.e4615842.model.HiveRequest
import uk.ac.tees.mad.e4615842.utils.uriToBase64
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// ── Data class to hold the full enriched result ───────────────────────────────
data class DetectionDetail(
    val label: String,
    val confidence: Int,
    val isAi: Boolean,
    val generator: String,
    val deepfakeRisk: String,
    val deepfakeScore: Int
)

// ── UI State ──────────────────────────────────────────────────────────────────
sealed class DetectionResult {
    object Idle : DetectionResult()
    object Loading : DetectionResult()
    data class Success(val detail: DetectionDetail) : DetectionResult()
    data class Error(val message: String) : DetectionResult()
}

// ── Generator name mapping ────────────────────────────────────────────────────
fun resolveGenerator(classes: List<uk.ac.tees.mad.e4615842.model.ClassResult>): String {
    val generatorClasses = mapOf(
        "midjourney"             to "Midjourney",
        "stablediffusion"        to "Stable Diffusion",
        "dalle"                  to "DALL·E",
        "firefly"                to "Adobe Firefly",
        "imagen"                 to "Google Imagen",
        "other_image_generators" to "Other AI Generator"
    )
    val best = classes
        .filter { generatorClasses.containsKey(it.className.lowercase()) }
        .maxByOrNull { it.score }
    return if (best != null && best.score > 0.05)
        generatorClasses[best.className.lowercase()] ?: "Unknown Generator"
    else "None detected"
}

// ── Deepfake risk label ───────────────────────────────────────────────────────
fun resolveDeepfakeRisk(score: Double): String = when {
    score >= 0.7 -> "High"
    score >= 0.3 -> "Medium"
    else         -> "Low"
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
        mutableStateOf(
            File(context.cacheDir,
                "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg")
        )
    }

    var cameraUri by remember {
        mutableStateOf(
            androidx.core.content.FileProvider.getUriForFile(
                context, "${context.packageName}.provider", photoFile)
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) { imageUri = cameraUri; detectionResult = DetectionResult.Idle }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { imageUri = uri; detectionResult = DetectionResult.Idle }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ── Header ────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("TruthLens", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text("AI Image Detector", fontSize = 13.sp, color = Color.Gray)
            }
            Row {
                TextButton(onClick = onViewHistory) { Text("History") }
                TextButton(onClick = onViewProfile) { Text("Profile") }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Image Preview ─────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth().height(260.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📷", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("No image selected", color = Color.Gray,
                            textAlign = TextAlign.Center, fontSize = 15.sp)
                        Text("Use the buttons below to get started",
                            color = Color.LightGray, textAlign = TextAlign.Center, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Camera / Gallery ──────────────────────────────────────
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    photoFile = File(context.cacheDir,
                        "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg")
                    cameraUri = androidx.core.content.FileProvider.getUriForFile(
                        context, "${context.packageName}.provider", photoFile)
                    cameraLauncher.launch(cameraUri)
                },
                modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)
            ) { Text("📸  Camera") }

            Button(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)
            ) { Text("🖼️  Gallery") }
        }

        Spacer(Modifier.height(12.dp))

        // ── Analyse Button ────────────────────────────────────────
        Button(
            onClick = {
                if (imageUri == null) {
                    detectionResult = DetectionResult.Error("Please select or capture an image first.")
                    return@Button
                }

                // ── Network check ─────────────────────────────────
                val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE)
                        as android.net.ConnectivityManager
                val network = connectivityManager.activeNetwork
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                val isConnected = capabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

                if (!isConnected) {
                    detectionResult = DetectionResult.Error("No internet connection. Please check your network.")
                    return@Button
                }

                scope.launch {
                    detectionResult = DetectionResult.Loading

                    try {
                        val base64 = uriToBase64(context, imageUri!!)

                        if (base64 == null) {
                            detectionResult = DetectionResult.Error("Could not read the image. Please try a different one.")
                            return@launch
                        }

                        val mediaBase64 = "data:image/jpeg;base64,$base64"

                        val response = RetrofitInstance.api.detectImage(
                            "Bearer ${BuildConfig.HIVE_API_KEY}",
                            HiveRequest(input = listOf(HiveInput(mediaBase64 = mediaBase64)))
                        )

                        if (response.isSuccessful) {
                            val classes = response.body()?.output?.firstOrNull()?.classes

                            if (classes.isNullOrEmpty()) {
                                detectionResult = DetectionResult.Error("No results returned. Please try again.")
                                return@launch
                            }

                            // ── Authenticity ──────────────────────
                            val imageClasses = classes.filter {
                                it.className == "ai_generated" || it.className == "not_ai_generated"
                            }
                            val best = imageClasses.maxByOrNull { it.score }
                                ?: run {
                                    detectionResult = DetectionResult.Error("Could not interpret the API response.")
                                    return@launch
                                }

                            val isAi       = best.className == "ai_generated"
                            val label      = if (isAi) "⚠️ AI Generated" else "✅ Likely Real"
                            val confidence = (best.score * 100).toInt()

                            // ── Generator + deepfake ──────────────
                            val generator    = if (isAi) resolveGenerator(classes) else "N/A"
                            val deepfakeScore = classes.find { it.className == "deepfake" }?.score ?: 0.0
                            val deepfakeRisk  = resolveDeepfakeRisk(deepfakeScore)
                            val deepfakeInt   = (deepfakeScore * 100).toInt()

                            detectionResult = DetectionResult.Success(
                                DetectionDetail(
                                    label         = label,
                                    confidence    = confidence,
                                    isAi          = isAi,
                                    generator     = generator,
                                    deepfakeRisk  = deepfakeRisk,
                                    deepfakeScore = deepfakeInt
                                )
                            )

                            // ── Save to Room DB ───────────────────
                            scanRepository.insertScan(
                                ScanEntity(
                                    imageUri   = imageUri.toString(),
                                    label      = label,
                                    confidence = confidence,
                                    isAi       = isAi
                                )
                            )

                        } else {
                            detectionResult = when (response.code()) {
                                401  -> DetectionResult.Error("Unauthorised — check your API key.")
                                429  -> DetectionResult.Error("Too many requests. Please wait and try again.")
                                500  -> DetectionResult.Error("Server error. Please try again later.")
                                else -> DetectionResult.Error("API Error: ${response.code()}")
                            }
                        }

                    } catch (e: java.net.UnknownHostException) {
                        detectionResult = DetectionResult.Error("No internet connection. Please check your network.")
                    } catch (e: java.net.SocketTimeoutException) {
                        detectionResult = DetectionResult.Error("Request timed out. Please try again.")
                    } catch (e: Exception) {
                        detectionResult = DetectionResult.Error("Unexpected error: ${e.message}")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = detectionResult !is DetectionResult.Loading
        ) {
            Text("🔍  Analyse Image", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(24.dp))

        // ── Result Area ───────────────────────────────────────────
        when (val result = detectionResult) {

            is DetectionResult.Loading -> {
                CircularProgressIndicator()
                Spacer(Modifier.height(8.dp))
                Text("Analysing image...", color = Color.Gray, fontSize = 14.sp)
            }

            is DetectionResult.Success -> {
                val d           = result.detail
                val cardColor   = if (d.isAi) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                val textColor   = if (d.isAi) Color(0xFFB71C1C) else Color(0xFF1B5E20)
                val borderColor = if (d.isAi) Color(0xFFEF9A9A) else Color(0xFFA5D6A7)
                val barColor    = if (d.isAi) Color(0xFFE53935) else Color(0xFF43A047)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(d.label, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textColor)
                        Spacer(Modifier.height(4.dp))
                        Text("Confidence: ${d.confidence}%", fontSize = 15.sp, color = textColor)
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = d.confidence / 100f,
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = barColor,
                            trackColor = Color.White
                        )
                        Spacer(Modifier.height(16.dp))
                        Divider(color = borderColor, thickness = 1.dp)
                        Spacer(Modifier.height(14.dp))

                        if (d.isAi) {
                            ResultRow(icon = "🎨", label = "Generator", value = d.generator, textColor = textColor)
                            Spacer(Modifier.height(10.dp))
                        }

                        val deepfakeColor = when (d.deepfakeRisk) {
                            "High"   -> Color(0xFFB71C1C)
                            "Medium" -> Color(0xFFE65100)
                            else     -> Color(0xFF1B5E20)
                        }
                        ResultRow(
                            icon = "🎭",
                            label = "Deepfake Risk",
                            value = "${d.deepfakeRisk} (${d.deepfakeScore}%)",
                            textColor = deepfakeColor
                        )
                        Spacer(Modifier.height(14.dp))
                        Divider(color = borderColor, thickness = 1.dp)
                        Spacer(Modifier.height(10.dp))
                        Text("Result saved to history", fontSize = 12.sp, color = textColor.copy(alpha = 0.6f))
                    }
                }
            }

            is DetectionResult.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCC80))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("⚠️", fontSize = 22.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(result.message, color = Color(0xFFE65100), fontSize = 14.sp)
                    }
                }
            }

            is DetectionResult.Idle -> { /* nothing */ }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ── Reusable result row ───────────────────────────────────────────────────────
@Composable
fun ResultRow(icon: String, label: String, value: String, textColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            Text(label, fontSize = 14.sp, color = textColor.copy(alpha = 0.8f))
        }
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}