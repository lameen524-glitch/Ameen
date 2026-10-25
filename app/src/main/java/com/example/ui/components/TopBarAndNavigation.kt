package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Warehouse
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    viewModel: InventoryViewModel,
    onMenuClick: () -> Unit
) {
    val warehouses by viewModel.warehouses.collectAsState()
    val selectedWhId by viewModel.selectedWarehouseFilter.collectAsState()
    val lowStockCount by viewModel.lowStockCount.collectAsState()
    val outOfStockCount by viewModel.outOfStockCount.collectAsState()
    val totalAlerts = lowStockCount + outOfStockCount
    val language by viewModel.language.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showWarehouseDropdown by remember { mutableStateOf(false) }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.DASHBOARD) }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BrandNavyDark)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (language == "ar") "جرد أمين الذكي" else "AMEEN INVENTORY",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "AMEEN INVENTORY AI",
                        fontSize = 10.sp,
                        color = CyanAccentMuted,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        actions = {
            // Warehouse Filter Selector
            Box {
                AssistChip(
                    onClick = { showWarehouseDropdown = true },
                    label = {
                        val currentWhName = warehouses.find { it.id == selectedWhId }?.name ?: "كل المخازن"
                        Text(currentWhName, maxLines = 1, fontSize = 11.sp)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Warehouse, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    modifier = Modifier.testTag("warehouse_selector_chip")
                )

                DropdownMenu(
                    expanded = showWarehouseDropdown,
                    onDismissRequest = { showWarehouseDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("🏢 كل المخازن الموحدة", fontWeight = if (selectedWhId == 0L) FontWeight.Bold else FontWeight.Normal) },
                        onClick = {
                            viewModel.setSelectedWarehouseFilter(0L)
                            showWarehouseDropdown = false
                        }
                    )
                    warehouses.forEach { wh ->
                        DropdownMenuItem(
                            text = { Text("${if (wh.isMain) "⭐ " else "📍 "}${wh.name}") },
                            onClick = {
                                viewModel.setSelectedWarehouseFilter(wh.id)
                                showWarehouseDropdown = false
                            }
                        )
                    }
                }
            }

            // Ameen AI Quick Button
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.AMEEN_AI) },
                modifier = Modifier.testTag("ai_assistant_top_button")
            ) {
                BadgedBox(
                    badge = {
                        Badge(containerColor = PurpleAI) {
                            Text("AI", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "أمين AI",
                        tint = PurpleAI
                    )
                }
            }

            // Notifications Bell
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.NOTIFICATIONS) },
                modifier = Modifier.testTag("notifications_bell_button")
            ) {
                BadgedBox(
                    badge = {
                        if (totalAlerts > 0) {
                            Badge(containerColor = RoseDanger) {
                                Text("$totalAlerts", fontSize = 10.sp)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "التنبيهات",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Menu Drawer toggle
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.testTag("top_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "القائمة",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}

@Composable
fun AppBottomNavigationBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    onMoreClick: () -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == AppScreen.DASHBOARD,
            onClick = { onNavigate(AppScreen.DASHBOARD) },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "الرئيسية") },
            label = { Text("الرئيسية", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_dashboard")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.PRODUCTS || currentScreen == AppScreen.PRODUCT_DETAIL,
            onClick = { onNavigate(AppScreen.PRODUCTS) },
            icon = { Icon(Icons.Default.Inventory2, contentDescription = "المنتجات") },
            label = { Text("المنتجات", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_products")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.AUDIT || currentScreen == AppScreen.AUDIT_SESSION_RUNNER,
            onClick = { onNavigate(AppScreen.AUDIT) },
            icon = { Icon(Icons.Default.FactCheck, contentDescription = "الجرد") },
            label = { Text("الجرد", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_audit")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.POS_SALES,
            onClick = { onNavigate(AppScreen.POS_SALES) },
            icon = { Icon(Icons.Default.PointOfSale, contentDescription = "المبيعات") },
            label = { Text("المبيعات", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_pos")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.AMEEN_AI,
            onClick = { onNavigate(AppScreen.AMEEN_AI) },
            icon = { Icon(Icons.Default.SmartToy, contentDescription = "أمين AI", tint = PurpleAI) },
            label = { Text("أمين AI", fontSize = 11.sp, color = PurpleAI, fontWeight = FontWeight.Bold) },
            modifier = Modifier.testTag("nav_ai")
        )

        NavigationBarItem(
            selected = false,
            onClick = onMoreClick,
            icon = { Icon(Icons.Default.Widgets, contentDescription = "المزيد") },
            label = { Text("المزيد", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_more")
        )
    }
}
