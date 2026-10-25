package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.PurchaseInvoice
import com.example.data.model.Supplier
import com.example.ui.theme.*
import com.example.viewmodel.InventoryViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchasesScreen(viewModel: InventoryViewModel) {
    val purchases by viewModel.purchases.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val products by viewModel.products.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    var showNewPurchaseDialog by remember { mutableStateOf(false) }
    val numFmt = remember { NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 } }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewPurchaseDialog = true },
                containerColor = BluePrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("فاتورة شراء جديدة") },
                modifier = Modifier.testTag("new_purchase_fab")
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

            Text(
                text = "المشتريات وتوريد البضائع",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "إدارة فواتير الشراء واستلام المواد بالمخزن مع تحديث التكلفة وأرصدة الموردين تلقائياً.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (purchases.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("لا توجد فواتير شراء مسجلة")
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(purchases, key = { it.id }) { purchase ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(purchase.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(purchase.timestamp)),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("المورد: ${purchase.supplierName}", fontSize = 12.sp)
                                    Text(
                                        "${numFmt.format(purchase.totalAmount)} $currencySymbol",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = BluePrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "الحالة: مستلمة بالمخزن • المدفوع: ${numFmt.format(purchase.paidAmount)} $currencySymbol",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewPurchaseDialog) {
        NewPurchaseInvoiceDialog(
            suppliers = suppliers,
            products = products,
            currencySymbol = currencySymbol,
            onDismiss = { showNewPurchaseDialog = false },
            onSave = { supplierId, supplierName, items, unitCosts, paidAmount ->
                viewModel.recordPurchaseInvoice(supplierId, supplierName, items, unitCosts, paidAmount)
                showNewPurchaseDialog = false
            }
        )
    }
}

@Composable
fun NewPurchaseInvoiceDialog(
    suppliers: List<Supplier>,
    products: List<Product>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (supplierId: Long?, supplierName: String, items: List<Pair<Product, Double>>, unitCosts: List<Double>, paidAmount: Double) -> Unit
) {
    var selectedSupplier by remember { mutableStateOf(suppliers.firstOrNull()) }
    var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
    var qtyInput by remember { mutableStateOf("10") }
    var costInput by remember { mutableStateOf(selectedProduct?.costPrice?.toString() ?: "50000") }
    var paidInput by remember { mutableStateOf("") }

    val addedItems = remember { mutableStateListOf<Triple<Product, Double, Double>>() } // product, qty, cost

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text("فاتورة شراء واستلام بضاعة", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(10.dp))

                // Supplier picker
                var showSupplierDropdown by remember { mutableStateOf(false) }
                OutlinedButton(
                    onClick = { showSupplierDropdown = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(selectedSupplier?.name ?: "اختر المورد", fontSize = 12.sp)
                }
                DropdownMenu(expanded = showSupplierDropdown, onDismissRequest = { showSupplierDropdown = false }) {
                    suppliers.forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s.name) },
                            onClick = {
                                selectedSupplier = s
                                showSupplierDropdown = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Add item to purchase row
                Text("إضافة صنف للشحنة:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))

                var showProductDropdown by remember { mutableStateOf(false) }
                OutlinedButton(
                    onClick = { showProductDropdown = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedProduct?.name ?: "اختر المنتج", fontSize = 11.sp, maxLines = 1)
                }
                DropdownMenu(expanded = showProductDropdown, onDismissRequest = { showProductDropdown = false }) {
                    products.forEach { p ->
                        DropdownMenuItem(
                            text = { Text("${p.name} (حالي: ${p.quantity})") },
                            onClick = {
                                selectedProduct = p
                                costInput = p.costPrice.toString()
                                showProductDropdown = false
                            }
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = qtyInput,
                        onValueChange = { qtyInput = it },
                        label = { Text("الكمية") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = costInput,
                        onValueChange = { costInput = it },
                        label = { Text("سعر الشراء") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        val p = selectedProduct
                        val q = qtyInput.toDoubleOrNull() ?: 0.0
                        val c = costInput.toDoubleOrNull() ?: 0.0
                        if (p != null && q > 0 && c > 0) {
                            addedItems.add(Triple(p, q, c))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccentMuted)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إضافة الصنف للفاتورة")
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Added items list
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(addedItems) { (p, q, c) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${p.name} ($q ${p.unit})", fontSize = 11.sp, modifier = Modifier.weight(1f))
                            Text("${(q * c).toInt()} $currencySymbol", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                val total = addedItems.sumOf { it.second * it.third }
                Text("الإجمالي: ${total.toInt()} $currencySymbol", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                OutlinedTextField(
                    value = paidInput,
                    onValueChange = { paidInput = it },
                    label = { Text("المبلغ المدفوع للمورد (د.ع)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("اتركه فارغاً للدفع الكامل") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Button(
                        onClick = {
                            if (addedItems.isNotEmpty()) {
                                val paid = paidInput.toDoubleOrNull() ?: total
                                val itemsList = addedItems.map { it.first to it.second }
                                val costsList = addedItems.map { it.third }
                                onSave(selectedSupplier?.id, selectedSupplier?.name ?: "مورد عام", itemsList, costsList, paid)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text("اعتماد الفاتورة وتحديث المخزن")
                    }
                }
            }
        }
    }
}
