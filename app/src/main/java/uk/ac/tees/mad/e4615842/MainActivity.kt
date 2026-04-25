package uk.ac.tees.mad.e4615842

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import uk.ac.tees.mad.e4615842.data.AppDatabase
import uk.ac.tees.mad.e4615842.data.ScanRepository
import uk.ac.tees.mad.e4615842.repository.UserRepository
import uk.ac.tees.mad.e4615842.ui.history.HistoryScreen
import uk.ac.tees.mad.e4615842.ui.home.HomeScreen
import uk.ac.tees.mad.e4615842.ui.login.LoginScreen
import uk.ac.tees.mad.e4615842.ui.register.RegisterScreen
import uk.ac.tees.mad.e4615842.ui.splash.SplashScreen
import uk.ac.tees.mad.e4615842.ui.theme.TruthLensTheme

class MainActivity : ComponentActivity() {

    private val userRepository = UserRepository()

    // Room DB — initialised once, singleton via companion object
    private lateinit var scanRepository: ScanRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getDatabase(applicationContext)
        scanRepository = ScanRepository(db.scanDao())

        setContent {
            TruthLensTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppHost(userRepository, scanRepository)
                }
            }
        }
    }
}

@Composable
fun AppHost(
    userRepository: UserRepository,
    scanRepository: ScanRepository
) {
    // Navigation states: splash → login → register → home → history
    var currentScreen by remember { mutableStateOf("splash") }

    when (currentScreen) {

        "splash" -> SplashScreen(
            onSplashComplete = { currentScreen = "login" }
        )

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
            scanRepository = scanRepository,
            onViewHistory = { currentScreen = "history" },
            onLogout = {
                userRepository.logout()
                currentScreen = "login"
            }
        )

        "history" -> HistoryScreen(
            scanRepository = scanRepository,
            onBack = { currentScreen = "home" }
        )
    }
}