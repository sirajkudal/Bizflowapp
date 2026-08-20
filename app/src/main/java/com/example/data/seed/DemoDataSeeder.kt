package com.example.data.seed

import com.example.data.local.BizFlowDatabase
import com.example.data.local.entities.*
import java.util.UUID

object DemoDataSeeder {

    suspend fun seedDatabase(database: BizFlowDatabase) {
        val now = System.currentTimeMillis()
        val oneDay = 24L * 60 * 60 * 1000

        // 1. Subscription Plans (Configurable by admin)
        val defaultPlans = listOf(
            SubscriptionPlanEntity(
                id = "FREE",
                name = "Free",
                monthlyPrice = 0.0,
                productLimit = 50,
                customerLimit = 50,
                featuresJson = "50 products, 50 customers, Basic invoices, Basic dashboard",
                isMultiBranchAllowed = false,
                isStaffAllowed = false,
                isReportsAllowed = false,
                trialDays = 0
            ),
            SubscriptionPlanEntity(
                id = "STARTER",
                name = "Starter",
                monthlyPrice = 299.0,
                productLimit = 500,
                customerLimit = 100000,
                featuresJson = "500 products, Unlimited customers, Unlimited invoices, Expense tracking, Basic reports",
                isMultiBranchAllowed = false,
                isStaffAllowed = false,
                isReportsAllowed = true,
                trialDays = 14
            ),
            SubscriptionPlanEntity(
                id = "PROFESSIONAL",
                name = "Professional",
                monthlyPrice = 699.0,
                productLimit = 100000,
                customerLimit = 100000,
                featuresJson = "Unlimited products, Unlimited customers, Advanced reports, Staff accounts, Inventory alerts, Data export, Priority support",
                isMultiBranchAllowed = false,
                isStaffAllowed = true,
                isReportsAllowed = true,
                trialDays = 14
            ),
            SubscriptionPlanEntity(
                id = "BUSINESS",
                name = "Business",
                monthlyPrice = 1499.0,
                productLimit = 100000,
                customerLimit = 100000,
                featuresJson = "Everything in Professional, Multiple branches, Advanced permissions, Advanced analytics, API access, Priority support",
                isMultiBranchAllowed = true,
                isStaffAllowed = true,
                isReportsAllowed = true,
                trialDays = 14
            )
        )
        database.subscriptionDao().insertPlans(defaultPlans)

        // 2. Demo User & Demo Business
        val demoUserId = "demo_user_101"
        val demoUser = UserEntity(
            id = demoUserId,
            email = "demo@bizflow.app",
            fullName = "Aarav Sharma",
            role = "OWNER",
            isEmailVerified = true,
            createdAt = now - (60 * oneDay)
        )
        database.userDao().insertUser(demoUser)

        val demoBusinessId = "demo_biz_01"
        val demoBiz = BusinessEntity(
            id = demoBusinessId,
            ownerId = demoUserId,
            name = "Apex Retail & Trading Co.",
            ownerName = "Aarav Sharma",
            email = "contact@apexretail.com",
            phone = "+91 98765 43210",
            address = "42 MG Road, Cyber Tech Park, Bengaluru, KA 560001",
            country = "India",
            currencySymbol = "₹",
            defaultTaxRate = 18.0,
            invoicePrefix = "APX-",
            logoEmoji = "⚡",
            isDemo = true,
            createdAt = now - (60 * oneDay)
        )
        database.businessDao().insertBusiness(demoBiz)

        // Main Branch & Secondary Branch
        database.businessDao().insertBranch(
            BranchEntity(
                id = "branch_main",
                businessId = demoBusinessId,
                name = "Main Flagship Store (MG Road)",
                address = "42 MG Road, Bengaluru",
                phone = "+91 98765 43210",
                isMainBranch = true
            )
        )
        database.businessDao().insertBranch(
            BranchEntity(
                id = "branch_sub",
                businessId = demoBusinessId,
                name = "Indiranagar Hub",
                address = "100ft Road, Indiranagar, Bengaluru",
                phone = "+91 98765 43211",
                isMainBranch = false
            )
        )

        // Subscription for demo business
        database.subscriptionDao().insertSubscription(
            SubscriptionEntity(
                id = "sub_demo_01",
                businessId = demoBusinessId,
                planId = "BUSINESS",
                status = "ACTIVE",
                startDate = now - (30 * oneDay),
                renewalDate = now + (30 * oneDay),
                lastBilledAmount = 1499.0,
                autoRenew = true
            )
        )

        // 3. 10 Realistic Customers
        val customerNames = listOf(
            Triple("Rajesh Electronics & Repairs", "+91 98200 11223", "rajesh@relectronics.in"),
            Triple("Priya Mehta (Freelancer)", "+91 98450 33445", "priya.mehta@gmail.com"),
            Triple("Zenith Digital Agency", "+91 98110 55667", "finance@zenithagency.co"),
            Triple("Vikram Logistics & Goods", "+91 99000 77889", "vikram@vikramtrans.com"),
            Triple("Urban Cafe & Roastery", "+91 98800 99001", "accounts@urbancafe.in"),
            Triple("Dr. Sneha Verma Clinic", "+91 97400 22334", "dr.verma@healthplus.org"),
            Triple("CloudNine Softwares Pvt Ltd", "+91 96100 44556", "procurement@cloudnine.tech"),
            Triple("Karan Patel Wholesalers", "+91 95000 66778", "karan@pateltraders.com"),
            Triple("GreenLeaf Organics", "+91 94400 88990", "greenleaf@organics.store"),
            Triple("Ananya Designs Studio", "+91 93300 11224", "hello@ananyadesigns.art")
        )

        val customerEntities = customerNames.mapIndexed { index, (name, phone, email) ->
            CustomerEntity(
                id = "cust_${index + 1}",
                businessId = demoBusinessId,
                name = name,
                phone = phone,
                email = email,
                address = "Suite #${100 + index}, Commercial Block, Bengaluru",
                taxId = "GSTIN29ABCDE${1000 + index}Z${index % 9}",
                notes = if (index % 3 == 0) "VIP Corporate Client" else "Standard Terms",
                totalPurchases = (index + 1) * 8450.0,
                outstandingBalance = if (index % 2 == 1) (index * 1250.0) else 0.0,
                createdAt = now - ((50 - index * 3) * oneDay)
            )
        }
        customerEntities.forEach { database.customerDao().insertCustomer(it) }

        // 4. 20 Realistic Products across Categories
        val rawProducts = listOf(
            ProductRaw("Wireless Ergonomic Keyboard", "ACC-WKB-01", "890100101", "Accessories", 1200.0, 2499.0, 18.0, 45, 10, "LogiTech Dist"),
            ProductRaw("USB-C Ultra Fast Docking Hub", "ACC-HUB-02", "890100102", "Accessories", 1800.0, 3799.0, 18.0, 28, 8, "Anker Supply"),
            ProductRaw("27-inch 4K IPS Monitor", "DSP-4KM-03", "890100103", "Displays", 14000.0, 22499.0, 18.0, 12, 5, "Dell Direct"),
            ProductRaw("Noise-Cancelling Studio Headset", "AUD-NCH-04", "890100104", "Audio", 3200.0, 6999.0, 18.0, 3, 5, "Sony Wholesale"), // Low stock
            ProductRaw("Thermal POS Receipt Printer", "POS-TPR-05", "890100105", "POS Hardware", 2500.0, 4999.0, 18.0, 18, 6, "Epson B2B"),
            ProductRaw("Wireless Barcode Laser Scanner", "POS-BSC-06", "890100106", "POS Hardware", 950.0, 1999.0, 18.0, 34, 10, "Honeywell Tech"),
            ProductRaw("Heavy Duty Cash Drawer (RJ11)", "POS-CDR-07", "890100107", "POS Hardware", 1400.0, 2899.0, 18.0, 15, 5, "TVS Electronics"),
            ProductRaw("Precision Optical Mouse", "ACC-OPM-08", "890100108", "Accessories", 350.0, 799.0, 18.0, 80, 15, "LogiTech Dist"),
            ProductRaw("1000VA Line Interactive UPS", "PWR-UPS-09", "890100109", "Power", 2800.0, 5499.0, 18.0, 8, 4, "APC Power"),
            ProductRaw("CAT6 High-Speed Cable (305m)", "NET-CBL-10", "890100110", "Networking", 3500.0, 6200.0, 18.0, 2, 4, "D-Link Trade"), // Low stock
            ProductRaw("Dual-Band Wi-Fi 6 Router", "NET-RTR-11", "890100111", "Networking", 1900.0, 3999.0, 18.0, 22, 6, "TP-Link India"),
            ProductRaw("1TB High Performance NVMe SSD", "STR-SSD-12", "890100112", "Storage", 3100.0, 5899.0, 18.0, 40, 10, "Kingston Dist"),
            ProductRaw("2TB External Rugged HDD", "STR-HDD-13", "890100113", "Storage", 2900.0, 5199.0, 18.0, 19, 5, "Seagate Direct"),
            ProductRaw("Executive Mesh Office Chair", "FUR-CHR-14", "890100114", "Furniture", 4200.0, 8499.0, 18.0, 7, 3, "Featherlite Dist"),
            ProductRaw("Motorized Height Adjustable Desk", "FUR-DSK-15", "890100115", "Furniture", 12000.0, 21999.0, 18.0, 4, 2, "ErgoSmart"),
            ProductRaw("1080p StreamPro Web Camera", "ACC-CAM-16", "890100116", "Accessories", 1100.0, 2499.0, 18.0, 25, 8, "LogiTech Dist"),
            ProductRaw("Magnetic Whiteboard (4x3 ft)", "OFF-WBD-17", "890100117", "Office Supplies", 800.0, 1699.0, 18.0, 14, 5, "Solo Stationery"),
            ProductRaw("A4 Copier Paper Box (5 Reams)", "OFF-PPR-18", "890100118", "Office Supplies", 900.0, 1450.0, 12.0, 60, 20, "JK Paper Mill"),
            ProductRaw("Anti-Glare LED Desk Lamp", "OFF-LMP-19", "890100119", "Office Supplies", 600.0, 1299.0, 18.0, 31, 10, "Syska LED"),
            ProductRaw("Smart NFC Card Reader", "POS-NFC-20", "890100120", "POS Hardware", 1300.0, 2699.0, 18.0, 1, 5, "Verifone B2B") // Low stock
        )

        val productEntities = rawProducts.mapIndexed { index, p ->
            ProductEntity(
                id = "prod_${index + 1}",
                businessId = demoBusinessId,
                name = p.name,
                sku = p.sku,
                barcode = p.barcode,
                category = p.category,
                purchasePrice = p.purchasePrice,
                sellingPrice = p.sellingPrice,
                taxRate = p.taxRate,
                stockQuantity = p.stockQuantity,
                minStockLevel = p.minStockLevel,
                supplier = p.supplier,
                description = "High quality commercial grade ${p.name.lowercase()}.",
                createdAt = now - (45 * oneDay)
            )
        }
        productEntities.forEach { database.productDao().insertProduct(it) }

        // Initial inventory movement records
        productEntities.forEach { p ->
            database.productDao().insertMovement(
                InventoryMovementEntity(
                    id = "mov_init_${p.id}",
                    businessId = demoBusinessId,
                    productId = p.id,
                    productName = p.name,
                    movementType = "IN",
                    quantityChange = p.stockQuantity + 15,
                    balanceAfter = p.stockQuantity + 15,
                    reason = "Initial Supplier Delivery Batch #2026-Q1",
                    timestamp = now - (45 * oneDay)
                )
            )
        }

        // 5. 30 Realistic Sales across recent 30 days
        val paymentMethods = listOf("UPI", "Cash", "Card", "Bank Transfer")
        for (i in 1..30) {
            val daysAgo = (30 - i)
            val saleTime = now - (daysAgo * oneDay) + (i * 1800000L)
            val cust = customerEntities[i % customerEntities.size]
            val prod1 = productEntities[(i * 3) % productEntities.size]
            val prod2 = productEntities[(i * 7) % productEntities.size]

            val qty1 = (i % 3) + 1
            val qty2 = (i % 2) + 1
            val item1Total = prod1.sellingPrice * qty1
            val item2Total = prod2.sellingPrice * qty2

            val subtotal = item1Total + item2Total
            val discount = if (i % 4 == 0) subtotal * 0.05 else 0.0
            val taxable = subtotal - discount
            val tax = taxable * 0.18
            val grandTotal = taxable + tax
            val method = paymentMethods[i % paymentMethods.size]

            val saleId = "sale_${1000 + i}"
            val sale = SaleEntity(
                id = saleId,
                businessId = demoBusinessId,
                branchId = if (i % 5 == 0) "branch_sub" else "branch_main",
                customerId = cust.id,
                customerName = cust.name,
                subtotal = subtotal,
                discountAmount = discount,
                taxAmount = tax,
                grandTotal = grandTotal,
                paymentMethod = method,
                invoiceId = "inv_${2000 + i}",
                timestamp = saleTime
            )
            database.saleDao().insertSale(sale)

            val saleItems = listOf(
                SaleItemEntity(
                    id = "sitem_${i}_1",
                    saleId = saleId,
                    productId = prod1.id,
                    productName = prod1.name,
                    quantity = qty1,
                    unitPrice = prod1.sellingPrice,
                    totalPrice = item1Total
                ),
                SaleItemEntity(
                    id = "sitem_${i}_2",
                    saleId = saleId,
                    productId = prod2.id,
                    productName = prod2.name,
                    quantity = qty2,
                    unitPrice = prod2.sellingPrice,
                    totalPrice = item2Total
                )
            )
            database.saleDao().insertSaleItems(saleItems)

            // Also record inventory movement
            database.productDao().insertMovement(
                InventoryMovementEntity(
                    id = "mov_sale_${saleId}",
                    businessId = demoBusinessId,
                    productId = prod1.id,
                    productName = prod1.name,
                    movementType = "OUT",
                    quantityChange = -qty1,
                    balanceAfter = prod1.stockQuantity,
                    reason = "Sale #$saleId (${cust.name})",
                    referenceId = saleId,
                    timestamp = saleTime
                )
            )
        }

        // 6. 10 Realistic Invoices
        val invoiceStatuses = listOf("PAID", "SENT", "PAID", "PARTIALLY_PAID", "OVERDUE", "PAID", "SENT", "DRAFT", "PAID", "OVERDUE")
        for (i in 1..10) {
            val cust = customerEntities[i - 1]
            val daysAgo = (25 - i * 2)
            val issueDate = now - (daysAgo * oneDay)
            val dueDate = issueDate + (14 * oneDay)
            val prod = productEntities[i % productEntities.size]
            val qty = (i % 4) + 1
            val subtotal = prod.sellingPrice * qty
            val tax = subtotal * 0.18
            val total = subtotal + tax
            val status = invoiceStatuses[i - 1]
            val paid = when (status) {
                "PAID" -> total
                "PARTIALLY_PAID" -> total * 0.5
                else -> 0.0
            }

            val invId = "inv_${2000 + i}"
            val invNumber = "APX-2026-${1000 + i}"
            val invoice = InvoiceEntity(
                id = invId,
                businessId = demoBusinessId,
                invoiceNumber = invNumber,
                customerId = cust.id,
                customerName = cust.name,
                customerEmail = cust.email,
                customerPhone = cust.phone,
                customerAddress = cust.address,
                issueDate = issueDate,
                dueDate = dueDate,
                subtotal = subtotal,
                discountAmount = 0.0,
                taxAmount = tax,
                grandTotal = total,
                paidAmount = paid,
                status = status,
                notes = "Thank you for your valued partnership. Payment via Bank / UPI.",
                terms = "Payment due within 14 calendar days.",
                createdAt = issueDate
            )
            database.invoiceDao().insertInvoice(invoice)

            val invoiceItems = listOf(
                InvoiceItemEntity(
                    id = "invitem_${i}_1",
                    invoiceId = invId,
                    productId = prod.id,
                    description = prod.name,
                    quantity = qty,
                    unitPrice = prod.sellingPrice,
                    taxRate = prod.taxRate,
                    totalAmount = subtotal
                )
            )
            database.invoiceDao().insertInvoiceItems(invoiceItems)

            if (paid > 0) {
                database.paymentDao().insertPayment(
                    PaymentEntity(
                        id = "pay_inv_${i}",
                        businessId = demoBusinessId,
                        invoiceId = invId,
                        customerId = cust.id,
                        customerName = cust.name,
                        amount = paid,
                        paymentDate = issueDate + (2 * oneDay),
                        paymentMethod = if (i % 2 == 0) "UPI" else "Bank Transfer",
                        referenceNumber = "TXN-8849${i}09",
                        notes = "Payment received towards $invNumber"
                    )
                )
            }
        }

        // 7. 15 Realistic Expenses across all categories
        val expensesData = listOf(
            Triple("Commercial Shop Rent (Flagship)", 45000.0, "Rent"),
            Triple("Staff Salaries - Senior Sales Assoc", 32000.0, "Salary"),
            Triple("Staff Salaries - Inventory Clerk", 22000.0, "Salary"),
            Triple("Commercial BESCOM Electricity Bill", 4850.0, "Electricity"),
            Triple("Airtel Fiber 1Gbps Business Internet", 2399.0, "Internet"),
            Triple("Local Delivery & Logistics Van", 6200.0, "Transport"),
            Triple("Google Ads & Local Meta Promo", 8500.0, "Marketing"),
            Triple("Packaging Boxes & Bubble Rolls", 3400.0, "Supplies"),
            Triple("POS Thermal Rolls & Ribbons (100 rolls)", 1200.0, "Supplies"),
            Triple("Shop Pest Control & Sanitization", 1800.0, "Other"),
            Triple("Store Coffee & Tea Pantry Supply", 1500.0, "Supplies"),
            Triple("Indiranagar Hub Sub-Rent", 25000.0, "Rent"),
            Triple("Staff Overtime & Refreshments", 3100.0, "Salary"),
            Triple("Accounting & GST Filing Retainer", 5000.0, "Other"),
            Triple("Fuel & Local Logistics", 2800.0, "Transport")
        )

        expensesData.mapIndexed { index, (title, amt, cat) ->
            val daysAgo = (index * 2) + 1
            ExpenseEntity(
                id = "exp_${index + 1}",
                businessId = demoBusinessId,
                title = title,
                category = cat,
                amount = amt,
                date = now - (daysAgo * oneDay),
                paymentMethod = if (amt > 10000) "Bank Transfer" else "UPI",
                description = "Recurring monthly operational expenditure."
            )
        }.forEach { database.expenseDao().insertExpense(it) }

        // 8. Staff accounts
        val staffList = listOf(
            StaffEntity(
                id = "staff_1",
                businessId = demoBusinessId,
                fullName = "Rohan Kulkarni",
                email = "rohan@apexretail.com",
                phone = "+91 98451 11223",
                role = "MANAGER"
            ),
            StaffEntity(
                id = "staff_2",
                businessId = demoBusinessId,
                fullName = "Kavita Rao",
                email = "kavita@apexretail.com",
                phone = "+91 98452 22334",
                role = "ACCOUNTANT"
            ),
            StaffEntity(
                id = "staff_3",
                businessId = demoBusinessId,
                fullName = "Sunil Gowda",
                email = "sunil@apexretail.com",
                phone = "+91 98453 33445",
                role = "STAFF"
            )
        )
        staffList.forEach { database.staffDao().insertStaff(it) }

        // 9. Initial Notifications
        val notifs = listOf(
            NotificationEntity(
                id = "notif_1",
                businessId = demoBusinessId,
                title = "Low Stock Alert: 3 Items Below Minimum",
                message = "Studio Headset, CAT6 Cable, and Smart NFC Card Reader have reached reorder levels.",
                type = "LOW_STOCK",
                actionRoute = "inventory"
            ),
            NotificationEntity(
                id = "notif_2",
                businessId = demoBusinessId,
                title = "Overdue Invoice APX-2026-1005",
                message = "Invoice for Urban Cafe (₹14,500) is past its due date. Follow up with client.",
                type = "OVERDUE_INVOICE",
                actionRoute = "invoices"
            ),
            NotificationEntity(
                id = "notif_3",
                businessId = demoBusinessId,
                title = "Payment Received: ₹26,548",
                message = "CloudNine Softwares cleared invoice APX-2026-1007 via Bank Transfer.",
                type = "PAYMENT_RECEIVED",
                actionRoute = "payments"
            ),
            NotificationEntity(
                id = "notif_4",
                businessId = demoBusinessId,
                title = "SaaS Subscription Active: Business Plan",
                message = "Your BizFlow Business subscription is active with Multi-Branch enabled.",
                type = "SUBSCRIPTION",
                actionRoute = "subscription"
            )
        )
        notifs.forEach { database.notificationDao().insertNotification(it) }

        // 10. Audit Logs
        val logs = listOf(
            AuditLogEntity(
                id = "log_1",
                businessId = demoBusinessId,
                action = "BUSINESS_CREATED",
                entityType = "Business",
                details = "Initialized Apex Retail & Trading Co. with Multi-Branch support."
            ),
            AuditLogEntity(
                id = "log_2",
                businessId = demoBusinessId,
                action = "SUBSCRIPTION_UPGRADE",
                entityType = "Subscription",
                details = "Upgraded business to Business Tier (₹1,499/mo)."
            ),
            AuditLogEntity(
                id = "log_3",
                businessId = demoBusinessId,
                action = "PRODUCT_BATCH_IMPORT",
                entityType = "Inventory",
                details = "Imported 20 catalog SKUs with initial stock allocations."
            )
        )
        logs.forEach { database.auditLogDao().insertLog(it) }

        // 11. Support Tickets
        val tickets = listOf(
            SupportTicketEntity(
                id = "tkt_1",
                businessId = demoBusinessId,
                userEmail = "contact@apexretail.com",
                subject = "How to configure thermal receipt printer via USB?",
                category = "TECHNICAL",
                priority = "MEDIUM",
                description = "We purchased an Epson thermal POS printer. How can we format standard 80mm receipts?",
                status = "RESOLVED",
                adminReply = "Hello Aarav, you can select standard 80mm thermal format directly in Invoice Settings."
            ),
            SupportTicketEntity(
                id = "tkt_2",
                businessId = demoBusinessId,
                userEmail = "contact@apexretail.com",
                subject = "API Webhooks for third-party e-commerce sync",
                category = "FEATURE_REQUEST",
                priority = "LOW",
                description = "Would love to hook inventory webhook triggers directly with our Shopify store.",
                status = "IN_PROGRESS",
                adminReply = "Our engineering team is actively testing the REST webhook dispatcher on the Business plan."
            )
        )
        tickets.forEach { database.supportTicketDao().insertTicket(it) }
    }

    private data class ProductRaw(
        val name: String,
        val sku: String,
        val barcode: String,
        val category: String,
        val purchasePrice: Double,
        val sellingPrice: Double,
        val taxRate: Double,
        val stockQuantity: Int,
        val minStockLevel: Int,
        val supplier: String
    )
}
