package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.viewmodel.InventoryViewModel

@Composable
fun SettingsAndAboutScreen(viewModel: InventoryViewModel) {
    val language by viewModel.language.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val allowNegativeStock by viewModel.allowNegativeStock.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showResetConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text("الإعدادات والصلاحيات", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        // User Profile & Role Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(BluePrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(currentUser.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("الدور الحالي: ${currentUser.role.titleAr}", fontSize = 12.sp, color = CyanAccentMuted, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("تبديل دور المستخدم للتجربة والاختبار:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = currentUser.role == UserRole.OWNER,
                        onClick = { viewModel.switchUserRole(UserRole.OWNER) },
                        label = { Text("مالك", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = currentUser.role == UserRole.MANAGER,
                        onClick = { viewModel.switchUserRole(UserRole.MANAGER) },
                        label = { Text("مدير", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = currentUser.role == UserRole.AUDITOR,
                        onClick = { viewModel.switchUserRole(UserRole.AUDITOR) },
                        label = { Text("موظف جرد", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = currentUser.role == UserRole.SALES,
                        onClick = { viewModel.switchUserRole(UserRole.SALES) },
                        label = { Text("مبيعات", fontSize = 11.sp) }
                    )
                }
            }
        }

        // System Settings
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("تفضيلات النظام والعملة", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                // Currency
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("العملة المعتمدة", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("الدينار العراقي (IQD)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = currency == "IQD",
                            onClick = { viewModel.setCurrency("IQD", "د.ع") },
                            label = { Text("د.ع IQD", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = currency == "USD",
                            onClick = { viewModel.setCurrency("USD", "$") },
                            label = { Text("$ USD", fontSize = 11.sp) }
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Language
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("لغة الواجهة", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(if (language == "ar") "العربية (RTL)" else "English (LTR)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = language == "ar",
                            onClick = { viewModel.setLanguage("ar") },
                            label = { Text("العربية", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = language == "en",
                            onClick = { viewModel.setLanguage("en") },
                            label = { Text("English", fontSize = 11.sp) }
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Negative stock toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("السماح بالبيع بالسالب", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("إتمام البيع حتى لو كانت كمية المخزن غير كافية", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = allowNegativeStock,
                        onCheckedChange = { viewModel.setAllowNegativeStock(it) },
                        modifier = Modifier.testTag("negative_stock_switch")
                    )
                }
            }
        }

        // Demo Data Management Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("إدارة البيانات التجريبية", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "يمكنك استعادة مجموعة البيانات التجريبية الشاملة للاختبار (منتجات، جلسات جرد، مشتريات، مبيعات وموردين).",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showResetConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberWarning)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إعادة تحميل البيانات التجريبية")
                }
            }
        }

        // Official Intellectual Property and Copyright Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BrandNavyDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF132238))
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "AMEEN INVENTORY AI",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "جرد أمين الذكي",
                    color = CyanAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "نظام متكامل لإدارة المخازن والجرد والمبيعات والمشتريات مدعوماً بالذكاء الاصطناعي.",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = Color(0xFF2A4365))
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "حقوق التصميم والبرمجة والنشر:",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "© 2026 أمين الشريفي — Ameen Al-Sharifi. All Rights Reserved.",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("استعادة البيانات التجريبية") },
            text = { Text("هل ترغب في إعادة ضبط البيانات وتحميل عينات تجريبية واقعية جديدة؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetDemoData()
                        showResetConfirm = false
                    }
                ) {
                    Text("نعم، استعد البيانات")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
