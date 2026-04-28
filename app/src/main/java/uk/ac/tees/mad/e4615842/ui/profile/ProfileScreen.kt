package uk.ac.tees.mad.e4615842.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import uk.ac.tees.mad.e4615842.data.ScanRepository
import uk.ac.tees.mad.e4615842.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    scanRepository: ScanRepository,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val scope       = rememberCoroutineScope()
    val user        = FirebaseAuth.getInstance().currentUser
    val email       = user?.email ?: "Unknown"
    val userId      = user?.uid?.take(8)?.uppercase() ?: "N/A"
    val createdDate = user?.metadata?.creationTimestamp?.let {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
    } ?: "Unknown"

    var totalScans by remember { mutableStateOf(0) }
    var aiCount    by remember { mutableStateOf(0) }
    var realCount  by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        scope.launch {
            val scans  = scanRepository.allScans.first()
            totalScans = scans.size
            aiCount    = scans.count { it.isAi }
            realCount  = scans.count { !it.isAi }
        }
    }

    val initials = email.take(2).uppercase()

    Scaffold(
        containerColor = DeepBackground,
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold,
                    color = TextPrimary, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(16.dp))

            // ── Avatar ────────────────────────────────────────────
            Box(contentAlignment = Alignment.Center) {
                // Glow ring
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(TealPrimary.copy(0.3f), Color.Transparent)
                            ), CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(TealVariant, TealPrimary)
                            )
                        )
                        .border(2.dp, TealPrimary.copy(0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, fontSize = 28.sp,
                        fontWeight = FontWeight.Bold, color = Color(0xFF001A14))
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(email, fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text("MEMBER SINCE $createdDate", fontSize = 9.sp,
                color = TealPrimary, letterSpacing = 2.sp)

            Spacer(Modifier.height(28.dp))

            // ── Stats ─────────────────────────────────────────────
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(Modifier.weight(1f), "🔍", totalScans.toString(), "TOTAL")
                StatTile(Modifier.weight(1f), "⚠", aiCount.toString(), "AI FOUND", AiRed)
                StatTile(Modifier.weight(1f), "✓", realCount.toString(), "REAL", RealGreen)
            }

            Spacer(Modifier.height(20.dp))

            // ── Account card ──────────────────────────────────────
            ProfileCard(title = "ACCOUNT") {
                ProfileRow("Email", email)
                Divider(color = BorderSubtle, modifier = Modifier.padding(vertical = 10.dp))
                ProfileRow("User ID", "#$userId")
                Divider(color = BorderSubtle, modifier = Modifier.padding(vertical = 10.dp))
                ProfileRow("Member Since", createdDate)
                Divider(color = BorderSubtle, modifier = Modifier.padding(vertical = 10.dp))
                ProfileRow(
                    "Detection Rate",
                    if (totalScans > 0)
                        "${((aiCount.toFloat() / totalScans) * 100).toInt()}% AI detected"
                    else "No scans yet",
                    if (totalScans > 0 && aiCount > realCount) AiRed else RealGreen
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── App card ──────────────────────────────────────────
            ProfileCard(title = "ABOUT TRUTHLENS") {
                ProfileRow("Version", "1.0.0")
                Divider(color = BorderSubtle, modifier = Modifier.padding(vertical = 10.dp))
                ProfileRow("Detection Engine", "Hive AI v3")
                Divider(color = BorderSubtle, modifier = Modifier.padding(vertical = 10.dp))
                ProfileRow("Authentication", "Firebase Auth")
                Divider(color = BorderSubtle, modifier = Modifier.padding(vertical = 10.dp))
                ProfileRow("Local Storage", "Room Database")
            }

            Spacer(Modifier.height(28.dp))

            // ── Logout ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AiRedBg)
                    .border(1.dp, AiRedBorder, RoundedCornerShape(12.dp))
            ) {
                TextButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Sign Out", color = AiRed,
                        fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun StatTile(modifier: Modifier, icon: String, value: String, label: String,
             valueColor: Color = TextPrimary) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardSurface)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 20.sp)
            Spacer(Modifier.height(6.dp))
            Text(value, fontSize = 24.sp,
                fontWeight = FontWeight.Bold, color = valueColor)
            Text(label, fontSize = 9.sp,
                color = TextMuted, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun ProfileCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardSurface)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(title, fontSize = 10.sp, color = TealPrimary,
                letterSpacing = 2.sp, fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 14.dp))
            content()
        }
    }
}

@Composable
fun ProfileRow(label: String, value: String, valueColor: Color = TextPrimary) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = TextSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = valueColor)
    }
}