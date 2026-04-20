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
import uk.ac.tees.mad.e4615842.model.HiveRequest
import uk.ac.tees.mad.e4615842.utils.uriToBase64
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// Sealed class to represent the three possible UI states for the result
sealed class DetectionResult {
    object Idle : DetectionResult()
    object Loading : DetectionResult()
    data class Success(val label: String, val confidence: Int, val isAi: Boolean) : DetectionResult()
    data class Error(val message: String) : DetectionResult()
}

@Composable
fun HomeScreen(
    onImageSelected: (Uri?) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var detectionResult by remember { mutableStateOf<DetectionResult>(DetectionResult.Idle) }

    // Create a fresh temp file for every camera capture
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
            onImageSelected(cameraUri)
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            detectionResult = DetectionResult.Idle
            onImageSelected(uri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ── Header ──────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TruthLens",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onLogout) {
                Text("Logout", color = MaterialTheme.colorScheme.error)
            }
        }

        Text(
            text = "Detect AI-generated images instantly",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Image Preview ────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F3F3))
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
                    Text(
                        text = "📷\nNo image selected",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Action Buttons ───────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    // Create a fresh file each time so the URI is valid
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
                Text("📸 Camera")
            }

            Button(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("🖼️ Gallery")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Analyse Button ───────────────────────────────────────
        Button(
            onClick = {
                // Validate: no image selected
                if (imageUri == null) {
                    detectionResult = DetectionResult.Error("Please select or capture an image first.")
                    return@Button
                }

                scope.launch {
                    detectionResult = DetectionResult.Loading

                    try {
                        val base64 = uriToBase64(context, imageUri!!)

                        if (base64 == null) {
                            detectionResult = DetectionResult.Error("Could not read image. Please try again.")
                            return@launch
                        }

                        val response = RetrofitInstance.api.detectImage(
                            "Bearer ${BuildConfig.HIVE_API_KEY}",
                            HiveRequest(base64)
                        )

                        if (response.isSuccessful) {
                            val classes = response.body()?.output?.firstOrNull()?.classes

                            if (classes.isNullOrEmpty()) {
                                detectionResult = DetectionResult.Error("No results returned from API.")
                                return@launch
                            }

                            // Pick the class with the highest confidence score
                            val best = classes.maxByOrNull { it.score }

                            if (best == null) {
                                detectionResult = DetectionResult.Error("Could not interpret API response.")
                                return@launch
                            }

                            val isAi = best.className.lowercase().contains("ai") ||
                                    best.className.lowercase().contains("fake") ||
                                    best.className.lowercase() == "ai_generated"

                            val label = if (isAi) "⚠️ AI Generated" else "✅ Likely Real"
                            val confidence = (best.score * 100).toInt()

                            detectionResult = DetectionResult.Success(label, confidence, isAi)

                        } else {
                            detectionResult = when (response.code()) {
                                401 -> DetectionResult.Error("Unauthorised — check your API key.")
                                429 -> DetectionResult.Error("Too many requests. Please wait and try again.")
                                500 -> DetectionResult.Error("API server error. Try again later.")
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
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = detectionResult !is DetectionResult.Loading
        ) {
            Text("🔍 Analyse Image", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Result Area ──────────────────────────────────────────
        when (val result = detectionResult) {

            is DetectionResult.Loading -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Analysing image...", color = Color.Gray)
            }

            is DetectionResult.Success -> {
                val cardColor = if (result.isAi) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                val textColor = if (result.isAi) Color(0xFFB71C1C) else Color(0xFF1B5E20)
                val borderColor = if (result.isAi) Color(0xFFEF9A9A) else Color(0xFFA5D6A7)

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
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Confidence: ${result.confidence}%",
                            fontSize = 16.sp,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = result.confidence / 100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (result.isAi) Color(0xFFE53935) else Color(0xFF43A047),
                            trackColor = Color.White
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
                        Text("⚠️", fontSize = 20.sp)
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
                // Nothing shown until the user acts
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}