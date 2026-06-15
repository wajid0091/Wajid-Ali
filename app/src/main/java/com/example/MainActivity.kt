package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.ui.AppViewModel
import com.example.ui.Screen
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions

class MainActivity : ComponentActivity(), IUnityAdsInitializationListener {

    private var unityGameId = "2840915"
    private var isAdReady = false
    private var testMode = false
    
    // Callbacks
    private var pendingRewardCallback: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel = ViewModelProvider(this)[AppViewModel::class.java]
        
        // Fetch values
        lifecycleScope.launch {
            viewModel.allSettings.collect { settingsList ->
                val gameId = settingsList.find { it.key == "unity_game_id" }?.value ?: "2840915"
                if (!UnityAds.isInitialized && gameId.isNotEmpty()) {
                    unityGameId = gameId
                    UnityAds.initialize(applicationContext, unityGameId, testMode, this@MainActivity)
                }
            }
        }

        setContent {
            val isDark by viewModel.isDarkMode.collectAsState()
            val navStack by viewModel.navigationStack.collectAsState()
            val isShowingAd by viewModel.isShowingAd.collectAsState()

            MyApplicationTheme(darkTheme = isDark, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
                        val currentScreen = navStack.lastOrNull() ?: Screen.Login

                        // --- Dynamic Screen Routing ---
                        when (val screen = currentScreen) {
                            is Screen.Login -> LoginScreen(viewModel)
                            is Screen.Register -> RegisterScreen(viewModel)
                            is Screen.Dashboard -> DashboardScreen(viewModel)
                            is Screen.TournamentDetails -> TournamentDetailsScreen(viewModel, screen.tournamentId)
                            is Screen.AdminLogin -> AdminPanelScreen(viewModel)
                            else -> LoginScreen(viewModel)
                        }

                        // --- FULL-SCREEN AD PLAYER OVERLAY ---
                        if (isShowingAd) {
                            SimulatedAdPlayerOverlay(viewModel)
                        }
                    }

                    // --- Toast Trigger Hook ---
                    val context = LocalContext.current
                    LaunchedEffect(Unit) {
                        var activeToast: Toast? = null
                        viewModel.toastMessage.collect { msg ->
                            activeToast?.cancel()
                            activeToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT).apply {
                                show()
                            }
                        }
                    }

                    // --- Back Pressed Interceptor ---
                    BackHandler(enabled = navStack.size > 1) {
                        viewModel.navigateBack()
                    }
                }
            }
        }
    }
    override fun onInitializationComplete() {
        isAdReady = true
    }

    override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError?, message: String?) {
        Toast.makeText(this, "Unity Ads Failed: $message", Toast.LENGTH_SHORT).show()
    }

    fun showRewardedAd(adUnitId: String, onReward: () -> Unit) {
        if (!isAdReady || !UnityAds.isInitialized) {
            Toast.makeText(this, "Ad is not ready yet, loading...", Toast.LENGTH_SHORT).show()
            return
        }
        pendingRewardCallback = onReward

        val showListener = object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(placementId: String, error: UnityAds.UnityAdsShowError, message: String) {
                Toast.makeText(this@MainActivity, "Failed to show ad: $message", Toast.LENGTH_SHORT).show()
            }

            override fun onUnityAdsShowStart(placementId: String) {}
            override fun onUnityAdsShowClick(placementId: String) {}
            override fun onUnityAdsShowComplete(placementId: String, state: UnityAds.UnityAdsShowCompletionState) {
                if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                    pendingRewardCallback?.invoke()
                    Toast.makeText(this@MainActivity, "Reward Granted!", Toast.LENGTH_SHORT).show()
                } else if (state == UnityAds.UnityAdsShowCompletionState.SKIPPED) {
                    Toast.makeText(this@MainActivity, "Ad skipped, no reward.", Toast.LENGTH_SHORT).show()
                }
                pendingRewardCallback = null
            }
        }
        UnityAds.show(this, adUnitId, UnityAdsShowOptions(), showListener)
    }
}

@Composable
fun SimulatedAdPlayerOverlay(viewModel: AppViewModel) {
    val progressSeconds by viewModel.adProgressSeconds.collectAsState()
    val settingsList by viewModel.allSettings.collectAsState()
    val gameId = settingsList.find { it.key == "unity_game_id" }?.value ?: "2840915"
    val rewardedId = settingsList.find { it.key == "unity_rewarded_id" }?.value ?: "Rewarded_Android"

    androidx.compose.ui.window.Dialog(
        onDismissRequest = { /* Lock dismissal until ad completion */ },
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        androidx.compose.material3.Text(
                            text = "Sponsor Video Ad",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                        androidx.compose.material3.Text(
                            text = "GameID: $gameId • Target: $rewardedId",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        androidx.compose.material3.Text(
                            text = if (progressSeconds > 0) "Seconds left: ${progressSeconds}s" else "Granting Reward...",
                            color = Color(0xFFF59E0B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    listOf(
                                        Color(0xFFD97706),
                                        Color(0xFF6B21A8)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            androidx.compose.material3.Text(
                                "ANU BATTLE ARENA",
                                color = Color.White,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            androidx.compose.material3.Text(
                                "Streaming High Quality Video ad...",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            androidx.compose.material3.CircularProgressIndicator(color = Color(0xFFF59E0B))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    androidx.compose.material3.Text(
                        "Anu Battle rewards are brought to you by brand sponsors. Please do not close until the countdown finishes.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                androidx.compose.material3.Text(
                    text = "Unity Ads Engine Simulation Mode",
                    color = Color.DarkGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }
    }
}
