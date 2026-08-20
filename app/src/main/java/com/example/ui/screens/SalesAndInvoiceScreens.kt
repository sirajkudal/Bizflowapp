package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.*
import com.example.data.repository.BizFlowRepository
import com.example.model.PaymentMethod
import com.example.service.ReportExportService
import com.example.ui.components.BizCard
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SearchAndFilterBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---------------- POS SALES SCREEN ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosSalesScreen(
    business: BusinessEntity?,
    products: List<ProductEntity>,
    customers: List<CustomerEntity>,
    cartItems: List<BizFlowRepository.CartItem>,
    onAddToCart: (ProductEntity) -> Unit,
    onUpdateQuantity: (productId: String, qty: Int) -> Unit,
    onRemoveFromCart: (productId: String) -> Unit,
    onClearCart: () -> Unit,
    onProcessSale: (customerId: String, customerName: String, discount: Double, tax: Double, method: String, genInvoice: Boolean, (SaleEntity) -> Unit) -> Unit,
    onViewInvoice: (String) -> Unit
) {
    val currency = business?.currencySymbol ?: "₹"
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showCheckoutSheet by remember { mutableStateOf(false) }
    var completedSale by remember { mutableStateOf<SaleEntity?>(null) }

    val categories = listOf("All") + products.map { it.category }.distinct()
    val filteredProducts = products.filter { p ->
        val matchesQuery = p.name.contains(searchQuery, ignoreCase = true) ||
                p.sku.contains(searchQuery, ignoreCase = true) ||
                p.barcode.contains(searchQuery, ignoreCase = true)
        val matchesCat = selectedCategory == "All" || p.category == selectedCategory
        matchesQuery && matchesCat
    }

    val cartTotalCount = cartItems.sumOf { it.quantity }
    val cartSubtotal = cartItems.sumOf { it.product.sellingPrice * it.quantity }

    Scaffold(
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "$cartTotalCount items in cart",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400
                            )
                            Text(
                                "$currency${String.format(Locale.US, "%,.2f", cartSubtotal)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = BrandPrimary
                            )
                        }
                        Button(
                            onClick = { showCheckoutSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("pos_checkout_btn")
                        ) {
                            Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Review & Pay", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            SearchAndFilterBar(
                searchQuery = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search POS item by name, SKU or barcode..."
            )

            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredProducts.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.PointOfSale,
                    title = "No Items Found",
                    description = "No items matched your POS search."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        val inCart = cartItems.find { it.product.id == product.id }?.quantity ?: 0
                        BizCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(product.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("SKU: ${product.sku} • In Stock: ${product.stockQuantity}", style = MaterialTheme.typography.bodySmall, color = Slate400)
                                    Text(
                                        "$currency${String.format(Locale.US, "%,.2f", product.sellingPrice)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandPrimary
                                    )
                                }

                                if (inCart > 0) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = { onUpdateQuantity(product.id, inCart - 1) },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Slate200.copy(alpha = 0.4f))
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                        }
                                        Text("$inCart", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp))
                                        IconButton(
                                            onClick = { onUpdateQuantity(product.id, inCart + 1) },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(BrandPrimary)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = { onAddToCart(product) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Checkout Bottom Sheet
    if (showCheckoutSheet) {
        CheckoutModalSheet(
            currency = currency,
            defaultTaxRate = business?.defaultTaxRate ?: 18.0,
            cartItems = cartItems,
            customers = customers,
            onDismiss = { showCheckoutSheet = false },
            onUpdateQuantity = onUpdateQuantity,
            onRemove = onRemoveFromCart,
            onConfirmSale = { custId, custName, discount, tax, method, genInv ->
                onProcessSale(custId, custName, discount, tax, method, genInv) { sale ->
                    showCheckoutSheet = false
                    completedSale = sale
                }
            }
        )
    }

    // Completed Sale Success Dialog
    if (completedSale != null) {
        val sale = completedSale!!
        AlertDialog(
            onDismissRequest = { completedSale = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessEmerald)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sale Complete!")
                }
            },
            text = {
                Column {
                    Text("Receipt Total: $currency${String.format(Locale.US, "%,.2f", sale.grandTotal)}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SuccessEmerald)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Customer: ${sale.customerName}")
                    Text("Payment Method: ${sale.paymentMethod}")
                    Text("Transaction ID: ${sale.id}")
                    if (sale.invoiceId.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Tax Invoice Generated and linked to transaction.", color = BrandPrimary, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val invId = sale.invoiceId
                        completedSale = null
                        if (invId.isNotBlank()) {
                            onViewInvoice(invId)
                        }
                    }
                ) {
                    Text(if (sale.invoiceId.isNotBlank()) "View Tax Invoice" else "Done")
                }
            },
            dismissButton = {
                TextButton(onClick = { completedSale = null }) { Text("New Sale") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckoutModalSheet(
    currency: String,
    defaultTaxRate: Double,
    cartItems: List<BizFlowRepository.CartItem>,
    customers: List<CustomerEntity>,
    onDismiss: () -> Unit,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemove: (String) -> Unit,
    onConfirmSale: (customerId: String, customerName: String, discount: Double, tax: Double, method: String, genInvoice: Boolean) -> Unit
) {
    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var walkInName by remember { mutableStateOf("Walk-in Customer") }
    var discountStr by remember { mutableStateOf("0") }
    var applyTax by remember { mutableStateOf(true) }
    var selectedMethod by remember { mutableStateOf("UPI") }
    var generateInvoice by remember { mutableStateOf(true) }

    val subtotal = cartItems.sumOf { it.product.sellingPrice * it.quantity }
    val discount = discountStr.toDoubleOrNull() ?: 0.0
    val taxableAmount = (subtotal - discount).coerceAtLeast(0.0)
    val taxAmount = if (applyTax) taxableAmount * (defaultTaxRate / 100.0) else 0.0
    val grandTotal = taxableAmount + taxAmount

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text("Point of Sale Checkout", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // Cart Items Review
            cartItems.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("$currency${String.format(Locale.US, "%.2f", item.product.sellingPrice)} each", style = MaterialTheme.typography.bodySmall, color = Slate400)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onUpdateQuantity(item.product.id, item.quantity - 1) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                        Text("${item.quantity}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                        IconButton(onClick = { onUpdateQuantity(item.product.id, item.quantity + 1) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "$currency${String.format(Locale.US, "%.2f", item.product.sellingPrice * item.quantity)}",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Customer Selector
            Text("Customer Attribution", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            var expandedCustDropdown by remember { mutableStateOf(false) }

            OutlinedCard(
                onClick = { expandedCustDropdown = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedCustomer?.name ?: walkInName, style = MaterialTheme.typography.bodyMedium)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            DropdownMenu(
                expanded = expandedCustDropdown,
                onDismissRequest = { expandedCustDropdown = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Walk-in Customer (Guest)") },
                    onClick = {
                        selectedCustomer = null
                        walkInName = "Walk-in Customer"
                        expandedCustDropdown = false
                    }
                )
                customers.forEach { c ->
                    DropdownMenuItem(
                        text = { Text("${c.name} (${c.phone})") },
                        onClick = {
                            selectedCustomer = c
                            walkInName = c.name
                            expandedCustDropdown = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Discount & Tax
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = discountStr,
                    onValueChange = { discountStr = it },
                    label = { Text("Discount ($currency)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = applyTax,
                    onClick = { applyTax = !applyTax },
                    label = { Text("Add Tax (${defaultTaxRate.toInt()}%)") },
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Payment Method Selector
            Text("Payment Method", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            val methods = listOf("UPI", "Cash", "Card", "Bank Transfer")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(methods) { m ->
                    FilterChip(
                        selected = selectedMethod == m,
                        onClick = { selectedMethod = m },
                        label = { Text(m) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Financial Summary
            BizCard(backgroundColor = MaterialTheme.colorScheme.surfaceVariant) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
                    Text("$currency${String.format(Locale.US, "%.2f", subtotal)}", style = MaterialTheme.typography.bodyMedium)
                }
                if (discount > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Discount", style = MaterialTheme.typography.bodyMedium, color = DangerRose)
                        Text("- $currency${String.format(Locale.US, "%.2f", discount)}", style = MaterialTheme.typography.bodyMedium, color = DangerRose)
                    }
                }
                if (applyTax) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tax / GST ($defaultTaxRate%)", style = MaterialTheme.typography.bodyMedium)
                        Text("+ $currency${String.format(Locale.US, "%.2f", taxAmount)}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Grand Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("$currency${String.format(Locale.US, "%,.2f", grandTotal)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = BrandPrimary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val custId = selectedCustomer?.id ?: ""
                    val custName = selectedCustomer?.name ?: walkInName
                    onConfirmSale(custId, custName, discount, taxAmount, selectedMethod, generateInvoice)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("confirm_sale_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessEmerald),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Complete & Collect $currency${String.format(Locale.US, "%,.2f", grandTotal)}", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ---------------- INVOICE LIST SCREEN ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreen(
    currency: String,
    invoices: List<InvoiceEntity>,
    customers: List<CustomerEntity>,
    products: List<ProductEntity>,
    onSelectInvoice: (InvoiceEntity) -> Unit,
    onCreateInvoice: (customerId: String, name: String, email: String, phone: String, address: String, items: List<InvoiceItemEntity>, discount: Double, tax: Double, notes: String, terms: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("ALL") }
    var showCreateDialog by remember { mutableStateOf(false) }

    val statuses = listOf("ALL", "SENT", "PAID", "PARTIALLY_PAID", "OVERDUE", "DRAFT")

    val filteredInvoices = invoices.filter { inv ->
        val matchesQuery = inv.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                inv.customerName.contains(searchQuery, ignoreCase = true)
        val matchesStatus = if (selectedStatus == "ALL") true else inv.status.equals(selectedStatus, ignoreCase = true)
        matchesQuery && matchesStatus
    }

    val totalInvoiced = invoices.sumOf { it.grandTotal }
    val totalPaid = invoices.sumOf { it.paidAmount }
    val totalPending = totalInvoiced - totalPaid

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = BrandPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("create_invoice_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Invoice")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            SearchAndFilterBar(
                searchQuery = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search by invoice # or customer name..."
            )

            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(statuses) { st ->
                    FilterChip(
                        selected = selectedStatus == st,
                        onClick = { selectedStatus = st },
                        label = { Text(st.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Invoicing Quick Metric Summary
            BizCard(backgroundColor = MaterialTheme.colorScheme.surfaceVariant) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Total Invoiced", style = MaterialTheme.typography.labelSmall, color = Slate400)
                        Text("$currency${String.format(Locale.US, "%,.0f", totalInvoiced)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Collected", style = MaterialTheme.typography.labelSmall, color = Slate400)
                        Text("$currency${String.format(Locale.US, "%,.0f", totalPaid)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SuccessEmerald)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Pending Due", style = MaterialTheme.typography.labelSmall, color = Slate400)
                        Text("$currency${String.format(Locale.US, "%,.0f", totalPending)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DangerRose)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredInvoices.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.ReceiptLong,
                    title = "No Invoices Found",
                    description = if (searchQuery.isNotEmpty()) "No invoice matching your filter." else "Generate professional tax invoices for clients and track collections.",
                    actionButtonText = if (searchQuery.isEmpty()) "Create First Invoice" else null,
                    onActionClick = { showCreateDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredInvoices, key = { it.id }) { invoice ->
                        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
                        val dateStr = dateFormat.format(Date(invoice.issueDate))
                        val dueStr = dateFormat.format(Date(invoice.dueDate))

                        BizCard(onClick = { onSelectInvoice(invoice) }) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(invoice.invoiceNumber, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        StatusBadge(status = invoice.status)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(invoice.customerName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text("Issued: $dateStr • Due: $dueStr", style = MaterialTheme.typography.bodySmall, color = Slate400)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "$currency${String.format(Locale.US, "%,.2f", invoice.grandTotal)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandPrimary
                                    )
                                    if (invoice.paidAmount > 0 && invoice.status != "PAID") {
                                        Text(
                                            "Paid: $currency${String.format(Locale.US, "%,.0f", invoice.paidAmount)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SuccessEmerald
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateCustomInvoiceModal(
            currency = currency,
            customers = customers,
            products = products,
            onDismiss = { showCreateDialog = false },
            onCreate = { custId, name, email, phone, addr, items, disc, tax, notes, terms ->
                onCreateInvoice(custId, name, email, phone, addr, items, disc, tax, notes, terms)
                showCreateDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateCustomInvoiceModal(
    currency: String,
    customers: List<CustomerEntity>,
    products: List<ProductEntity>,
    onDismiss: () -> Unit,
    onCreate: (customerId: String, name: String, email: String, phone: String, address: String, items: List<InvoiceItemEntity>, discount: Double, tax: Double, notes: String, terms: String) -> Unit
) {
    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var customName by remember { mutableStateOf("") }
    var customEmail by remember { mutableStateOf("") }
    var customPhone by remember { mutableStateOf("") }
    var customAddress by remember { mutableStateOf("") }
    var lineItems by remember { mutableStateOf(listOf(InvoiceItemEntity(id = "1", invoiceId = "", productId = "", description = "Professional Services", quantity = 1, unitPrice = 1000.0, taxRate = 18.0, totalAmount = 1000.0))) }
    var discountStr by remember { mutableStateOf("0") }
    var taxRateStr by remember { mutableStateOf("18.0") }
    var notes by remember { mutableStateOf("Thank you for your business. Please pay within 14 days.") }
    var terms by remember { mutableStateOf("Late payments are subject to a 1.5% monthly surcharge.") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text("Create Tax Invoice", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // Select Customer
            Text("Client / Customer", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            var expandedCustDropdown by remember { mutableStateOf(false) }

            OutlinedCard(
                onClick = { expandedCustDropdown = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedCustomer?.name ?: customName.ifBlank { "Select from CRM or enter below" }, style = MaterialTheme.typography.bodyMedium)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            DropdownMenu(
                expanded = expandedCustDropdown,
                onDismissRequest = { expandedCustDropdown = false }
            ) {
                customers.forEach { c ->
                    DropdownMenuItem(
                        text = { Text("${c.name} (${c.phone})") },
                        onClick = {
                            selectedCustomer = c
                            customName = c.name
                            customEmail = c.email
                            customPhone = c.phone
                            customAddress = c.address
                            expandedCustDropdown = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            if (selectedCustomer == null) {
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Customer Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customPhone,
                    onValueChange = { customPhone = it },
                    label = { Text("Customer Phone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customEmail,
                    onValueChange = { customEmail = it },
                    label = { Text("Customer Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Line Items", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            lineItems.forEachIndexed { index, item ->
                BizCard(modifier = Modifier.padding(vertical = 4.dp)) {
                    OutlinedTextField(
                        value = item.description,
                        onValueChange = { desc ->
                            lineItems = lineItems.toMutableList().also {
                                it[index] = item.copy(description = desc)
                            }
                        },
                        label = { Text("Description / Item") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = "${item.quantity}",
                            onValueChange = { q ->
                                val qty = q.toIntOrNull() ?: 1
                                lineItems = lineItems.toMutableList().also {
                                    it[index] = item.copy(quantity = qty, totalAmount = qty * item.unitPrice)
                                }
                            },
                            label = { Text("Qty") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = "${item.unitPrice}",
                            onValueChange = { p ->
                                val price = p.toDoubleOrNull() ?: 0.0
                                lineItems = lineItems.toMutableList().also {
                                    it[index] = item.copy(unitPrice = price, totalAmount = item.quantity * price)
                                }
                            },
                            label = { Text("Unit Price ($currency)") },
                            singleLine = true,
                            modifier = Modifier.weight(1.5f)
                        )
                    }
                }
            }

            TextButton(
                onClick = {
                    lineItems = lineItems + InvoiceItemEntity(
                        id = "${lineItems.size + 1}",
                        invoiceId = "",
                        productId = "",
                        description = "Additional Item",
                        quantity = 1,
                        unitPrice = 500.0,
                        taxRate = 18.0,
                        totalAmount = 500.0
                    )
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Line Item")
            }

            Spacer(modifier = Modifier.height(14.dp))
            val subtotal = lineItems.sumOf { it.totalAmount }
            val discount = discountStr.toDoubleOrNull() ?: 0.0
            val taxRate = taxRateStr.toDoubleOrNull() ?: 18.0
            val taxAmount = (subtotal - discount).coerceAtLeast(0.0) * (taxRate / 100.0)
            val grandTotal = (subtotal - discount).coerceAtLeast(0.0) + taxAmount

            BizCard(backgroundColor = MaterialTheme.colorScheme.surfaceVariant) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
                    Text("$currency${String.format(Locale.US, "%.2f", subtotal)}", style = MaterialTheme.typography.bodyMedium)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tax ($taxRate%)", style = MaterialTheme.typography.bodyMedium)
                    Text("+ $currency${String.format(Locale.US, "%.2f", taxAmount)}", style = MaterialTheme.typography.bodyMedium)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Due", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("$currency${String.format(Locale.US, "%,.2f", grandTotal)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = BrandPrimary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    val name = selectedCustomer?.name ?: customName.ifBlank { "Valued Client" }
                    val custId = selectedCustomer?.id ?: ""
                    onCreate(custId, name, customEmail, customPhone, customAddress, lineItems, discount, taxAmount, notes, terms)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate & Issue Invoice", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// ---------------- INVOICE DETAIL & PRINTABLE VIEWER ----------------

@Composable
fun InvoiceDetailViewerScreen(
    business: BusinessEntity?,
    invoice: InvoiceEntity,
    items: List<InvoiceItemEntity>,
    onRecordPayment: (amount: Double, method: String, ref: String, notes: String) -> Unit,
    onBack: () -> Unit
) {
    val currency = business?.currencySymbol ?: "₹"
    var showRecordPaymentDialog by remember { mutableStateOf(false) }
    var shareNotice by remember { mutableStateOf<String?>(null) }

    val printableText = remember(invoice, items) {
        ReportExportService.generateInvoicePrintableText(business, invoice, items)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Tax Invoice Preview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row {
                IconButton(onClick = {
                    shareNotice = "Invoice copied to clipboard / ready to share on WhatsApp."
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = BrandPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (shareNotice != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SuccessEmerald.copy(alpha = 0.15f))
                    .padding(8.dp)
            ) {
                Text(shareNotice ?: "", color = SuccessEmeraldDark, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Formatted Document Canvas
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = printableText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (invoice.status != "PAID") {
            Button(
                onClick = { showRecordPaymentDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessEmerald),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Payment, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Record Payment Collected", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showRecordPaymentDialog) {
        val balanceDue = (invoice.grandTotal - invoice.paidAmount).coerceAtLeast(0.0)
        var payAmountStr by remember { mutableStateOf("$balanceDue") }
        var payMethod by remember { mutableStateOf("UPI") }
        var refNum by remember { mutableStateOf("REF-${System.currentTimeMillis() % 100000}") }
        var notes by remember { mutableStateOf("Settled by customer") }

        AlertDialog(
            onDismissRequest = { showRecordPaymentDialog = false },
            title = { Text("Record Invoice Payment") },
            text = {
                Column {
                    Text("Total Balance Due: $currency${String.format(Locale.US, "%,.2f", balanceDue)}", color = DangerRose, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = payAmountStr,
                        onValueChange = { payAmountStr = it },
                        label = { Text("Payment Amount ($currency)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = payMethod,
                        onValueChange = { payMethod = it },
                        label = { Text("Payment Method (UPI, Cash, Bank)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = refNum,
                        onValueChange = { refNum = it },
                        label = { Text("Reference / UTR Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = payAmountStr.toDoubleOrNull() ?: balanceDue
                        onRecordPayment(amt, payMethod, refNum, notes)
                        showRecordPaymentDialog = false
                    }
                ) {
                    Text("Confirm Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecordPaymentDialog = false }) { Text("Cancel") }
            }
        )
    }
}
