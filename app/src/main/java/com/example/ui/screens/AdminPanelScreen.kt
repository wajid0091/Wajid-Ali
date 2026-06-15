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
import com.example.data.models.AppSetting
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
    var selectedTab by remember { mutableStateOf(0) } // 0: Users, 1: Tourneys, 2: Deposits, 3: Withdrawals, 4: Banners, 5: Broadcast, 6: Unity Ads, 7: Task Rewards

    val tabs = listOf("Users", "Tourneys", "Deposits", "Withdraw", "Banners", "Broadcast", "Unity Ads", "Task Rewards")

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
                6 -> AdminUnityAdsSubPanel(viewModel)
                7 -> AdminTaskRewardsSubPanel(viewModel)
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
    var editingTournament by remember { mutableStateOf<Tournament?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Tournament Schedules", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
            Button(
                onClick = { editingTournament = null; showCreateDialog = true },
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
                                    IconButton(onClick = { 
                                        editingTournament = t
                                        showCreateDialog = true 
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Blue)
                                    }
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
        var name by remember { mutableStateOf(editingTournament?.name ?: "") }
        var type by remember { mutableStateOf(editingTournament?.type ?: "Clash Squad") }
        var entryFee by remember { mutableStateOf(if(editingTournament != null && editingTournament?.entryFee!! > 0) editingTournament?.entryFee.toString() else "") }
        var prizePool by remember { mutableStateOf(if(editingTournament != null && editingTournament?.prizePool!! > 0) editingTournament?.prizePool.toString() else "") }
        var slots by remember { mutableStateOf(editingTournament?.totalSlots?.toString() ?: "20") }
        var adsRequired by remember { mutableStateOf(editingTournament?.adsRequired?.toString() ?: "0") }
        var date by remember { mutableStateOf(editingTournament?.date ?: viewModel.getCurrentDateString()) }
        var time by remember { mutableStateOf(editingTournament?.time ?: "18:00") }
        var mapType by remember { mutableStateOf(editingTournament?.mapType ?: "Bermuda") }
        var roomId by remember { mutableStateOf(editingTournament?.roomId ?: "") }
        var roomPwd by remember { mutableStateOf(editingTournament?.roomPassword ?: "") }
        var visMode by remember { mutableStateOf(editingTournament?.visibilityMode ?: "Scheduled") } // Permanent vs Scheduled
        var imageUrl by remember { mutableStateOf(editingTournament?.imageUrl ?: "") }
        var isUploading by remember { mutableStateOf(false) }
        val context = LocalContext.current

        val galleryLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                ImageUploader.uploadToImgBB(context, uri, onProgress = { isUploading = it }) { url ->
                    if (url != null) {
                        imageUrl = url
                    }
                }
            }
        }

        AlertDialog(
            onDismissRequest = { 
                showCreateDialog = false
                editingTournament = null 
            },
            title = { Text(if(editingTournament != null) "Edit Match Lobby" else "Assemble New Match Lobby", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Lobby Name") })
                    
                    Text("Select or upload tournament banner image:", fontSize = 11.sp, color = Color.Gray)
                    Text("Recommended Size: 600x300 for Banners.", fontSize = 10.sp, color = Color(0xFFF59E0B))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = imageUrl, 
                            onValueChange = { imageUrl = it }, 
                            label = { Text("Banner Image URL") },
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("CHOOSE FILE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    if (isUploading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(color = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Uploading to ImgBB...", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    if (imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Preview Banner",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }

                    Text("Match Type Selection:", fontSize = 12.sp, color = Color.White)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { type = "Clash Squad" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (type == "Clash Squad") Color(0xFFF59E0B) else Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) { Text("Clash Squad", color = if (type == "Clash Squad") Color.Black else Color.White, fontSize = 10.sp) }
                        Button(
                            onClick = { type = "Battle Royale" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (type == "Battle Royale") Color(0xFFF59E0B) else Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) { Text("Battle Royale", color = if (type == "Battle Royale") Color.Black else Color.White, fontSize = 10.sp) }
                    }

                    OutlinedTextField(value = entryFee, onValueChange = { entryFee = it }, label = { Text("Entry Fee (Rs.)") })
                    OutlinedTextField(value = prizePool, onValueChange = { prizePool = it }, label = { Text("Prize Pool (Rs.) (Leave empty if none)") })
                    OutlinedTextField(value = slots, onValueChange = { slots = it }, label = { Text("Total Slots size") })
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Match Date (yyyy-MM-dd)") })
                    OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Match Start Time (HH:mm)") })
                    OutlinedTextField(value = mapType, onValueChange = { mapType = it }, label = { Text("Map Location Name") })
                    OutlinedTextField(value = roomId, onValueChange = { roomId = it }, label = { Text("Lobby ID (Room ID)") })
                    OutlinedTextField(value = roomPwd, onValueChange = { roomPwd = it }, label = { Text("Lobby Entry Password") })
                    OutlinedTextField(value = visMode, onValueChange = { visMode = it }, label = { Text("Visibility: 'Permanent' or 'Scheduled'") })
                    OutlinedTextField(value = adsRequired, onValueChange = { adsRequired = it }, label = { Text("Required Ads to Join") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newT = Tournament(
                            id = editingTournament?.id ?: 0,
                            name = name.ifEmpty { "BGMI Elite Cup" },
                            type = type,
                            entryFee = entryFee.toDoubleOrNull() ?: 0.0,
                            prizePool = prizePool.toDoubleOrNull() ?: 0.0,
                            totalSlots = slots.toIntOrNull() ?: 20,
                            filledSlots = editingTournament?.filledSlots ?: 0,
                            date = date,
                            time = time,
                            status = editingTournament?.status ?: "Open",
                            mapType = mapType,
                            roomId = roomId,
                            roomPassword = roomPwd,
                            visibilityMode = visMode,
                            imageUrl = imageUrl,
                            description = editingTournament?.description ?: "Admin assembled tournament for Anu Battle league.",
                            adsRequired = adsRequired.toIntOrNull() ?: 0
                        )
                        if (editingTournament != null) {
                            viewModel.adminUpdateTournament(newT)
                        } else {
                            viewModel.adminCreateTournament(newT)
                        }
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                ) {
                    Text(if(editingTournament != null) "UPDATE LOBBY" else "SAVE TO LIVE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showCreateDialog = false
                    editingTournament = null 
                }) { Text("CANCEL", color = Color.White) }
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
    var showPaymentConfig by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Main Balance Cash Deposits Sheets", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
            IconButton(onClick = { showPaymentConfig = true }) {
                Icon(Icons.Default.Settings, contentDescription = "Payment Settings", tint = Color.White)
            }
        }
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

    if (showPaymentConfig) {
        var epName by remember { mutableStateOf(viewModel.getSettingValue("payment_easypaisa_name", "Ahsan")) }
        var epNum by remember { mutableStateOf(viewModel.getSettingValue("payment_easypaisa_number", "03001234567")) }
        var jcName by remember { mutableStateOf(viewModel.getSettingValue("payment_jazzcash_name", "Anu Battle")) }
        var jcNum by remember { mutableStateOf(viewModel.getSettingValue("payment_jazzcash_number", "03001234568")) }

        AlertDialog(
            onDismissRequest = { showPaymentConfig = false },
            title = { Text("Payment Methods Setup", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("EasyPaisa Settings", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = epName, onValueChange = { epName = it }, label = { Text("Account Name") })
                    OutlinedTextField(value = epNum, onValueChange = { epNum = it }, label = { Text("Account Number") })
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("JazzCash Settings", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = jcName, onValueChange = { jcName = it }, label = { Text("Account Name") })
                    OutlinedTextField(value = jcNum, onValueChange = { jcNum = it }, label = { Text("Account Number") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateSetting("payment_easypaisa_name", epName)
                        viewModel.updateSetting("payment_easypaisa_number", epNum)
                        viewModel.updateSetting("payment_jazzcash_name", jcName)
                        viewModel.updateSetting("payment_jazzcash_number", jcNum)
                        viewModel.triggerToast("Payment Methods Updated!")
                        showPaymentConfig = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                ) { Text("SAVE", color = Color.Black) }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentConfig = false }) { Text("CANCEL", color = Color.White) }
            }
        )
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

                    Text("Recommended Size: 1024x512 px (Horizontal 2:1) for Sliding Promotion images.", fontSize = 11.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)

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

// ═══════════════════════════════════════
//   SUB PANELS: UNITY ADS INTEGRATION
// ═══════════════════════════════════════
@Composable
fun AdminUnityAdsSubPanel(viewModel: AppViewModel) {
    val settingsList by viewModel.allSettings.collectAsState()
    
    // Read current settings
    val currentGameId = settingsList.find { it.key == "unity_game_id" }?.value ?: ""
    val currentAdsEnabled = settingsList.find { it.key == "unity_ads_enabled" }?.value ?: "false"
    val currentTestMode = settingsList.find { it.key == "unity_test_mode" }?.value ?: "true"
    val currentInterstitialId = settingsList.find { it.key == "unity_interstitial_id" }?.value ?: ""
    val currentRewardedId = settingsList.find { it.key == "unity_rewarded_id" }?.value ?: ""
    val currentBannerId = settingsList.find { it.key == "unity_banner_id" }?.value ?: ""
    val currentCoinsPerPkr = settingsList.find { it.key == "coins_per_pkr" }?.value ?: "10"
    val currentEpName = settingsList.find { it.key == "payment_easypaisa_name" }?.value ?: "Ahsan"
    val currentEpNum = settingsList.find { it.key == "payment_easypaisa_number" }?.value ?: "03001234567"
    val currentJcName = settingsList.find { it.key == "payment_jazzcash_name" }?.value ?: "Anu Battle"
    val currentJcNum = settingsList.find { it.key == "payment_jazzcash_number" }?.value ?: "03001234568"

    // Form fields
    var gameId by remember(currentGameId) { mutableStateOf(currentGameId) }
    var adsEnabled by remember(currentAdsEnabled) { mutableStateOf(currentAdsEnabled == "true") }
    var testMode by remember(currentTestMode) { mutableStateOf(currentTestMode == "true") }
    var interstitialId by remember(currentInterstitialId) { mutableStateOf(currentInterstitialId) }
    var rewardedId by remember(currentRewardedId) { mutableStateOf(currentRewardedId) }
    var bannerId by remember(currentBannerId) { mutableStateOf(currentBannerId) }
    var coinsPerPkr by remember(currentCoinsPerPkr) { mutableStateOf(currentCoinsPerPkr) }
    var epName by remember(currentEpName) { mutableStateOf(currentEpName) }
    var epNum by remember(currentEpNum) { mutableStateOf(currentEpNum) }
    var jcName by remember(currentJcName) { mutableStateOf(currentJcName) }
    var jcNum by remember(currentJcNum) { mutableStateOf(currentJcNum) }

    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Unity Ads CRM & Integration", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
        Text(
            "Configure Unity Ads parameters below. These parameters will sync with global players instantly once saved to Firebase.",
            fontSize = 11.sp,
            color = Color.Gray
        )

        HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))

        // Toggle rows
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Show Ads System Globally", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                Text("Toggle to instantly activate/deactivate ads for all screens in the app.", fontSize = 11.sp, color = Color.Gray)
            }
            Switch(
                checked = adsEnabled,
                onCheckedChange = { adsEnabled = it }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Unity Ads Test Mode", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                Text("Enable to receive testing/simulated Unity Ads without standard ad limits.", fontSize = 11.sp, color = Color.Gray)
            }
            Switch(
                checked = testMode,
                onCheckedChange = { testMode = it }
            )
        }

        HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))

        OutlinedTextField(
            value = gameId,
            onValueChange = { gameId = it },
            label = { Text("Unity Game ID (Android)") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFF59E0B)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = interstitialId,
            onValueChange = { interstitialId = it },
            label = { Text("Interstitial Placement ID") },
            placeholder = { Text("e.g. Interstitial_Android") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFF59E0B)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = rewardedId,
            onValueChange = { rewardedId = it },
            label = { Text("Rewarded Placement ID") },
            placeholder = { Text("e.g. Rewarded_Android") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFF59E0B)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = bannerId,
            onValueChange = { bannerId = it },
            label = { Text("Banner Placement ID") },
            placeholder = { Text("e.g. Banner_Android") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFF59E0B)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))

        Text("Exchange Rate configuration (Coins -> PKR)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Sets how many Coins equal 1 PKR in the user wallet conversion panel.", fontSize = 11.sp, color = Color.Gray)

        OutlinedTextField(
            value = coinsPerPkr,
            onValueChange = { coinsPerPkr = it },
            label = { Text("Exchange Rate: Coins per 1 PKR") },
            placeholder = { Text("e.g. 10.0") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFF59E0B)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))

        Text("Payment Account Details Setup", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Customize EasyPaisa and JazzCash numbers and titles displayed on user deposit screens.", fontSize = 11.sp, color = Color.Gray)

        OutlinedTextField(
            value = epName,
            onValueChange = { epName = it },
            label = { Text("EasyPaisa Account Title") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFF59E0B)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = epNum,
            onValueChange = { epNum = it },
            label = { Text("EasyPaisa Account Phone Number") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFF59E0B)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = jcName,
            onValueChange = { jcName = it },
            label = { Text("JazzCash Account Title") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFF59E0B)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = jcNum,
            onValueChange = { jcNum = it },
            label = { Text("JazzCash Account Phone Number") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFF59E0B)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                isSaving = true
                var count = 0
                val total = 11
                
                fun checkComplete() {
                    count++
                    if (count == total) {
                        isSaving = false
                        viewModel.triggerToast("Configuration updated inside Live Server successfully!")
                    }
                }

                viewModel.updateSetting("unity_game_id", gameId) { checkComplete() }
                viewModel.updateSetting("unity_ads_enabled", adsEnabled.toString()) { checkComplete() }
                viewModel.updateSetting("unity_test_mode", testMode.toString()) { checkComplete() }
                viewModel.updateSetting("unity_interstitial_id", interstitialId) { checkComplete() }
                viewModel.updateSetting("unity_rewarded_id", rewardedId) { checkComplete() }
                viewModel.updateSetting("unity_banner_id", bannerId) { checkComplete() }
                viewModel.updateSetting("coins_per_pkr", coinsPerPkr) { checkComplete() }
                viewModel.updateSetting("payment_easypaisa_name", epName) { checkComplete() }
                viewModel.updateSetting("payment_easypaisa_number", epNum) { checkComplete() }
                viewModel.updateSetting("payment_jazzcash_name", jcName) { checkComplete() }
                viewModel.updateSetting("payment_jazzcash_number", jcNum) { checkComplete() }
            },
            enabled = !isSaving,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
            } else {
                Text("SAVE & UPDATE CONFIGS", fontWeight = FontWeight.Black, color = Color.Black)
            }
        }
    }
}

