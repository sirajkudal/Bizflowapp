package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.entities.CustomerEntity
import com.example.data.local.entities.InvoiceEntity
import com.example.data.local.entities.ProductEntity
import com.example.model.PlanTier
import com.example.ui.components.BizCard
import com.example.ui.components.BizFlowTopBar
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BizFlowViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BizFlowApp(viewModel: BizFlowViewModel) {
    val navController = rememberNavController()

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val currentBusiness by viewModel.currentBusiness.collectAsStateWithLifecycle()
    val allBusinesses by viewModel.businesses.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val movements by viewModel.movements.collectAsStateWithLifecycle()
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val invoices by viewModel.invoices.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val staff by viewModel.staff.collectAsStateWithLifecycle()
    val plans by viewModel.subscriptionPlans.collectAsStateWithLifecycle()
    val subscription by viewModel.activeSubscription.collectAsStateWithLifecycle()
    val allSubs by viewModel.allSubscriptions.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val tickets by viewModel.supportTickets.collectAsStateWithLifecycle()
    val allAdminTickets by viewModel.allAdminTickets.collectAsStateWithLifecycle()
    val selectedRange by viewModel.selectedDateRange.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

    var selectedLegalDoc by remember { mutableStateOf<String?>(null) }
    var selectedInvoiceForDetail by remember { mutableStateOf<InvoiceEntity?>(null) }
    var showMoreMenuSheet by remember { mutableStateOf(false) }

    val unreadNotifs = notifications.count { !it.isRead }
    val currency = currentBusiness?.currencySymbol ?: "₹"

    // Toast/Snackbar message handler
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val isTopLevelScreen = currentRoute in listOf(
        "dashboard", "pos", "invoices", "inventory", "customers",
        "expenses", "payments", "reports", "staff", "subscription",
        "admin", "notifications", "support", "settings"
    )

    val isPublicScreen = currentRoute in listOf("landing", "auth", "onboarding")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (isTopLevelScreen) {
                val screenTitle = when (currentRoute) {
                    "dashboard" -> currentBusiness?.name ?: "BizFlow"
                    "pos" -> "POS Checkout"
                    "invoices" -> "Invoices & Billing"
                    "inventory" -> "Inventory Catalog"
                    "customers" -> "Customers CRM"
                    "expenses" -> "Expense Manager"
                    "payments" -> "Payment Receipts"
                    "reports" -> "Reports & Analytics"
                    "staff" -> "Staff & Access"
                    "subscription" -> "Subscriptions"
                    "admin" -> "Super Admin"
                    "notifications" -> "Notifications"
                    "support" -> "Help Desk"
                    "settings" -> "Store Settings"
                    else -> "BizFlow"
                }

                BizFlowTopBar(
                    title = screenTitle,
                    subtitle = if (currentRoute == "dashboard") currentBusiness?.ownerName else null,
                    businessLogo = currentBusiness?.logoEmoji ?: "🏢",
                    businessName = currentBusiness?.name,
                    unreadNotifsCount = unreadNotifs,
                    showBackButton = currentRoute !in listOf("dashboard", "pos", "invoices", "inventory"),
                    onBackClick = { navController.navigate("dashboard") },
                    onNotifClick = { navController.navigate("notifications") },
                    onSettingsClick = { navController.navigate("settings") },
                    actions = {
                        if (currentRoute == "dashboard") {
                            IconButton(onClick = { navController.navigate("admin") }) {
                                Icon(Icons.Outlined.AdminPanelSettings, contentDescription = "Admin", tint = BrandPrimary)
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (isTopLevelScreen) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentRoute == "dashboard",
                        onClick = { navController.navigate("dashboard") },
                        icon = { Icon(if (currentRoute == "dashboard") Icons.Filled.Dashboard else Icons.Outlined.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Dashboard", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_dashboard")
                    )
                    NavigationBarItem(
                        selected = currentRoute == "pos",
                        onClick = { navController.navigate("pos") },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (cartItems.isNotEmpty()) {
                                        Badge(containerColor = BrandPrimary) { Text("${cartItems.sumOf { it.quantity }}") }
                                    }
                                }
                            ) {
                                Icon(if (currentRoute == "pos") Icons.Filled.PointOfSale else Icons.Outlined.PointOfSale, contentDescription = "POS")
                            }
                        },
                        label = { Text("POS", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_pos")
                    )
                    NavigationBarItem(
                        selected = currentRoute == "invoices",
                        onClick = { navController.navigate("invoices") },
                        icon = { Icon(if (currentRoute == "invoices") Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong, contentDescription = "Invoices") },
                        label = { Text("Invoices", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_invoices")
                    )
                    NavigationBarItem(
                        selected = currentRoute == "inventory",
                        onClick = { navController.navigate("inventory") },
                        icon = { Icon(if (currentRoute == "inventory") Icons.Filled.Inventory2 else Icons.Outlined.Inventory2, contentDescription = "Inventory") },
                        label = { Text("Inventory", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_inventory")
                    )
                    NavigationBarItem(
                        selected = showMoreMenuSheet || currentRoute in listOf("customers", "expenses", "payments", "reports", "staff", "subscription", "admin", "settings", "support"),
                        onClick = { showMoreMenuSheet = true },
                        icon = { Icon(Icons.Default.Menu, contentDescription = "More Modules") },
                        label = { Text("More", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_more")
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = "dashboard"
            ) {
                // Public Landing Page
                composable("landing") {
                    LandingPageScreen(
                        plans = plans,
                        onStartFree = { navController.navigate("auth") },
                        onViewDemo = {
                            viewModel.resetDemoData {
                                navController.navigate("dashboard")
                            }
                        },
                        onSignIn = { navController.navigate("auth") },
                        onViewLegal = { doc -> selectedLegalDoc = doc }
                    )
                }

                // Auth Screen
                composable("auth") {
                    AuthScreen(
                        onLoginSuccess = { email ->
                            viewModel.login(email) { ok ->
                                if (ok) navController.navigate("dashboard")
                            }
                        },
                        onRegisterSuccess = { email, name ->
                            viewModel.register(email, name) {
                                navController.navigate("onboarding")
                            }
                        },
                        onDemoLogin = {
                            viewModel.login("demo@bizflow.app") {
                                navController.navigate("dashboard")
                            }
                        },
                        onBackToLanding = { navController.navigate("landing") }
                    )
                }

                // Onboarding Setup Wizard
                composable("onboarding") {
                    OnboardingScreen(
                        userEmail = currentUser?.email ?: "owner@store.com",
                        onComplete = { name, owner, email, phone, addr, country, curr, tax, prefix, emoji ->
                            viewModel.createBusiness(name, owner, email, phone, addr, country, curr, tax, prefix, emoji) {
                                navController.navigate("dashboard")
                            }
                        }
                    )
                }

                // Main Dashboard
                composable("dashboard") {
                    DashboardScreen(
                        business = currentBusiness,
                        sales = sales,
                        expenses = expenses,
                        customers = customers,
                        products = products,
                        invoices = invoices,
                        selectedRange = selectedRange,
                        onSelectRange = { viewModel.setDateRange(it) },
                        onNavigateToPos = { navController.navigate("pos") },
                        onNavigateToInvoice = { navController.navigate("invoices") },
                        onNavigateToInventory = { navController.navigate("inventory") },
                        onNavigateToCustomers = { navController.navigate("customers") },
                        onNavigateToReports = { navController.navigate("reports") }
                    )
                }

                // POS Sales Screen
                composable("pos") {
                    PosSalesScreen(
                        business = currentBusiness,
                        products = products,
                        customers = customers,
                        cartItems = cartItems,
                        onAddToCart = { viewModel.addToCart(it) },
                        onUpdateQuantity = { id, q -> viewModel.updateCartQuantity(id, q) },
                        onRemoveFromCart = { viewModel.removeFromCart(it) },
                        onClearCart = { viewModel.clearCart() },
                        onProcessSale = { custId, custName, discount, tax, method, genInv, onOk ->
                            viewModel.processSale(custId, custName, discount, tax, method, genInv, onOk)
                        },
                        onViewInvoice = { invId ->
                            val inv = invoices.find { it.id == invId }
                            if (inv != null) {
                                selectedInvoiceForDetail = inv
                                navController.navigate("invoice_detail")
                            }
                        }
                    )
                }

                // Invoices List
                composable("invoices") {
                    InvoiceListScreen(
                        currency = currency,
                        invoices = invoices,
                        customers = customers,
                        products = products,
                        onSelectInvoice = { inv ->
                            selectedInvoiceForDetail = inv
                            navController.navigate("invoice_detail")
                        },
                        onCreateInvoice = { custId, name, email, phone, addr, items, disc, tax, notes, terms ->
                            viewModel.createInvoice(custId, name, email, phone, addr, items, disc, tax, notes, terms) { inv ->
                                selectedInvoiceForDetail = inv
                                navController.navigate("invoice_detail")
                            }
                        }
                    )
                }

                // Invoice Detail Viewer
                composable("invoice_detail") {
                    if (selectedInvoiceForDetail != null) {
                        var invoiceItems by remember { mutableStateOf(emptyList<com.example.data.local.entities.InvoiceItemEntity>()) }
                        LaunchedEffect(selectedInvoiceForDetail) {
                            selectedInvoiceForDetail?.let {
                                invoiceItems = viewModel.getInvoiceItems(it.id)
                            }
                        }
                        InvoiceDetailViewerScreen(
                            business = currentBusiness,
                            invoice = selectedInvoiceForDetail!!,
                            items = invoiceItems,
                            onRecordPayment = { amt, method, ref, notes ->
                                selectedInvoiceForDetail?.let { inv ->
                                    viewModel.recordInvoicePayment(inv, amt, method, ref, notes) {
                                        selectedInvoiceForDetail = inv.copy(
                                            paidAmount = inv.paidAmount + amt,
                                            status = if (inv.paidAmount + amt >= inv.grandTotal) "PAID" else "PARTIALLY_PAID"
                                        )
                                    }
                                }
                            },
                            onBack = { navController.navigateUp() }
                        )
                    }
                }

                // Inventory & Catalog
                composable("inventory") {
                    ProductListScreen(
                        currency = currency,
                        products = products,
                        onAddProduct = { name, sku, barcode, cat, cost, price, tax, qty, minStock, supp, desc ->
                            viewModel.addProduct(name, sku, barcode, cat, cost, price, tax, qty, minStock, supp, desc) {}
                        },
                        onUpdateProduct = { viewModel.updateProduct(it) {} },
                        onDeleteProduct = { viewModel.deleteProduct(it) },
                        onAdjustStock = { prod, delta, reason -> viewModel.adjustStock(prod, delta, reason) {} },
                        onAddToCart = {
                            viewModel.addToCart(it)
                            navController.navigate("pos")
                        }
                    )
                }

                // Customers CRM
                composable("customers") {
                    CustomerListScreen(
                        currency = currency,
                        customers = customers,
                        onAddCustomer = { name, phone, email, address, taxId, notes ->
                            viewModel.addCustomer(name, phone, email, address, taxId, notes) {}
                        },
                        onUpdateCustomer = { viewModel.updateCustomer(it) {} },
                        onDeleteCustomer = { viewModel.deleteCustomer(it) },
                        onCreateInvoiceForCustomer = { cust ->
                            navController.navigate("invoices")
                        }
                    )
                }

                // Expenses Screen
                composable("expenses") {
                    ExpenseScreen(
                        currency = currency,
                        expenses = expenses,
                        onAddExpense = { title, cat, amount, method, desc ->
                            viewModel.addExpense(title, cat, amount, method, desc) {}
                        },
                        onDeleteExpense = { viewModel.deleteExpense(it) }
                    )
                }

                // Payments Screen
                composable("payments") {
                    PaymentsScreen(
                        currency = currency,
                        payments = payments
                    )
                }

                // Reports Screen
                composable("reports") {
                    ReportsScreen(
                        currency = currency,
                        sales = sales,
                        expenses = expenses,
                        products = products,
                        customers = customers
                    )
                }

                // Staff & Roles
                composable("staff") {
                    StaffScreen(
                        staffList = staff,
                        onAddStaff = { name, email, phone, role -> viewModel.addStaff(name, email, phone, role) {} },
                        onUpdateRole = { stf, r -> viewModel.updateStaffRole(stf, r) },
                        onRemoveStaff = { viewModel.removeStaff(it) }
                    )
                }

                // Subscriptions & Plans
                composable("subscription") {
                    SubscriptionScreen(
                        currentSubscription = subscription,
                        plans = plans,
                        onUpgradePlan = { planId, amt -> viewModel.upgradeSubscription(planId, amt) {} }
                    )
                }

                // Admin Control Panel
                composable("admin") {
                    AdminPanelScreen(
                        businesses = allBusinesses,
                        subscriptions = allSubs,
                        plans = plans,
                        tickets = allAdminTickets,
                        onUpdatePlan = { viewModel.adminUpdatePlan(it) {} },
                        onReplyTicket = { tkt, st, rep -> viewModel.adminReplyTicket(tkt, st, rep) {} }
                    )
                }

                // Notifications
                composable("notifications") {
                    NotificationsScreen(
                        notifications = notifications,
                        onMarkRead = { viewModel.markNotificationRead(it) },
                        onMarkAllRead = { viewModel.markAllNotificationsRead() }
                    )
                }

                // Support Desk
                composable("support") {
                    SupportScreen(
                        tickets = tickets,
                        onCreateTicket = { sub, cat, prio, desc -> viewModel.createSupportTicket(sub, cat, prio, desc) {} }
                    )
                }

                // Settings Screen
                composable("settings") {
                    SettingsScreen(
                        currentBusiness = currentBusiness,
                        allBusinesses = allBusinesses,
                        onSelectBusiness = { viewModel.selectBusiness(it) },
                        onUpdateBusiness = { viewModel.updateBusiness(it) },
                        onResetDemoData = { viewModel.resetDemoData {} },
                        onViewLegal = { doc -> selectedLegalDoc = doc },
                        onNavigateToLanding = { navController.navigate("landing") }
                    )
                }
            }
        }
    }

    // More Modules Hub Bottom Sheet
    if (showMoreMenuSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreMenuSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text("All Business Modules", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))

                val menuModules = listOf(
                    MenuModuleItem("Customers CRM", Icons.Outlined.People, "customers"),
                    MenuModuleItem("Expenses", Icons.Outlined.AccountBalanceWallet, "expenses"),
                    MenuModuleItem("Payments Ledger", Icons.Outlined.Payment, "payments"),
                    MenuModuleItem("BI Reports", Icons.Outlined.Assessment, "reports"),
                    MenuModuleItem("Staff & Roles", Icons.Outlined.Badge, "staff"),
                    MenuModuleItem("Subscriptions", Icons.Outlined.Stars, "subscription"),
                    MenuModuleItem("Super Admin", Icons.Outlined.AdminPanelSettings, "admin"),
                    MenuModuleItem("Help & Support", Icons.Outlined.SupportAgent, "support"),
                    MenuModuleItem("Store Settings", Icons.Outlined.Settings, "settings"),
                    MenuModuleItem("Landing Page", Icons.Outlined.Language, "landing")
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(menuModules) { item ->
                        BizCard(
                            onClick = {
                                showMoreMenuSheet = false
                                navController.navigate(item.route)
                            }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(BrandPrimary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(item.icon, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(22.dp))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (selectedLegalDoc != null) {
        LegalDocViewerModal(
            docType = selectedLegalDoc!!,
            onDismiss = { selectedLegalDoc = null }
        )
    }
}

private data class MenuModuleItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)
