package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiMessage
import com.example.ai.AmeenAiEngine
import com.example.ai.InventorySnapshot
import com.example.data.local.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppScreen {
    DASHBOARD,
    PRODUCTS,
    PRODUCT_DETAIL,
    AUDIT,
    AUDIT_SESSION_RUNNER,
    POS_SALES,
    PURCHASES,
    PARTNERS,
    FINANCE,
    WAREHOUSES,
    AMEEN_AI,
    NOTIFICATIONS,
    SETTINGS
}

data class CartItem(
    val product: Product,
    val quantity: Double = 1.0,
    val discountPercent: Double = 0.0
) {
    val unitPrice: Double get() = product.salePrice
    val totalPrice: Double get() = (unitPrice * quantity) * (1.0 - (discountPercent / 100.0))
}

class InventoryViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val dao = database.inventoryDao()

    // Navigation & Screen
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    private val _selectedAuditSessionId = MutableStateFlow<Long?>(null)
    val selectedAuditSessionId: StateFlow<Long?> = _selectedAuditSessionId.asStateFlow()

    // User & Preferences
    private val _currentUser = MutableStateFlow(CurrentUser())
    val currentUser: StateFlow<CurrentUser> = _currentUser.asStateFlow()

    private val _language = MutableStateFlow("ar")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _currency = MutableStateFlow("IQD")
    val currency: StateFlow<String> = _currency.asStateFlow()

    private val _currencySymbol = MutableStateFlow("د.ع")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _allowNegativeStock = MutableStateFlow(false)
    val allowNegativeStock: StateFlow<Boolean> = _allowNegativeStock.asStateFlow()

    // Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedWarehouseFilter = MutableStateFlow(0L) // 0 = All
    val selectedWarehouseFilter: StateFlow<Long> = _selectedWarehouseFilter.asStateFlow()

    // UI Feedback & Dialogs
    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    private val _isBarcodeScannerOpen = MutableStateFlow(false)
    val isBarcodeScannerOpen: StateFlow<Boolean> = _isBarcodeScannerOpen.asStateFlow()

    // Live Data Streams
    val products = dao.getAllProducts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val warehouses = dao.getAllWarehouses().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categories = dao.getAllCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val auditSessions = dao.getAllAuditSessions().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val sales = dao.getAllSales().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val purchases = dao.getAllPurchases().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val suppliers = dao.getAllSuppliers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val customers = dao.getAllCustomers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val expenses = dao.getAllExpenses().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val auditLogs = dao.getRecentAuditLogs().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val transfers = dao.getAllTransfers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Aggregates
    val productCount = dao.getProductCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val totalQuantity = dao.getTotalQuantity().map { it ?: 0.0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val totalCostValue = dao.getTotalCostValue().map { it ?: 0.0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val totalSaleValue = dao.getTotalSaleValue().map { it ?: 0.0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val lowStockCount = dao.getLowStockCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val outOfStockCount = dao.getOutOfStockCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val totalSalesAmount = dao.getTotalSalesAmount().map { it ?: 0.0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val totalPurchasesAmount = dao.getTotalPurchasesAmount().map { it ?: 0.0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val totalExpensesAmount = dao.getTotalExpenses().map { it ?: 0.0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val totalCustomerDebts = dao.getTotalCustomerDebts().map { it ?: 0.0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val totalSupplierDebts = dao.getTotalSupplierDebts().map { it ?: 0.0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // POS Cart State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _selectedCustomerId = MutableStateFlow<Long?>(null)
    val selectedCustomerId: StateFlow<Long?> = _selectedCustomerId.asStateFlow()

    private val _paymentMethod = MutableStateFlow("CASH")
    val paymentMethod: StateFlow<String> = _paymentMethod.asStateFlow()

    private val _paidAmountInput = MutableStateFlow("")
    val paidAmountInput: StateFlow<String> = _paidAmountInput.asStateFlow()

    // AI Messages
    private val _aiMessages = MutableStateFlow<List<AiMessage>>(listOf(
        AiMessage(
            sender = "AMEEN_AI",
            text = "مرحباً بك! أنا «أمين AI»، مساعدك الذكي لإدارة المخزون والجرد المتطور. اسألني عن النواقص، كميات إعادة الطلب، الأرباح، أو تحليل الجرد الفعلي."
        )
    ))
    val aiMessages: StateFlow<List<AiMessage>> = _aiMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // Active Audit Items
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeAuditItems: StateFlow<List<InventoryAuditItem>> = _selectedAuditSessionId
        .flatMapLatest { sessionId ->
            if (sessionId != null) dao.getAuditItems(sessionId) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Load initial settings
        viewModelScope.launch {
            dao.getSetting("APP_LANGUAGE")?.let { _language.value = it }
            dao.getSetting("CURRENCY_SYMBOL")?.let { _currencySymbol.value = it }
            dao.getSetting("ALLOW_NEGATIVE_STOCK")?.let { _allowNegativeStock.value = it.toBoolean() }
        }
    }

    // --- NAVIGATION ---
    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun viewProductDetail(product: Product) {
        _selectedProduct.value = product
        _currentScreen.value = AppScreen.PRODUCT_DETAIL
    }

    fun openAuditSession(sessionId: Long) {
        _selectedAuditSessionId.value = sessionId
        _currentScreen.value = AppScreen.AUDIT_SESSION_RUNNER
    }

    fun setLanguage(lang: String) {
        _language.value = lang
        viewModelScope.launch { dao.setSetting(AppSetting("APP_LANGUAGE", lang)) }
    }

    fun setCurrency(curr: String, symbol: String) {
        _currency.value = curr
        _currencySymbol.value = symbol
        viewModelScope.launch {
            dao.setSetting(AppSetting("CURRENCY", curr))
            dao.setSetting(AppSetting("CURRENCY_SYMBOL", symbol))
        }
    }

    fun setAllowNegativeStock(allow: Boolean) {
        _allowNegativeStock.value = allow
        viewModelScope.launch {
            dao.setSetting(AppSetting("ALLOW_NEGATIVE_STOCK", allow.toString()))
        }
    }

    fun switchUserRole(role: UserRole) {
        _currentUser.value = _currentUser.value.copy(role = role)
        showFeedback("تم التبديل إلى دور: ${role.titleAr}")
    }

    fun setSearchQuery(q: String) {
        _searchQuery.value = q
    }

    fun setSelectedCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun setSelectedWarehouseFilter(whId: Long) {
        _selectedWarehouseFilter.value = whId
    }

    fun showFeedback(msg: String) {
        _feedbackMessage.value = msg
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    fun setBarcodeScannerOpen(isOpen: Boolean) {
        _isBarcodeScannerOpen.value = isOpen
    }

    // --- PRODUCT CRUD ---
    fun saveProduct(
        id: Long = 0,
        name: String,
        sku: String,
        barcode: String,
        category: String,
        brand: String,
        description: String,
        costPrice: Double,
        salePrice: Double,
        quantity: Double,
        minThreshold: Double,
        supplierName: String,
        shelfLocation: String,
        unit: String,
        warehouseId: Long
    ) {
        viewModelScope.launch {
            // Validation
            if (name.isBlank() || sku.isBlank()) {
                showFeedback("خطأ: يرجى كتابة اسم المنتج ورمز SKU على الأقل")
                return@launch
            }
            if (costPrice < 0 || salePrice < 0 || quantity < 0) {
                showFeedback("خطأ: لا يمكن إدخال أسعار أو كميات سالبة")
                return@launch
            }

            // Check SKU uniqueness
            val existing = dao.getProductBySku(sku.trim())
            if (existing != null && existing.id != id) {
                showFeedback("خطأ: رمز SKU ($sku) مستخدم مسبقاً لمنتج آخر!")
                return@launch
            }

            val product = Product(
                id = id,
                name = name.trim(),
                sku = sku.trim().uppercase(),
                barcode = barcode.trim(),
                category = category.trim().ifBlank { "عام" },
                brand = brand.trim(),
                description = description.trim(),
                costPrice = costPrice,
                salePrice = salePrice,
                quantity = quantity,
                minThreshold = minThreshold,
                supplierName = supplierName.trim(),
                shelfLocation = shelfLocation.trim(),
                unit = unit.trim().ifBlank { "قطعة" },
                updatedAt = System.currentTimeMillis(),
                warehouseId = warehouseId
            )

            if (id == 0L) {
                val newId = dao.insertProduct(product)
                dao.insertAuditLog(
                    AuditLog(
                        userName = _currentUser.value.name,
                        userRole = _currentUser.value.role.titleAr,
                        action = "CREATE",
                        entity = "Product",
                        details = "إضافة منتج جديد: $name (SKU: $sku) كمية: $quantity"
                    )
                )
                showFeedback("تم حفظ المنتج بنجاح (#$newId)")
            } else {
                dao.updateProduct(product)
                dao.insertAuditLog(
                    AuditLog(
                        userName = _currentUser.value.name,
                        userRole = _currentUser.value.role.titleAr,
                        action = "UPDATE",
                        entity = "Product",
                        details = "تعديل المنتج: $name (SKU: $sku)"
                    )
                )
                showFeedback("تم تحديث بيانات المنتج بنجاح")
                _selectedProduct.value = product
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            dao.deleteProduct(product)
            dao.insertAuditLog(
                AuditLog(
                    userName = _currentUser.value.name,
                    userRole = _currentUser.value.role.titleAr,
                    action = "DELETE",
                    entity = "Product",
                    details = "حذف المنتج: ${product.name} (SKU: ${product.sku})"
                )
            )
            showFeedback("تم حذف المنتج: ${product.name}")
            if (_selectedProduct.value?.id == product.id) {
                _selectedProduct.value = null
                _currentScreen.value = AppScreen.PRODUCTS
            }
        }
    }

    fun duplicateProduct(product: Product) {
        viewModelScope.launch {
            val newSku = "${product.sku}-COPY-${System.currentTimeMillis() % 1000}"
            val newBarcode = if (product.barcode.isNotBlank()) "${product.barcode}1" else ""
            val copy = product.copy(
                id = 0,
                name = "${product.name} (نسخة)",
                sku = newSku,
                barcode = newBarcode,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val newId = dao.insertProduct(copy)
            showFeedback("تم إنشاء نسخة جديدة من المنتج (SKU: $newSku)")
        }
    }

    // --- SMART INVENTORY AUDIT ---
    fun createAuditSession(title: String, warehouseId: Long, warehouseName: String) {
        viewModelScope.launch {
            val session = InventoryAuditSession(
                warehouseId = warehouseId,
                warehouseName = warehouseName,
                title = title.ifBlank { "جلسة جرد ${SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())}" },
                status = "DRAFT"
            )
            val sessionId = dao.insertAuditSession(session)

            // Populate all current warehouse products into the audit item table with systemQty
            val currentProducts = dao.getProductsByWarehouse(warehouseId).first()
            val auditItems = currentProducts.map { p ->
                InventoryAuditItem(
                    sessionId = sessionId,
                    productId = p.id,
                    productName = p.name,
                    sku = p.sku,
                    barcode = p.barcode,
                    systemQty = p.quantity,
                    countedQty = p.quantity, // initial counted equals system until counted
                    discrepancyQty = 0.0,
                    costPrice = p.costPrice,
                    discrepancyValue = 0.0
                )
            }
            dao.insertAuditItems(auditItems)

            dao.insertAuditLog(
                AuditLog(
                    userName = _currentUser.value.name,
                    userRole = _currentUser.value.role.titleAr,
                    action = "START_AUDIT",
                    entity = "AuditSession",
                    details = "بدء جلسة جرد جديدة: $title لمخزن: $warehouseName"
                )
            )

            openAuditSession(sessionId)
            showFeedback("تم بدء جلسة الجرد بنجاح")
        }
    }

    fun updateAuditItemCount(sessionId: Long, productId: Long, newCountedQty: Double, reason: String = "", notes: String = "") {
        viewModelScope.launch {
            val item = dao.getAuditItem(sessionId, productId) ?: return@launch
            val diffQty = newCountedQty - item.systemQty
            val diffVal = diffQty * item.costPrice
            val updated = item.copy(
                countedQty = newCountedQty,
                discrepancyQty = diffQty,
                discrepancyValue = diffVal,
                reason = reason.ifBlank { item.reason },
                notes = notes.ifBlank { item.notes }
            )
            dao.insertOrUpdateAuditItem(updated)

            // Update session aggregate totals
            recalculateAuditSessionTotals(sessionId)
        }
    }

    private suspend fun recalculateAuditSessionTotals(sessionId: Long) {
        val items = dao.getAuditItems(sessionId).first()
        val totalCounted = items.count { it.countedQty != it.systemQty }
        val sumDiffQty = items.sumOf { it.discrepancyQty }
        val sumDiffVal = items.sumOf { it.discrepancyValue }

        val session = dao.getAuditSessionById(sessionId) ?: return
        dao.updateAuditSession(
            session.copy(
                totalItemsCounted = items.size,
                totalDiscrepancyQty = sumDiffQty,
                totalDiscrepancyValue = sumDiffVal
            )
        )
    }

    // SAFE ATOMIC AUDIT APPROVAL (CRITICAL)
    fun approveAuditSession(sessionId: Long) {
        viewModelScope.launch {
            val session = dao.getAuditSessionById(sessionId) ?: return@launch
            if (session.status == "APPROVED") {
                showFeedback("تنبيه: تم اعتماد هذه الجلسة مسبقاً، لا يمكن الاعتماد مرتين!")
                return@launch
            }

            val items = dao.getAuditItems(sessionId).first()

            // Update each product's quantity to match the physical counted count
            items.forEach { item ->
                dao.updateProductQuantity(item.productId, item.countedQty)
            }

            // Lock session as APPROVED
            val approved = session.copy(
                status = "APPROVED",
                approvedAt = System.currentTimeMillis(),
                approvedBy = _currentUser.value.name
            )
            dao.updateAuditSession(approved)

            // Log Immutable Audit trail
            dao.insertAuditLog(
                AuditLog(
                    userName = _currentUser.value.name,
                    userRole = _currentUser.value.role.titleAr,
                    action = "APPROVE_AUDIT",
                    entity = "AuditSession",
                    details = "اعتماد جلسة الجرد '${session.title}' وتحديث كميات المخزن. صافي الفروقات: ${session.totalDiscrepancyQty} قطعة بقيمة ${session.totalDiscrepancyValue} د.ع"
                )
            )

            showFeedback("✅ تم اعتماد الجرد بنجاح وتحديث أرصدة المخزون الفعلية.")
        }
    }

    // --- POS & SALES ---
    fun addToCart(product: Product) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val item = currentList[index]
            currentList[index] = item.copy(quantity = item.quantity + 1.0)
        } else {
            currentList.add(CartItem(product = product, quantity = 1.0))
        }
        _cartItems.value = currentList
    }

    fun updateCartItemQuantity(productId: Long, qty: Double) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            if (qty <= 0) {
                currentList.removeAt(index)
            } else {
                currentList[index] = currentList[index].copy(quantity = qty)
            }
            _cartItems.value = currentList
        }
    }

    fun removeCartItem(productId: Long) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _paidAmountInput.value = ""
    }

    fun setPaymentMethod(method: String) {
        _paymentMethod.value = method
    }

    fun setPaidAmountInput(amount: String) {
        _paidAmountInput.value = amount
    }

    fun setSelectedCustomer(customerId: Long?) {
        _selectedCustomerId.value = customerId
    }

    // SAFE POS CHECKOUT TRANSACTION
    fun checkoutSale() {
        viewModelScope.launch {
            val items = _cartItems.value
            if (items.isEmpty()) {
                showFeedback("خطأ: سلة المبيعات فارغة")
                return@launch
            }

            // Validate stock availability unless negative stock is allowed
            if (!_allowNegativeStock.value) {
                for (item in items) {
                    val freshProduct = dao.getProductById(item.product.id)
                    val available = freshProduct?.quantity ?: 0.0
                    if (available < item.quantity) {
                        showFeedback("خطأ: الكمية المطلوبة للمنتج '${item.product.name}' غير متوفرة! (المتوفر: $available)")
                        return@launch
                    }
                }
            }

            val total = items.sumOf { it.totalPrice }
            val paid = _paidAmountInput.value.toDoubleOrNull() ?: if (_paymentMethod.value == "CREDIT") 0.0 else total
            val remainingDebt = total - paid

            var custName = "زبون نقدي عام"
            val customerId = _selectedCustomerId.value
            if (customerId != null) {
                val customer = dao.getAllCustomers().first().find { it.id == customerId }
                if (customer != null) custName = customer.name
            }

            val invoiceNo = "INV-${System.currentTimeMillis() % 1000000}"
            val saleInvoice = SaleInvoice(
                invoiceNumber = invoiceNo,
                customerId = customerId,
                customerName = custName,
                warehouseId = 1,
                subtotal = total,
                discount = 0.0,
                totalAmount = total,
                paidAmount = paid,
                paymentMethod = _paymentMethod.value,
                cashierName = _currentUser.value.name,
                notes = if (remainingDebt > 0) "متبقي بذمة العميل: $remainingDebt" else "مدفوع بالكامل"
            )

            val invoiceId = dao.insertSaleInvoice(saleInvoice)

            // Insert sale items and atomically decrement stock
            val saleItems = items.map { item ->
                SaleItem(
                    invoiceId = invoiceId,
                    productId = item.product.id,
                    productName = item.product.name,
                    sku = item.product.sku,
                    unitPrice = item.unitPrice,
                    costPrice = item.product.costPrice,
                    quantity = item.quantity,
                    totalPrice = item.totalPrice
                )
            }
            dao.insertSaleItems(saleItems)

            // Decrement product inventory
            for (item in items) {
                val product = dao.getProductById(item.product.id)
                if (product != null) {
                    val newQty = product.quantity - item.quantity
                    dao.updateProductQuantity(product.id, newQty)
                }
            }

            // Update customer debt if credit
            if (remainingDebt > 0 && customerId != null) {
                dao.adjustCustomerBalance(customerId, remainingDebt)
            }

            // Audit Log
            dao.insertAuditLog(
                AuditLog(
                    userName = _currentUser.value.name,
                    userRole = _currentUser.value.role.titleAr,
                    action = "SALE",
                    entity = "SaleInvoice",
                    details = "إتمام فاتورة بيع $invoiceNo للعميل $custName بإجمالي $total د.ع (مدفوع: $paid د.ع)"
                )
            )

            clearCart()
            showFeedback("✅ تم تسجيل فاتورة البيع ($invoiceNo) بنجاح!")
        }
    }

    // --- PURCHASES ---
    fun recordPurchaseInvoice(
        supplierId: Long?,
        supplierName: String,
        items: List<Pair<Product, Double>>, // Product and purchased Qty
        unitCosts: List<Double>,
        paidAmount: Double
    ) {
        viewModelScope.launch {
            if (items.isEmpty()) {
                showFeedback("خطأ: يرجى إضافة منتجات لفاتورة الشراء")
                return@launch
            }

            var subtotal = 0.0
            items.forEachIndexed { i, pair ->
                subtotal += pair.second * unitCosts[i]
            }

            val invoiceNo = "PUR-${System.currentTimeMillis() % 1000000}"
            val purchaseInvoice = PurchaseInvoice(
                invoiceNumber = invoiceNo,
                supplierId = supplierId,
                supplierName = supplierName.ifBlank { "مورد عام" },
                warehouseId = 1,
                subtotal = subtotal,
                totalAmount = subtotal,
                paidAmount = paidAmount,
                paymentMethod = if (paidAmount >= subtotal) "CASH" else "CREDIT"
            )

            val invoiceId = dao.insertPurchaseInvoice(purchaseInvoice)

            val purchaseItems = items.mapIndexed { i, pair ->
                PurchaseItem(
                    invoiceId = invoiceId,
                    productId = pair.first.id,
                    productName = pair.first.name,
                    unitCost = unitCosts[i],
                    quantity = pair.second,
                    totalCost = pair.second * unitCosts[i]
                )
            }
            dao.insertPurchaseItems(purchaseItems)

            // Increase product stock and update cost price
            items.forEachIndexed { i, pair ->
                val product = dao.getProductById(pair.first.id)
                if (product != null) {
                    val newQty = product.quantity + pair.second
                    // Weighted average or latest cost price
                    dao.updateProduct(product.copy(quantity = newQty, costPrice = unitCosts[i], updatedAt = System.currentTimeMillis()))
                }
            }

            // Update supplier balance if remaining debt
            val remainingDebt = subtotal - paidAmount
            if (remainingDebt > 0 && supplierId != null) {
                dao.adjustSupplierBalance(supplierId, remainingDebt)
            }

            dao.insertAuditLog(
                AuditLog(
                    userName = _currentUser.value.name,
                    userRole = _currentUser.value.role.titleAr,
                    action = "PURCHASE",
                    entity = "PurchaseInvoice",
                    details = "تسجيل فاتورة شراء $invoiceNo من المورد $supplierName بإجمالي $subtotal د.ع"
                )
            )

            showFeedback("✅ تم تسجيل فاتورة الشراء واستلام البضاعة بالمخزن بنجاح.")
        }
    }

    // --- EXPENSES ---
    fun addExpense(title: String, category: String, amount: Double, notes: String) {
        viewModelScope.launch {
            if (title.isBlank() || amount <= 0) {
                showFeedback("خطأ: يرجى كتابة بيان المصروف ومبلغ صحيح")
                return@launch
            }
            dao.insertExpense(
                Expense(
                    title = title.trim(),
                    category = category.ifBlank { "عام" },
                    amount = amount,
                    recordedBy = _currentUser.value.name,
                    notes = notes.trim()
                )
            )
            dao.insertAuditLog(
                AuditLog(
                    userName = _currentUser.value.name,
                    userRole = _currentUser.value.role.titleAr,
                    action = "EXPENSE",
                    entity = "Expense",
                    details = "تسجيل مصروف: $title بمبلغ $amount د.ع ($category)"
                )
            )
            showFeedback("تم تسجيل المصروف بنجاح")
        }
    }

    // --- PARTNERS (SUPPLIERS & CUSTOMERS) ---
    fun addSupplier(name: String, phone: String, address: String, initialDebt: Double, notes: String) {
        viewModelScope.launch {
            if (name.isBlank()) return@launch
            dao.insertSupplier(
                Supplier(
                    name = name.trim(),
                    phone = phone.trim(),
                    address = address.trim(),
                    balanceDue = initialDebt,
                    notes = notes.trim()
                )
            )
            showFeedback("تمت إضافة المورد بنجاح")
        }
    }

    fun addCustomer(name: String, phone: String, address: String, initialDebt: Double, notes: String) {
        viewModelScope.launch {
            if (name.isBlank()) return@launch
            dao.insertCustomer(
                Customer(
                    name = name.trim(),
                    phone = phone.trim(),
                    address = address.trim(),
                    balanceDue = initialDebt,
                    notes = notes.trim()
                )
            )
            showFeedback("تمت إضافة العميل بنجاح")
        }
    }

    // --- MULTI-WAREHOUSE TRANSFER ---
    fun transferStock(fromWhId: Long, toWhId: Long, productId: Long, quantity: Double, notes: String) {
        viewModelScope.launch {
            if (fromWhId == toWhId) {
                showFeedback("خطأ: يجب اختيار مستودعين مختلفين للتحويل")
                return@launch
            }
            if (quantity <= 0) {
                showFeedback("خطأ: الكمية يجب أن تكون أكبر من صفر")
                return@launch
            }

            val product = dao.getProductById(productId) ?: return@launch
            if (product.quantity < quantity && !_allowNegativeStock.value) {
                showFeedback("خطأ: الكمية المتوفرة في المخزن (${product.quantity}) لا تكفي للتحويل")
                return@launch
            }

            // Deduct from source and record transfer
            dao.updateProductQuantity(productId, product.quantity - quantity)

            dao.insertTransfer(
                WarehouseTransfer(
                    fromWarehouseId = fromWhId,
                    toWarehouseId = toWhId,
                    productId = productId,
                    productName = product.name,
                    quantity = quantity,
                    notes = notes,
                    transferredBy = _currentUser.value.name
                )
            )

            dao.insertAuditLog(
                AuditLog(
                    userName = _currentUser.value.name,
                    userRole = _currentUser.value.role.titleAr,
                    action = "TRANSFER_STOCK",
                    entity = "WarehouseTransfer",
                    details = "تحويل $quantity ${product.unit} من المنتج ${product.name} بين المخازن"
                )
            )

            showFeedback("✅ تم تنفيذ مناقلة المخزون بنجاح")
        }
    }

    // --- AI ASSISTANT ---
    fun sendAiQuestion(question: String) {
        if (question.isBlank()) return

        val userMsg = AiMessage(sender = "USER", text = question.trim())
        _aiMessages.value = _aiMessages.value + userMsg
        _isAiThinking.value = true

        viewModelScope.launch {
            val allProducts = dao.getAllProducts().first()
            val lowStock = allProducts.filter { it.quantity <= it.minThreshold && it.quantity > 0 }
            val outOfStock = allProducts.filter { it.quantity <= 0 }
            val pastAudits = dao.getAllAuditSessions().first()

            val snapshot = InventorySnapshot(
                totalProducts = productCount.value,
                totalQuantity = totalQuantity.value,
                totalCostValue = totalCostValue.value,
                totalSaleValue = totalSaleValue.value,
                lowStockCount = lowStockCount.value,
                outOfStockCount = outOfStockCount.value,
                totalSales = totalSalesAmount.value,
                totalPurchases = totalPurchasesAmount.value,
                totalExpenses = totalExpensesAmount.value,
                customerDebts = totalCustomerDebts.value,
                supplierDebts = totalSupplierDebts.value,
                lowStockItems = lowStock,
                outOfStockItems = outOfStock,
                recentAuditSessions = pastAudits
            )

            val answer = AmeenAiEngine.askAmeenAi(question, snapshot)

            _aiMessages.value = _aiMessages.value + AiMessage(
                sender = "AMEEN_AI",
                text = answer,
                isEstimatedForecast = question.contains("توقع") || question.contains("طلب")
            )
            _isAiThinking.value = false
        }
    }

    // --- CSV EXPORT & IMPORT ---
    fun exportProductsToCsv(): String {
        val list = products.value
        val sb = StringBuilder()
        sb.append("ID,Name,SKU,Barcode,Category,CostPrice,SalePrice,Quantity,Unit,MinThreshold,ShelfLocation\n")
        list.forEach { p ->
            sb.append("${p.id},\"${p.name.replace("\"", "\"\"")}\",\"${p.sku}\",\"${p.barcode}\",\"${p.category}\",${p.costPrice},${p.salePrice},${p.quantity},\"${p.unit}\",${p.minThreshold},\"${p.shelfLocation}\"\n")
        }
        return sb.toString()
    }

    fun importProductsFromCsv(csvContent: String): Pair<Int, Int> {
        var importedCount = 0
        var errorCount = 0
        try {
            val lines = csvContent.lines().filter { it.isNotBlank() }
            if (lines.size <= 1) return Pair(0, 0)

            viewModelScope.launch {
                for (line in lines.drop(1)) {
                    val tokens = line.split(",").map { it.trim().removeSurrounding("\"") }
                    if (tokens.size >= 7) {
                        try {
                            val name = tokens[1]
                            val sku = tokens[2]
                            val barcode = if (tokens.size > 3) tokens[3] else ""
                            val category = if (tokens.size > 4) tokens[4] else "عام"
                            val costPrice = tokens[5].toDoubleOrNull() ?: 0.0
                            val salePrice = tokens[6].toDoubleOrNull() ?: 0.0
                            val quantity = if (tokens.size > 7) tokens[7].toDoubleOrNull() ?: 0.0 else 0.0

                            if (name.isNotBlank() && sku.isNotBlank()) {
                                dao.insertProduct(
                                    Product(
                                        name = name,
                                        sku = sku.uppercase(),
                                        barcode = barcode,
                                        category = category,
                                        costPrice = costPrice,
                                        salePrice = salePrice,
                                        quantity = quantity
                                    )
                                )
                                importedCount++
                            } else {
                                errorCount++
                            }
                        } catch (e: Exception) {
                            errorCount++
                        }
                    }
                }
                showFeedback("تم استيراد $importedCount منتج بنجاح (فشل $errorCount)")
            }
        } catch (e: Exception) {
            errorCount++
        }
        return Pair(importedCount, errorCount)
    }

    // --- DEMO DATA RE-SEED ---
    fun resetDemoData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.clearAllTables()
                AppDatabase.populateInitialData(dao)
            }
            showFeedback("✅ تم استرجاع البيانات التجريبية الشاملة بنجاح.")
        }
    }
}
