package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.ui.components.StatCard
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.InventoryViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: InventoryViewModel) {
    val productCount by viewModel.productCount.collectAsState()
    val totalQty by viewModel.totalQuantity.collectAsState()
    val totalCost by viewModel.totalCostValue.collectAsState()
    val totalSale by viewModel.totalSaleValue.collectAsState()
    val lowStockCount by viewModel.lowStockCount.collectAsState()
    val outOfStockCount by viewModel.outOfStockCount.collectAsState()
    val totalSales by viewModel.totalSalesAmount.collectAsState()
    val totalPurchases by viewModel.totalPurchasesAmount.collectAsState()
    val totalExpenses by viewModel.totalExpensesAmount.collectAsState()
    val customerDebts by viewModel.totalCustomerDebts.collectAsState()
    val supplierDebts by viewModel.totalSupplierDebts.collectAsState()
    val products by viewModel.products.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val auditSessions by viewModel.auditSessions.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    val numFmt = remember { NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 } }

    val grossProfit = totalSales * 0.25
    val netProfit = grossProfit - totalExpenses

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // Hero SaaS Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BrandNavyDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "لوحة التحكم الذكية — Ameen Dashboard",
                                    color = CyanAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "نظام جرد أمين المتطور AI",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = CyanAccent.copy(alpha = 0.15f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sensors,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.padding(8.dp).size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Financial Snapshot Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF132238))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("إجمالي المخزون (تكلفة)", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text("${numFmt.format(totalCost)} $currencySymbol", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            VerticalDivider(
                                modifier = Modifier.height(30.dp),
                                color = Color(0xFF2A4365)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("قيمة البيع المتوقعة", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text("${numFmt.format(totalSale)} $currencySymbol", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            VerticalDivider(
                                modifier = Modifier.height(30.dp),
                                color = Color(0xFF2A4365)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("صافي الربح", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text(
                                    "${numFmt.format(netProfit)} $currencySymbol",
                                    color = if (netProfit >= 0) EmeraldSuccess else RoseDanger,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Action Shortcuts
        item {
            Text(
                text = "الإجراءات السريعة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionCard(
                    title = "جرد فعلي",
                    subtitle = "مطابقة المخزن",
                    icon = Icons.Default.FactCheck,
                    color = BluePrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.AUDIT) }
                )
                ActionCard(
                    title = "بيع سريع (POS)",
                    subtitle = "فاتورة جديدة",
                    icon = Icons.Default.PointOfSale,
                    color = EmeraldSuccess,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.POS_SALES) }
                )
                ActionCard(
                    title = "أمين AI",
                    subtitle = "استشارة ذكية",
                    icon = Icons.Default.SmartToy,
                    color = PurpleAI,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.AMEEN_AI) }
                )
            }
        }

        // Section: Core Stock & Products Metrics (Items 1-6)
        item {
            Text(
                text = "حالة المخزون والأصناف",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "عدد الأصناف",
                    value = "$productCount منتج",
                    subtitle = "إجمالي المواد المعرفة",
                    icon = Icons.Default.Category,
                    accentColor = BluePrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.PRODUCTS) }
                )
                StatCard(
                    title = "إجمالي الوحدات",
                    value = "${numFmt.format(totalQty)} قطعة",
                    subtitle = "في كل المستودعات",
                    icon = Icons.Default.AllInbox,
                    accentColor = CyanAccentMuted,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "منخفض المخزون",
                    value = "$lowStockCount صنف",
                    subtitle = "وصل لحد إعادة الطلب",
                    icon = Icons.Default.WarningAmber,
                    accentColor = AmberWarning,
                    trendText = if (lowStockCount > 0) "يلزم الشراء" else "آمن",
                    isPositiveTrend = lowStockCount == 0,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.NOTIFICATIONS) }
                )
                StatCard(
                    title = "أصناف نافدة",
                    value = "$outOfStockCount صنف",
                    subtitle = "الكمية صفر حالياً",
                    icon = Icons.Default.HighlightOff,
                    accentColor = RoseDanger,
                    trendText = if (outOfStockCount > 0) "حرج!" else "ممتاز",
                    isPositiveTrend = outOfStockCount == 0,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.NOTIFICATIONS) }
                )
            }
        }

        // Section: Sales, Purchases, Profits & Debts (Items 7-10)
        item {
            Text(
                text = "المؤشرات المالية والمبيعات",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "إجمالي المبيعات",
                    value = "${numFmt.format(totalSales)} $currencySymbol",
                    subtitle = "مبيعات نقدية وآجلة",
                    icon = Icons.Default.AttachMoney,
                    accentColor = EmeraldSuccess,
                    isPositiveTrend = true,
                    trendText = "نشط",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.POS_SALES) }
                )
                StatCard(
                    title = "إجمالي المشتريات",
                    value = "${numFmt.format(totalPurchases)} $currencySymbol",
                    subtitle = "وارد البضائع للمخزن",
                    icon = Icons.Default.ShoppingBag,
                    accentColor = BluePrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.PURCHASES) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "ديون لنا بذمة العملاء",
                    value = "${numFmt.format(customerDebts)} $currencySymbol",
                    subtitle = "مستحقات للتحصيل",
                    icon = Icons.Default.CallReceived,
                    accentColor = AmberWarning,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.PARTNERS) }
                )
                StatCard(
                    title = "ديون علينا للموردين",
                    value = "${numFmt.format(supplierDebts)} $currencySymbol",
                    subtitle = "واجبة السداد",
                    icon = Icons.Default.CallMade,
                    accentColor = RoseDanger,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.PARTNERS) }
                )
            }
        }

        // Visual Breakdown Chart (Sales vs Purchases vs Expenses)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "مقارنة التدفق المالي (مبيعات vs مشتريات vs مصاريف)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val maxVal = maxOf(totalSales, totalPurchases, totalExpenses, 100000.0)

                    ChartBar(label = "المبيعات", value = totalSales, max = maxVal, color = EmeraldSuccess, currency = currencySymbol)
                    Spacer(modifier = Modifier.height(10.dp))
                    ChartBar(label = "المشتريات", value = totalPurchases, max = maxVal, color = BluePrimary, currency = currencySymbol)
                    Spacer(modifier = Modifier.height(10.dp))
                    ChartBar(label = "المصاريف التشغيلية", value = totalExpenses, max = maxVal, color = RoseDanger, currency = currencySymbol)
                }
            }
        }

        // Top Products & Stagnant Items
        item {
            Text(
                text = "حركة المنتجات الأبرز",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    products.take(4).forEach { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable { viewModel.viewProductDetail(p) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when {
                                                p.quantity <= 0 -> RoseDanger.copy(alpha = 0.2f)
                                                p.quantity <= p.minThreshold -> AmberWarning.copy(alpha = 0.2f)
                                                else -> EmeraldSuccess.copy(alpha = 0.2f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = null,
                                        tint = when {
                                            p.quantity <= 0 -> RoseDanger
                                            p.quantity <= p.minThreshold -> AmberWarning
                                            else -> EmeraldSuccess
                                        },
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(p.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1)
                                    Text("SKU: ${p.sku} • الرف: ${p.shelfLocation.ifBlank { "غير محدد" }}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${p.quantity} ${p.unit}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${numFmt.format(p.salePrice)} $currencySymbol", fontSize = 11.sp, color = CyanAccentMuted)
                            }
                        }
                        if (p != products.take(4).last()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        }
                    }
                }
            }
        }

        // Recent Audit Log Feed
        item {
            Text(
                text = "سجل العمليات والتدقيق الأخير",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (auditLogs.isEmpty()) {
                        Text("لا توجد عمليات مسجلة حتى الآن", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    } else {
                        auditLogs.take(5).forEach { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (log.action) {
                                                "SALE" -> EmeraldSuccess
                                                "PURCHASE" -> BluePrimary
                                                "APPROVE_AUDIT" -> CyanAccent
                                                "DELETE" -> RoseDanger
                                                else -> AmberWarning
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(log.details, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text(
                                        "${log.userName} (${log.userRole}) • ${SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault()).format(Date(log.timestamp))}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Mandatory Copyright Footer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "AMEEN INVENTORY AI — جرد أمين الذكي",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "© 2026 أمين الشريفي — Ameen Al-Sharifi. All Rights Reserved.",
                    fontSize = 11.sp,
                    color = CyanAccentMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
private fun ChartBar(label: String, value: Double, max: Double, color: Color, currency: String) {
    val numFmt = remember { NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 } }
    val fraction = (value / max).toFloat().coerceIn(0.02f, 1f)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("${numFmt.format(value)} $currency", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}
