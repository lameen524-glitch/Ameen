package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Product
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.InventoryViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(viewModel: InventoryViewModel) {
    val allProducts by viewModel.products.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val isScannerOpen by viewModel.isBarcodeScannerOpen.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var stockStatusFilter by remember { mutableStateOf("ALL") } // ALL, LOW, OUT, AVAILABLE
    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var showCsvDialog by remember { mutableStateOf(false) }

    val numFmt = remember { NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 } }

    val filteredProducts = remember(allProducts, searchQuery, selectedCategory, stockStatusFilter) {
        allProducts.filter { p ->
            val matchQuery = searchQuery.isBlank() ||
                    p.name.contains(searchQuery, ignoreCase = true) ||
                    p.sku.contains(searchQuery, ignoreCase = true) ||
                    p.barcode.contains(searchQuery, ignoreCase = true) ||
                    p.shelfLocation.contains(searchQuery, ignoreCase = true)

            val matchCategory = selectedCategory.isBlank() || p.category == selectedCategory

            val matchStatus = when (stockStatusFilter) {
                "LOW" -> p.quantity <= p.minThreshold && p.quantity > 0
                "OUT" -> p.quantity <= 0
                "AVAILABLE" -> p.quantity > p.minThreshold
                else -> true
            }

            matchQuery && matchCategory && matchStatus
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    productToEdit = null
                    showAddDialog = true
                },
                containerColor = BluePrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة منتج جديد")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ابحث بالاسم، SKU، الباركود، أو الرف...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "مسح")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("product_search_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Barcode Scan Button
                IconButton(
                    onClick = { viewModel.setBarcodeScannerOpen(true) },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .testTag("product_barcode_scan_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "مسح باركود",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // CSV Export/Import Button
                IconButton(
                    onClick = { showCsvDialog = true },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = "استيراد وتصدير CSV")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stock Status Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = stockStatusFilter == "ALL",
                    onClick = { stockStatusFilter = "ALL" },
                    label = { Text("الكل (${allProducts.size})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = stockStatusFilter == "AVAILABLE",
                    onClick = { stockStatusFilter = "AVAILABLE" },
                    label = { Text("متوفر", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = stockStatusFilter == "LOW",
                    onClick = { stockStatusFilter = "LOW" },
                    label = { Text("منخفض", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AmberWarning.copy(alpha = 0.2f))
                )
                FilterChip(
                    selected = stockStatusFilter == "OUT",
                    onClick = { stockStatusFilter = "OUT" },
                    label = { Text("نافد", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RoseDanger.copy(alpha = 0.2f))
                )
            }

            // Categories Row
            if (categories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        SuggestionChip(
                            onClick = { selectedCategory = "" },
                            label = { Text("جميع الأقسام", fontSize = 11.sp, fontWeight = if (selectedCategory.isBlank()) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                    items(categories) { cat ->
                        SuggestionChip(
                            onClick = { selectedCategory = if (selectedCategory == cat) "" else cat },
                            label = { Text(cat, fontSize = 11.sp, fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Product List
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "لم يتم العثور على منتجات مطابقة للبحث" else "لا توجد منتجات مسجلة",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductItemCard(
                            product = product,
                            currencySymbol = currencySymbol,
                            onCardClick = { viewModel.viewProductDetail(product) },
                            onEdit = {
                                productToEdit = product
                                showAddDialog = true
                            },
                            onDuplicate = { viewModel.duplicateProduct(product) },
                            onDelete = { viewModel.deleteProduct(product) }
                        )
                    }
                }
            }
        }
    }

    // Barcode Scanner Dialog
    BarcodeScannerDialog(
        isOpen = isScannerOpen,
        onDismiss = { viewModel.setBarcodeScannerOpen(false) },
        onBarcodeScanned = { code ->
            searchQuery = code
            val matched = allProducts.find { it.barcode == code || it.sku.equals(code, ignoreCase = true) }
            if (matched != null) {
                viewModel.viewProductDetail(matched)
            } else {
                viewModel.showFeedback("تم البحث عن الكود: $code")
            }
        },
        sampleBarcodes = allProducts.take(3).map { it.barcode to it.name }
    )

    // Add / Edit Product Dialog
    if (showAddDialog) {
        AddEditProductDialog(
            initialProduct = productToEdit,
            onDismiss = { showAddDialog = false },
            onSave = { name, sku, barcode, category, brand, desc, cost, sale, qty, minTh, sup, shelf, unit ->
                viewModel.saveProduct(
                    id = productToEdit?.id ?: 0L,
                    name = name,
                    sku = sku,
                    barcode = barcode,
                    category = category,
                    brand = brand,
                    description = desc,
                    costPrice = cost,
                    salePrice = sale,
                    quantity = qty,
                    minThreshold = minTh,
                    supplierName = sup,
                    shelfLocation = shelf,
                    unit = unit,
                    warehouseId = productToEdit?.warehouseId ?: 1L
                )
                showAddDialog = false
            }
        )
    }

    // CSV Import / Export Dialog
    if (showCsvDialog) {
        CsvManagerDialog(
            viewModel = viewModel,
            onDismiss = { showCsvDialog = false }
        )
    }
}

@Composable
fun ProductItemCard(
    product: Product,
    currencySymbol: String,
    onCardClick: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val numFmt = remember { NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 } }
    var menuExpanded by remember { mutableStateOf(false) }

    val statusColor = when {
        product.quantity <= 0 -> RoseDanger
        product.quantity <= product.minThreshold -> AmberWarning
        else -> EmeraldSuccess
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = product.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SKU: ${product.sku}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Options Menu
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "خيارات")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("عرض التفاصيل والباركود") },
                            leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onCardClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("تعديل المنتج") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("تكرار / نسخ المنتج") },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onDuplicate()
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("حذف المنتج", color = RoseDanger) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = RoseDanger) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pricing & Quantity Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("سعر البيع", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${numFmt.format(product.salePrice)} $currencySymbol",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column {
                    Text("التكلفة", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${numFmt.format(product.costPrice)} $currencySymbol",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("الكمية الحالية", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "${product.quantity} ${product.unit}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = statusColor
                        )
                    }
                }
            }

            if (product.shelfLocation.isNotBlank() || product.barcode.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (product.shelfLocation.isNotBlank()) {
                        Text("📍 الرف: ${product.shelfLocation}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (product.barcode.isNotBlank()) {
                        Text("🔢 ${product.barcode}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditProductDialog(
    initialProduct: Product?,
    onDismiss: () -> Unit,
    onSave: (
        name: String, sku: String, barcode: String, category: String, brand: String,
        desc: String, cost: Double, sale: Double, qty: Double, minTh: Double,
        sup: String, shelf: String, unit: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var sku by remember { mutableStateOf(initialProduct?.sku ?: "SKU-${System.currentTimeMillis() % 10000}") }
    var barcode by remember { mutableStateOf(initialProduct?.barcode ?: "") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "إلكترونيات") }
    var brand by remember { mutableStateOf(initialProduct?.brand ?: "") }
    var description by remember { mutableStateOf(initialProduct?.description ?: "") }
    var costPrice by remember { mutableStateOf(initialProduct?.costPrice?.toString() ?: "0") }
    var salePrice by remember { mutableStateOf(initialProduct?.salePrice?.toString() ?: "0") }
    var quantity by remember { mutableStateOf(initialProduct?.quantity?.toString() ?: "10") }
    var minThreshold by remember { mutableStateOf(initialProduct?.minThreshold?.toString() ?: "3") }
    var supplierName by remember { mutableStateOf(initialProduct?.supplierName ?: "") }
    var shelfLocation by remember { mutableStateOf(initialProduct?.shelfLocation ?: "A-01") }
    var unit by remember { mutableStateOf(initialProduct?.unit ?: "قطعة") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (initialProduct == null) "إضافة منتج جديد" else "تعديل بيانات المنتج",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("اسم المنتج *") },
                            modifier = Modifier.fillMaxWidth().testTag("product_name_input"),
                            singleLine = true
                        )
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = sku,
                                onValueChange = { sku = it },
                                label = { Text("رمز SKU *") },
                                modifier = Modifier.weight(1f).testTag("product_sku_input"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = barcode,
                                onValueChange = { barcode = it },
                                label = { Text("الباركود") },
                                modifier = Modifier.weight(1f).testTag("product_barcode_input"),
                                singleLine = true
                            )
                        }
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = { category = it },
                                label = { Text("القسم / التصنيف") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = unit,
                                onValueChange = { unit = it },
                                label = { Text("الوحدة (قطعة/كرتون)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = costPrice,
                                onValueChange = { costPrice = it },
                                label = { Text("سعر التكلفة (د.ع) *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("product_cost_input"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = salePrice,
                                onValueChange = { salePrice = it },
                                label = { Text("سعر البيع (د.ع) *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("product_sale_input"),
                                singleLine = true
                            )
                        }
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = quantity,
                                onValueChange = { quantity = it },
                                label = { Text("الكمية الحالية *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("product_qty_input"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = minThreshold,
                                onValueChange = { minThreshold = it },
                                label = { Text("الحد الأدنى للتنبيه") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = shelfLocation,
                                onValueChange = { shelfLocation = it },
                                label = { Text("مكان الرف / الموقع") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = brand,
                                onValueChange = { brand = it },
                                label = { Text("الماركة / الشركة") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = supplierName,
                            onValueChange = { supplierName = it },
                            label = { Text("اسم المورد المعتمد") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("وصف ومواصفات المنتج") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val cost = costPrice.toDoubleOrNull() ?: 0.0
                            val sale = salePrice.toDoubleOrNull() ?: 0.0
                            val qty = quantity.toDoubleOrNull() ?: 0.0
                            val minTh = minThreshold.toDoubleOrNull() ?: 3.0
                            onSave(name, sku, barcode, category, brand, description, cost, sale, qty, minTh, supplierName, shelfLocation, unit)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        modifier = Modifier.testTag("save_product_confirm_btn")
                    ) {
                        Text("حفظ المنتج")
                    }
                }
            }
        }
    }
}

@Composable
fun CsvManagerDialog(
    viewModel: InventoryViewModel,
    onDismiss: () -> Unit
) {
    var csvText by remember { mutableStateOf(viewModel.exportProductsToCsv()) }
    var importStatus by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = "تصدير واستيراد ملفات CSV / Excel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "يمكنك نسخ بيانات المنتجات أدناه أو لصق بيانات جديدة واستيرادها مباشرة.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = csvText,
                    onValueChange = { csvText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                )

                if (importStatus != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(importStatus!!, color = EmeraldSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إغلاق")
                    }
                    Button(
                        onClick = {
                            val res = viewModel.importProductsFromCsv(csvText)
                            importStatus = "تم الاستيراد: ${res.first} منتج بنجاح!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                    ) {
                        Text("استيراد وتحديث الآن")
                    }
                }
            }
        }
    }
}
