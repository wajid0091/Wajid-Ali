package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.data.models.*
import com.example.ui.AppViewModel
import com.example.ui.HomeTab
import com.example.ui.Screen
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import com.example.data.ImageUploader
import coil.compose.AsyncImage

@Composable
fun DashboardScreen(viewModel: AppViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showNotificationsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            DashboardHeader(
                currentUser = currentUser,
                onNotificationClick = { showNotificationsDialog = true },
                onSettingClick = { viewModel.toggleTheme() },
                onAdminClick = { viewModel.navigateTo(Screen.AdminLogin) }
            )
        },
        bottomBar = {
            DashboardBottomBar(
                currentTab = currentTab,
                onTabSelect = { viewModel.navigateToTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    if (isDark) {
                        Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF020617)))
                    } else {
                        Brush.verticalGradient(listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0)))
                    }
                )
        ) {
            when (currentTab) {
                HomeTab.Home -> HomeScreenContent(viewModel)
                HomeTab.Games -> GamesScreenContent(viewModel)
                HomeTab.Store -> StoreScreenContent(viewModel)
                HomeTab.Rewards -> RewardsScreenContent(viewModel)
                HomeTab.Profile -> ProfileScreenContent(viewModel)
            }
        }
    }

    if (showNotificationsDialog) {
        val notificationsList by viewModel.notifications.collectAsState()
        NotificationTray(
            notifications = notificationsList,
            onDismiss = { showNotificationsDialog = false },
            onClearClick = {
                // simulated clear trigger
            }
        )
    }
}

