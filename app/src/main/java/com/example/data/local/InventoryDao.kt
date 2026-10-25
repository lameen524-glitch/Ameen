package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    // --- PRODUCTS ---
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Query("SELECT * FROM products WHERE sku = :sku LIMIT 1")
    suspend fun getProductBySku(sku: String): Product?

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): Product?

    @Query("SELECT * FROM products WHERE warehouseId = :warehouseId ORDER BY name ASC")
    fun getProductsByWarehouse(warehouseId: Long): Flow<List<Product>>

    @Query("""
        SELECT * FROM products 
        WHERE (name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%')
        AND (:category = '' OR category = :category)
        AND (:warehouseId = 0 OR warehouseId = :warehouseId)
        ORDER BY name ASC
    """)
    fun searchProducts(query: String, category: String, warehouseId: Long): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>): List<Long>

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("UPDATE products SET quantity = :newQty, updatedAt = :updatedAt WHERE id = :productId")
    suspend fun updateProductQuantity(productId: Long, newQty: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM products")
    fun getProductCount(): Flow<Int>

    @Query("SELECT SUM(quantity) FROM products")
    fun getTotalQuantity(): Flow<Double?>

    @Query("SELECT SUM(quantity * costPrice) FROM products")
    fun getTotalCostValue(): Flow<Double?>

    @Query("SELECT SUM(quantity * salePrice) FROM products")
    fun getTotalSaleValue(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM products WHERE quantity <= minThreshold AND quantity > 0")
    fun getLowStockCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM products WHERE quantity <= 0")
    fun getOutOfStockCount(): Flow<Int>

    @Query("SELECT DISTINCT category FROM products WHERE category != ''")
    fun getAllCategories(): Flow<List<String>>

    // --- WAREHOUSES ---
    @Query("SELECT * FROM warehouses ORDER BY isMain DESC, name ASC")
    fun getAllWarehouses(): Flow<List<Warehouse>>

    @Query("SELECT * FROM warehouses WHERE id = :id")
    suspend fun getWarehouseById(id: Long): Warehouse?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarehouse(warehouse: Warehouse): Long

    @Update
    suspend fun updateWarehouse(warehouse: Warehouse)

    @Delete
    suspend fun deleteWarehouse(warehouse: Warehouse)

    // --- TRANSFERS ---
    @Insert
    suspend fun insertTransfer(transfer: WarehouseTransfer): Long

    @Query("SELECT * FROM warehouse_transfers ORDER BY timestamp DESC")
    fun getAllTransfers(): Flow<List<WarehouseTransfer>>

    // --- INVENTORY AUDIT SESSIONS ---
    @Query("SELECT * FROM inventory_audit_sessions ORDER BY createdAt DESC")
    fun getAllAuditSessions(): Flow<List<InventoryAuditSession>>

    @Query("SELECT * FROM inventory_audit_sessions WHERE id = :sessionId")
    suspend fun getAuditSessionById(sessionId: Long): InventoryAuditSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditSession(session: InventoryAuditSession): Long

    @Update
    suspend fun updateAuditSession(session: InventoryAuditSession)

    @Delete
    suspend fun deleteAuditSession(session: InventoryAuditSession)

    @Query("DELETE FROM inventory_audit_items WHERE sessionId = :sessionId")
    suspend fun deleteAuditItemsForSession(sessionId: Long)

    // --- INVENTORY AUDIT ITEMS ---
    @Query("SELECT * FROM inventory_audit_items WHERE sessionId = :sessionId ORDER BY productName ASC")
    fun getAuditItems(sessionId: Long): Flow<List<InventoryAuditItem>>

    @Query("SELECT * FROM inventory_audit_items WHERE sessionId = :sessionId AND productId = :productId LIMIT 1")
    suspend fun getAuditItem(sessionId: Long, productId: Long): InventoryAuditItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAuditItem(item: InventoryAuditItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditItems(items: List<InventoryAuditItem>)

    @Delete
    suspend fun deleteAuditItem(item: InventoryAuditItem)

    // --- SALES ---
    @Query("SELECT * FROM sale_invoices ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<SaleInvoice>>

    @Query("SELECT * FROM sale_invoices WHERE id = :invoiceId")
    suspend fun getSaleInvoiceById(invoiceId: Long): SaleInvoice?

    @Query("SELECT * FROM sale_items WHERE invoiceId = :invoiceId")
    suspend fun getSaleItems(invoiceId: Long): List<SaleItem>

    @Insert
    suspend fun insertSaleInvoice(invoice: SaleInvoice): Long

    @Insert
    suspend fun insertSaleItems(items: List<SaleItem>)

    @Query("SELECT SUM(totalAmount) FROM sale_invoices WHERE isReturned = 0")
    fun getTotalSalesAmount(): Flow<Double?>

    @Query("SELECT SUM(paidAmount) FROM sale_invoices WHERE isReturned = 0")
    fun getTotalSalesPaid(): Flow<Double?>

    // --- PURCHASES ---
    @Query("SELECT * FROM purchase_invoices ORDER BY timestamp DESC")
    fun getAllPurchases(): Flow<List<PurchaseInvoice>>

    @Query("SELECT * FROM purchase_invoices WHERE id = :invoiceId")
    suspend fun getPurchaseInvoiceById(invoiceId: Long): PurchaseInvoice?

    @Query("SELECT * FROM purchase_items WHERE invoiceId = :invoiceId")
    suspend fun getPurchaseItems(invoiceId: Long): List<PurchaseItem>

    @Insert
    suspend fun insertPurchaseInvoice(invoice: PurchaseInvoice): Long

    @Insert
    suspend fun insertPurchaseItems(items: List<PurchaseItem>)

    @Query("SELECT SUM(totalAmount) FROM purchase_invoices")
    fun getTotalPurchasesAmount(): Flow<Double?>

    // --- PARTNERS (SUPPLIERS & CUSTOMERS) ---
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<Supplier>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: Supplier): Long

    @Update
    suspend fun updateSupplier(supplier: Supplier)

    @Delete
    suspend fun deleteSupplier(supplier: Supplier)

    @Query("UPDATE suppliers SET balanceDue = balanceDue + :amount WHERE id = :supplierId")
    suspend fun adjustSupplierBalance(supplierId: Long, amount: Double)

    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("UPDATE customers SET balanceDue = balanceDue + :amount WHERE id = :customerId")
    suspend fun adjustCustomerBalance(customerId: Long, amount: Double)

    @Query("SELECT SUM(balanceDue) FROM customers")
    fun getTotalCustomerDebts(): Flow<Double?>

    @Query("SELECT SUM(balanceDue) FROM suppliers")
    fun getTotalSupplierDebts(): Flow<Double?>

    // --- EXPENSES ---
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Insert
    suspend fun insertExpense(expense: Expense): Long

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalExpenses(): Flow<Double?>

    // --- AUDIT LOGS ---
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 200")
    fun getRecentAuditLogs(): Flow<List<AuditLog>>

    @Insert
    suspend fun insertAuditLog(log: AuditLog)

    // --- SETTINGS ---
    @Query("SELECT value FROM app_settings WHERE `key` = :key")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSetting)

    @Query("SELECT * FROM app_settings")
    fun getAllSettings(): Flow<List<AppSetting>>
}
