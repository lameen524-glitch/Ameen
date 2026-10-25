package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Product::class,
        Warehouse::class,
        WarehouseTransfer::class,
        InventoryAuditSession::class,
        InventoryAuditItem::class,
        SaleInvoice::class,
        SaleItem::class,
        PurchaseInvoice::class,
        PurchaseItem::class,
        Supplier::class,
        Customer::class,
        Expense::class,
        AuditLog::class,
        AppSetting::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ameen_smart_inventory.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate initial data on separate thread
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    populateInitialData(database.inventoryDao())
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateInitialData(dao: InventoryDao) {
            // Warehouses
            val mainWh = dao.insertWarehouse(
                Warehouse(name = "المخزن الرئيسي (المنطقة الصناعية)", code = "WH-MAIN", location = "بغداد - الكرخ", isMain = true, managerName = "أمين الشريفي", contactPhone = "07701234567")
            )
            val baghdadWh = dao.insertWarehouse(
                Warehouse(name = "مستودع فرع المنصور", code = "WH-MNSR", location = "بغداد - المنصور", isMain = false, managerName = "علي الكرخي", contactPhone = "07809876543")
            )
            val basraWh = dao.insertWarehouse(
                Warehouse(name = "مستودع فرع البصرة", code = "WH-BSR", location = "البصرة - العشار", isMain = false, managerName = "حيدر البصري", contactPhone = "07505554433")
            )

            // Settings
            dao.setSetting(AppSetting("COMPANY_NAME", "مجموعة أمين للتجارة والمقاولات"))
            dao.setSetting(AppSetting("CURRENCY", "IQD"))
            dao.setSetting(AppSetting("CURRENCY_SYMBOL", "د.ع"))
            dao.setSetting(AppSetting("ALLOW_NEGATIVE_STOCK", "false"))
            dao.setSetting(AppSetting("APP_LANGUAGE", "ar"))

            // Suppliers
            val sup1 = dao.insertSupplier(Supplier(name = "شركة النخيل للاستيراد والتوزيع", phone = "07712345678", address = "بغداد - الشورجة", balanceDue = 1250000.0, notes = "مورد أجهزة ومواد إلكترونية"))
            val sup2 = dao.insertSupplier(Supplier(name = "مؤسسة دجلة للتجهيزات الطبية", phone = "07802345678", address = "بغداد - باب المعظم", balanceDue = 0.0, notes = "مورد أدوات ومستلزمات صحية"))
            val sup3 = dao.insertSupplier(Supplier(name = "شركة الرافدين للتجارة العامة", phone = "07501234567", address = "أربيل - شارع 60", balanceDue = 450000.0, notes = "تجهيزات منزلية ومكتبية"))

            // Customers
            val cust1 = dao.insertCustomer(Customer(name = "شركة الأفق للحلول الرقمية", phone = "07709876543", address = "بغداد - الكرادة", balanceDue = 850000.0, notes = "عميل دائم - دفع آجل"))
            val cust2 = dao.insertCustomer(Customer(name = "مركز النور التجاري", phone = "07812349876", address = "النجف الأشرف", balanceDue = 320000.0, notes = "سحب أسبوعي"))
            val cust3 = dao.insertCustomer(Customer(name = "متجر الفيحاء للأجهزة", phone = "07519876543", address = "البصرة", balanceDue = 0.0, notes = "دفع نقدي فوري"))

            // Products (Real items with SKU and Barcode)
            val sampleProducts = listOf(
                Product(
                    name = "لابتوب ديل انسبايرون 15 Pro",
                    sku = "DELL-INSP-15",
                    barcode = "628100100101",
                    category = "إلكترونيات",
                    brand = "Dell",
                    description = "معالج Core i7 الجيل 13، ذاكرة 16GB، قرص 512GB SSD",
                    costPrice = 850000.0,
                    salePrice = 980000.0,
                    quantity = 14.0,
                    minThreshold = 3.0,
                    supplierName = "شركة النخيل للاستيراد والتوزيع",
                    shelfLocation = "A-12-04",
                    unit = "قطعة",
                    warehouseId = mainWh
                ),
                Product(
                    name = "طابعة باركود حرارية زيبرا ZD220",
                    sku = "ZEBRA-ZD220",
                    barcode = "628100100102",
                    category = "أجهزة مكتبية",
                    brand = "Zebra",
                    description = "طابعة ملصقات باركود حرارية سريعة للمخازن والمحلات",
                    costPrice = 140000.0,
                    salePrice = 185000.0,
                    quantity = 22.0,
                    minThreshold = 5.0,
                    supplierName = "شركة النخيل للاستيراد والتوزيع",
                    shelfLocation = "B-03-01",
                    unit = "قطعة",
                    warehouseId = mainWh
                ),
                Product(
                    name = "قارئ باركود ليزري لاسلكي 2D",
                    sku = "SCAN-2D-WL",
                    barcode = "628100100103",
                    category = "أجهزة مكتبية",
                    brand = "Honeywell",
                    description = "ماسح ضوئي محمول بتقنية البلوتوث والـ 2.4G لمسح أكواد QR وBarcodes",
                    costPrice = 45000.0,
                    salePrice = 65000.0,
                    quantity = 35.0,
                    minThreshold = 8.0,
                    supplierName = "شركة النخيل للاستيراد والتوزيع",
                    shelfLocation = "B-03-02",
                    unit = "قطعة",
                    warehouseId = mainWh
                ),
                Product(
                    name = "شاشة سامسونج منحنية 27 بوصة FHD",
                    sku = "SAM-MON-27",
                    barcode = "628100100104",
                    category = "إلكترونيات",
                    brand = "Samsung",
                    description = "معدل تحديث 75Hz، منفذ HDMI و VGA",
                    costPrice = 165000.0,
                    salePrice = 210000.0,
                    quantity = 8.0,
                    minThreshold = 4.0,
                    supplierName = "شركة الرافدين للتجارة العامة",
                    shelfLocation = "A-05-02",
                    unit = "قطعة",
                    warehouseId = mainWh
                ),
                Product(
                    name = "بكرة ملصقات باركود حرارية 50x30 مم",
                    sku = "LBL-ROLL-5030",
                    barcode = "628100100105",
                    category = "مستهلكات",
                    brand = "Ameen Supplies",
                    description = "بكرة 1000 ملصق ورقي حراري مقاوم للرطوبة",
                    costPrice = 2500.0,
                    salePrice = 4500.0,
                    quantity = 180.0,
                    minThreshold = 50.0,
                    supplierName = "شركة النخيل للاستيراد والتوزيع",
                    shelfLocation = "C-01-08",
                    unit = "بكرة",
                    warehouseId = mainWh
                ),
                Product(
                    name = "راوتر واي فاي 6 تي بي لينك AX3000",
                    sku = "TPL-AX3000",
                    barcode = "628100100106",
                    category = "شبكات",
                    brand = "TP-Link",
                    description = "سرعة تصل 3000Mbps تغطية واسعة للمكاتب والبيوت",
                    costPrice = 68000.0,
                    salePrice = 90000.0,
                    quantity = 2.0, // Low stock trigger!
                    minThreshold = 5.0,
                    supplierName = "شركة الرافدين للتجارة العامة",
                    shelfLocation = "B-08-03",
                    unit = "قطعة",
                    warehouseId = mainWh
                ),
                Product(
                    name = "وحدة خزن خارجي SSD سعة 1 تيرابايت",
                    sku = "SSD-EXT-1TB",
                    barcode = "628100100107",
                    category = "إلكترونيات",
                    brand = "Kingston",
                    description = "سرعة قراءة 1050MB/s، هيكل ألمنيوم مقاوم للصدمات",
                    costPrice = 95000.0,
                    salePrice = 125000.0,
                    quantity = 0.0, // Out of stock trigger!
                    minThreshold = 4.0,
                    supplierName = "شركة النخيل للاستيراد والتوزيع",
                    shelfLocation = "A-02-05",
                    unit = "قطعة",
                    warehouseId = mainWh
                ),
                Product(
                    name = "كرسي مكتبي طبي هيدروليك مريح",
                    sku = "CHR-MED-01",
                    barcode = "628100100108",
                    category = "أثاث مكتبي",
                    brand = "ErgoPlus",
                    description = "مسند رأس قابل للتعديل ودعم فقرات الظهر",
                    costPrice = 110000.0,
                    salePrice = 155000.0,
                    quantity = 11.0,
                    minThreshold = 3.0,
                    supplierName = "شركة الرافدين للتجارة العامة",
                    shelfLocation = "D-02-01",
                    unit = "قطعة",
                    warehouseId = baghdadWh
                )
            )

            dao.insertProducts(sampleProducts)

            // Initial Expenses
            dao.insertExpense(Expense(title = "إيجار مخزن الكرخ الشهري", category = "إيجار", amount = 1200000.0, recordedBy = "المحاسب", notes = "مدفوع عن شهر تشرين الأول"))
            dao.insertExpense(Expense(title = "فاتورة كهرباء ومولد المستودع", category = "كهرباء وصيانة", amount = 350000.0, recordedBy = "المحاسب", notes = "اشتراك مولد صناعي"))
            dao.insertExpense(Expense(title = "نقل وتفريغ بضاعة شحنة ديل", category = "نقل وتوصيل", amount = 95000.0, recordedBy = "موظف مخزن", notes = "أجور نقل داخلي"))

            // Sample Sale Invoice
            val saleInvId = dao.insertSaleInvoice(
                SaleInvoice(
                    invoiceNumber = "INV-2026-001",
                    customerId = cust1,
                    customerName = "شركة الأفق للحلول الرقمية",
                    warehouseId = mainWh,
                    subtotal = 1165000.0,
                    discount = 15000.0,
                    totalAmount = 1150000.0,
                    paidAmount = 300000.0, // 850000 debt
                    paymentMethod = "CREDIT",
                    cashierName = "أمين الشريفي",
                    notes = "دفعة أولى 300,000 والباقي آجل لمدة 15 يوم"
                )
            )

            dao.insertSaleItems(listOf(
                SaleItem(invoiceId = saleInvId, productId = 1, productName = "لابتوب ديل انسبايرون 15 Pro", sku = "DELL-INSP-15", unitPrice = 980000.0, costPrice = 850000.0, quantity = 1.0, totalPrice = 980000.0),
                SaleItem(invoiceId = saleInvId, productId = 2, productName = "طابعة باركود حرارية زيبرا ZD220", sku = "ZEBRA-ZD220", unitPrice = 185000.0, costPrice = 140000.0, quantity = 1.0, totalPrice = 185000.0)
            ))

            // Sample Audit Session (Completed past session)
            val auditSessionId = dao.insertAuditSession(
                InventoryAuditSession(
                    warehouseId = mainWh,
                    warehouseName = "المخزن الرئيسي (المنطقة الصناعية)",
                    title = "جرد دوري شهري - الربع الأخير 2026",
                    status = "APPROVED",
                    createdAt = System.currentTimeMillis() - 86400000L * 3,
                    approvedAt = System.currentTimeMillis() - 86400000L * 2,
                    approvedBy = "أمين الشريفي",
                    totalItemsCounted = 5,
                    totalDiscrepancyQty = -1.0,
                    totalDiscrepancyValue = -45000.0,
                    notes = "تمت المطابقة والاعتماد بعد التحقق من حركة الإخراج"
                )
            )

            dao.insertAuditItems(listOf(
                InventoryAuditItem(sessionId = auditSessionId, productId = 1, productName = "لابتوب ديل انسبايرون 15 Pro", sku = "DELL-INSP-15", barcode = "628100100101", systemQty = 14.0, countedQty = 14.0, discrepancyQty = 0.0, costPrice = 850000.0, discrepancyValue = 0.0, reason = "مطابق تماماً", notes = "حالة ممتازة"),
                InventoryAuditItem(sessionId = auditSessionId, productId = 3, productName = "قارئ باركود ليزري لاسلكي 2D", sku = "SCAN-2D-WL", barcode = "628100100103", systemQty = 36.0, countedQty = 35.0, discrepancyQty = -1.0, costPrice = 45000.0, discrepancyValue = -45000.0, reason = "عجز قطعة واحدة", notes = "تم فحص الرف وإثبات الكسر أثناء المناولة")
            ))

            // Initial Audit Log
            dao.insertAuditLog(
                AuditLog(
                    userName = "أمين الشريفي",
                    userRole = "مالك النظام",
                    action = "INITIALIZE",
                    entity = "SYSTEM",
                    details = "بدء تشغيل وتأسيس قاعدة بيانات جرد أمين الذكي AMEEN INVENTORY AI"
                )
            )
        }
    }
}
