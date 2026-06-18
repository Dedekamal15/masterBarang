package com.assettrack.presentation.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.assettrack.domain.model.Asset
import com.assettrack.presentation.components.AssetStatusBadge
import com.assettrack.presentation.components.SyncDot
import com.assettrack.presentation.theme.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onScanFabClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Sync Status Banner ────────────────────────────────────────────────
        SyncBanner(
            uiState = uiState,
            onSyncNow = viewModel::onSyncNow,
            onDismiss = viewModel::onDismissSyncMessage
        )

        // ── Search Bar ────────────────────────────────────────────────────────
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            placeholder = {
                Text("Cari barang, SN, IMEI...", color = MaterialTheme.colorScheme.outline)
            },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            trailingIcon = {
                // Tombol Sync Manual di search bar
                if (uiState.pendingSyncCount > 0 && !uiState.isSyncing) {
                    IconButton(onClick = viewModel::onSyncNow) {
                        Icon(
                            Icons.Filled.CloudUpload,
                            contentDescription = "Sync sekarang",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                } else if (uiState.isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 4.dp),
                        strokeWidth = 2.dp,
                        color = Primary
                    )
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = OutlineVariant,
                focusedContainerColor = SurfaceContainerLowest,
                unfocusedContainerColor = SurfaceContainerLowest
            )
        )

        // ── Tabs ──────────────────────────────────────────────────────────────
        TabRow(
            selectedTabIndex = uiState.activeTab.ordinal,
            containerColor = Surface,
            contentColor = Primary,
            divider = { HorizontalDivider(color = OutlineVariant) }
        ) {
            Tab(
                selected = uiState.activeTab == DashboardTab.GUDANG,
                onClick = { viewModel.onTabChange(DashboardTab.GUDANG) },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Gudang", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        if (uiState.activeTab == DashboardTab.GUDANG) {
                            Badge { Text("${uiState.filteredAssets.size}") }
                        }
                    }
                }
            )
            Tab(
                selected = uiState.activeTab == DashboardTab.DI_LUAR,
                onClick = { viewModel.onTabChange(DashboardTab.DI_LUAR) },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Di Luar", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        if (uiState.activeTab == DashboardTab.DI_LUAR && uiState.filteredAssets.isNotEmpty()) {
                            Badge(containerColor = MaterialTheme.colorScheme.error) {
                                Text("${uiState.filteredAssets.size}")
                            }
                        }
                    }
                }
            )
        }

        Spacer(Modifier.height(8.dp))

        // ── Asset List ────────────────────────────────────────────────────────
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = Primary)
                        Text(
                            "Memuat data...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            uiState.filteredAssets.isEmpty() -> EmptyState(tab = uiState.activeTab)
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.filteredAssets, key = { it.id }) { asset ->
                        AssetCard(asset = asset)
                    }
                    item { Spacer(Modifier.height(100.dp)) }
                }
            }
        }
    }
}

// ── Sync Banner ───────────────────────────────────────────────────────────────

@Composable
private fun SyncBanner(
    uiState: DashboardUiState,
    onSyncNow: () -> Unit,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = uiState.pendingSyncCount > 0 || uiState.isSyncing ||
                  uiState.syncStatus == SyncStatus.ERROR,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        when {
            uiState.isSyncing -> {
                // Banner biru saat sedang sync
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Primary)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Text(
                        uiState.syncMessage,
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            uiState.syncStatus == SyncStatus.ERROR -> {
                // Banner merah saat error
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Filled.ErrorOutline, null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        uiState.syncMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = onSyncNow,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) { Text("Coba Lagi") }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.Close, null, tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            uiState.pendingSyncCount > 0 -> {
                // Banner kuning saat ada pending
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF3E0))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Filled.CloudOff, null,
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(18.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${uiState.pendingSyncCount} data belum tersinkronisasi",
                            color = Color(0xFFE65100),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (uiState.lastSyncTime.isNotBlank()) {
                            Text(
                                "Terakhir sync: ${uiState.lastSyncTime}",
                                color = Color(0xFFBF360C),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    // ── TOMBOL SYNC MANUAL ────────────────────────────────────
                    Button(
                        onClick = onSyncNow,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Sync, null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Sync", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

// ── Asset Card ────────────────────────────────────────────────────────────────

@Composable
private fun AssetCard(asset: Asset) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        border = BorderStroke(1.dp, OutlineVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        asset.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "SN: ${asset.serialNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (asset.category.isNotBlank()) {
                        Text(
                            asset.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                AssetStatusBadge(status = asset.status)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Filled.Inventory2, null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        asset.location.ifBlank { "Lokasi tidak diisi" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SyncDot(isSynced = asset.isSynced)
                    Text(
                        if (asset.isSynced) "Synced" else "Pending",
                        fontSize = 10.sp,
                        color = if (asset.isSynced) Color(0xFF137333)
                        else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// ── Empty State ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(tab: DashboardTab) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                if (tab == DashboardTab.GUDANG) Icons.Filled.Inventory2
                else Icons.Filled.Output,
                null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.outlineVariant
            )
            Text(
                if (tab == DashboardTab.GUDANG) "Tidak ada barang di gudang"
                else "Tidak ada barang yang sedang dipinjam",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
