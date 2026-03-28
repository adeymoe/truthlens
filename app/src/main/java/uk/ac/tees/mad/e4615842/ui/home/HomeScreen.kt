package uk.ac.tees.mad.e4615842.ui.home

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import uk.ac.tees.mad.e4615842.api.RetrofitInstance
import uk.ac.tees.mad.e4615842.model.HiveRequest
import uk.ac.tees.mad.e4615842.utils.uriToBase64
import androidx.compose.ui.platform.LocalContext

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

    Column(modifier = Modifier.padding(16.dp)) {

        TextButton(onClick = onLogout) {
            Text("Logout")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            // You already implemented picker earlier
        }) {
            Text("Upload Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            imageUri?.let { uri ->
                scope.launch {
                    isLoading = true
                    try {
                        val base64 = uriToBase64(context, uri)

                        val response = RetrofitInstance.api.detectImage(
                            HiveRequest(base64)
                        )

                        if (response.isSuccessful) {
                            val output = response.body()?.output?.firstOrNull()
                            val result = output?.classes?.joinToString("\n") {
                                "${it.className}: ${it.score}"
                            }
                            resultText = result ?: "No result"
                        } else {
                            resultText = "API Error"
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

        if (isLoading) {
            CircularProgressIndicator()
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = resultText)
    }
}