package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BarcodeVisualView
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.InventoryViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(viewModel: InventoryViewModel) {
    val product = viewModel.selectedProduct.collectAsState().value
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val numFmt = remember { NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 } }

    BackHandler {
        viewModel.navigateTo(AppScreen.PRODUCTS)
    }

    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لم يتم تحديد أي منتج")
        }
        return
    }

    val profitPerUnit = product.salePrice - product.costPrice
    val marginPercent = if (product.costPrice > 0) (profitPerUnit / product.costPrice) * 100 else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product.name, maxLines = 1, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.PRODUCTS) },
                        modifier = Modifier.testTag("product_detail_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.duplicateProduct(product) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ")
                    }
                    IconButton(onClick = {
                        viewModel.addToCart(product)
                        viewModel.showFeedback("تمت إضافة المنتج إلى سلة المبيعات")
                    }) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = "بيع")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Printable Barcode & SKU Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ملصق الباركود للمنتج — Barcode Label",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    BarcodeVisualView(
                        barcodeText = if (product.barcode.isNotBlank()) product.barcode else product.sku,
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${product.name} • ${numFmt.format(product.salePrice)} $currencySymbol",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.showFeedback("تم إرسال ملصق الباركود إلى طابعة الملصقات الحرارية") },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccentMuted)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("طباعة ملصق الرف والباركود", fontSize = 12.sp)
                    }
                }
            }

            // Financial & Profit Margin Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "التحليل المالي وهوامش الربح",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("سعر التكلفة", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${numFmt.format(product.costPrice)} $currencySymbol", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text("سعر البيع", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${numFmt.format(product.salePrice)} $currencySymbol", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("ربح القطعة", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "${numFmt.format(profitPerUnit)} $currencySymbol (${String.format("%.1f", marginPercent)}%)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = EmeraldSuccess
                            )
                        }
                    }
                }
            }

            // Inventory & Location Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "بيانات التخزين والموقع",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    DetailRow(label = "الكمية المتوفرة حالياً", value = "${product.quantity} ${product.unit}")
                    DetailRow(label = "الحد الأدنى للمخزون", value = "${product.minThreshold} ${product.unit}")
                    DetailRow(label = "موقع التخزين ورقم الرف", value = product.shelfLocation.ifBlank { "غير محدد" })
                    DetailRow(label = "التصنيف / القسم", value = product.category)
                    DetailRow(label = "الشركة المصنعة / الماركة", value = product.brand.ifBlank { "غير مسجلة" })
                    DetailRow(label = "اسم المورد المعتمد", value = product.supplierName.ifBlank { "غير مسجل" })
                    DetailRow(label = "تاريخ الإضافة", value = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(product.createdAt)))
                }
            }

            // Description
            if (product.description.isNotBlank()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("وصف المنتج وملاحظاته", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(product.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
