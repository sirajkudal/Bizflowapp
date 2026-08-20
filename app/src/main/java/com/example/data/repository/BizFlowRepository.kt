package com.example.data.repository

import com.example.data.local.BizFlowDatabase
import com.example.data.local.entities.*
import com.example.data.seed.DemoDataSeeder
import com.example.model.PlanTier
import com.example.model.StaffRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.UUID

class BizFlowRepository(private val db: BizFlowDatabase) {

    // Auth & Users
    fun getCurrentUser(): Flow<UserEntity?> = db.userDao().getCurrentUser()
    fun getAllUsers(): Flow<List<UserEntity>> = db.userDao().getAllUsers()

    suspend fun registerUser(email: String, fullName: String): UserEntity = withContext(Dispatchers.IO) {
        val user = UserEntity(
            id = UUID.randomUUID().toString(),
            email = email.trim(),
            fullName = fullName.trim(),
            role = "OWNER",
            isEmailVerified = true
        )
        db.userDao().insertUser(user)
        user
    }

    suspend fun loginUser(email: String): UserEntity? = withContext(Dispatchers.IO) {
        db.userDao().getUserByEmail(email.trim())
    }

    // Businesses
    fun getAllBusinesses(): Flow<List<BusinessEntity>> = db.businessDao().getAllBusinesses()
    fun getBusinessById(id: String): Flow<BusinessEntity?> = db.businessDao().getBusinessById(id)
    fun getBranches(businessId: String): Flow<List<BranchEntity>> = db.businessDao().getBranchesForBusiness(businessId)

    suspend fun createBusiness(
        ownerId: String,
        name: String,
        ownerName: String,
        email: String,
        phone: String,
        address: String,
        country: String = "India",
        currencySymbol: String = "₹",
        taxRate: Double = 18.0,
        invoicePrefix: String = "INV-",
        logoEmoji: String = "🏢"
    ): BusinessEntity = withContext(Dispatchers.IO) {
        val bizId = UUID.randomUUID().toString()
        val biz = BusinessEntity(
            id = bizId,
            ownerId = ownerId,
            name = name.trim(),
            ownerName = ownerName.trim(),
            email = email.trim(),
            phone = phone.trim(),
            address = address.trim(),
            country = country,
            currencySymbol = currencySymbol,
            defaultTaxRate = taxRate,
            invoicePrefix = invoicePrefix,
            logoEmoji = logoEmoji,
            isDemo = false
        )
        db.businessDao().insertBusiness(biz)

        // Main branch
        db.businessDao().insertBranch(
            BranchEntity(
                id = "branch_${UUID.randomUUID()}",
                businessId = bizId,
                name = "Main Store",
                address = address,
                phone = phone,
                isMainBranch = true
            )
        )

        // Default Free Plan Subscription
        db.subscriptionDao().insertSubscription(
            SubscriptionEntity(
                id = "sub_${UUID.randomUUID()}",
                businessId = bizId,
                planId = "FREE",
                status = "ACTIVE",
                renewalDate = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
                lastBilledAmount = 0.0
            )
        )

        logAction(bizId, "BUSINESS_CREATED", "Business", "Created business profile: $name")
        biz
    }

    suspend fun updateBusiness(biz: BusinessEntity) = withContext(Dispatchers.IO) {
        db.businessDao().updateBusiness(biz)
        logAction(biz.id, "BUSINESS_UPDATED", "Business", "Updated business profile settings")
    }

    // Customers
    fun getCustomers(businessId: String): Flow<List<CustomerEntity>> = db.customerDao().getCustomersByBusiness(businessId)

