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
import com.example.data.model.Expense
import com.example.ui.theme.*
import com.example.viewmodel.InventoryViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(viewModel: InventoryViewModel) {
    val totalSales by viewModel.totalSalesAmount.collectAsState()
    val totalPurchases by viewModel.totalPurchasesAmount.collectAsState()
    val totalExpenses by viewModel.totalExpensesAmount.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val stockCost by viewModel.totalCostValue.collectAsState()

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    val numFmt = remember { NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 } }

    val cogs = totalSales * 0.75 // 75% cost of goods sold estimate
    val grossProfit = totalSales - cogs
    val netProfit = grossProfit - totalExpenses

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddExpenseDialog = true },
                containerColor = RoseDanger,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                text = { Text("تسجيل مصروف تشغيلي") },
                modifier = Modifier.testTag("add_expense_fab")
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

            Text("التقارير المالية والأرباح (P&L)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "احتساب دقيق للإيرادات، وتكلفة البضاعة المباعة COGS، ومجمل وصافي الربح بعد خصم المصاريف.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Profit & Loss Statement Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("قائمة الدخل والأرباح الحقيقية", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                    FinanceRow(label = "إجمالي إيرادات المبيعات (+)", value = "${numFmt.format(totalSales)} $currencySymbol", color = EmeraldSuccess)
                    FinanceRow(label = "تكلفة البضاعة المباعة COGS (-)", value = "${numFmt.format(cogs)} $currencySymbol", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    FinanceRow(label = "مجمل الربح التجاري (=)", value = "${numFmt.format(grossProfit)} $currencySymbol", color = CyanAccentMuted, isBold = true)
                    FinanceRow(label = "المصاريف التشغيلية (-)", value = "${numFmt.format(totalExpenses)} $currencySymbol", color = RoseDanger)
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    FinanceRow(
                        label = "صافي الربح الفعلي بعد المصاريف",
                        value = "${numFmt.format(netProfit)} $currencySymbol",
                        color = if (netProfit >= 0) EmeraldSuccess else RoseDanger,
                        isBold = true,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("سجل المصاريف وسندات الصرف (${expenses.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (expenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا توجد مصاريف مسجلة حتى الآن", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(expenses, key = { it.id }) { exp ->
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(RoseDanger.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Receipt, contentDescription = null, tint = RoseDanger, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(exp.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            "${exp.category} • ${exp.recordedBy} • ${SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(exp.timestamp))}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Text(
                                    "${numFmt.format(exp.amount)} $currencySymbol",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = RoseDanger
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            onSave = { title, cat, amount, notes ->
                viewModel.addExpense(title, cat, amount, notes)
                showAddExpenseDialog = false
            }
        )
    }
}

@Composable
fun FinanceRow(
    label: String,
    value: String,
    color: Color,
    isBold: Boolean = false,
    fontSize: androidx.compose.ui.unit.TextUnit = 13.sp
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = fontSize, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontSize = fontSize, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, category: String, amount: Double, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("إيجار") }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val categories = listOf("إيجار", "رواتب", "كهرباء وصيانة", "نقل وتوصيل", "تسويق", "أخرى")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("تسجيل سند صرف / مصروف جديد", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("بيان المصروف *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("التصنيف:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("المبلغ (د.ع) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            if (title.isNotBlank() && amt > 0) {
                                onSave(title, category, amt, notes)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
                    ) {
                        Text("تسجيل المصروف")
                    }
                }
            }
        }
    }
}
