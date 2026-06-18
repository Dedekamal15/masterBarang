package com.assettrack.presentation.screens.history

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.assettrack.domain.model.Transaction
import com.assettrack.domain.model.TransactionType
import com.assettrack.presentation.components.SyncDot
import com.assettrack.presentation.components.TransactionTypeBadge
import com.assettrack.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {

        // Search + filter
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari barang atau penerima...") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = OutlineVariant,
                    focusedContainerColor = SurfaceContainerLowest,
                    unfocusedContainerColor = SurfaceContainerLowest
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    null         to "Semua",
                    TransactionType.CHECK_OUT to "Keluar",
                    TransactionType.CHECK_IN  to "Masuk"
                ).forEach { (type, label) ->
                    FilterChip(
                        selected = uiState.filterType == type,
                        onClick  = { viewModel.onFilterType(type) },
                        label    = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SecondaryContainer,
                            selectedLabelColor = OnSecondaryContainer
                        )
                    )
                }
            }
        }

        // Table header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("WAKTU",    modifier = Modifier.weight(1.5f), style = headerStyle())
            Text("BARANG",     modifier = Modifier.weight(2.5f), style = headerStyle())
            Text("PENERIMA", modifier = Modifier.weight(2f),   style = headerStyle())
            Text("TIPE",     modifier = Modifier.weight(1f),   style = headerStyle())
        }
        HorizontalDivider(color = OutlineVariant)

        if (uiState.filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.History, null, Modifier.size(48.dp), tint = OutlineVariant)
                    Text(
                        "Tidak ada riwayat transaksi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn {
                items(uiState.filtered, key = { it.id }) { tx ->
                    TransactionRow(
                        tx         = tx,
                        isExpanded = uiState.expandedId == tx.id,
                        onToggle   = { viewModel.onToggleExpand(tx.id) }
                    )
                    HorizontalDivider(color = SurfaceVariant)
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun TransactionRow(tx: Transaction, isExpanded: Boolean, onToggle: () -> Unit) {
    Column {
        Card(
            onClick = onToggle,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isExpanded) SurfaceContainerLow else SurfaceContainerLowest
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    formatTs(tx.timestampMs),
                    modifier = Modifier.weight(1.5f),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                Column(Modifier.weight(2.5f)) {
                    Text(tx.assetName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Text(tx.assetSerialNumber, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                }
                Text(
                    tx.recipientName.ifBlank { "-" },
                    modifier = Modifier.weight(2f),
                    style = MaterialTheme.typography.bodySmall
                )
                Box(Modifier.weight(1f)) {
                    TransactionTypeBadge(type = tx.type)
                }
                Icon(
                    if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter   = expandVertically() + fadeIn(),
            exit    = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (tx.latitude != null && tx.longitude != null) {
                    Text(
                        "GPS VERIFICATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text("Lokasi", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "%.5f, %.5f".format(tx.latitude, tx.longitude),
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            tx.gpsAccuracyMeters?.let {
                                Text("±${it.toInt()}m", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Tujuan", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text(tx.destination.ifBlank { "-" }, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (tx.notes.isNotBlank()) {
                    Text("CATATAN", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(tx.notes, style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SyncDot(isSynced = tx.isSynced)
                    Text(
                        if (tx.isSynced) "Tersinkronisasi ke server" else "Menunggu sinkronisasi...",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (tx.isSynced) Color(0xFF137333) else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun headerStyle() = MaterialTheme.typography.labelSmall.copy(
    color = MaterialTheme.colorScheme.onSurfaceVariant
)

private fun formatTs(ms: Long): String =
    SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(Date(ms))
