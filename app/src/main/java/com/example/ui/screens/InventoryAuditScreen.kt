package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.InventoryAuditItem
import com.example.data.model.InventoryAuditSession
import com.example.data.model.Warehouse
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.InventoryViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryAuditScreen(viewModel: InventoryViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val auditSessions by viewModel.auditSessions.collectAsState()
    val activeSessionId by viewModel.selectedAuditSessionId.collectAsState()
    val activeItems by viewModel.activeAuditItems.collectAsState()
    val warehouses by viewModel.warehouses.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    var showNewSessionDialog by remember { mutableStateOf(false) }

    if (currentScreen == AppScreen.AUDIT_SESSION_RUNNER && activeSessionId != null) {
        val currentSession = auditSessions.find { it.id == activeSessionId }
        AuditSessionRunnerView(
            session = currentSession,
            items = activeItems,
            currencySymbol = currencySymbol,
            viewModel = viewModel,
            onBack = { viewModel.navigateTo(AppScreen.AUDIT) }
        )
    } else {
        // Sessions List View
        Scaffold(
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showNewSessionDialog = true },
                    containerColor = BluePrimary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("بدء جلسة جرد جديدة") },
                    modifier = Modifier.testTag("start_audit_session_fab")
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
                    text = "جلسات الجرد الفعلي الذكية",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "إدارة ومطابقة الكميات الفعلية مع النظام بدقة واحتساب فروقات العجز والزيادة.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (auditSessions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FactCheck,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("لا توجد جلسات جرد سابقة", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "اضغط على زر 'بدء جلسة جرد جديدة' لمطابقة محتويات المخزن.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 90.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(auditSessions, key = { it.id }) { session ->
                            AuditSessionCard(
                                session = session,
                                currencySymbol = currencySymbol,
                                onClick = { viewModel.openAuditSession(session.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showNewSessionDialog) {
        NewAuditSessionDialog(
            warehouses = warehouses,
            onDismiss = { showNewSessionDialog = false },
            onCreate = { title, whId, whName ->
                viewModel.createAuditSession(title, whId, whName)
                showNewSessionDialog = false
            }
        )
    }
}

@Composable
fun AuditSessionCard(
    session: InventoryAuditSession,
    currencySymbol: String,
    onClick: () -> Unit
) {
    val numFmt = remember { NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 } }
    val isApproved = session.status == "APPROVED"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isApproved) EmeraldSuccess.copy(alpha = 0.15f) else AmberWarning.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isApproved) Icons.Default.CheckCircle else Icons.Default.Pending,
                            contentDescription = null,
                            tint = if (isApproved) EmeraldSuccess else AmberWarning,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isApproved) "معتمد ومحدث" else "مسودة قيد المراجعة",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isApproved) EmeraldSuccess else AmberWarning
                        )
                    }
                }

                Text(
                    text = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(session.createdAt)),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = session.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "الموقع: ${session.warehouseName}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("الأصناف", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${session.totalItemsCounted} صنف", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column {
                    Text("فرق الكميات", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${session.totalDiscrepancyQty} وحدة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (session.totalDiscrepancyQty < 0) RoseDanger else if (session.totalDiscrepancyQty > 0) EmeraldSuccess else MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("قيمة الفروقات", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${numFmt.format(session.totalDiscrepancyValue)} $currencySymbol",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (session.totalDiscrepancyValue < 0) RoseDanger else if (session.totalDiscrepancyValue > 0) EmeraldSuccess else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditSessionRunnerView(
    session: InventoryAuditSession?,
    items: List<InventoryAuditItem>,
    currencySymbol: String,
    viewModel: InventoryViewModel,
    onBack: () -> Unit
) {
    val numFmt = remember { NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 } }
    var searchQuery by remember { mutableStateOf("") }
    var isScannerOpen by remember { mutableStateOf(false) }
    var showApproveConfirmDialog by remember { mutableStateOf(false) }
    var itemForDiscrepancyReason by remember { mutableStateOf<InventoryAuditItem?>(null) }

    BackHandler {
        onBack()
    }

    if (session == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("الجلسة غير متوفرة")
        }
        return
    }

    val isApproved = session.status == "APPROVED"

    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) items else items.filter {
            it.productName.contains(searchQuery, ignoreCase = true) ||
                    it.sku.contains(searchQuery, ignoreCase = true) ||
                    it.barcode.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(session.title, maxLines = 1, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "${session.warehouseName} • ${if (isApproved) "معتمدة ومقفلة" else "مسودة جارية"}",
                            fontSize = 11.sp,
                            color = if (isApproved) EmeraldSuccess else AmberWarning
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("audit_runner_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { isScannerOpen = true }) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "مسح باركود", tint = CyanAccent)
                    }
                }
            )
        },
        bottomBar = {
            if (!isApproved) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.showFeedback("تم حفظ مسودة الجرد بنجاح")
                                onBack()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ كمسودة")
                        }

                        Button(
                            onClick = { showApproveConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("approve_audit_session_btn")
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("اعتماد وتحديث المخزن")
                        }
                    }
                }
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

            // Discrepancy Summary Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BrandNavyCard)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("إجمالي المواد", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Text("${items.size}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("فرق القطع", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Text(
                        "${session.totalDiscrepancyQty}",
                        color = if (session.totalDiscrepancyQty < 0) RoseDanger else if (session.totalDiscrepancyQty > 0) EmeraldSuccess else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("قيمة الفروقات", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Text(
                        "${numFmt.format(session.totalDiscrepancyValue)} $currencySymbol",
                        color = if (session.totalDiscrepancyValue < 0) RoseDanger else if (session.totalDiscrepancyValue > 0) EmeraldSuccess else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("بحث عن صنف للمطابقة والعد...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Audit Items List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredItems, key = { it.productId }) { item ->
                    AuditItemRow(
                        item = item,
                        currencySymbol = currencySymbol,
                        isReadOnly = isApproved,
                        onCountChanged = { newCount ->
                            viewModel.updateAuditItemCount(session.id, item.productId, newCount)
                        },
                        onReasonClick = {
                            itemForDiscrepancyReason = item
                        }
                    )
                }
            }
        }
    }

    // Barcode Scanner Dialog for audit
    BarcodeScannerDialog(
        isOpen = isScannerOpen,
        onDismiss = { isScannerOpen = false },
        onBarcodeScanned = { code ->
            val matched = items.find { it.barcode == code || it.sku.equals(code, ignoreCase = true) }
            if (matched != null) {
                // Increment physical count by 1 upon barcode scan
                viewModel.updateAuditItemCount(session.id, matched.productId, matched.countedQty + 1.0)
                viewModel.showFeedback("تم مسح: ${matched.productName} (+1)")
            } else {
                searchQuery = code
            }
        },
        sampleBarcodes = items.take(2).map { it.barcode to it.productName }
    )

    // Reason & Notes Dialog
    if (itemForDiscrepancyReason != null) {
        val currentItem = itemForDiscrepancyReason!!
        var reasonText by remember { mutableStateOf(currentItem.reason) }
        var notesText by remember { mutableStateOf(currentItem.notes) }

        Dialog(onDismissRequest = { itemForDiscrepancyReason = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("سبب الفرق وملاحظات الجرد", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(currentItem.productName, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    val commonReasons = listOf("عجز أو كسر أثناء المناولة", "بضاعة منتهية الصلاحية", "زيادة شحنة من المورد", "خطأ في تسجيل مبيعات سابقة")
                    commonReasons.forEach { r ->
                        AssistChip(
                            onClick = { reasonText = r },
                            label = { Text(r, fontSize = 11.sp) },
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    OutlinedTextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        label = { Text("سبب الاختلاف") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("ملاحظات إضافية") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { itemForDiscrepancyReason = null }) {
                            Text("إلغاء")
                        }
                        Button(
                            onClick = {
                                viewModel.updateAuditItemCount(session.id, currentItem.productId, currentItem.countedQty, reasonText, notesText)
                                itemForDiscrepancyReason = null
                            }
                        ) {
                            Text("حفظ")
                        }
                    }
                }
            }
        }
    }

    // Approve Confirmation Dialog
    if (showApproveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showApproveConfirmDialog = false },
            title = { Text("اعتماد الجرد الفعلي") },
            text = {
                Column {
                    Text("هل أنت متأكد من اعتماد نتائج الجرد؟")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("سيتم تحديث كميات المخزن تلقائياً لتطابق الكميات المعدودة فعلياً، وسيتم قفل الجلسة لمنع أي تعديل لاحق.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("إجمالي الفروقات: ${session.totalDiscrepancyQty} وحدة (${numFmt.format(session.totalDiscrepancyValue)} $currencySymbol)", fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.approveAuditSession(session.id)
                        showApproveConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                ) {
                    Text("نعم، اعتمد وحدّث المخزون")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApproveConfirmDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun AuditItemRow(
    item: InventoryAuditItem,
    currencySymbol: String,
    isReadOnly: Boolean,
    onCountChanged: (Double) -> Unit,
    onReasonClick: () -> Unit
) {
    val numFmt = remember { NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 } }
    val diff = item.discrepancyQty
    val hasDiff = diff != 0.0

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasDiff) {
                if (diff < 0) RoseDanger.copy(alpha = 0.08f) else EmeraldSuccess.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.productName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("SKU: ${item.sku} • في النظام: ${item.systemQty}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Physical Count Controls
                if (!isReadOnly) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (item.countedQty > 0) onCountChanged(item.countedQty - 1.0)
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "تقليل", modifier = Modifier.size(16.dp))
                        }

                        Text(
                            text = "${item.countedQty.toInt()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        IconButton(
                            onClick = { onCountChanged(item.countedQty + 1.0) },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "زيادة", modifier = Modifier.size(16.dp))
                        }
                    }
                } else {
                    Text(
                        text = "الفعلي: ${item.countedQty}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            if (hasDiff) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الفرق: ${if (diff > 0) "+$diff" else "$diff"} (${numFmt.format(item.discrepancyValue)} $currencySymbol)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (diff < 0) RoseDanger else EmeraldSuccess
                    )

                    TextButton(
                        onClick = onReasonClick,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (item.reason.isNotBlank()) "السبب: ${item.reason}" else "+ تسجيل السبب",
                            fontSize = 11.sp,
                            color = CyanAccentMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NewAuditSessionDialog(
    warehouses: List<Warehouse>,
    onDismiss: () -> Unit,
    onCreate: (title: String, warehouseId: Long, warehouseName: String) -> Unit
) {
    var title by remember { mutableStateOf("جرد دوري ${SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())}") }
    var selectedWh by remember { mutableStateOf(warehouses.firstOrNull() ?: Warehouse(id = 1, name = "المخزن الرئيسي", code = "MAIN")) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("بدء جلسة جرد مخزني جديدة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان الجلسة") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("اختر المخزن المراد جرده:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))

                warehouses.forEach { wh ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedWh = wh }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedWh.id == wh.id,
                            onClick = { selectedWh = wh }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(wh.name, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء")
                    }
                    Button(
                        onClick = {
                            onCreate(title, selectedWh.id, selectedWh.name)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text("بدء الجرد الآن")
                    }
                }
            }
        }
    }
}