@Composable
fun DashboardHeader(
    currentUser: User?,
    onNotificationClick: () -> Unit,
    onSettingClick: () -> Unit,
    onAdminClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onAdminClick() }
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = currentUser?.username?.take(1)?.uppercase() ?: "U"
                    Text(
                        text = initial,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currentUser?.username ?: "Guest Gamer",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 120.dp)
                        )
                        if (currentUser?.isVerified == true) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified pro badge",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = "UID: ${currentUser?.id?.toString()?.padStart(6, '0') ?: "000000"}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val cashBal = (currentUser?.mainWallet ?: 0.0) + (currentUser?.winningWallet ?: 0.0) + (currentUser?.bonusWallet ?: 0.0)
                Row(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Wallet Balance",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Rs.${String.format("%.1f", cashBal)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Coins Balance",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${currentUser?.coins ?: 0}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications list",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onSettingClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Toggle Light/Dark Theme",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardBottomBar(currentTab: HomeTab, onTabSelect: (HomeTab) -> Unit) {
    NavigationBar(
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple(HomeTab.Home, "Home", Icons.Default.Home),
            Triple(HomeTab.Games, "Games", Icons.Default.PlayArrow),
            Triple(HomeTab.Store, "Store", Icons.Default.ShoppingCart),
            Triple(HomeTab.Rewards, "Rewards", Icons.Default.Star),
            Triple(HomeTab.Profile, "Profile", Icons.Default.Person)
        )

        items.forEach { (tab, label, icon) ->
            NavigationBarItem(
                selected = currentTab == tab,
                onClick = { onTabSelect(tab) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            )
        }
    }
}

@Composable
fun HomeScreenContent(viewModel: AppViewModel) {
    val tournamentsList by viewModel.tournaments.collectAsState()
    val bannersList by viewModel.banners.collectAsState()
    val joinedMatches by viewModel.userJoinedTournaments.collectAsState()

    var activeHomeSubTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        HomeScreenBannerSlider(banners = bannersList)

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Game Venues",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        CategoriesRow(onCategorySelect = { cat ->
            viewModel.navigateToTab(HomeTab.Games)
        })

        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    Button(
                        onClick = { activeHomeSubTab = 0 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeHomeSubTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (activeHomeSubTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Featured Arena", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { activeHomeSubTab = 1 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeHomeSubTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (activeHomeSubTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("My Tournaments", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (activeHomeSubTab == 0) {
            val featured = tournamentsList.filter { it.status == "Open" || it.status == "Upcoming" || it.status == "Live" }
            if (featured.isEmpty()) {
                EmptyStateCard(msg = "No featured open lobbies available. Admins creating matches soon!")
            } else {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    featured.forEach { t ->
                        TournamentCard(tournament = t, onClick = {
                            viewModel.navigateTo(Screen.TournamentDetails(t.id))
                        })
                    }
                }
            }
        } else {
            if (joinedMatches.isEmpty()) {
                EmptyStateCard(msg = "You have not joined any tournaments yet! Find live lobbies in featured matches.")
            } else {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    joinedMatches.forEach { jt ->
                        val currentMatch = tournamentsList.find { it.id == jt.tournamentId }
                        if (currentMatch != null) {
                            TournamentCard(
                                tournament = currentMatch,
                                onClick = { viewModel.navigateTo(Screen.TournamentDetails(currentMatch.id)) },
                                registrationTag = "JOINED"
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}


@Composable
fun HomeScreenBannerSlider(banners: List<BannerBanner>) {
    var activePage by remember { mutableStateOf(0) }

    LaunchedEffect(banners) {
        if (banners.isNotEmpty()) {
            while (true) {
                delay(4000)
                activePage = (activePage + 1) % banners.size
            }
        }
    }

    if (banners.isNotEmpty()) {
        val banner = banners.getOrNull(activePage)
        if (banner != null) {
            val gradientBanner = Brush.horizontalGradient(
                colors = listOf(MaterialTheme.colorScheme.primary, Color(0xFF00497D))
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(150.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (banner.imageUrl.startsWith("http")) {
                        AsyncImage(
                            model = banner.imageUrl,
                            contentDescription = null,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Add a dark overlay so text remains readable
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(gradientBanner)
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(220.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = banner.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = banner.description,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 4.dp, end = 4.dp)
                    ) {
                        banners.forEachIndexed { idx, _ ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .size(if (idx == activePage) 12.dp else 6.dp, 6.dp)
                                    .clip(CircleShape)
                                    .background(if (idx == activePage) Color.White else Color.White.copy(alpha = 0.4f))
                            )
                        }
                    }
                }
                }
            }
        }
    } else {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(150.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, Color(0xFF00497D))))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Anu Battle Elite Arena", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun CategoriesRow(onCategorySelect: (String) -> Unit) {
    val items = listOf(
        Pair("Clash Squad", Icons.Default.Star),
        Pair("Battle Royale", Icons.Default.PlayArrow),
        Pair("Lone Wolf", Icons.Default.Person),
        Pair("Custom Pool", Icons.Default.List)
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items) { pair ->
            val name = pair.first
            val icon = pair.second
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onCategorySelect(name) },
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = name, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun GamesScreenContent(viewModel: AppViewModel) {
    val tournamentsList by viewModel.tournaments.collectAsState()
    var queryStr by remember { mutableStateOf("") }
    var filterCategory by remember { mutableStateOf("All") }
    var filterStatus by remember { mutableStateOf("All") }

    val statuses = listOf("All", "Open", "Upcoming", "Live", "Completed")
    val categories = listOf("All", "Clash Squad", "Battle Royale", "Lone Wolf", "Custom Tournament")

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = queryStr,
            onValueChange = { queryStr = it },
            placeholder = { Text("Search tourneys...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = true
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            items(categories) { cat ->
                val selected = filterCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            if (selected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(
                                alpha = 0.5f
                            ),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { filterCategory = cat }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            items(statuses) { status ->
                val selected = filterStatus == status
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            if (selected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(
                                alpha = 0.5f
                            ),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { filterStatus = status }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        val filteredList = tournamentsList.filter { t ->
            val matchQuery = t.name.contains(queryStr, ignoreCase = true) || t.description.contains(queryStr, ignoreCase = true)
            val matchCat = filterCategory == "All" || t.type == filterCategory
            val matchStatus = filterStatus == "All" || t.status == filterStatus
            matchQuery && matchCat && matchStatus
        }

        if (filteredList.isEmpty()) {
            EmptyStateCard(msg = "No match lobbies fit this selection criteria. Reset parameters to re-search.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredList) { t ->
                    TournamentCard(tournament = t, onClick = {
                        viewModel.navigateTo(Screen.TournamentDetails(t.id))
                    })
                }
            }
        }
    }
}

@Composable
fun StoreScreenContent(viewModel: AppViewModel) {
    val packagesList by viewModel.coinPackages.collectAsState()
    val userDeposits by viewModel.userDeposits.collectAsState()
    val userWithdrawals by viewModel.userWithdrawals.collectAsState()

    var showDepositRequestDialog by remember { mutableStateOf(false) }
    var showWithdrawRequestDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Wallet Operations",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val u = viewModel.currentUser.value
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Pocket Money Balance", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text("Rs.${u?.mainWallet ?: 0.0}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Winnings (Withdrawable)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text("Rs.${u?.winningWallet ?: 0.0}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Bonus Wallet: Rs.${u?.bonusWallet ?: 0.0}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
                    Text("Reserve: 0.0", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { showDepositRequestDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Deposit Cash", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showWithdrawRequestDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Withdraw", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Buy Coins (Voucher Packages)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        packagesList.forEach { pkg ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(pkg.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(pkg.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                "Yields ${pkg.coins} Coins" + if (pkg.bonusCoins > 0) " (+${pkg.bonusCoins} Bonus)" else "",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.purchaseCoinsPackage(pkg) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Rs.${pkg.price}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.Black)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- COINS TO PKR EXCHANGE CONVERTER PANEL ---
        val exchangeRateStr = viewModel.getSettingValue("coins_per_pkr", "10")
        val exchangeRate = exchangeRateStr.toDoubleOrNull() ?: 10.0

        var coinsInput by remember { mutableStateOf("") }
        val coinsToConvert = coinsInput.toIntOrNull() ?: 0
        val pkrCalculated = if (exchangeRate > 0) coinsToConvert / exchangeRate else 0.0

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Convert Coins to PKR Wallet",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Current Rate: $exchangeRate Coins = Rs.1.0 PKR. Convert your earnings instantly into real pocket cash!",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = coinsInput,
                        onValueChange = { coinsInput = it },
                        label = { Text("Enter Coins") },
                        placeholder = { Text("e.g. 100") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            viewModel.convertCoinsToPkr(coinsToConvert)
                            coinsInput = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("EXCHANGE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                if (coinsToConvert > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You will receive: Rs.${String.format(java.util.Locale.US, "%.2f", pkrCalculated)} in Main Wallet balance",
                        color = Color(0xFF10B981),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (userDeposits.isNotEmpty() || userWithdrawals.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "My Transfer Requests Tracker",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column {
                userDeposits.take(3).forEach { dep ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Dep: Rs.${dep.amount} (${dep.paymentMethod})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text = dep.status,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (dep.status) {
                                "Approved" -> Color(0xFF10B981)
                                "Rejected" -> Color(0xFFEF4444)
                                else -> Color(0xFFF59E0B)
                            }
                        )
                    }
                }
                userWithdrawals.take(3).forEach { wd ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Wd: Rs.${wd.amount} (${wd.paymentMethod})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text = wd.status,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (wd.status) {
                                "Approved" -> Color(0xFF10B981)
                                "Rejected" -> Color(0xFFEF4444)
                                else -> Color(0xFFF59E0B)
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }

    if (showDepositRequestDialog) {
        DepositRequestModal(
            onDismiss = { showDepositRequestDialog = false },
            onSubmit = { amount, method, txId, screenshotUrl ->
                viewModel.submitDepositRequest(amount, method, txId, screenshotUrl)
                showDepositRequestDialog = false
            }
        )
    }

    if (showWithdrawRequestDialog) {
        WithdrawalRequestModal(
            onDismiss = { showWithdrawRequestDialog = false },
            onSubmit = { amount, accName, accNum, method ->
                viewModel.submitWithdrawRequest(amount, accName, accNum, method)
                showWithdrawRequestDialog = false
            }
        )
    }
}

@Composable
fun RewardsScreenContent(viewModel: AppViewModel) {
    val dailyTasks by viewModel.userDailyTasks.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Daily Rewards Arena",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Claim consecutive daily bonuses. Resets if streak lapses.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        val rewardsList = listOf(5, 5, 5, 10, 5, 5, 15)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            rewardsList.forEachIndexed { index, coinsAmt ->
                val day = index + 1
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (day == 7) Color(0xFFD97706) else MaterialTheme.colorScheme.surface
                        )
                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .clickable { viewModel.claimDailyReward(day, coinsAmt) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Day $day",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (day == 7) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (day == 7) Color.White else Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "$coinsAmt Coins",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (day == 7) Color.White else Color(0xFFF59E0B)
                        )
                    }
                }
            }
        }

        // --- DIRECT WATCH AD AND EARN CAMPAIGN ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Watch Unity Ads & Earn",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFF59E0B)
                    )
                    Text(
                        "Watch a 10s video ad to instantly receive +2 Coins directly inside your wallet!",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val activity = context as? com.example.MainActivity
                        if (activity != null) {
                            val rewardedId = viewModel.getSettingValue("unity_rewarded_id", "Rewarded_Android")
                            activity.showRewardedAd(rewardedId) {
                                val cUser = viewModel.currentUser.value
                                if (cUser != null) {
                                    viewModel.incrementTaskProgress(cUser.id, "WATCH_AD_5", 1)
                                    viewModel.creditDirectCoins(2, "Unity Video Ad Reward")
                                }
                            }
                        } else {
                            viewModel.triggerToast("Cannot show ad context")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play ad icon", tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PLAY (+2)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Daily Battle Assignments",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Fulfill these requests inside gameplay matches to claim vouchers.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (dailyTasks.isEmpty()) {
            EmptyStateCard(msg = "Initializing dynamic daily task registries for profile...")
        } else {
            dailyTasks.forEach { task ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Reward: ${task.rewardCoins} Coins",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFF59E0B)
                                )
                            }

                            if (task.isClaimed) {
                                Button(
                                    onClick = {},
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                                    enabled = false,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Claimed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                }
                            } else if (task.isCompleted) {
                                Button(
                                    onClick = { viewModel.claimTaskReward(task) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Claim Reward", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                if (task.taskType == "WATCH_AD_5") {
                                    Button(
                                        onClick = {
                                            viewModel.playSimulatedAd {
                                                val cUser = viewModel.currentUser.value
                                                if (cUser != null) {
                                                    viewModel.incrementTaskProgress(cUser.id, "WATCH_AD_5", 1)
                                                    viewModel.creditDirectCoins(2, "Unity Video Ad Reward")
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Black)
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("Play (${task.currentCount}/${task.targetCount})", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                } else {
                                    Text(
                                        text = "${task.currentCount}/${task.targetCount}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        val progressRatio = if (task.targetCount > 0) task.currentCount.toFloat() / task.targetCount else 0f
                        LinearProgressIndicator(
                            progress = progressRatio,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFFF59E0B),
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        var showSimulationSuite by remember { mutableStateOf(false) }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { showSimulationSuite = !showSimulationSuite },
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFF3B82F6))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Task simulation panel (Offline Tests)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    Icon(
                        imageVector = if (showSimulationSuite) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                if (showSimulationSuite) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Increments gaming values or friend refer counters locally so you can click Claim on rewards files without actual gameplay.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(0.8f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.simulateGamePlayMinutes(5) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Play 5 min", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.simulateGamePlayMinutes(15) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Play 15 min", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.incrementTaskProgress(viewModel.currentUser.value?.id ?: 0, "WIN_TOURNAMENT") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Simulate Win", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun ProfileScreenContent(viewModel: AppViewModel) {
    val u = viewModel.currentUser.value
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showReferDetailsDialog by remember { mutableStateOf(false) }
    var showMatchHistoryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFEF4444)))),
                    contentAlignment = Alignment.Center
                ) {
                    val fallback = u?.username?.take(1)?.uppercase() ?: "P"
                    Text(fallback, fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color.White)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(u?.username ?: "Combat Cadet", fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                    if (u?.isVerified == true) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                    }
                }

                Text(u?.email ?: "esports.user@anubattle.com", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    ProfileStatItem("Matches", "${u?.matchesPlayed ?: 0}")
                    ProfileStatItem("Wins", "${u?.matchesWon ?: 0}")
                    ProfileStatItem("Earnings", "Rs.${u?.totalEarnings ?: 0.0}")
                    ProfileStatItem("Referrals", "${u?.referralCount ?: 0}")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Personal Customizations", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(10.dp))

        ProfileMenuActionItem(Icons.Default.Edit, "Edit Profile Details", "Change account display handles with custom avatars") {
            showEditProfileDialog = true
        }

        ProfileMenuActionItem(Icons.Default.List, "My Match History", "View details about your finished lobby scores") {
            showMatchHistoryDialog = true
        }

        ProfileMenuActionItem(Icons.Default.Share, "Refer & Claim Rewards", "Check unique code options and redeem codes") {
            showReferDetailsDialog = true
        }

        ProfileMenuActionItem(Icons.Default.Phone, "Contact Anu Battle Support", "Lobby disputes and instant deposits helper details") {
            // instant popup trigger message
            viewModel.triggerToast("Support is active via Telegram: @AnuBattleSupport")
        }

        ProfileMenuActionItem(Icons.Default.ExitToApp, "Logout from Arena", "Clear auto registration states", tint = Color(0xFFEF4444)) {
            viewModel.logout()
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    if (showEditProfileDialog) {
        EditProfileModal(
            currentName = u?.username ?: "",
            onDismiss = { showEditProfileDialog = false },
            onSave = { name ->
                viewModel.saveUserProfile(name, "avatar_1")
                showEditProfileDialog = false
            }
        )
    }

    if (showReferDetailsDialog) {
        ReferCodeModal(
            code = u?.referralCode ?: "ANU555",
            onDismiss = { showReferDetailsDialog = false },
            onApply = { code ->
                viewModel.applyReferralCode(code)
            }
        )
    }

    if (showMatchHistoryDialog) {
        MatchHistoryModal(
            viewModel = viewModel,
            onDismiss = { showMatchHistoryDialog = false }
        )
    }
}

@Composable
fun ProfileStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
fun ProfileMenuActionItem(
    icon: ImageVector,
    title: String,
    description: String,
    tint: Color = Color(0xFFF59E0B),
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun TournamentCard(
    tournament: Tournament,
    onClick: () -> Unit,
    registrationTag: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
            ) {
                if (tournament.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = tournament.imageUrl,
                        contentDescription = "Tournament Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Black.copy(alpha = 0.2f), Color.Black.copy(alpha = 0.75f))
                                )
                            )
                    )
                } else {
                    val gradientType = when (tournament.type) {
                        "Clash Squad" -> Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, Color(0xFF00497D)))
                        "Battle Royale" -> Brush.linearGradient(listOf(Color(0xFF00497D), Color(0xFF425E7B)))
                        else -> Brush.linearGradient(listOf(MaterialTheme.colorScheme.secondary, Color(0xFF1B2E3C)))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(gradientType)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .background(
                            when (tournament.status) {
                                "Open" -> MaterialTheme.colorScheme.primary
                                "Full" -> MaterialTheme.colorScheme.secondary
                                "Live" -> Color(0xFFD32F2F)
                                else -> MaterialTheme.colorScheme.outline
                            },
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        tournament.status.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = when (tournament.status) {
                            "Open" -> MaterialTheme.colorScheme.onPrimary
                            "Full" -> MaterialTheme.colorScheme.onSecondary
                            "Live" -> Color.White
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }

                if (registrationTag != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            registrationTag, 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        tournament.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${tournament.type} • ${tournament.format} • ${tournament.mapType}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("PRIZE POOL", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    Text("Rs.${tournament.prizePool}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Column {
                    Text("ENTRY FEE", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (tournament.entryFee > 0) "Rs.${tournament.entryFee}" else "FREE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("SLOTS FILLED", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    Text(
                        "${tournament.filledSlots}/${tournament.totalSlots}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(msg: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(0.6f), modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = msg,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationTray(
    notifications: List<Notification>,
    onDismiss: () -> Unit,
    onClearClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("CLOSE", fontWeight = FontWeight.Bold) }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Notifications", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Box(modifier = Modifier.heightIn(max = 300.dp)) {
                if (notifications.isEmpty()) {
                    Text("No new notifications.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(notifications) { n ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(n.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(n.message, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${n.date} ${n.time}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun DepositRequestModal(onDismiss: () -> Unit, onSubmit: (Double, String, String, String?) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var txId by remember { mutableStateOf("") }
    var scaleMethod by remember { mutableStateOf("UPI / QR Code") }
    
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
        onDismissRequest = onDismiss,
        title = { Text("Submit Deposit Proof", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("QR Code: SCAN UPI AND UPLOAD PROOF.\nEnter deposit details for Admin verification.", fontSize = 12.sp)

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (PKR)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF59E0B)),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = txId,
                    onValueChange = { txId = it },
                    label = { Text("Transaction Code / ID") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF59E0B)),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add image", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Screenshot", color = Color.White)
                }

                if (isUploading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        CircularProgressIndicator(color = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Uploading screenshot...", fontSize = 11.sp)
                    }
                }

                if (uploadedUrl != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Done", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Proof uploaded successfully!", fontSize = 11.sp, color = Color(0xFF10B981))
                    }
                    
                    AsyncImage(
                        model = uploadedUrl,
                        contentDescription = "Selected screenshot preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.DarkGray)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amtVal = amount.toDoubleOrNull() ?: 0.0
                    onSubmit(amtVal, scaleMethod, txId, uploadedUrl)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
            ) {
                Text("SUBMIT PROOF", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
}

@Composable
fun WithdrawalRequestModal(onDismiss: () -> Unit, onSubmit: (Double, String, String, String) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var accName by remember { mutableStateOf("") }
    var accNum by remember { mutableStateOf("") }
    var selectMethod by remember { mutableStateOf("UPI Transfer") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request Cash Out", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Money is disbursed directly to your payout account once approved.", fontSize = 12.sp)

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (PKR)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF59E0B)),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = accName,
                    onValueChange = { accName = it },
                    label = { Text("Account Holder Name") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF59E0B)),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = accNum,
                    onValueChange = { accNum = it },
                    label = { Text("UPI ID or Bank Account No.") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF59E0B)),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amtVal = amount.toDoubleOrNull() ?: 0.0
                    onSubmit(amtVal, accName, accNum, selectMethod)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
            ) {
                Text("SUBMIT REQUEST", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
}

@Composable
fun EditProfileModal(currentName: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Handle", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("New Username") },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF59E0B)),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onSave(newName) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))) {
                Text("SAVE", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
}

@Composable
fun ReferCodeModal(code: String, onDismiss: () -> Unit, onApply: (String) -> Unit) {
    var codeField by remember { mutableStateOf("") }
    val ctx = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Refer & Earn Arena", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Invite friends to Anu Battle! They get 10 PKR on registration and you gain 15 PKR + 20 coins once they set up.", fontSize = 12.sp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("My Invite Voucher Code", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                        Text(code, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFFF59E0B))
                    }
                    IconButton(onClick = {
                        val cb = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cb.setPrimaryClip(ClipData.newPlainText("invite_code", code))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFFF59E0B))
                    }
                }

                Divider()

                Text("Been Referred? Input friend's code here:", fontSize = 12.sp)
                OutlinedTextField(
                    value = codeField,
                    onValueChange = { codeField = it.uppercase() },
                    placeholder = { Text("Input Referral Code") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF59E0B)),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onApply(codeField) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))) {
                Text("APPLY", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
}

@Composable
fun MatchHistoryModal(viewModel: AppViewModel, onDismiss: () -> Unit) {
    val txs by viewModel.userTransactions.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("CLOSE", fontWeight = FontWeight.Bold) }
        },
        title = { Text("My Historic Logs", fontWeight = FontWeight.Bold) },
        text = {
            Box(modifier = Modifier.heightIn(max = 350.dp)) {
                if (txs.isEmpty()) {
                    Text("No transactions or match entries found.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(txs) { tx ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = tx.type,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (tx.type == "Deposit" || tx.type == "Reward Credit" || tx.type == "Daily Reward" || tx.type == "Referral Bonus") {
                                                "+Rs.${tx.amount}"
                                            } else {
                                                "-Rs.${tx.amount}"
                                            },
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (tx.status == "Rejected") Color.Gray else if (tx.type == "Deposit" || tx.type == "Reward Credit" || tx.type == "Daily Reward" || tx.type == "Referral Bonus") Color(0xFF10B981) else Color(0xFFEF4444)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(tx.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Wallet: ${tx.walletType}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                        Text("${tx.date} ${tx.time}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
