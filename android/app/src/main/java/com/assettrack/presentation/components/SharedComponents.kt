package com.assettrack.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.assettrack.domain.model.AssetStatus
import com.assettrack.domain.model.TransactionType
import com.assettrack.presentation.Screen
import com.assettrack.presentation.theme.*

// ── Top App Bar ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetTrackTopBar(
    pendingCount: Int,
    isSyncing: Boolean,
    onSyncClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.AccountBalance, null, tint = Primary)
                Text("MasterBarang", fontWeight = FontWeight.Bold, color = Primary)
            }
        },
        actions = {
            // Infinite transition selalu berjalan; nilai rotation hanya dipakai saat isSyncing
            val infiniteTransition = rememberInfiniteTransition(label = "sync_rotate")
            val infiniteRotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )
            val rotation = if (isSyncing) infiniteRotation else 0f

            IconButton(
                onClick = onSyncClick,
                enabled = !isSyncing
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Sync,
                        contentDescription = "Sync",
                        tint = when {
                            isSyncing        -> Primary
                            pendingCount > 0 -> MaterialTheme.colorScheme.error
                            else             -> Color(0xFF137333)
                        },
                        modifier = Modifier.rotate(rotation)
                    )
                    // Badge jumlah pending
                    if (pendingCount > 0 && !isSyncing) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (pendingCount > 9) "9+" else "$pendingCount",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Surface,
            titleContentColor = Primary
        )
    )
}

// ── Bottom Navigation ─────────────────────────────────────────────────────────

private data class NavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

private val navItems = listOf(
    NavItem(Screen.Dashboard,    "Dashboard",  Icons.Filled.Dashboard),
    NavItem(Screen.Registration, "Daftar",     Icons.Filled.AppRegistration),
    NavItem(Screen.Transaction,  "Transaksi",  Icons.Filled.SwapHoriz),
    NavItem(Screen.History,      "Riwayat",    Icons.Filled.History)
)

@Composable
fun AssetTrackBottomNav(navController: NavController) {
    val backStack = navController.currentBackStackEntryAsState()
    val currentRoute = backStack.value?.destination?.route

    NavigationBar(
        containerColor = Surface,
        tonalElevation = 0.dp
    ) {
        navItems.forEach { item ->
            val selected = currentRoute == item.screen.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(Screen.Dashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, item.label) },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = OnSecondaryContainer,
                    selectedTextColor = OnSecondaryContainer,
                    indicatorColor = SecondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

// ── Asset Status Badge ────────────────────────────────────────────────────────

@Composable
fun AssetStatusBadge(status: AssetStatus) {
    val (bg, fg, label, icon) = when (status) {
        AssetStatus.AVAILABLE   -> StatusTuple(StatusActiveBackground,      StatusActiveForeground,      "Active",      Icons.Filled.CheckCircle)
        AssetStatus.BORROWED    -> StatusTuple(StatusBorrowedBackground,    StatusBorrowedForeground,    "Borrowed",    Icons.Filled.Output)
        AssetStatus.MAINTENANCE -> StatusTuple(StatusMaintenanceBackground, StatusMaintenanceForeground, "Maintenance", Icons.Filled.Warning)
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(14.dp))
        Text(label, color = fg, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ── Transaction Type Badge ────────────────────────────────────────────────────

@Composable
fun TransactionTypeBadge(type: TransactionType) {
    val bg    = if (type == TransactionType.CHECK_OUT) ErrorContainer    else SecondaryContainer
    val fg    = if (type == TransactionType.CHECK_OUT) OnErrorContainer  else OnSecondaryContainer
    val label = if (type == TransactionType.CHECK_OUT) "OUT"             else "IN"
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = fg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

// ── Sync Dot ──────────────────────────────────────────────────────────────────

@Composable
fun SyncDot(isSynced: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(if (isSynced) Color(0xFF10B981) else MaterialTheme.colorScheme.error)
    )
}

// ── Scan FAB ─────────────────────────────────────────────────────────────────

@Composable
fun ScanFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = Primary,
        contentColor = OnPrimary,
        shape = CircleShape
    ) {
        Icon(Icons.Filled.QrCodeScanner, "Scan")
    }
}

// ── Internal helper ───────────────────────────────────────────────────────────

private data class StatusTuple(
    val bg: Color, val fg: Color,
    val label: String, val icon: ImageVector
)