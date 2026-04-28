package uk.ac.tees.mad.e4615842.ui.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import uk.ac.tees.mad.e4615842.data.ScanEntity
import uk.ac.tees.mad.e4615842.data.ScanRepository
import uk.ac.tees.mad.e4615842.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(scanRepository: ScanRepository, onBack: () -> Unit) {
    val scans  by scanRepository.allScans.collectAsState(initial = emptyList())
    val scope  = rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DeepBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Scan History", fontWeight = FontWeight.Bold,
                            color = TextPrimary, fontSize = 18.sp)
                        if (scans.isNotEmpty())
                            Text("${scans.size} scan${if (scans.size == 1) "" else "s"}",
                                fontSize = 11.sp, color = TextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextPrimary)
                    }
                },
                actions = {
                    if (scans.isNotEmpty())
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Default.DeleteSweep, null, tint = AiRed)
                        }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        if (scans.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) { Text("🗂️", fontSize = 36.sp) }
                    Spacer(Modifier.height(16.dp))
                    Text("No scans yet", fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    Spacer(Modifier.height(6.dp))
                    Text("Images you analyse will appear here",
                        fontSize = 13.sp, color = TextMuted)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(scans, key = { it.id }) { scan ->
                    HistoryCard(scan = scan,
                        onDelete = { scope.launch { scanRepository.deleteScan(scan) } })
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = CardSurface,
            title = { Text("Clear All History", color = TextPrimary) },
            text = { Text("This will permanently delete all scan records.",
                color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { scanRepository.deleteAllScans() }
                    showClearDialog = false
                }) { Text("Clear All", color = AiRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun HistoryCard(scan: ScanEntity, onDelete: () -> Unit) {
    val isAi        = scan.isAi
    val accentColor = if (isAi) AiRed else RealGreen
    val bgColor     = if (isAi) AiRedBg else RealGreenBg
    val borderColor = if (isAi) AiRedBorder else RealGreenBorder
    val dateStr     = SimpleDateFormat("dd MMM yyyy  •  HH:mm",
        Locale.getDefault()).format(Date(scan.timestamp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceElevated)
                    .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            ) {
                Image(
                    painter = rememberAsyncImagePainter(scan.imageUri),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                // Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(accentColor.copy(0.15f))
                        .border(1.dp, accentColor.copy(0.25f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        if (isAi) "⚠  AI GENERATED" else "✓  LIKELY REAL",
                        fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        color = accentColor, letterSpacing = 0.5.sp
                    )
                }
                Spacer(Modifier.height(5.dp))
                Text("${scan.confidence}% confidence", fontSize = 13.sp,
                    fontWeight = FontWeight.Medium, color = TextPrimary)
                Spacer(Modifier.height(3.dp))
                Text(dateStr, fontSize = 11.sp, color = TextMuted)
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Text("×", fontSize = 18.sp, color = TextMuted)
            }
        }
    }
}