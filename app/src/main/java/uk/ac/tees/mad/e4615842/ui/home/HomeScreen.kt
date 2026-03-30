package uk.ac.tees.mad.e4615842.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import uk.ac.tees.mad.e4615842.api.RetrofitInstance
import uk.ac.tees.mad.e4615842.model.HiveRequest
import uk.ac.tees.mad.e4615842.utils.uriToBase64
import uk.ac.tees.mad.e4615842.BuildConfig

@Composable
fun HomeScreen(
    onImageSelected: (Uri?) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var resultText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // ✅ Image Picker
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        onImageSelected(uri)
    }

    Column(modifier = Modifier.padding(16.dp)) {

        // ✅ Logout
        TextButton(onClick = onLogout) {
            Text("Logout")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Upload Button
        Button(onClick = {
            launcher.launch("image/*")
        }) {
            Text("Upload Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Show selected image
        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "Selected Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Analyse Button
        Button(onClick = {
            imageUri?.let { uri ->
                scope.launch {
                    isLoading = true
                    resultText = ""

                    try {
                        val base64 = uriToBase64(context, uri)

                        val response = RetrofitInstance.api.detectImage(
                            "Bearer ${BuildConfig.HIVE_API_KEY}",
                            HiveRequest(base64) // ✅ FIXED
                        )

                        if (response.isSuccessful) {
                            val output = response.body()?.output?.firstOrNull()
                            val result = output?.classes?.joinToString("\n") {
                                "${it.className}: ${it.score}"
                            }
                            resultText = result ?: "No result"
                        } else {
                            resultText = "API Error: ${response.code()}"
                        }

                    } catch (e: Exception) {
                        resultText = "Error: ${e.message}"
                    }

                    isLoading = false
                }
            }
        }) {
            Text("Analyse Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Loading Indicator
        if (isLoading) {
            CircularProgressIndicator()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Result Display
        Text(text = resultText)
    }
}