package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.InventoryViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun NotificationsScreen(viewModel: InventoryViewModel) {
    val products by viewModel.products.collectAsState()
    val auditSessions by viewModel.auditSessions.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val numFmt = remember { NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 } }

    val outOfStock = products.filter { it.quantity <= 0 }
    val lowStock = products.filter { it.quantity <= it.minThreshold && it.quantity > 0 }
    val sessionsWithDiff = auditSessions.filter { it.totalDiscrepancyQty != 0.0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("مركز التنبيهات الذكية", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "إشعارات فورية للنواقص، والكميات الحرجة، وفروقات الجرد.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = CircleShape,
                color = RoseDanger.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "${outOfStock.size + lowStock.size} تنبيه",
                    color = RoseDanger,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Out of stock items (CRITICAL)
            if (outOfStock.isNotEmpty()) {
                item {
                    Text("🚨 أصناف نافدة كلياً (الكمية 0):", fontWeight = FontWeight.Bold, color = RoseDanger, fontSize = 13.sp)
                }
                items(outOfStock) { p ->
                    NotificationCard(
                        title = p.name,
                        message = "الكمية الحالية 0. المورد المقترح: ${p.supplierName.ifBlank { "مورد معتمد" }} (SKU: ${p.sku})",
                        tag = "نفاد تام",
                        tagColor = RoseDanger,
                        icon = Icons.Default.Dangerous,
                        onAction = { viewModel.viewProductDetail(p) }
                    )
                }
            }

            // Low stock items
            if (lowStock.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("⚠️ أصناف بلغت الحد الأدنى للطلب:", fontWeight = FontWeight.Bold, color = AmberWarning, fontSize = 13.sp)
                }
                items(lowStock) { p ->
                    NotificationCard(
                        title = p.name,
                        message = "المتبقي: ${p.quantity} ${p.unit} (الحد الأدنى: ${p.minThreshold}). يُنصح بطلب دفعة جديدة.",
                        tag = "منخفض",
                        tagColor = AmberWarning,
                        icon = Icons.Default.WarningAmber,
                        onAction = { viewModel.viewProductDetail(p) }
                    )
                }
            }

            // Inventory discrepancy alerts
            if (sessionsWithDiff.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("📋 تنبيهات فروقات الجرد الفعلي:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                items(sessionsWithDiff) { s ->
                    NotificationCard(
                        title = s.title,
                        message = "تسجيل فروقات بمقدار ${s.totalDiscrepancyQty} قطعة بقيمة ${numFmt.format(s.totalDiscrepancyValue)} $currencySymbol في ${s.warehouseName}",
                        tag = "فروقات جرد",
                        tagColor = CyanAccentMuted,
                        icon = Icons.Default.FactCheck,
                        onAction = { viewModel.openAuditSession(s.id) }
                    )
                }
            }

            if (outOfStock.isEmpty() && lowStock.isEmpty() && sessionsWithDiff.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("جميع المؤشرات ممتازة!", fontWeight = FontWeight.Bold)
                            Text("لا توجد نواقص أو مشكلات في المخزون حالياً.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    title: String,
    message: String,
    tag: String,
    tagColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onAction: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAction() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(tagColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = tagColor, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = tagColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = tag,
                            color = tagColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(message, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
