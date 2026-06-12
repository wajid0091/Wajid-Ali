package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import com.example.ui.AppViewModel
import com.example.ui.Screen
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel = ViewModelProvider(this)[AppViewModel::class.java]

        setContent {
            val isDark by viewModel.isDarkMode.collectAsState()
            val navStack by viewModel.navigationStack.collectAsState()

            MyApplicationTheme(darkTheme = isDark, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
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

                    // --- Toast Trigger Hook ---
                    val context = LocalContext.current
                    LaunchedEffect(Unit) {
                        viewModel.toastMessage.collect { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
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
}
