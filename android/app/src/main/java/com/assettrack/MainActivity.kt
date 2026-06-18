package com.assettrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.assettrack.presentation.Screen
import com.assettrack.presentation.components.AssetTrackBottomNav
import com.assettrack.presentation.components.AssetTrackTopBar
import com.assettrack.presentation.components.ScanFab
import com.assettrack.presentation.screens.dashboard.DashboardScreen
import com.assettrack.presentation.screens.dashboard.DashboardViewModel
import com.assettrack.presentation.screens.history.HistoryScreen
import com.assettrack.presentation.screens.registration.RegistrationScreen
import com.assettrack.presentation.screens.transaction.TransactionScreen
import com.assettrack.presentation.theme.AssetTrackTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AssetTrackTheme {
                AssetTrackRoot()
            }
        }
    }
}

@Composable
private fun AssetTrackRoot() {
    val navController = rememberNavController()
    val dashboardVm: DashboardViewModel = hiltViewModel()
    val dashboardState by dashboardVm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AssetTrackTopBar(
                pendingCount = dashboardState.pendingSyncCount,
                isSyncing    = dashboardState.isSyncing,
                onSyncClick  = dashboardVm::onSyncNow
            )
        },
        bottomBar = {
            AssetTrackBottomNav(navController = navController)
        },
        floatingActionButton = {
            ScanFab(onClick = {
                navController.navigate(Screen.Transaction.route) {
                    launchSingleTop = true
                }
            })
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Dashboard.route,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel      = dashboardVm,
                    onScanFabClick = { navController.navigate(Screen.Transaction.route) }
                )
            }
            composable(Screen.Registration.route) { RegistrationScreen() }
            composable(Screen.Transaction.route)  { TransactionScreen() }
            composable(Screen.History.route)      { HistoryScreen() }
        }
    }
}
