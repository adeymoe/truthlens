package uk.ac.tees.mad.e4615842

import android.os.Bundle
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import uk.ac.tees.mad.e4615842.repository.UserRepository
import uk.ac.tees.mad.e4615842.ui.login.LoginScreen
import uk.ac.tees.mad.e4615842.ui.register.RegisterScreen
import uk.ac.tees.mad.e4615842.ui.theme.TruthLensTheme
import uk.ac.tees.mad.e4615842.ui.home.HomeScreen

class MainActivity : ComponentActivity() {

    private val repository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TruthLensTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AuthHost(repository)
                }
            }
        }
    }
}

@Composable
fun AuthHost(userRepository: UserRepository) {

    var currentScreen by remember { mutableStateOf("login") }

    when (currentScreen) {

        "login" -> LoginScreen(
            onLoginClick = { email, password, callback ->
                userRepository.firebaseLogin(email, password) { success, msg ->
                    if (success) currentScreen = "home"
                    callback(success, msg)
                }
            },
            onNavigateToRegister = { currentScreen = "register" }
        )

        "register" -> RegisterScreen(
            onRegisterClick = { email, password, callback ->
                userRepository.firebaseRegister(email, password) { success, msg ->
                    if (success) currentScreen = "home"
                    callback(success, msg)
                }
            },
            onNavigateToLogin = { currentScreen = "login" }
        )

        "home" -> HomeScreen(
            onImageSelected = { uri ->
                println("Image selected: $uri")
            },
            onLogout = {
                userRepository.logout()
                currentScreen = "login"
            }
        )
    }
}