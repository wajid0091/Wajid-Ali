package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.BannerBanner
import com.example.data.models.Tournament
import com.example.data.models.User
import com.example.ui.AppViewModel
import com.example.ui.Screen
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import com.example.data.ImageUploader
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(viewModel: AppViewModel) {
    var isAdminAuthenticated by remember { mutableStateOf(false) }
    var usernameField by remember { mutableStateOf("") }
    var passwordField by remember { mutableStateOf("") }

    if (!isAdminAuthenticated) {
        // --- ADMIN LOGIN PORTAL ---
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Admin Authentication") },
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
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "OFFLINE COCKPIT GATEWAY",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Input credentials to bypass local encryption filters.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = usernameField,
                            onValueChange = { usernameField = it },
                            label = { Text("Display Username") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = passwordField,
                            onValueChange = { passwordField = it },
                            label = { Text("Password") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (usernameField == "admin" && passwordField == "admin123") {
                                    isAdminAuthenticated = true
                                    viewModel.triggerToast("Access Granted. Master Mode enabled.")
                                } else {
                                    viewModel.triggerToast("Bypass Refused: Invalid credentials!")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("DECRYPT GATE", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    } else {
        // --- MASTER ADMIN COCKPIT DASHBOARD ---
        AdminDashboardContent(viewModel = viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardContent(viewModel: AppViewModel) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Users, 1: Tourneys, 2: Deposits, 3: Withdrawals, 4: Banners, 5: Broadcast

    val tabs = listOf("Users", "Tourneys", "Deposits", "Withdraw", "Banners", "Broadcast")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anu System CRM [Offline]", fontWeight = FontWeight.Black, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit Cockpit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            // Horizontal scrollable control row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 16.dp,
                modifier = Modifier.navigationBarsPadding().padding(bottom = 12.dp)
            ) {
                tabs.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> AdminUsersSubPanel(viewModel)
                1 -> AdminTournamentsSubPanel(viewModel)
                2 -> AdminDepositsSubPanel(viewModel)
                3 -> AdminWithdrawSubPanel(viewModel)
                4 -> AdminBannersSubPanel(viewModel)
                5 -> AdminBroadcastSubPanel(viewModel)
            }
        }
    }
}


// ═══════════════════════════════════════
//   SUB PANELS: USERS CRM
// ═══════════════════════════════════════
@Composable
fun AdminUsersSubPanel(viewModel: AppViewModel) {
    val usersList by viewModel.allUsers.collectAsState()
    var showEditWalletDialog by remember { mutableStateOf<User?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Active User Profile Directory", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
        Spacer(modifier = Modifier.height(12.dp))

        if (usersList.isEmpty()) {
            Text("No registered users recorded.", color = Color.Gray)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(usersList) { u ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(u.username, fontWeight = FontWeight.Bold, color = Color.White)
                                        if (u.isBanned) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(Color.Red, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text("BANNED", fontSize = 8.sp, color = Color.White)
                                            }
                                        }
                                    }
                                    Text("UID: ${u.id} • ${u.email}", fontSize = 11.sp, color = Color.Gray)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(onClick = { showEditWalletDialog = u }) {
                                        Icon(Icons.Default.Star, contentDescription = "Edit Ledger", tint = Color(0xFF10B981))
                                    }
                                    IconButton(onClick = { viewModel.adminBanUnbanUser(u.id, !u.isBanned) }) {
                                        Icon(
                                            imageVector = if (u.isBanned) Icons.Default.Check else Icons.Default.Lock,
                                            contentDescription = "Ban Status Toggle",
                                            tint = if (u.isBanned) Color.Green else Color.Red
                                        )
                                    }
                                    IconButton(onClick = { viewModel.adminDeleteUser(u.username, u.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete account record", tint = Color.Red)
                                    }
                                }
                            }

                            Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Main Wallet: Rs.${u.mainWallet}", fontSize = 11.sp, color = Color.White.copy(0.7f))
                                Text("Winnings: Rs.${u.winningWallet}", fontSize = 11.sp, color = Color.White.copy(0.7f))
                                Text("Bonus: Rs.${u.bonusWallet}", fontSize = 11.sp, color = Color.White.copy(0.7f))
                                Text("Coins: ${u.coins}", fontSize = 11.sp, color = Color.White.copy(0.7f))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditWalletDialog != null) {
        val user = showEditWalletDialog!!
        var mainW by remember { mutableStateOf(user.mainWallet.toString()) }
        var winW by remember { mutableStateOf(user.winningWallet.toString()) }
        var bonusW by remember { mutableStateOf(user.bonusWallet.toString()) }
        var coinsVal by remember { mutableStateOf(user.coins.toString()) }

        AlertDialog(
            onDismissRequest = { showEditWalletDialog = null },
            title = { Text("Edit ledger for ${user.username}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = mainW, onValueChange = { mainW = it }, label = { Text("Main Wallet Cash (Rs.)") })
                    OutlinedTextField(value = winW, onValueChange = { winW = it }, label = { Text("Winning Wallet Cash (Rs.)") })
                    OutlinedTextField(value = bonusW, onValueChange = { bonusW = it }, label = { Text("Bonus Wallet Cash (Rs.)") })
                    OutlinedTextField(value = coinsVal, onValueChange = { coinsVal = it }, label = { Text("Coins Balance") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminEditWalletBalance(
                            userId = user.id,
                            main = mainW.toDoubleOrNull() ?: user.mainWallet,
                            winning = winW.toDoubleOrNull() ?: user.winningWallet,
                            bonus = bonusW.toDoubleOrNull() ?: user.bonusWallet,
                            coins = coinsVal.toIntOrNull() ?: user.coins
                        )
                        showEditWalletDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                ) {
                    Text("OVERRIDE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditWalletDialog = null }) { Text("CANCEL") }
            }
        )
    }
}


// ═══════════════════════════════════════
//   SUB PANELS: TOURNAMENT MANAGEMENT
// ═══════════════════════════════════════
@Composable
fun AdminTournamentsSubPanel(viewModel: AppViewModel) {
    val tourneysList by viewModel.tournaments.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Tournament Schedules", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(4.dp))
                Text("CREATE", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (tourneysList.isEmpty()) {
            Text("No active tournament lobbies available.", color = Color.Gray)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tourneysList) { t ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(t.name, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("${t.type} • Fee: Rs.${t.entryFee} • Status: ${t.status}", fontSize = 11.sp, color = Color.Gray)
                                }

                                Row {
                                    IconButton(onClick = { viewModel.adminDeleteTournament(t) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Tournament", tint = Color.Red)
                                    }
                                }
                            }

                            Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 4.dp))

                            // Display room id passcode credentials & editing controls
                            Text("Room ID: ${t.roomId.ifEmpty { "None" }} | Pwd: ${t.roomPassword.ifEmpty { "None" }} | Mode: ${t.visibilityMode}", fontSize = 11.sp, color = Color(0xFFF59E0B))
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("Clash Squad") }
        var entryFee by remember { mutableStateOf("") }
        var prizePool by remember { mutableStateOf("") }
        var slots by remember { mutableStateOf("20") }
        var date by remember { mutableStateOf(viewModel.getCurrentDateString()) }
        var time by remember { mutableStateOf("18:00") }
        var mapType by remember { mutableStateOf("Bermuda") }
        var roomId by remember { mutableStateOf("") }
        var roomPwd by remember { mutableStateOf("") }
        var visMode by remember { mutableStateOf("Scheduled") } // Permanent vs Scheduled

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Assemble New Match Lobby", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Lobby Name") })
                    OutlinedTextField(value = entryFee, onValueChange = { entryFee = it }, label = { Text("Entry Fee (Rs.)") })
                    OutlinedTextField(value = prizePool, onValueChange = { prizePool = it }, label = { Text("Prize Pool (Rs.)") })
                    OutlinedTextField(value = slots, onValueChange = { slots = it }, label = { Text("Total Slots size") })
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Match Date (yyyy-MM-dd)") })
                    OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Match Start Time (HH:mm)") })
                    OutlinedTextField(value = mapType, onValueChange = { mapType = it }, label = { Text("Map Location Name") })
                    OutlinedTextField(value = roomId, onValueChange = { roomId = it }, label = { Text("Lobby ID (Room ID)") })
                    OutlinedTextField(value = roomPwd, onValueChange = { roomPwd = it }, label = { Text("Lobby Entry Password") })
                    OutlinedTextField(value = visMode, onValueChange = { visMode = it }, label = { Text("Visibility: 'Permanent' or 'Scheduled'") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newT = Tournament(
                            name = name.ifEmpty { "BGMI Elite Cup" },
                            type = type,
                            entryFee = entryFee.toDoubleOrNull() ?: 10.0,
                            prizePool = prizePool.toDoubleOrNull() ?: 100.0,
                            totalSlots = slots.toIntOrNull() ?: 20,
                            filledSlots = 0,
                            date = date,
                            time = time,
                            status = "Open",
                            mapType = mapType,
                            roomId = roomId,
                            roomPassword = roomPwd,
                            visibilityMode = visMode,
                            description = "Admin assembled tournament for Anu Battle league."
                        )
                        viewModel.adminCreateTournament(newT)
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                ) {
                    Text("SAVE TO LIVE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("CANCEL") }
            }
        )
    }
}


// ═══════════════════════════════════════
//   SUB PANELS: DEPOSIT APPROVALS
// ═══════════════════════════════════════
@Composable
fun AdminDepositsSubPanel(viewModel: AppViewModel) {
    val depositsList by viewModel.allDeposits.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Main Balance Cash Deposits Sheets", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
        Spacer(modifier = Modifier.height(12.dp))

        if (depositsList.isEmpty()) {
            Text("No user deposit requests recorded.", color = Color.Gray)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(depositsList) { d ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("User ID: ${d.userId} (${d.username})", fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Amount: Rs.${d.amount} via ${d.paymentMethod}", fontSize = 11.sp, color = Color.Gray)
                                    Text("Tx Code: ${d.transactionId}", fontSize = 11.sp, color = Color(0xFFF59E0B))
                                }

                                Box(
                                    modifier = Modifier
                                        .background(
                                            when (d.status) {
                                                "Approved" -> Color(0xFF10B981)
                                                "Rejected" -> Color(0xFFEF4444)
                                                else -> Color(0xFFF59E0B)
                                            },
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(d.status.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }

                            if (d.status == "Pending") {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.adminApproveDeposit(d.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("APPROVE", fontWeight = FontWeight.Black)
                                    }
                                    Button(
                                        onClick = { viewModel.adminRejectDeposit(d.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("REJECT", fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ═══════════════════════════════════════
//   SUB PANELS: WITHDRAWAL APPROVALS
// ═══════════════════════════════════════
@Composable
fun AdminWithdrawSubPanel(viewModel: AppViewModel) {
    val withdrawalsList by viewModel.allWithdrawals.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Winnings Cash Payout Disbursements", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
        Spacer(modifier = Modifier.height(12.dp))

        if (withdrawalsList.isEmpty()) {
            Text("No user withdrawal sheets registered.", color = Color.Gray)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(withdrawalsList) { w ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Holder: ${w.accountName}", fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("UserId: ${w.userId} (${w.username}) • Rs.${w.amount}", fontSize = 11.sp, color = Color.Gray)
                                    Text("Channel: ${w.paymentMethod} | Acc: ${w.accountNumber}", fontSize = 11.sp, color = Color(0xFF3B82F6))
                                }

                                Box(
                                    modifier = Modifier
                                        .background(
                                            when (w.status) {
                                                "Approved" -> Color(0xFF10B981)
                                                "Rejected" -> Color(0xFFEF4444)
                                                else -> Color(0xFFF59E0B)
                                            },
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(w.status.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }

                            if (w.status == "Pending") {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.adminApproveWithdrawal(w.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("WIRE DISBURSE", fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { viewModel.adminRejectWithdrawal(w.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("REJECT & REFUND", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ═══════════════════════════════════════
//   SUB PANELS: BANNER CONFIGURES
// ═══════════════════════════════════════
@Composable
fun AdminBannersSubPanel(viewModel: AppViewModel) {
    val bannersList by viewModel.banners.collectAsState()
    var showAddBannerDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Active Slides Banners", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
            Button(
                onClick = { showAddBannerDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(4.dp))
                Text("ADD SLIDE", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (bannersList.isEmpty()) {
            Text("No sliding banners found on Database.", color = Color.Gray)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(bannersList) { b ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(b.title, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(b.description, fontSize = 11.sp, color = Color.Gray)
                            }
                            IconButton(onClick = { viewModel.adminDeleteBanner(b) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddBannerDialog) {
        var bannerTitle by remember { mutableStateOf("") }
        var bannerDesc by remember { mutableStateOf("") }
        var selectedUri by remember { mutableStateOf<Uri?>(null) }
        var uploadedUrl by remember { mutableStateOf<String?>(null) }
        var isUploading by remember { mutableStateOf(false) }
        val context = LocalContext.current

        val galleryLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                selectedUri = uri
                ImageUploader.uploadToImgBB(context, uri, onProgress = { isUploading = it }) { url ->
                    if (url != null) {
                        uploadedUrl = url
                    }
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showAddBannerDialog = false },
            title = { Text("Assemble Sliding Banner", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = bannerTitle, 
                        onValueChange = { bannerTitle = it }, 
                        label = { Text("Banner Title text") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = bannerDesc, 
                        onValueChange = { bannerDesc = it }, 
                        label = { Text("Description text") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add image", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Banner Image", color = Color.White)
                    }

                    if (isUploading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            CircularProgressIndicator(color = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Uploading banner image...", fontSize = 11.sp, color = Color.White)
                        }
                    }

                    if (uploadedUrl != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Done", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Banner image uploaded successfully!", fontSize = 11.sp, color = Color(0xFF10B981))
                        }
                        
                        AsyncImage(
                            model = uploadedUrl,
                            contentDescription = "Banner preview",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.DarkGray)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalUrl = uploadedUrl ?: "radial_bg"
                        viewModel.adminCreateBanner(BannerBanner(title = bannerTitle, description = bannerDesc, imageUrl = finalUrl))
                        showAddBannerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                ) {
                    Text("ADD", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBannerDialog = false }) { Text("CANCEL") }
            }
        )
    }
}


// ═══════════════════════════════════════
//   SUB PANELS: BROADCAST ANNOUNCEMENTS
// ═══════════════════════════════════════
@Composable
fun AdminBroadcastSubPanel(viewModel: AppViewModel) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Broadcast System Announcements", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
        Text("Construct a server alert broadcast globally visible on every player dashboard notifications panel tray.", fontSize = 11.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Announcement Title") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFF59E0B)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Detailed Alert Message") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFF59E0B)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            maxLines = 5
        )

        Button(
            onClick = {
                if (title.isBlank() || message.isBlank()) {
                    viewModel.triggerToast("Enter alert content completely!")
                } else {
                    viewModel.adminPostNotification(title, message)
                    title = ""
                    message = ""
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("PUSH BROADCAST NOW", fontWeight = FontWeight.Black, color = Color.Black)
        }
    }
}
