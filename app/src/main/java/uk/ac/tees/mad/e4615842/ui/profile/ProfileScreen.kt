package uk.ac.tees.mad.e4615842.ui.profile

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import uk.ac.tees.mad.e4615842.data.ScanRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    scanRepository: ScanRepository,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // ── Firebase user info ────────────────────────────────────────
    val user        = FirebaseAuth.getInstance().currentUser
    val email       = user?.email ?: "Unknown"
    val userId      = user?.uid?.take(8)?.uppercase() ?: "N/A"
    val createdDate = user?.metadata?.creationTimestamp?.let {
        java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            .format(java.util.Date(it))
    } ?: "Unknown"

    // ── Scan stats from Room DB ───────────────────────────────────
    var totalScans  by remember { mutableStateOf(0) }
    var aiCount     by remember { mutableStateOf(0) }
    var realCount   by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        scope.launch {
            val scans  = scanRepository.allScans.first()
            totalScans = scans.size
            aiCount    = scans.count { it.isAi }
            realCount  = scans.count { !it.isAi }
        }
    }

    // ── Avatar initials from email ────────────────────────────────
    val initials = email.take(2).uppercase()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
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
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = email,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Member since $createdDate",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(Modifier.height(28.dp))

            // ── Stats row ─────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = "🔍",
                    value = totalScans.toString(),
                    label = "Total Scans"
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = "⚠️",
                    value = aiCount.toString(),
                    label = "AI Detected",
                    valueColor = Color(0xFFB71C1C)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = "✅",
                    value = realCount.toString(),
                    label = "Real Images",
                    valueColor = Color(0xFF1B5E20)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Account details card ──────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Account Details",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    ProfileDetailRow(label = "Email", value = email)
                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.LightGray.copy(alpha = 0.5f))
                    ProfileDetailRow(label = "User ID", value = "#$userId")
                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.LightGray.copy(alpha = 0.5f))
                    ProfileDetailRow(label = "Account Created", value = createdDate)
                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.LightGray.copy(alpha = 0.5f))
                    ProfileDetailRow(
                        label = "Detection Rate",
                        value = if (totalScans > 0) "${((aiCount.toFloat() / totalScans) * 100).toInt()}% AI" else "No scans yet"
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── App info card ─────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "About TruthLens",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    ProfileDetailRow(label = "Version", value = "1.0.0")
                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.LightGray.copy(alpha = 0.5f))
                    ProfileDetailRow(label = "Detection Engine", value = "Hive AI v3")
                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.LightGray.copy(alpha = 0.5f))
                    ProfileDetailRow(label = "Authentication", value = "Firebase Auth")
                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.LightGray.copy(alpha = 0.5f))
                    ProfileDetailRow(label = "Local Storage", value = "Room Database")
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Logout button ─────────────────────────────────────
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = "Logout",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Stat card composable ──────────────────────────────────────────────────────
@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: String,
    value: String,
    label: String,
    valueColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 22.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color.Gray,
                maxLines = 1
            )
        }
    }
}

// ── Profile detail row composable ────────────────────────────────────────────
@Composable
fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}