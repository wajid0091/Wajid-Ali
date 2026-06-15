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

    private var unityGameId = "5763487"
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
                val gameId = settingsList.find { it.key == "unity_game_id" }?.value ?: "5763487"
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
        preloadAds()
    }

    override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError?, message: String?) {
        Toast.makeText(this, "Unity Ads Init Failed: $message", Toast.LENGTH_SHORT).show()
    }

    private fun preloadAds() {
        val loadListener = object : com.unity3d.ads.IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String) {}
            override fun onUnityAdsFailedToLoad(placementId: String, error: UnityAds.UnityAdsLoadError, message: String) {}
        }
        UnityAds.load("Rewarded_Android", loadListener)
        UnityAds.load("Interstitial_Android", loadListener)
    }

    private var lastInterstitialTime = 0L

    fun showInterstitialAd(adUnitId: String = "Interstitial_Android") {
        if (!isAdReady || !UnityAds.isInitialized) return
        val currentTime = System.currentTimeMillis()
        // Cooldown 2 minutes
        if (currentTime - lastInterstitialTime < 120_000) return

        val showListener = object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(placementId: String, error: UnityAds.UnityAdsShowError, message: String) {
                preloadAds() // Retry load
            }
            override fun onUnityAdsShowStart(placementId: String) {}
            override fun onUnityAdsShowClick(placementId: String) {}
            override fun onUnityAdsShowComplete(placementId: String, state: UnityAds.UnityAdsShowCompletionState) {
                lastInterstitialTime = System.currentTimeMillis()
                preloadAds()
            }
        }
        UnityAds.show(this, adUnitId, UnityAdsShowOptions(), showListener)
    }

    fun showRewardedAd(adUnitId: String, onReward: () -> Unit) {
        if (!isAdReady || !UnityAds.isInitialized) {
            Toast.makeText(this, "Ad is not ready yet, loading...", Toast.LENGTH_SHORT).show()
            preloadAds()
            return
        }
        pendingRewardCallback = onReward

        val showListener = object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(placementId: String, error: UnityAds.UnityAdsShowError, message: String) {
                Toast.makeText(this@MainActivity, "Failed to show ad: $message", Toast.LENGTH_SHORT).show()
                preloadAds()
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
                preloadAds()
            }
        }
        UnityAds.show(this, adUnitId, UnityAdsShowOptions(), showListener)
    }
}