    suspend fun addCustomer(
        businessId: String,
        name: String,
        phone: String,
        email: String,
        address: String,
        taxId: String,
        notes: String
    ): CustomerEntity = withContext(Dispatchers.IO) {
        val cust = CustomerEntity(
            id = "cust_${UUID.randomUUID()}",
            businessId = businessId,
            name = name.trim(),
            phone = phone.trim(),
            email = email.trim(),
            address = address.trim(),
            taxId = taxId.trim(),
            notes = notes.trim()
        )
        db.customerDao().insertCustomer(cust)
        logAction(businessId, "CUSTOMER_ADDED", "Customer", "Added new customer: $name")
        cust
    }

    suspend fun updateCustomer(customer: CustomerEntity) = withContext(Dispatchers.IO) {
        db.customerDao().updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: CustomerEntity) = withContext(Dispatchers.IO) {
        db.customerDao().deleteCustomer(customer)
        logAction(customer.businessId, "CUSTOMER_DELETED", "Customer", "Deleted customer: ${customer.name}")
    }

    // Products & Inventory
    fun getProducts(businessId: String): Flow<List<ProductEntity>> = db.productDao().getProductsByBusiness(businessId)
    fun getInventoryMovements(businessId: String): Flow<List<InventoryMovementEntity>> = db.productDao().getInventoryMovements(businessId)

    suspend fun addProduct(
        businessId: String,
        name: String,
        sku: String,
        barcode: String,
        category: String,
        purchasePrice: Double,
        sellingPrice: Double,
        taxRate: Double,
        stockQuantity: Int,
        minStockLevel: Int,
        supplier: String,
        description: String
    ): ProductEntity = withContext(Dispatchers.IO) {
        val prodId = "prod_${UUID.randomUUID()}"
        val product = ProductEntity(
            id = prodId,
            businessId = businessId,
            name = name.trim(),
            sku = sku.trim(),
            barcode = barcode.trim(),
            category = category.ifBlank { "General" }.trim(),
            purchasePrice = purchasePrice,
            sellingPrice = sellingPrice,
            taxRate = taxRate,
            stockQuantity = stockQuantity,
            minStockLevel = minStockLevel,
            supplier = supplier.trim(),
            description = description.trim()
        )
        db.productDao().insertProduct(product)

        if (stockQuantity > 0) {
            db.productDao().insertMovement(
                InventoryMovementEntity(
                    id = "mov_${UUID.randomUUID()}",
                    businessId = businessId,
                    productId = prodId,
                    productName = name,
                    movementType = "IN",
                    quantityChange = stockQuantity,
                    balanceAfter = stockQuantity,
                    reason = "Initial Stock Setup"
                )
            )
        }
        logAction(businessId, "PRODUCT_ADDED", "Product", "Created product: $name ($sku)")
        product
    }

