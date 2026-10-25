package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Product
import com.example.data.model.Warehouse
import com.example.ui.theme.*
import com.example.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiWarehouseScreen(viewModel: InventoryViewModel) {
    val warehouses by viewModel.warehouses.collectAsState()
    val transfers by viewModel.transfers.collectAsState()
    val products by viewModel.products.collectAsState()

    var showTransferDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showTransferDialog = true },
                containerColor = BluePrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
                text = { Text("مناقلة مخزون بين الفروع") },
                modifier = Modifier.testTag("stock_transfer_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text("المخازن المتعددة والمناقلات", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "إدارة المخزن الرئيسي والمستودعات الفرعية ونقل البضائع مع منع التكرار والتتبع الآمن.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("الفروع والمستودعات (${warehouses.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            warehouses.forEach { wh ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (wh.isMain) CyanAccent.copy(alpha = 0.2f) else BluePrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (wh.isMain) Icons.Default.Stars else Icons.Default.Warehouse,
                                    contentDescription = null,
                                    tint = if (wh.isMain) CyanAccentMuted else BluePrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(wh.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("الرمز: ${wh.code} • ${wh.location}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        if (wh.isMain) {
                            Surface(shape = RoundedCornerShape(10.dp), color = CyanAccent.copy(alpha = 0.15f)) {
                                Text("رئيسي", color = CyanAccentMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("سجل المناقلات والتحويلات (${transfers.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (transfers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا توجد مناقلات مسجلة حتى الآن", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(transfers, key = { it.id }) { t ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("تحويل: ${t.productName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        "الكمية: ${t.quantity} • المنفذ: ${t.transferredBy} • ${SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(t.timestamp))}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(shape = RoundedCornerShape(8.dp), color = EmeraldSuccess.copy(alpha = 0.15f)) {
                                    Text("ناجح", color = EmeraldSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTransferDialog) {
        StockTransferDialog(
            warehouses = warehouses,
            products = products,
            onDismiss = { showTransferDialog = false },
            onTransfer = { fromId, toId, prodId, qty, notes ->
                viewModel.transferStock(fromId, toId, prodId, qty, notes)
                showTransferDialog = false
            }
        )
    }
}

@Composable
fun StockTransferDialog(
    warehouses: List<Warehouse>,
    products: List<Product>,
    onDismiss: () -> Unit,
    onTransfer: (fromWarehouseId: Long, toWarehouseId: Long, productId: Long, qty: Double, notes: String) -> Unit
) {
    var fromWh by remember { mutableStateOf(warehouses.firstOrNull()) }
    var toWh by remember { mutableStateOf(warehouses.getOrNull(1) ?: warehouses.firstOrNull()) }
    var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
    var qtyInput by remember { mutableStateOf("5") }
    var notesInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("مناقلة بضاعة بين المخازن", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))

                Text("من مخزن:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                var showFromMenu by remember { mutableStateOf(false) }
                OutlinedButton(onClick = { showFromMenu = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(fromWh?.name ?: "اختر المصدر", fontSize = 12.sp)
                }
                DropdownMenu(expanded = showFromMenu, onDismissRequest = { showFromMenu = false }) {
                    warehouses.forEach { w ->
                        DropdownMenuItem(text = { Text(w.name) }, onClick = { fromWh = w; showFromMenu = false })
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("إلى مخزن:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                var showToMenu by remember { mutableStateOf(false) }
                OutlinedButton(onClick = { showToMenu = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(toWh?.name ?: "اختر الوجهة", fontSize = 12.sp)
                }
                DropdownMenu(expanded = showToMenu, onDismissRequest = { showToMenu = false }) {
                    warehouses.forEach { w ->
                        DropdownMenuItem(text = { Text(w.name) }, onClick = { toWh = w; showToMenu = false })
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("المنتج المراد نقله:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                var showProdMenu by remember { mutableStateOf(false) }
                OutlinedButton(onClick = { showProdMenu = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(selectedProduct?.name ?: "اختر المنتج", fontSize = 12.sp)
                }
                DropdownMenu(expanded = showProdMenu, onDismissRequest = { showProdMenu = false }) {
                    products.forEach { p ->
                        DropdownMenuItem(text = { Text("${p.name} (متوفر: ${p.quantity})") }, onClick = { selectedProduct = p; showProdMenu = false })
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = qtyInput,
                    onValueChange = { qtyInput = it },
                    label = { Text("الكمية المراد نقلها") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("سبب المناقلة وملاحظات") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Button(
                        onClick = {
                            val f = fromWh?.id ?: 1L
                            val t = toWh?.id ?: 2L
                            val p = selectedProduct?.id ?: 1L
                            val q = qtyInput.toDoubleOrNull() ?: 0.0
                            if (q > 0) {
                                onTransfer(f, t, p, q, notesInput)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text("تنفيذ المناقلة")
                    }
                }
            }
        }
    }
}
