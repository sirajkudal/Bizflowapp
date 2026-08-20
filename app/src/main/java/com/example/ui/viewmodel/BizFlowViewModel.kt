package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BizFlowDatabase
import com.example.data.local.entities.*
import com.example.data.repository.BizFlowRepository
import com.example.model.DateRangeFilter
import com.example.model.PlanTier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class BizFlowViewModel(application: Application) : AndroidViewModel(application) {

    private val db = BizFlowDatabase.getInstance(application)
    val repository = BizFlowRepository(db)

    // Current User
    val currentUser: StateFlow<UserEntity?> = repository.getCurrentUser()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All businesses owned or accessible
    val businesses: StateFlow<List<BusinessEntity>> = repository.getAllBusinesses()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Selected Business ID
    private val _selectedBusinessId = MutableStateFlow<String?>(null)
    val selectedBusinessId: StateFlow<String?> = _selectedBusinessId.asStateFlow()

    // Current Active Business
    val currentBusiness: StateFlow<BusinessEntity?> = combine(businesses, _selectedBusinessId) { bizList, selectedId ->
        if (selectedId != null) {
            bizList.find { it.id == selectedId } ?: bizList.firstOrNull()
        } else {
            bizList.firstOrNull()
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Branches
    val branches: StateFlow<List<BranchEntity>> = currentBusiness.flatMapLatest { biz ->
        if (biz != null) repository.getBranches(biz.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Customers
    val customers: StateFlow<List<CustomerEntity>> = currentBusiness.flatMapLatest { biz ->
        if (biz != null) repository.getCustomers(biz.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Products
    val products: StateFlow<List<ProductEntity>> = currentBusiness.flatMapLatest { biz ->
        if (biz != null) repository.getProducts(biz.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Inventory Movements
    val movements: StateFlow<List<InventoryMovementEntity>> = currentBusiness.flatMapLatest { biz ->
        if (biz != null) repository.getInventoryMovements(biz.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sales
    val sales: StateFlow<List<SaleEntity>> = currentBusiness.flatMapLatest { biz ->
        if (biz != null) repository.getSales(biz.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Invoices
    val invoices: StateFlow<List<InvoiceEntity>> = currentBusiness.flatMapLatest { biz ->
        if (biz != null) repository.getInvoices(biz.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Payments
    val payments: StateFlow<List<PaymentEntity>> = currentBusiness.flatMapLatest { biz ->
        if (biz != null) repository.getPayments(biz.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Expenses
    val expenses: StateFlow<List<ExpenseEntity>> = currentBusiness.flatMapLatest { biz ->
        if (biz != null) repository.getExpenses(biz.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Staff
    val staff: StateFlow<List<StaffEntity>> = currentBusiness.flatMapLatest { biz ->
        if (biz != null) repository.getStaff(biz.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Subscription & Plans
    val subscriptionPlans: StateFlow<List<SubscriptionPlanEntity>> = repository.getSubscriptionPlans()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val activeSubscription: StateFlow<SubscriptionEntity?> = currentBusiness.flatMapLatest { biz ->
        if (biz != null) repository.getSubscription(biz.id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val allSubscriptions: StateFlow<List<SubscriptionEntity>> = repository.getAllSubscriptions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notifications
    val notifications: StateFlow<List<NotificationEntity>> = currentBusiness.flatMapLatest { biz ->
        if (biz != null) repository.getNotifications(biz.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Support Tickets
    val supportTickets: StateFlow<List<SupportTicketEntity>> = currentBusiness.flatMapLatest { biz ->
        if (biz != null) repository.getTickets(biz.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAdminTickets: StateFlow<List<SupportTicketEntity>> = repository.getAllTickets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Audit Logs
    val auditLogs: StateFlow<List<AuditLogEntity>> = currentBusiness.flatMapLatest { biz ->
        if (biz != null) repository.getAuditLogs(biz.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filter & POS Cart State
    val selectedDateRange = MutableStateFlow(DateRangeFilter.DAYS_30)
    val cartItems = MutableStateFlow<List<BizFlowRepository.CartItem>>(emptyList())

    // User Feedback Banner/Toast
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        // Auto-seed if database is brand new
        viewModelScope.launch {
            val user = db.userDao().getUserByEmail("demo@bizflow.app")
            if (user == null) {
                repository.seedDemoData()
            }
        }
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    fun selectBusiness(bizId: String) {
        _selectedBusinessId.value = bizId
    }

    fun setDateRange(range: DateRangeFilter) {
        selectedDateRange.value = range
    }

    // Auth actions
    fun login(email: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = repository.loginUser(email)
            if (user != null) {
                showMessage("Welcome back, ${user.fullName}!")
                onComplete(true)
            } else {
                // Auto create or fail
                val newUser = repository.registerUser(email, email.substringBefore("@").replaceFirstChar { it.uppercase() })
                showMessage("Account created for ${newUser.email}!")
                onComplete(true)
            }
        }
    }

    fun register(email: String, fullName: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val user = repository.registerUser(email, fullName)
            showMessage("Account registered successfully! Please set up your business.")
            onComplete()
        }
    }

    fun createBusiness(
        name: String,
        ownerName: String,
        email: String,
        phone: String,
        address: String,
        country: String,
        currencySymbol: String,
        taxRate: Double,
        invoicePrefix: String,
        logoEmoji: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val owner = currentUser.value?.id ?: "user_default"
            val biz = repository.createBusiness(
                ownerId = owner,
                name = name,
                ownerName = ownerName,
                email = email,
                phone = phone,
                address = address,
                country = country,
                currencySymbol = currencySymbol,
                taxRate = taxRate,
                invoicePrefix = invoicePrefix,
                logoEmoji = logoEmoji
            )
            _selectedBusinessId.value = biz.id
            showMessage("Business '${biz.name}' created successfully!")
            onComplete()
        }
    }

    fun updateBusiness(biz: BusinessEntity) {
        viewModelScope.launch {
            repository.updateBusiness(biz)
            showMessage("Business settings saved.")
        }
    }

    // Customer Actions
    fun addCustomer(name: String, phone: String, email: String, address: String, taxId: String, notes: String, onDone: () -> Unit) {
        val biz = currentBusiness.value ?: return
        viewModelScope.launch {
            repository.addCustomer(biz.id, name, phone, email, address, taxId, notes)
            showMessage("Customer '$name' added.")
            onDone()
        }
    }

    fun updateCustomer(customer: CustomerEntity, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
            showMessage("Customer updated.")
            onDone()
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            showMessage("Customer deleted.")
        }
    }

    // Product Actions
    fun addProduct(
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
        description: String,
        onDone: () -> Unit
    ) {
        val biz = currentBusiness.value ?: return
        viewModelScope.launch {
            repository.addProduct(
                businessId = biz.id,
                name = name,
                sku = sku,
                barcode = barcode,
                category = category,
                purchasePrice = purchasePrice,
                sellingPrice = sellingPrice,
                taxRate = taxRate,
                stockQuantity = stockQuantity,
                minStockLevel = minStockLevel,
                supplier = supplier,
                description = description
            )
            showMessage("Product '$name' added to catalog.")
            onDone()
        }
    }

    fun updateProduct(product: ProductEntity, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.updateProduct(product)
            showMessage("Product '${product.name}' updated.")
            onDone()
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            showMessage("Product deleted.")
        }
    }

    fun adjustStock(product: ProductEntity, delta: Int, reason: String, onDone: () -> Unit) {
        val biz = currentBusiness.value ?: return
        viewModelScope.launch {
            val newBal = (product.stockQuantity + delta).coerceAtLeast(0)
            repository.adjustStock(biz.id, product.id, product.name, delta, newBal, reason)
            showMessage("Stock adjusted for ${product.name} ($delta). New Balance: $newBal")
            onDone()
        }
    }

    // POS Cart Actions
    fun addToCart(product: ProductEntity) {
        val current = cartItems.value.toMutableList()
        val idx = current.indexOfFirst { it.product.id == product.id }
        if (idx >= 0) {
            val existing = current[idx]
            current[idx] = existing.copy(quantity = existing.quantity + 1)
        } else {
            current.add(BizFlowRepository.CartItem(product, 1))
        }
        cartItems.value = current
    }

    fun updateCartQuantity(productId: String, qty: Int) {
        if (qty <= 0) {
            cartItems.value = cartItems.value.filter { it.product.id != productId }
        } else {
            cartItems.value = cartItems.value.map {
                if (it.product.id == productId) it.copy(quantity = qty) else it
            }
        }
    }

    fun removeFromCart(productId: String) {
        cartItems.value = cartItems.value.filter { it.product.id != productId }
    }

    fun clearCart() {
        cartItems.value = emptyList()
    }

    fun processSale(
        customerId: String,
        customerName: String,
        discountAmount: Double,
        taxAmount: Double,
        paymentMethod: String,
        generateInvoice: Boolean,
        onSuccess: (SaleEntity) -> Unit
    ) {
        val biz = currentBusiness.value ?: return
        val items = cartItems.value
        if (items.isEmpty()) {
            showMessage("Cart is empty.")
            return
        }
        viewModelScope.launch {
            val sale = repository.processSale(
                businessId = biz.id,
                customerId = customerId,
                customerName = customerName,
                items = items,
                discountAmount = discountAmount,
                taxAmount = taxAmount,
                paymentMethod = paymentMethod,
                generateInvoice = generateInvoice
            )
            clearCart()
            showMessage("Sale of ${biz.currencySymbol}${String.format("%.2f", sale.grandTotal)} recorded successfully!")
            onSuccess(sale)
        }
    }

    // Invoices
    fun createInvoice(
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
        onDone: (InvoiceEntity) -> Unit
    ) {
        val biz = currentBusiness.value ?: return
        viewModelScope.launch {
            val inv = repository.createCustomInvoice(
                businessId = biz.id,
                customerId = customerId,
                customerName = customerName,
                customerEmail = customerEmail,
                customerPhone = customerPhone,
                customerAddress = customerAddress,
                items = items,
                discountAmount = discountAmount,
                taxAmount = taxAmount,
                notes = notes,
                terms = terms
            )
            showMessage("Invoice ${inv.invoiceNumber} created.")
            onDone(inv)
        }
    }

    suspend fun getInvoiceItems(invoiceId: String): List<InvoiceItemEntity> {
        return repository.getInvoiceItems(invoiceId)
    }

    fun recordInvoicePayment(
        invoice: InvoiceEntity,
        amount: Double,
        method: String,
        ref: String,
        notes: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            repository.recordInvoicePayment(invoice, amount, method, ref, notes)
            showMessage("Payment of ₹$amount recorded for ${invoice.invoiceNumber}.")
            onDone()
        }
    }

    // Expenses
    fun addExpense(title: String, category: String, amount: Double, paymentMethod: String, description: String, onDone: () -> Unit) {
        val biz = currentBusiness.value ?: return
        viewModelScope.launch {
            repository.addExpense(biz.id, title, category, amount, paymentMethod, description)
            showMessage("Expense '$title' added.")
            onDone()
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            showMessage("Expense deleted.")
        }
    }

    // Staff
    fun addStaff(fullName: String, email: String, phone: String, role: String, onDone: () -> Unit) {
        val biz = currentBusiness.value ?: return
        viewModelScope.launch {
            repository.addStaff(biz.id, fullName, email, phone, role)
            showMessage("Staff member '$fullName' invited.")
            onDone()
        }
    }

    fun updateStaffRole(staff: StaffEntity, newRole: String) {
        viewModelScope.launch {
            repository.updateStaffRole(staff, newRole)
            showMessage("Role updated for ${staff.fullName}.")
        }
    }

    fun removeStaff(staff: StaffEntity) {
        viewModelScope.launch {
            repository.removeStaff(staff)
            showMessage("Staff member removed.")
        }
    }

    // Subscriptions
    fun upgradeSubscription(planId: String, billedAmount: Double, onDone: () -> Unit) {
        val biz = currentBusiness.value ?: return
        viewModelScope.launch {
            repository.updateSubscription(biz.id, planId, billedAmount)
            showMessage("Subscription upgraded to $planId plan!")
            onDone()
        }
    }

    fun adminUpdatePlan(plan: SubscriptionPlanEntity, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.updatePlanByAdmin(plan)
            showMessage("Plan '${plan.name}' pricing & limits updated.")
            onDone()
        }
    }

    // Notifications
    fun markNotificationRead(id: String) {
        viewModelScope.launch { repository.markNotificationRead(id) }
    }

    fun markAllNotificationsRead() {
        val biz = currentBusiness.value ?: return
        viewModelScope.launch { repository.markAllNotificationsRead(biz.id) }
    }

    // Support
    fun createSupportTicket(subject: String, category: String, priority: String, description: String, onDone: () -> Unit) {
        val biz = currentBusiness.value ?: return
        val email = currentUser.value?.email ?: biz.email
        viewModelScope.launch {
            repository.createTicket(biz.id, email, subject, category, priority, description)
            showMessage("Support ticket submitted. Ticket ID generated.")
            onDone()
        }
    }

    fun adminReplyTicket(ticket: SupportTicketEntity, status: String, reply: String, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.updateTicketStatus(ticket, status, reply)
            showMessage("Ticket #${ticket.id.takeLast(6)} updated.")
            onDone()
        }
    }

    // Reset Demo Data
    fun resetDemoData(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.seedDemoData()
            showMessage("Demo data reloaded successfully!")
            onDone()
        }
    }
}
