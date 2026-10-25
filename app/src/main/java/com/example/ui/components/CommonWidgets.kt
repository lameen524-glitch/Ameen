package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    accentColor: Color = CyanAccentMuted,
    trendText: String? = null,
    isPositiveTrend: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                if (trendText != null) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isPositiveTrend) EmeraldSuccess.copy(alpha = 0.15f) else RoseDanger.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isPositiveTrend) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (isPositiveTrend) EmeraldSuccess else RoseDanger,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = trendText,
                                color = if (isPositiveTrend) EmeraldSuccess else RoseDanger,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor
                )
            }
        }
    }
}

@Composable
fun BarcodeVisualView(
    barcodeText: String,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val hash = (if (barcodeText.isNotBlank()) barcodeText else "123456789012").hashCode()
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            val totalBars = 45
            val barWidth = size.width / (totalBars * 1.5f)
            var currentX = 10f
            for (i in 0 until totalBars) {
                val isThick = ((hash shr (i % 20)) and 1) == 1 || i % 3 == 0
                val width = if (isThick) barWidth * 1.4f else barWidth * 0.7f
                val barHeight = if (i < 4 || i > totalBars - 5 || (i > 20 && i < 25)) size.height else size.height * 0.85f
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(currentX, 0f),
                    size = Size(width, barHeight)
                )
                currentX += width + barWidth * 0.6f
                if (currentX >= size.width - 10f) break
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = barcodeText.ifBlank { "NO BARCODE" },
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            letterSpacing = 2.sp
        )
    }
}

@Composable
fun BarcodeScannerDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onBarcodeScanned: (String) -> Unit,
    sampleBarcodes: List<Pair<String, String>> = emptyList()
) {
    if (!isOpen) return

    var manualCode by remember { mutableStateOf("") }
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = BrandNavyDark,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "قارئ الباركود الذكي",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Simulated Viewfinder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F172A))
                        .border(2.dp, CyanAccentMuted, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Draw corner viewfinder targets
                        val cornerLen = 30f
                        val strokeW = 4f
                        val cColor = Color(0xFF00E5FF)
                        // Top-left
                        drawLine(cColor, Offset(20f, 20f), Offset(20f + cornerLen, 20f), strokeW)
                        drawLine(cColor, Offset(20f, 20f), Offset(20f, 20f + cornerLen), strokeW)
                        // Top-right
                        drawLine(cColor, Offset(size.width - 20f, 20f), Offset(size.width - 20f - cornerLen, 20f), strokeW)
                        drawLine(cColor, Offset(size.width - 20f, 20f), Offset(size.width - 20f, 20f + cornerLen), strokeW)
                        // Bottom-left
                        drawLine(cColor, Offset(20f, size.height - 20f), Offset(20f + cornerLen, size.height - 20f), strokeW)
                        drawLine(cColor, Offset(20f, size.height - 20f), Offset(20f, size.height - 20f - cornerLen), strokeW)
                        // Bottom-right
                        drawLine(cColor, Offset(size.width - 20f, size.height - 20f), Offset(size.width - 20f - cornerLen, size.height - 20f), strokeW)
                        drawLine(cColor, Offset(size.width - 20f, size.height - 20f), Offset(size.width - 20f, size.height - 20f - cornerLen), strokeW)

                        // Red scanning laser line
                        val yPos = size.height * laserY
                        drawLine(
                            color = Color(0xFFFF3366),
                            start = Offset(25f, yPos),
                            end = Offset(size.width - 25f, yPos),
                            strokeWidth = 3f
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = CyanAccent.copy(alpha = 0.7f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "وجّه الكاميرا أو القارئ نحو الباركود",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Manual barcode entry or quick scan options
                OutlinedTextField(
                    value = manualCode,
                    onValueChange = { manualCode = it },
                    label = { Text("أو اكتب الباركود / SKU يدوياً") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("barcode_manual_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedLabelColor = CyanAccent,
                        unfocusedLabelColor = Color(0xFF94A3B8)
                    ),
                    trailingIcon = {
                        if (manualCode.isNotBlank()) {
                            IconButton(onClick = {
                                onBarcodeScanned(manualCode.trim())
                                onDismiss()
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "تأكيد", tint = CyanAccent)
                            }
                        }
                    }
                )

                if (sampleBarcodes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "مسح سريع لمنتج من المخزن:",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sampleBarcodes.take(2).forEach { (code, name) ->
                            SuggestionChip(
                                onClick = {
                                    onBarcodeScanned(code)
                                    onDismiss()
                                },
                                label = { Text(name, maxLines = 1, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (manualCode.isNotBlank()) {
                            onBarcodeScanned(manualCode.trim())
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("confirm_scan_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إدخال الكود والبحث")
                }
            }
        }
    }
}
