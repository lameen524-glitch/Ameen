package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppBottomNavigationBar
import com.example.ui.components.AppTopBar
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.InventoryViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: InventoryViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val language by viewModel.language.collectAsState()
    val feedbackMessage by viewModel.feedbackMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val layoutDirection = if (language == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

    // Show feedback messages as snackbars
    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    // System BackHandler
    BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else if (currentScreen == AppScreen.AUDIT_SESSION_RUNNER) {
            viewModel.navigateTo(AppScreen.AUDIT)
        } else if (currentScreen == AppScreen.PRODUCT_DETAIL) {
            viewModel.navigateTo(AppScreen.PRODUCTS)
        } else {
            viewModel.navigateTo(AppScreen.DASHBOARD)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(300.dp),
                    drawerContainerColor = MaterialTheme.colorScheme.surface
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text(
                            text = "جرد أمين الذكي",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "AMEEN INVENTORY AI",
                            fontSize = 11.sp,
                            color = CyanAccentMuted,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                        label = { Text("لوحة التحكم الرئيسية") },
                        selected = currentScreen == AppScreen.DASHBOARD,
                        onClick = {
                            viewModel.navigateTo(AppScreen.DASHBOARD)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Inventory2, contentDescription = null) },
                        label = { Text("إدارة المنتجات والأصناف") },
                        selected = currentScreen == AppScreen.PRODUCTS,
                        onClick = {
                            viewModel.navigateTo(AppScreen.PRODUCTS)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.FactCheck, contentDescription = null) },
                        label = { Text("نظام الجرد الفعلي الذكي") },
                        selected = currentScreen == AppScreen.AUDIT,
                        onClick = {
                            viewModel.navigateTo(AppScreen.AUDIT)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.PointOfSale, contentDescription = null) },
                        label = { Text("المبيعات ونقطة البيع (POS)") },
                        selected = currentScreen == AppScreen.POS_SALES,
                        onClick = {
                            viewModel.navigateTo(AppScreen.POS_SALES)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.ShoppingBag, contentDescription = null) },
                        label = { Text("المشتريات وتوريد البضائع") },
                        selected = currentScreen == AppScreen.PURCHASES,
                        onClick = {
                            viewModel.navigateTo(AppScreen.PURCHASES)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.People, contentDescription = null) },
                        label = { Text("الشركاء (الموردون والعملاء والديون)") },
                        selected = currentScreen == AppScreen.PARTNERS,
                        onClick = {
                            viewModel.navigateTo(AppScreen.PARTNERS)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                        label = { Text("المالية والمصاريف والأرباح") },
                        selected = currentScreen == AppScreen.FINANCE,
                        onClick = {
                            viewModel.navigateTo(AppScreen.FINANCE)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Warehouse, contentDescription = null) },
                        label = { Text("المخازن المتعددة والمناقلات") },
                        selected = currentScreen == AppScreen.WAREHOUSES,
                        onClick = {
                            viewModel.navigateTo(AppScreen.WAREHOUSES)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.SmartToy, contentDescription = null, tint = PurpleAI) },
                        label = { Text("أمين AI (المستشار الذكي)", color = PurpleAI, fontWeight = FontWeight.Bold) },
                        selected = currentScreen == AppScreen.AMEEN_AI,
                        onClick = {
                            viewModel.navigateTo(AppScreen.AMEEN_AI)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                        label = { Text("مركز التنبيهات") },
                        selected = currentScreen == AppScreen.NOTIFICATIONS,
                        onClick = {
                            viewModel.navigateTo(AppScreen.NOTIFICATIONS)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("الإعدادات وحول النظام") },
                        selected = currentScreen == AppScreen.SETTINGS,
                        onClick = {
                            viewModel.navigateTo(AppScreen.SETTINGS)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "© 2026 أمين الشريفي — Ameen Al-Sharifi",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        ) {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("main_scaffold"),
                topBar = {
                    AppTopBar(
                        viewModel = viewModel,
                        onMenuClick = {
                            coroutineScope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        }
                    )
                },
                bottomBar = {
                    AppBottomNavigationBar(
                        currentScreen = currentScreen,
                        onNavigate = { viewModel.navigateTo(it) },
                        onMoreClick = {
                            coroutineScope.launch { drawerState.open() }
                        }
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (currentScreen) {
                        AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                        AppScreen.PRODUCTS -> ProductsScreen(viewModel = viewModel)
                        AppScreen.PRODUCT_DETAIL -> ProductDetailScreen(viewModel = viewModel)
                        AppScreen.AUDIT, AppScreen.AUDIT_SESSION_RUNNER -> InventoryAuditScreen(viewModel = viewModel)
                        AppScreen.POS_SALES -> PosSalesScreen(viewModel = viewModel)
                        AppScreen.PURCHASES -> PurchasesScreen(viewModel = viewModel)
                        AppScreen.PARTNERS -> PartnersScreen(viewModel = viewModel)
                        AppScreen.FINANCE -> FinanceScreen(viewModel = viewModel)
                        AppScreen.WAREHOUSES -> MultiWarehouseScreen(viewModel = viewModel)
                        AppScreen.AMEEN_AI -> AmeenAiScreen(viewModel = viewModel)
                        AppScreen.NOTIFICATIONS -> NotificationsScreen(viewModel = viewModel)
                        AppScreen.SETTINGS -> SettingsAndAboutScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
