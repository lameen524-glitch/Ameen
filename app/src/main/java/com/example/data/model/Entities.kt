package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["sku"], unique = true),
        Index(value = ["barcode"]),
        Index(value = ["warehouseId"])
    ]
)
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sku: String,
    val barcode: String = "",
    val category: String = "عام",
    val brand: String = "",
    val description: String = "",
    val costPrice: Double,
    val salePrice: Double,
    val quantity: Double,
    val minThreshold: Double = 5.0,
    val supplierName: String = "",
    val shelfLocation: String = "",
    val unit: String = "قطعة",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val expiryDate: String = "",
    val batchNumber: String = "",
    val warehouseId: Long = 1
)

@Entity(tableName = "warehouses")
data class Warehouse(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val code: String,
    val location: String = "",
    val isMain: Boolean = false,
    val managerName: String = "",
    val contactPhone: String = ""
)

@Entity(tableName = "warehouse_transfers")
data class WarehouseTransfer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fromWarehouseId: Long,
    val toWarehouseId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val status: String = "COMPLETED",
    val transferredBy: String = "أمين الشريفي"
)

@Entity(tableName = "inventory_audit_sessions")
data class InventoryAuditSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val warehouseId: Long,
    val warehouseName: String,
    val title: String,
    val status: String = "DRAFT", // DRAFT, APPROVED
    val createdAt: Long = System.currentTimeMillis(),
    val approvedAt: Long? = null,
    val approvedBy: String? = null,
    val totalItemsCounted: Int = 0,
    val totalDiscrepancyQty: Double = 0.0,
    val totalDiscrepancyValue: Double = 0.0,
    val notes: String = ""
)

@Entity(
    tableName = "inventory_audit_items",
    indices = [
        Index(value = ["sessionId", "productId"], unique = true)
    ]
)
data class InventoryAuditItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val productId: Long,
    val productName: String,
    val sku: String,
    val barcode: String = "",
    val systemQty: Double,
    val countedQty: Double,
    val discrepancyQty: Double, // countedQty - systemQty
    val costPrice: Double,
    val discrepancyValue: Double, // discrepancyQty * costPrice
    val reason: String = "",
    val notes: String = ""
)

@Entity(tableName = "sale_invoices")
data class SaleInvoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long? = null,
    val customerName: String = "زبون نقدي",
    val warehouseId: Long = 1,
    val subtotal: Double,
    val discount: Double = 0.0,
    val totalAmount: Double,
    val paidAmount: Double,
    val paymentMethod: String = "CASH", // CASH, CREDIT, CARD
    val timestamp: Long = System.currentTimeMillis(),
    val cashierName: String = "أمين",
    val notes: String = "",
    val isReturned: Boolean = false
)

@Entity(tableName = "sale_items")
data class SaleItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val productId: Long,
    val productName: String,
    val sku: String,
    val unitPrice: Double,
    val costPrice: Double,
    val quantity: Double,
    val totalPrice: Double
)

@Entity(tableName = "purchase_invoices")
data class PurchaseInvoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val supplierId: Long? = null,
    val supplierName: String = "مورد رئيسي",
    val warehouseId: Long = 1,
    val subtotal: Double,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val totalAmount: Double,
    val paidAmount: Double,
    val paymentMethod: String = "CASH",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "RECEIVED", // RECEIVED, PENDING
    val notes: String = ""
)

@Entity(tableName = "purchase_items")
data class PurchaseItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val productId: Long,
    val productName: String,
    val unitCost: Double,
    val quantity: Double,
    val totalCost: Double
)

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val balanceDue: Double = 0.0, // Amount owed to supplier
    val notes: String = ""
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val balanceDue: Double = 0.0, // Amount owed by customer to us
    val notes: String = ""
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // إيجار، رواتب، كهرباء، صيانة، نقل، تسويق، أخرى
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val recordedBy: String = "المحاسب",
    val notes: String = ""
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val userName: String,
    val userRole: String,
    val action: String, // CREATE, UPDATE, DELETE, APPROVE_AUDIT, TRANSFER_STOCK, SALE, PURCHASE
    val entity: String, // Product, AuditSession, Sale, Warehouse
    val details: String
)

@Entity(tableName = "app_settings")
data class AppSetting(
    @PrimaryKey val key: String,
    val value: String
)

enum class UserRole(val titleAr: String, val titleEn: String) {
    OWNER("مالك النظام", "Owner"),
    MANAGER("مدير", "Manager"),
    AUDITOR("موظف جرد", "Inventory Auditor"),
    SALES("موظف مبيعات", "Sales Staff"),
    WAREHOUSE("موظف مخزن", "Warehouse Staff"),
    ACCOUNTANT("محاسب", "Accountant")
}

data class CurrentUser(
    val id: String = "1",
    val name: String = "أمين الشريفي",
    val role: UserRole = UserRole.OWNER
)
