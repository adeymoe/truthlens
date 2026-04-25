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
import uk.ac.tees.mad.e4615842.model.HiveRequest
import uk.ac.tees.mad.e4615842.utils.uriToBase64
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// Sealed class to represent all possible UI states for the detection result
sealed class DetectionResult {
    object Idle : DetectionResult()
    object Loading : DetectionResult()
    data class Success(val label: String, val confidence: Int, val isAi: Boolean) : DetectionResult()
    data class Error(val message: String) : DetectionResult()
}

@Composable
fun HomeScreen(
    scanRepository: ScanRepository,
    onViewHistory: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var detectionResult by remember { mutableStateOf<DetectionResult>(DetectionResult.Idle) }

    // Create a fresh temp file for each camera session
    var photoFile by remember {
        mutableStateOf(
            File(
                context.cacheDir,
                "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
            )
        )
    }

    var cameraUri by remember {
        mutableStateOf(
            androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                photoFile
            )
        )
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = cameraUri
            detectionResult = DetectionResult.Idle
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            detectionResult = DetectionResult.Idle
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ── Header Row ────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TruthLens",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "AI Image Detector",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Row {
                TextButton(onClick = onViewHistory) {
                    Text("History")
                }
                TextButton(onClick = onLogout) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Image Preview Card ────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📷", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No image selected",
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Use the buttons below to get started",
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Camera / Gallery Buttons ──────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    // Always create a fresh file to avoid stale URI issues on repeat captures
                    photoFile = File(
                        context.cacheDir,
                        "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
                    )
                    cameraUri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        photoFile
                    )
                    cameraLauncher.launch(cameraUri)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("📸  Camera")
            }

            Button(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("🖼️  Gallery")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Analyse Button ────────────────────────────────────────
        Button(
            onClick = {
                if (imageUri == null) {
                    detectionResult = DetectionResult.Error("Please select or capture an image first.")
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

                        val response = RetrofitInstance.api.detectImage(
                            "Bearer ${BuildConfig.HIVE_API_KEY}",
                            HiveRequest(base64)
                        )

                        if (response.isSuccessful) {
                            val classes = response.body()?.output?.firstOrNull()?.classes

                            if (classes.isNullOrEmpty()) {
                                detectionResult = DetectionResult.Error("No results returned. Please try again.")
                                return@launch
                            }

                            val best = classes.maxByOrNull { it.score }

                            if (best == null) {
                                detectionResult = DetectionResult.Error("Could not interpret the API response.")
                                return@launch
                            }

                            // Normalise class name — Hive may return "ai_generated", "fake", etc.
                            val nameNorm = best.className.lowercase().replace("_", "").replace("-", "")
                            val isAi = nameNorm.contains("ai") ||
                                    nameNorm.contains("fake") ||
                                    nameNorm.contains("generated")

                            val label = if (isAi) "⚠️ AI Generated" else "✅ Likely Real"
                            val confidence = (best.score * 100).toInt()

                            val result = DetectionResult.Success(label, confidence, isAi)
                            detectionResult = result

                            // ── Persist result to Room DB ─────────────────────────
                            scanRepository.insertScan(
                                ScanEntity(
                                    imageUri  = imageUri.toString(),
                                    label     = label,
                                    confidence = confidence,
                                    isAi      = isAi
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
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = detectionResult !is DetectionResult.Loading
        ) {
            Text("🔍  Analyse Image", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Result Area ───────────────────────────────────────────
        when (val result = detectionResult) {

            is DetectionResult.Loading -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Analysing image...", color = Color.Gray, fontSize = 14.sp)
            }

            is DetectionResult.Success -> {
                val cardColor   = if (result.isAi) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                val textColor   = if (result.isAi) Color(0xFFB71C1C) else Color(0xFF1B5E20)
                val borderColor = if (result.isAi) Color(0xFFEF9A9A) else Color(0xFFA5D6A7)
                val barColor    = if (result.isAi) Color(0xFFE53935) else Color(0xFF43A047)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = result.label,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Confidence: ${result.confidence}%",
                            fontSize = 16.sp,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        LinearProgressIndicator(
                            progress = result.confidence / 100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = barColor,
                            trackColor = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Result saved to history",
                            fontSize = 12.sp,
                            color = textColor.copy(alpha = 0.65f)
                        )
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
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = result.message,
                            color = Color(0xFFE65100),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            is DetectionResult.Idle -> {
                // Nothing shown until the user interacts
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}