package com.assettrack

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.getSystemService
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
import com.assettrack.service.SyncForegroundService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val batteryOptLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        startSyncService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestBatteryOptimization()
        setContent {
            AssetTrackTheme {
                AssetTrackRoot()
            }
        }
    }

    private fun requestBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService<PowerManager>() ?: return
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                batteryOptLauncher.launch(
                    Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:$packageName")
                    )
                )
            } else {
                startSyncService()
            }
        } else {
            startSyncService()
        }
    }

    private fun startSyncService() {
        val intent = Intent(this, SyncForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
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