@Composable
fun AdminTaskRewardsSubPanel(viewModel: AppViewModel) {
    val templates by viewModel.taskTemplates.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingTemplate by remember { mutableStateOf<Map<String, Any>?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Dynamic Task Rewards Templates", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
            Button(
                onClick = { 
                    editingTemplate = null
                    showCreateDialog = true 
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task", tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ADD TASK", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (templates.isEmpty()) {
            Text("No task templates found.", color = Color.Gray)
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(templates) { t ->
                    val typeId = t["id"] as? String ?: ""
                    val title = t["title"] as? String ?: ""
                    val targetCount = (t["targetCount"] as? Number)?.toInt() ?: 0
                    val rewardCoins = (t["rewardCoins"] as? Number)?.toInt() ?: 0

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text("ID: $typeId • Reward: $rewardCoins Coins • Goal: $targetCount", fontSize = 11.sp, color = Color.Gray)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = {
                                    editingTemplate = t
                                    showCreateDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Task", tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = {
                                    viewModel.adminDeleteTaskTemplate(typeId)
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Task", tint = Color.Red, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var tid by remember { mutableStateOf(editingTemplate?.get("id") as? String ?: "") }
        var title by remember { mutableStateOf(editingTemplate?.get("title") as? String ?: "") }
        var targetCount by remember { mutableStateOf(editingTemplate?.get("targetCount")?.toString() ?: "5") }
        var rewardCoins by remember { mutableStateOf(editingTemplate?.get("rewardCoins")?.toString() ?: "10") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(if (editingTemplate != null) "Edit Task Template" else "Create Task Template", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = tid,
                        onValueChange = { if (editingTemplate == null) tid = it },
                        label = { Text("Task ID / Type (e.g. WATCH_AD_10)") },
                        enabled = editingTemplate == null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Title / Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = targetCount,
                        onValueChange = { targetCount = it },
                        label = { Text("Target Goal Count") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = rewardCoins,
                        onValueChange = { rewardCoins = it },
                        label = { Text("Reward Coins Value") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tid.isEmpty() || title.isEmpty()) {
                            viewModel.triggerToast("Please fill all required inputs!")
                            return@Button
                        }
                        val tGoal = targetCount.toIntOrNull() ?: 1
                        val rCoins = rewardCoins.toIntOrNull() ?: 0
                        if (editingTemplate != null) {
                            viewModel.adminUpdateTaskTemplate(tid, title, tGoal, rCoins)
                        } else {
                            viewModel.adminCreateTaskTemplate(tid, title, tGoal, rCoins)
                        }
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                ) {
                    Text("SAVE TEMPLATE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(onClick = { showCreateDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}
