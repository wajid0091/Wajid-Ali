package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Tournament
import com.example.ui.AppViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentDetailsScreen(viewModel: AppViewModel, tournamentId: Int) {
    val tournamentsList by viewModel.tournaments.collectAsState()
    val joinedMatches by viewModel.userJoinedTournaments.collectAsState()
    val t = tournamentsList.find { it.id == tournamentId }

    // Countdown active ticker
    var timeTickerMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            timeTickerMs = System.currentTimeMillis()
        }
    }

    if (t == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Match details") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.navigateBack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: Match lobby details not found.")
            }
        }
        return
    }

    val isUserRegistered = joinedMatches.any { it.tournamentId == t.id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(t.name, fontWeight = FontWeight.Black, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Entry Ticket cost:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(
                            text = if (t.entryFee > 0.0) "Rs.${t.entryFee}" else "FREE PLAY",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (isUserRegistered) {
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .width(180.dp)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = false
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("REGISTERED", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.joinTournament(t.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (t.status == "Open") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                contentColor = if (t.status == "Open") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
                            ),
                            enabled = t.status == "Open",
                            modifier = Modifier
                                .width(180.dp)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (t.status == "Open") "SECURE SLOT" else t.status.uppercase(),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // --- 1. PRO TOP BANNER HERO ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.primary, Color(0xFF00497D))
                        )
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(t.type.uppercase(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = t.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Mode: ${t.format} • Map: ${t.mapType} • Format: ${t.roomType}",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.82f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. SECURITY ROOM ID AND PASSWORD ---
            if (isUserRegistered) {
                RoomCredentialsBox(t = t, timeTickerMs = timeTickerMs)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- 3. CORE STATISTICS HIGHLIGHTS ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailsMetaItem(
                    icon = Icons.Default.Star,
                    label = "PRIZE POOL",
                    value = "Rs.${t.prizePool}",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                DetailsMetaItem(
                    icon = Icons.Default.List,
                    label = "DATE & TIME",
                    value = "${t.date}\n${t.time}",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )

                DetailsMetaItem(
                    icon = Icons.Default.Person,
                    label = "SLOTS REMAINING",
                    value = "${t.totalSlots - t.filledSlots} left\n(${t.filledSlots} filled)",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 4. DETAILS SECTIONS ---
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                
                // Description Card
                Text("Lobby Description", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = t.description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.padding(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Rewards breakdown
                Text("Rewards Index", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Kill Premium:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Rs.${t.killReward} per single kill", fontSize = 13.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }
                        Divider(modifier = Modifier.padding(vertical = 10.dp))
                        Text("Placement Allocations:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = t.rankReward,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Arena Standing Rules
                Text("Terms & Arena Standing Rules", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = t.rules,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun DetailsMetaItem(icon: ImageVector, label: String, value: String, tint: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun RoomCredentialsBox(t: Tournament, timeTickerMs: Long) {
    // Mode 1: Permanent -> Always display
    // Mode 2: Scheduled -> 10 minutes prior
    var isUnlocked by remember { mutableStateOf(false) }
    var countdownLabel by remember { mutableStateOf("") }

    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    LaunchedEffect(timeTickerMs, t) {
        if (t.isRoomVisibleManuallyOverride) {
            isUnlocked = true
        } else if (t.visibilityMode == "Permanent") {
            isUnlocked = true
        } else {
            // Mode 2: Scheduled. Parse date time
            try {
                val matchDateTime = sdf.parse("${t.date} ${t.time}")
                if (matchDateTime != null) {
                    val matchTimeMs = matchDateTime.time
                    val unlockTimeMs = matchTimeMs - (10 * 60 * 1000) // 10 minutes prior
                    val diffMs = unlockTimeMs - timeTickerMs

                    if (diffMs <= 0) {
                        isUnlocked = true
                    } else {
                        isUnlocked = false
                        // calculate diff format
                        val totalSecs = diffMs / 1000
                        val hours = totalSecs / 3600
                        val mins = (totalSecs % 3600) / 60
                        val secs = totalSecs % 60

                        countdownLabel = if (hours > 0) {
                            String.format("Creds unlock in %dh %dm", hours, mins)
                        } else if (mins > 0) {
                            String.format("Creds unlock in %dm %ds", mins, secs)
                        } else {
                            String.format("Creds unlock in %ds", secs)
                        }
                    }
                } else {
                    isUnlocked = true // fallback
                }
            } catch (e: Exception) {
                isUnlocked = true // fallback
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isUnlocked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, if (isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Room Entry Passwords",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isUnlocked) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ROOM ID", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text(
                            text = t.roomId.ifEmpty { "WAITING_ADMIN" },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ROOM PASSWORD", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text(
                            text = t.roomPassword.ifEmpty { "WAITING_ADMIN" },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Note: Do not share these room credentials. Doing so results in account bans.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            } else {
                Text(
                    text = countdownLabel.ifEmpty { "Lobby Credentials Scheduled" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Scheduled Mode: Credentials unlock exactly 10 minutes prior to match schedule.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