    suspend fun updateProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        db.productDao().updateProduct(product)
        logAction(product.businessId, "PRODUCT_UPDATED", "Product", "Updated product: ${product.name}")
    }

    suspend fun deleteProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        db.productDao().deleteProduct(product)
        logAction(product.businessId, "PRODUCT_DELETED", "Product", "Deleted product: ${product.name}")
    }

    suspend fun adjustStock(
        businessId: String,
        productId: String,
        productName: String,
        delta: Int,
        newBalance: Int,
        reason: String
    ) = withContext(Dispatchers.IO) {
        db.productDao().adjustStock(productId, delta)
        db.productDao().insertMovement(
            InventoryMovementEntity(
                id = "mov_${UUID.randomUUID()}",
                businessId = businessId,
                productId = productId,
                productName = productName,
                movementType = if (delta >= 0) "IN" else "OUT",
                quantityChange = delta,
                balanceAfter = newBalance,
                reason = reason
            )
        )
        // Check for low stock alert
        val prod = db.productDao().getProductById(productId)
        if (prod != null && prod.stockQuantity <= prod.minStockLevel) {
            db.notificationDao().insertNotification(
                NotificationEntity(
                    id = "notif_${UUID.randomUUID()}",
                    businessId = businessId,
                    title = "Low Stock Warning: ${prod.name}",
                    message = "Stock level is now ${prod.stockQuantity} (Minimum required: ${prod.minStockLevel}). Please reorder from ${prod.supplier}.",
                    type = "LOW_STOCK",
                    actionRoute = "inventory"
                )
            )
        }
    }

    // Sales & Point-of-Sale
    fun getSales(businessId: String): Flow<List<SaleEntity>> = db.saleDao().getSalesByBusiness(businessId)

    data class CartItem(
        val product: ProductEntity,
        val quantity: Int
    )

    suspend fun processSale(
        businessId: String,
        customerId: String,
        customerName: String,
        items: List<CartItem>,
        discountAmount: Double,
        taxAmount: Double,
        paymentMethod: String,
        generateInvoice: Boolean = true
    ): SaleEntity = withContext(Dispatchers.IO) {
        val saleId = "sale_${UUID.randomUUID()}"
        val subtotal = items.sumOf { it.product.sellingPrice * it.quantity }
        val grandTotal = (subtotal - discountAmount) + taxAmount

        val invId = if (generateInvoice) "inv_${UUID.randomUUID()}" else ""
        val now = System.currentTimeMillis()

        val sale = SaleEntity(
            id = saleId,
            businessId = businessId,
            customerId = customerId,
            customerName = customerName.ifBlank { "Walk-in Customer" },
            subtotal = subtotal,
            discountAmount = discountAmount,
            taxAmount = taxAmount,
            grandTotal = grandTotal,
            paymentMethod = paymentMethod,
            invoiceId = invId,
            timestamp = now
        )
        db.saleDao().insertSale(sale)

        val saleItems = items.map { item ->
            SaleItemEntity(
                id = "sitem_${UUID.randomUUID()}",
                saleId = saleId,
                productId = item.product.id,
                productName = item.product.name,
                quantity = item.quantity,
                unitPrice = item.product.sellingPrice,
                totalPrice = item.product.sellingPrice * item.quantity
            )
        }
        db.saleDao().insertSaleItems(saleItems)

        // Deduct inventory & record movement
        items.forEach { item ->
            val updatedQty = item.product.stockQuantity - item.quantity
            db.productDao().adjustStock(item.product.id, -item.quantity)
            db.productDao().insertMovement(
                InventoryMovementEntity(
                    id = "mov_${UUID.randomUUID()}",
                    businessId = businessId,
                    productId = item.product.id,
                    productName = item.product.name,
                    movementType = "OUT",
                    quantityChange = -item.quantity,
                    balanceAfter = updatedQty,
                    reason = "Sale #$saleId ($customerName)",
                    referenceId = saleId,
                    timestamp = now
                )
            )
            if (updatedQty <= item.product.minStockLevel) {
                db.notificationDao().insertNotification(
                    NotificationEntity(
                        id = "notif_${UUID.randomUUID()}",
                        businessId = businessId,
                        title = "Low Stock: ${item.product.name}",
                        message = "Only $updatedQty items left in stock after recent sale.",
                        type = "LOW_STOCK",
                        actionRoute = "inventory"
                    )
                )
            }
        }

        // Generate Invoice if requested
        if (generateInvoice) {
            val biz = db.businessDao().getBusinessByIdDirect(businessId)
            val prefix = biz?.invoicePrefix ?: "INV-"
            val invoiceNumber = "$prefix${System.currentTimeMillis() % 1000000}"

            val invoice = InvoiceEntity(
                id = invId,
                businessId = businessId,
                invoiceNumber = invoiceNumber,
                customerId = customerId,
                customerName = customerName,
                issueDate = now,
                dueDate = now + (14L * 24 * 60 * 60 * 1000),
                subtotal = subtotal,
                discountAmount = discountAmount,
                taxAmount = taxAmount,
                grandTotal = grandTotal,
                paidAmount = grandTotal,
                status = "PAID",
                createdAt = now
            )
            db.invoiceDao().insertInvoice(invoice)

            val invoiceItems = items.map { item ->
                InvoiceItemEntity(
                    id = "invitem_${UUID.randomUUID()}",
                    invoiceId = invId,
                    productId = item.product.id,
                    description = item.product.name,
                    quantity = item.quantity,
                    unitPrice = item.product.sellingPrice,
                    taxRate = item.product.taxRate,
                    totalAmount = item.product.sellingPrice * item.quantity
                )
            }
            db.invoiceDao().insertInvoiceItems(invoiceItems)

            // Record payment entry
            db.paymentDao().insertPayment(
                PaymentEntity(
                    id = "pay_${UUID.randomUUID()}",
                    businessId = businessId,
                    invoiceId = invId,
                    customerId = customerId,
                    customerName = customerName,
                    amount = grandTotal,
                    paymentDate = now,
                    paymentMethod = paymentMethod,
                    referenceNumber = "POS-SALE-$saleId",
                    notes = "Immediate point-of-sale checkout"
                )
            )
        }

        if (customerId.isNotBlank()) {
            db.customerDao().updateCustomerBalances(customerId, grandTotal, 0.0)
        }

        logAction(businessId, "SALE_COMPLETED", "Sale", "Processed sale #$saleId for ₹$grandTotal via $paymentMethod")
        sale
    }

    // Invoices
    fun getInvoices(businessId: String): Flow<List<InvoiceEntity>> = db.invoiceDao().getInvoicesByBusiness(businessId)
    suspend fun getInvoiceItems(invoiceId: String): List<InvoiceItemEntity> = db.invoiceDao().getInvoiceItems(invoiceId)

    suspend fun createCustomInvoice(
        businessId: String,
        customerId: String,
        customerName: String,
        customerEmail: String,
        customerPhone: String,
        customerAddress: String,
        items: List<InvoiceItemEntity>,
        discountAmount: Double,
        taxAmount: Double,
        notes: String,
        terms: String,
        dueDateDays: Int = 14
    ): InvoiceEntity = withContext(Dispatchers.IO) {
        val biz = db.businessDao().getBusinessByIdDirect(businessId)
        val prefix = biz?.invoicePrefix ?: "INV-"
        val invId = "inv_${UUID.randomUUID()}"
        val invNum = "$prefix${System.currentTimeMillis() % 1000000}"
        val now = System.currentTimeMillis()
        val subtotal = items.sumOf { it.totalAmount }
        val grandTotal = (subtotal - discountAmount) + taxAmount

        val invoice = InvoiceEntity(
            id = invId,
            businessId = businessId,
            invoiceNumber = invNum,
            customerId = customerId,
            customerName = customerName,
            customerEmail = customerEmail,
            customerPhone = customerPhone,
            customerAddress = customerAddress,
            issueDate = now,
            dueDate = now + (dueDateDays.toLong() * 24 * 60 * 60 * 1000),
            subtotal = subtotal,
            discountAmount = discountAmount,
            taxAmount = taxAmount,
            grandTotal = grandTotal,
            paidAmount = 0.0,
            status = "SENT",
            notes = notes,
            terms = terms,
            createdAt = now
        )
        db.invoiceDao().insertInvoice(invoice)

        val updatedItems = items.map { it.copy(id = "invitem_${UUID.randomUUID()}", invoiceId = invId) }
        db.invoiceDao().insertInvoiceItems(updatedItems)

        if (customerId.isNotBlank()) {
            db.customerDao().updateCustomerBalances(customerId, grandTotal, grandTotal)
        }

        logAction(businessId, "INVOICE_CREATED", "Invoice", "Issued invoice $invNum to $customerName (Total: ₹$grandTotal)")
        invoice
    }

    suspend fun recordInvoicePayment(
        invoice: InvoiceEntity,
        paymentAmount: Double,
        method: String,
        referenceNumber: String,
        notes: String
    ) = withContext(Dispatchers.IO) {
        val newPaid = invoice.paidAmount + paymentAmount
        val newStatus = when {
            newPaid >= invoice.grandTotal -> "PAID"
            newPaid > 0 -> "PARTIALLY_PAID"
            else -> invoice.status
        }
        val updatedInv = invoice.copy(paidAmount = newPaid, status = newStatus)
        db.invoiceDao().updateInvoice(updatedInv)

        db.paymentDao().insertPayment(
            PaymentEntity(
                id = "pay_${UUID.randomUUID()}",
                businessId = invoice.businessId,
                invoiceId = invoice.id,
                customerId = invoice.customerId,
                customerName = invoice.customerName,
                amount = paymentAmount,
                paymentDate = System.currentTimeMillis(),
                paymentMethod = method,
                referenceNumber = referenceNumber,
                notes = notes
            )
        )

        if (invoice.customerId.isNotBlank()) {
            db.customerDao().updateCustomerBalances(invoice.customerId, 0.0, -paymentAmount)
        }

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID()}",
                businessId = invoice.businessId,
                title = "Payment Received: ₹$paymentAmount",
                message = "Payment of ₹$paymentAmount recorded for invoice ${invoice.invoiceNumber} (${invoice.customerName}).",
                type = "PAYMENT_RECEIVED",
                actionRoute = "payments"
            )
        )

        logAction(invoice.businessId, "PAYMENT_RECORDED", "Payment", "Received ₹$paymentAmount towards ${invoice.invoiceNumber}")
    }

    // Expenses
    fun getExpenses(businessId: String): Flow<List<ExpenseEntity>> = db.expenseDao().getExpensesByBusiness(businessId)

    suspend fun addExpense(
        businessId: String,
        title: String,
        category: String,
        amount: Double,
        paymentMethod: String,
        description: String,
        receiptUri: String = ""
    ): ExpenseEntity = withContext(Dispatchers.IO) {
        val exp = ExpenseEntity(
            id = "exp_${UUID.randomUUID()}",
            businessId = businessId,
            title = title.trim(),
            category = category,
            amount = amount,
            date = System.currentTimeMillis(),
            paymentMethod = paymentMethod,
            description = description.trim(),
            receiptUri = receiptUri
        )
        db.expenseDao().insertExpense(exp)
        logAction(businessId, "EXPENSE_ADDED", "Expense", "Logged expense: $title (₹$amount, $category)")
        exp
    }

    suspend fun deleteExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
        db.expenseDao().deleteExpense(expense)
        logAction(expense.businessId, "EXPENSE_DELETED", "Expense", "Deleted expense: ${expense.title}")
    }

    // Payments
    fun getPayments(businessId: String): Flow<List<PaymentEntity>> = db.paymentDao().getPaymentsByBusiness(businessId)

    // Staff
    fun getStaff(businessId: String): Flow<List<StaffEntity>> = db.staffDao().getStaffByBusiness(businessId)

    suspend fun addStaff(
        businessId: String,
        fullName: String,
        email: String,
        phone: String,
        role: String
    ): StaffEntity = withContext(Dispatchers.IO) {
        val staff = StaffEntity(
            id = "staff_${UUID.randomUUID()}",
            businessId = businessId,
            fullName = fullName.trim(),
            email = email.trim(),
            phone = phone.trim(),
            role = role
        )
        db.staffDao().insertStaff(staff)
        logAction(businessId, "STAFF_INVITED", "Staff", "Added staff member: $fullName ($role)")
        staff
    }

    suspend fun updateStaffRole(staff: StaffEntity, newRole: String) = withContext(Dispatchers.IO) {
        db.staffDao().updateStaff(staff.copy(role = newRole))
        logAction(staff.businessId, "STAFF_ROLE_UPDATED", "Staff", "Changed ${staff.fullName} role to $newRole")
    }

    suspend fun removeStaff(staff: StaffEntity) = withContext(Dispatchers.IO) {
        db.staffDao().deleteStaff(staff)
        logAction(staff.businessId, "STAFF_REMOVED", "Staff", "Removed staff: ${staff.fullName}")
    }

    // Subscriptions
    fun getSubscriptionPlans(): Flow<List<SubscriptionPlanEntity>> = db.subscriptionDao().getAllPlans()
    fun getSubscription(businessId: String): Flow<SubscriptionEntity?> = db.subscriptionDao().getSubscriptionForBusiness(businessId)
    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>> = db.subscriptionDao().getAllSubscriptions()

    suspend fun updateSubscription(businessId: String, newPlanId: String, billedAmount: Double) = withContext(Dispatchers.IO) {
        val sub = SubscriptionEntity(
            id = "sub_${UUID.randomUUID()}",
            businessId = businessId,
            planId = newPlanId,
            status = "ACTIVE",
            startDate = System.currentTimeMillis(),
            renewalDate = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
            lastBilledAmount = billedAmount,
            autoRenew = true
        )
        db.subscriptionDao().insertSubscription(sub)
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID()}",
                businessId = businessId,
                title = "Subscription Updated: $newPlanId Plan",
                message = "Your business subscription is now upgraded to $newPlanId plan.",
                type = "SUBSCRIPTION",
                actionRoute = "subscription"
            )
        )
        logAction(businessId, "SUBSCRIPTION_CHANGED", "Subscription", "Upgraded plan to $newPlanId (₹$billedAmount/mo)")
    }

    suspend fun updatePlanByAdmin(plan: SubscriptionPlanEntity) = withContext(Dispatchers.IO) {
        db.subscriptionDao().updatePlan(plan)
    }

    // Notifications
    fun getNotifications(businessId: String): Flow<List<NotificationEntity>> = db.notificationDao().getNotificationsByBusiness(businessId)
    suspend fun markNotificationRead(id: String) = db.notificationDao().markAsRead(id)
    suspend fun markAllNotificationsRead(businessId: String) = db.notificationDao().markAllAsRead(businessId)

    // Support Tickets
    fun getTickets(businessId: String): Flow<List<SupportTicketEntity>> = db.supportTicketDao().getTicketsByBusiness(businessId)
    fun getAllTickets(): Flow<List<SupportTicketEntity>> = db.supportTicketDao().getAllTickets()

    suspend fun createTicket(
        businessId: String,
        userEmail: String,
        subject: String,
        category: String,
        priority: String,
        description: String
    ): SupportTicketEntity = withContext(Dispatchers.IO) {
        val ticket = SupportTicketEntity(
            id = "tkt_${UUID.randomUUID()}",
            businessId = businessId,
            userEmail = userEmail,
            subject = subject.trim(),
            category = category,
            priority = priority,
            description = description.trim(),
            status = "OPEN"
        )
        db.supportTicketDao().insertTicket(ticket)
        logAction(businessId, "SUPPORT_TICKET_CREATED", "Support", "Created support ticket: $subject")
        ticket
    }

    suspend fun updateTicketStatus(ticket: SupportTicketEntity, status: String, adminReply: String = "") = withContext(Dispatchers.IO) {
        db.supportTicketDao().updateTicket(ticket.copy(status = status, adminReply = adminReply.ifBlank { ticket.adminReply }))
    }

    // Audit Logs
    fun getAuditLogs(businessId: String): Flow<List<AuditLogEntity>> = db.auditLogDao().getAuditLogs(businessId)

    suspend fun logAction(businessId: String, action: String, entityType: String, details: String) = withContext(Dispatchers.IO) {
        db.auditLogDao().insertLog(
            AuditLogEntity(
                id = "log_${UUID.randomUUID()}",
                businessId = businessId,
                action = action,
                entityType = entityType,
                details = details
            )
        )
    }

    suspend fun seedDemoData() = withContext(Dispatchers.IO) {
        DemoDataSeeder.seedDatabase(db)
    }
}
