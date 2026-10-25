package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.data.model.Customer
import com.example.data.model.Supplier
import com.example.ui.theme.*
import com.example.viewmodel.InventoryViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnersScreen(viewModel: InventoryViewModel) {
    val suppliers by viewModel.suppliers.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val totalCustDebts by viewModel.totalCustomerDebts.collectAsState()
    val totalSuppDebts by viewModel.totalSupplierDebts.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = Suppliers, 1 = Customers
    var showAddPartnerDialog by remember { mutableStateOf(false) }
    val numFmt = remember { NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 } }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddPartnerDialog = true },
                containerColor = BluePrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text(if (activeTab == 0) "إضافة مورد جديد" else "إضافة عميل جديد") },
                modifier = Modifier.testTag("add_partner_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            TabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("الموردون (${suppliers.size})") },
                    icon = { Icon(Icons.Default.Business, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("العملاء (${customers.size})") },
                    icon = { Icon(Icons.Default.People, contentDescription = null) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Debt Summary Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ديون مستحقة لنا (العملاء)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${numFmt.format(totalCustDebts)} $currencySymbol", fontWeight = FontWeight.Bold, color = AmberWarning, fontSize = 14.sp)
                    }
                    Divider(modifier = Modifier.height(30.dp).width(1.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ديون علينا (للموردين)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${numFmt.format(totalSuppDebts)} $currencySymbol", fontWeight = FontWeight.Bold, color = RoseDanger, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (activeTab == 0) {
                // Suppliers List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(suppliers, key = { it.id }) { s ->
                        PartnerCard(
                            name = s.name,
                            phone = s.phone,
                            address = s.address,
                            balanceDue = s.balanceDue,
                            notes = s.notes,
                            isSupplier = true,
                            currencySymbol = currencySymbol
                        )
                    }
                }
            } else {
                // Customers List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(customers, key = { it.id }) { c ->
                        PartnerCard(
                            name = c.name,
                            phone = c.phone,
                            address = c.address,
                            balanceDue = c.balanceDue,
                            notes = c.notes,
                            isSupplier = false,
                            currencySymbol = currencySymbol
                        )
                    }
                }
            }
        }
    }

    if (showAddPartnerDialog) {
        AddPartnerDialog(
            isSupplier = activeTab == 0,
            onDismiss = { showAddPartnerDialog = false },
            onSave = { name, phone, address, debt, notes ->
                if (activeTab == 0) {
                    viewModel.addSupplier(name, phone, address, debt, notes)
                } else {
                    viewModel.addCustomer(name, phone, address, debt, notes)
                }
                showAddPartnerDialog = false
            }
        )
    }
}

@Composable
fun PartnerCard(
    name: String,
    phone: String,
    address: String,
    balanceDue: Double,
    notes: String,
    isSupplier: Boolean,
    currencySymbol: String
) {
    val numFmt = remember { NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 } }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isSupplier) BluePrimary.copy(alpha = 0.2f) else CyanAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSupplier) Icons.Default.Business else Icons.Default.Person,
                            contentDescription = null,
                            tint = if (isSupplier) BluePrimary else CyanAccentMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (phone.isNotBlank()) {
                            Text("📞 $phone", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isSupplier) "مستحق له:" else "مستحق عليه:",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${numFmt.format(balanceDue)} $currencySymbol",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (balanceDue > 0) (if (isSupplier) RoseDanger else AmberWarning) else EmeraldSuccess
                    )
                }
            }

            if (address.isNotBlank() || notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                if (address.isNotBlank()) {
                    Text("📍 العنوان: $address", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (notes.isNotBlank()) {
                    Text("📝 $notes", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun AddPartnerDialog(
    isSupplier: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, address: String, initialDebt: Double, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var debt by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = if (isSupplier) "إضافة مورد جديد" else "إضافة عميل جديد",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("الاسم *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("العنوان / المدينة") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = debt,
                    onValueChange = { debt = it },
                    label = { Text("الرصيد الافتتاحي / الدين الحالي (د.ع)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(name, phone, address, debt.toDoubleOrNull() ?: 0.0, notes)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text("حفظ")
                    }
                }
            }
        }
    }
}
