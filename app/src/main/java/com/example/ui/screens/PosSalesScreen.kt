package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Customer
import com.example.data.model.Product
import com.example.data.model.SaleInvoice
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.theme.*
import com.example.viewmodel.CartItem
import com.example.viewmodel.InventoryViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosSalesScreen(viewModel: InventoryViewModel) {
    val products by viewModel.products.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val salesHistory by viewModel.sales.collectAsState()
    val selectedCustomerId by viewModel.selectedCustomerId.collectAsState()
    val paymentMethod by viewModel.paymentMethod.collectAsState()
    val paidAmountInput by viewModel.paidAmountInput.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = POS Register, 1 = Sales History
    var productSearch by remember { mutableStateOf("") }
    var isScannerOpen by remember { mutableStateOf(false) }
    var lastReceiptInvoice by remember { mutableStateOf<SaleInvoice?>(null) }
    var showReceiptDialog by remember { mutableStateOf(false) }

    val numFmt = remember { NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 } }
    val cartTotal = cartItems.sumOf { it.totalPrice }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = activeTab) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("نقطة البيع (الكاشير)") },
                icon = { Icon(Icons.Default.PointOfSale, contentDescription = null) },
                modifier = Modifier.testTag("tab_pos_register")
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("سجل الفواتير (${salesHistory.size})") },
                icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                modifier = Modifier.testTag("tab_sales_history")
            )
        }

        if (activeTab == 0) {
            // POS Register View: Top Products Search & Grid, Bottom/Side Cart
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                // Products Picker Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(end = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = productSearch,
                            onValueChange = { productSearch = it },
                            placeholder = { Text("بحث أو مسح...", fontSize = 12.sp) },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { isScannerOpen = true },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyanAccentMuted)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "مسح", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val filteredProducts = products.filter {
                        productSearch.isBlank() ||
                                it.name.contains(productSearch, ignoreCase = true) ||
                                it.sku.contains(productSearch, ignoreCase = true) ||
                                it.barcode.contains(productSearch, ignoreCase = true)
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 70.dp)
                    ) {
                        items(filteredProducts, key = { it.id }) { product ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.addToCart(product) },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(product.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                        Text(
                                            "${numFmt.format(product.salePrice)} $currencySymbol • متبقي: ${product.quantity} ${product.unit}",
                                            fontSize = 11.sp,
                                            color = if (product.quantity <= 0) RoseDanger else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    FilledIconButton(
                                        onClick = { viewModel.addToCart(product) },
                                        modifier = Modifier.size(32.dp),
                                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = BluePrimary)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "إضافة للسلة", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Cart Summary Column
                Card(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("سلة البيع (${cartItems.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            if (cartItems.isNotEmpty()) {
                                TextButton(
                                    onClick = { viewModel.clearCart() },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("تفريغ", color = RoseDanger, fontSize = 11.sp)
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        // Cart items list
                        if (cartItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("السلة فارغة", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(cartItems, key = { it.product.id }) { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.product.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                            Text("${numFmt.format(item.totalPrice)} $currencySymbol", fontSize = 10.sp, color = CyanAccentMuted)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { viewModel.updateCartItemQuantity(item.product.id, item.quantity - 1.0) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                                            }
                                            Text("${item.quantity.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            IconButton(
                                                onClick = { viewModel.updateCartItemQuantity(item.product.id, item.quantity + 1.0) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        // Customer Picker
                        var showCustomerMenu by remember { mutableStateOf(false) }
                        val currentCustomerName = customers.find { it.id == selectedCustomerId }?.name ?: "زبون نقدي عام"

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { showCustomerMenu = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(currentCustomerName, fontSize = 11.sp, maxLines = 1)
                            }
                            DropdownMenu(expanded = showCustomerMenu, onDismissRequest = { showCustomerMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text("زبون نقدي عام") },
                                    onClick = {
                                        viewModel.setSelectedCustomer(null)
                                        showCustomerMenu = false
                                    }
                                )
                                customers.forEach { cust ->
                                    DropdownMenuItem(
                                        text = { Text("${cust.name} (دين: ${numFmt.format(cust.balanceDue)})") },
                                        onClick = {
                                            viewModel.setSelectedCustomer(cust.id)
                                            showCustomerMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Payment Methods Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            FilterChip(
                                selected = paymentMethod == "CASH",
                                onClick = { viewModel.setPaymentMethod("CASH") },
                                label = { Text("نقدي", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = paymentMethod == "CREDIT",
                                onClick = { viewModel.setPaymentMethod("CREDIT") },
                                label = { Text("آجل/دين", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = paymentMethod == "CARD",
                                onClick = { viewModel.setPaymentMethod("CARD") },
                                label = { Text("بطاقة", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Total & Checkout
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("المجموع:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${numFmt.format(cartTotal)} $currencySymbol", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EmeraldSuccess)
                        }

                        Button(
                            onClick = {
                                viewModel.checkoutSale()
                            },
                            enabled = cartItems.isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pos_checkout_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إتمام الفاتورة والطباعة", fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            // Sales History View
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(salesHistory, key = { it.id }) { sale ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                lastReceiptInvoice = sale
                                showReceiptDialog = true
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(sale.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(sale.timestamp)),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("العميل: ${sale.customerName}", fontSize = 12.sp)
                                Text(
                                    "${numFmt.format(sale.totalAmount)} $currencySymbol",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = EmeraldSuccess
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "الدفع: ${sale.paymentMethod} • الكاشير: ${sale.cashierName} • ${sale.notes}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Barcode Scanner Dialog
    BarcodeScannerDialog(
        isOpen = isScannerOpen,
        onDismiss = { isScannerOpen = false },
        onBarcodeScanned = { code ->
            val matched = products.find { it.barcode == code || it.sku.equals(code, ignoreCase = true) }
            if (matched != null) {
                viewModel.addToCart(matched)
                viewModel.showFeedback("تمت إضافة: ${matched.name} إلى السلة")
            } else {
                productSearch = code
            }
        },
        sampleBarcodes = products.take(2).map { it.barcode to it.name }
    )

    // Printable Receipt Dialog
    if (showReceiptDialog && lastReceiptInvoice != null) {
        val inv = lastReceiptInvoice!!
        Dialog(onDismissRequest = { showReceiptDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("جرد أمين الذكي — AMEEN INVENTORY", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
                    Text("فاتورة بيع رسمية", fontSize = 12.sp, color = Color.DarkGray)
                    Text(inv.invoiceNumber, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp)

                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.LightGray)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("العميل: ${inv.customerName}", fontSize = 11.sp, color = Color.Black)
                        Text(SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(inv.timestamp)), fontSize = 11.sp, color = Color.DarkGray)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("طريقة الدفع: ${inv.paymentMethod}", fontSize = 11.sp, color = Color.Black)
                        Text("الكاشير: ${inv.cashierName}", fontSize = 11.sp, color = Color.DarkGray)
                    }

                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.LightGray)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("المبلغ الإجمالي:", fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("${numFmt.format(inv.totalAmount)} $currencySymbol", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("المدفوع نقدياً:", fontSize = 12.sp, color = Color.DarkGray)
                        Text("${numFmt.format(inv.paidAmount)} $currencySymbol", fontSize = 12.sp, color = Color.DarkGray)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "© 2026 أمين الشريفي — Ameen Al-Sharifi",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = { showReceiptDialog = false }) {
                            Text("إغلاق", color = Color.Black)
                        }
                        Button(
                            onClick = {
                                viewModel.showFeedback("تمت الطباعة بنجاح عبر طابعة الفواتير")
                                showReceiptDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandNavyDark)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("طباعة الفاتورة")
                        }
                    }
                }
            }
        }
    }
}
