package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.CustomerEntity
import com.example.data.local.entities.ProductEntity
import com.example.ui.components.BizCard
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SearchAndFilterBar
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    currency: String,
    customers: List<CustomerEntity>,
    onAddCustomer: (name: String, phone: String, email: String, address: String, taxId: String, notes: String) -> Unit,
    onUpdateCustomer: (CustomerEntity) -> Unit,
    onDeleteCustomer: (CustomerEntity) -> Unit,
    onCreateInvoiceForCustomer: (CustomerEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterOutstandingOnly by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var selectedCustomerForDetails by remember { mutableStateOf<CustomerEntity?>(null) }

    val filteredCustomers = customers.filter { cust ->
        val matchesQuery = cust.name.contains(searchQuery, ignoreCase = true) ||
                cust.phone.contains(searchQuery, ignoreCase = true) ||
                cust.email.contains(searchQuery, ignoreCase = true)
        val matchesFilter = if (filterOutstandingOnly) cust.outstandingBalance > 0 else true
        matchesQuery && matchesFilter
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BrandPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_customer_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer")
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
                placeholder = "Search customers by name, phone or email..."
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = filterOutstandingOnly,
                    onClick = { filterOutstandingOnly = !filterOutstandingOnly },
                    label = { Text("Outstanding Balance Only (${customers.count { it.outstandingBalance > 0 }})", fontSize = 12.sp) }
                )
                Text("${filteredCustomers.size} customers", style = MaterialTheme.typography.bodySmall, color = Slate400)
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredCustomers.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.People,
                    title = "No Customers Found",
                    description = if (searchQuery.isNotEmpty()) "No customer matched your search query." else "Add your client database to track invoices, credit, and sales.",
                    actionButtonText = if (searchQuery.isEmpty()) "Add First Customer" else null,
                    onActionClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredCustomers, key = { it.id }) { customer ->
                        CustomerItemCard(
                            customer = customer,
                            currency = currency,
                            onClick = { selectedCustomerForDetails = customer },
                            onEdit = { editingCustomer = customer },
                            onDelete = { onDeleteCustomer(customer) },
                            onCreateInvoice = { onCreateInvoiceForCustomer(customer) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog || editingCustomer != null) {
        val target = editingCustomer
        CustomerFormDialog(
            initialCustomer = target,
            onDismiss = {
                showAddDialog = false
                editingCustomer = null
            },
            onSave = { name, phone, email, address, taxId, notes ->
                if (target != null) {
                    onUpdateCustomer(
                        target.copy(
                            name = name,
                            phone = phone,
                            email = email,
                            address = address,
                            taxId = taxId,
                            notes = notes
                        )
                    )
                } else {
                    onAddCustomer(name, phone, email, address, taxId, notes)
                }
                showAddDialog = false
                editingCustomer = null
            }
        )
    }

    if (selectedCustomerForDetails != null) {
        val cust = selectedCustomerForDetails!!
        CustomerDetailsBottomSheet(
            customer = cust,
            currency = currency,
            onDismiss = { selectedCustomerForDetails = null },
            onEdit = {
                selectedCustomerForDetails = null
                editingCustomer = cust
            },
            onCreateInvoice = {
                selectedCustomerForDetails = null
                onCreateInvoiceForCustomer(cust)
            }
        )
    }
}

@Composable
private fun CustomerItemCard(
    customer: CustomerEntity,
    currency: String,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCreateInvoice: () -> Unit
) {
    BizCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(BrandPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = customer.name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = BrandPrimary,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = customer.phone.ifBlank { customer.email.ifBlank { "No contact specified" } },
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$currency${String.format(Locale.US, "%,.0f", customer.totalPurchases)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = SuccessEmerald
                )
                if (customer.outstandingBalance > 0) {
                    Text(
                        text = "Due: $currency${String.format(Locale.US, "%,.0f", customer.outstandingBalance)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = DangerRose,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerFormDialog(
    initialCustomer: CustomerEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, email: String, address: String, taxId: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(initialCustomer?.name ?: "") }
    var phone by remember { mutableStateOf(initialCustomer?.phone ?: "") }
    var email by remember { mutableStateOf(initialCustomer?.email ?: "") }
    var address by remember { mutableStateOf(initialCustomer?.address ?: "") }
    var taxId by remember { mutableStateOf(initialCustomer?.taxId ?: "") }
    var notes by remember { mutableStateOf(initialCustomer?.notes ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialCustomer == null) "Add Customer" else "Edit Customer") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Billing Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = taxId,
                    onValueChange = { taxId = it },
                    label = { Text("GSTIN / Tax ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Terms") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(error ?: "", color = DangerRose, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        error = "Name is required."
                        return@Button
                    }
                    onSave(name, phone, email, address, taxId, notes)
                }
            ) {
                Text("Save Customer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerDetailsBottomSheet(
    customer: CustomerEntity,
    currency: String,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onCreateInvoice: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(customer.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Customer CRM ID: ${customer.id.takeLast(8)}", style = MaterialTheme.typography.labelSmall, color = Slate400)
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Customer", tint = BrandPrimary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            BizCard(backgroundColor = MaterialTheme.colorScheme.surfaceVariant) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Total Lifetime Purchases", style = MaterialTheme.typography.bodySmall, color = Slate400)
                        Text("$currency${String.format(Locale.US, "%,.2f", customer.totalPurchases)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SuccessEmerald)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Outstanding Balance", style = MaterialTheme.typography.bodySmall, color = Slate400)
                        Text("$currency${String.format(Locale.US, "%,.2f", customer.outstandingBalance)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (customer.outstandingBalance > 0) DangerRose else Slate500)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            DetailRow(Icons.Default.Phone, "Phone", customer.phone.ifBlank { "Not provided" })
            DetailRow(Icons.Default.Email, "Email", customer.email.ifBlank { "Not provided" })
            DetailRow(Icons.Default.LocationOn, "Address", customer.address.ifBlank { "Not provided" })
            DetailRow(Icons.Default.Badge, "GSTIN / Tax ID", customer.taxId.ifBlank { "Unregistered" })
            if (customer.notes.isNotBlank()) {
                DetailRow(Icons.Default.Notes, "Notes", customer.notes)
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onCreateInvoice,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Tax Invoice for ${customer.name}")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Slate400, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Slate400)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// ---------------- Product List Screen ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    currency: String,
    products: List<ProductEntity>,
    onAddProduct: (name: String, sku: String, barcode: String, category: String, cost: Double, price: Double, tax: Double, qty: Int, minStock: Int, supplier: String, desc: String) -> Unit,
    onUpdateProduct: (ProductEntity) -> Unit,
    onDeleteProduct: (ProductEntity) -> Unit,
    onAdjustStock: (ProductEntity, delta: Int, reason: String) -> Unit,
    onAddToCart: (ProductEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var filterLowStockOnly by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var adjustingStockProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var showBarcodeScannerSim by remember { mutableStateOf(false) }

    val categories = listOf("All") + products.map { it.category }.distinct()

    val filteredProducts = products.filter { p ->
        val matchesQuery = p.name.contains(searchQuery, ignoreCase = true) ||
                p.sku.contains(searchQuery, ignoreCase = true) ||
                p.barcode.contains(searchQuery, ignoreCase = true)
        val matchesCat = selectedCategory == "All" || p.category == selectedCategory
        val matchesStock = if (filterLowStockOnly) p.stockQuantity <= p.minStockLevel else true
        matchesQuery && matchesCat && matchesStock
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BrandPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
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
                placeholder = "Search catalog by name, SKU or barcode...",
                trailingContent = {
                    IconButton(
                        onClick = { showBarcodeScannerSim = true },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Barcode", tint = BrandPrimary)
                    }
                }
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

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = filterLowStockOnly,
                    onClick = { filterLowStockOnly = !filterLowStockOnly },
                    label = { Text("Low Stock Alert (${products.count { it.stockQuantity <= it.minStockLevel }})", fontSize = 12.sp) }
                )
                Text("${filteredProducts.size} SKUs", style = MaterialTheme.typography.bodySmall, color = Slate400)
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredProducts.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.Inventory2,
                    title = "No Products in Catalog",
                    description = if (searchQuery.isNotEmpty()) "No product matching your filters." else "Build your inventory catalog to start billing sales.",
                    actionButtonText = if (searchQuery.isEmpty()) "Add First Product" else null,
                    onActionClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductItemCard(
                            product = product,
                            currency = currency,
                            onEdit = { editingProduct = product },
                            onDelete = { onDeleteProduct(product) },
                            onAdjustStock = { adjustingStockProduct = product },
                            onAddToCart = { onAddToCart(product) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog || editingProduct != null) {
        val target = editingProduct
        ProductFormDialog(
            initialProduct = target,
            onDismiss = {
                showAddDialog = false
                editingProduct = null
            },
            onSave = { name, sku, barcode, cat, cost, price, tax, qty, minStock, supp, desc ->
                if (target != null) {
                    onUpdateProduct(
                        target.copy(
                            name = name,
                            sku = sku,
                            barcode = barcode,
                            category = cat,
                            purchasePrice = cost,
                            sellingPrice = price,
                            taxRate = tax,
                            stockQuantity = qty,
                            minStockLevel = minStock,
                            supplier = supp,
                            description = desc
                        )
                    )
                } else {
                    onAddProduct(name, sku, barcode, cat, cost, price, tax, qty, minStock, supp, desc)
                }
                showAddDialog = false
                editingProduct = null
            }
        )
    }

    if (adjustingStockProduct != null) {
        val prod = adjustingStockProduct!!
        StockAdjustDialog(
            product = prod,
            onDismiss = { adjustingStockProduct = null },
            onConfirm = { delta, reason ->
                onAdjustStock(prod, delta, reason)
                adjustingStockProduct = null
            }
        )
    }

    if (showBarcodeScannerSim) {
        BarcodeScannerModal(
            products = products,
            onDismiss = { showBarcodeScannerSim = false },
            onBarcodeFound = { barcode ->
                searchQuery = barcode
                showBarcodeScannerSim = false
            }
        )
    }
}

@Composable
private fun ProductItemCard(
    product: ProductEntity,
    currency: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAdjustStock: () -> Unit,
    onAddToCart: () -> Unit
) {
    val isLowStock = product.stockQuantity <= product.minStockLevel
    val margin = if (product.purchasePrice > 0) {
        ((product.sellingPrice - product.purchasePrice) / product.purchasePrice) * 100
    } else 0.0

    BizCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Slate200.copy(alpha = 0.4f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(product.category, style = MaterialTheme.typography.labelSmall, color = Slate600)
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "SKU: ${product.sku} • Barcode: ${product.barcode.ifBlank { "N/A" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
                if (product.supplier.isNotBlank()) {
                    Text("Supplier: ${product.supplier}", style = MaterialTheme.typography.labelSmall, color = Slate500)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$currency${String.format(Locale.US, "%,.2f", product.sellingPrice)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimary
                )
                Text(
                    text = "Cost: $currency${String.format(Locale.US, "%,.0f", product.purchasePrice)} (+${String.format(Locale.US, "%.0f", margin)}%)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate400
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isLowStock) DangerRose.copy(alpha = 0.15f) else SuccessEmerald.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isLowStock) "Low Stock: ${product.stockQuantity} Left" else "Stock: ${product.stockQuantity}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isLowStock) DangerRoseDark else SuccessEmeraldDark
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onAdjustStock, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Tune, contentDescription = "Adjust Stock", tint = Slate500, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Slate500, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onAddToCart, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = "Add to Cart", tint = BrandPrimary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun ProductFormDialog(
    initialProduct: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, sku: String, barcode: String, cat: String, cost: Double, price: Double, tax: Double, qty: Int, minStock: Int, supplier: String, desc: String) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var sku by remember { mutableStateOf(initialProduct?.sku ?: "SKU-${System.currentTimeMillis() % 100000}") }
    var barcode by remember { mutableStateOf(initialProduct?.barcode ?: "") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "General") }
    var costStr by remember { mutableStateOf(initialProduct?.purchasePrice?.toString() ?: "0.0") }
    var priceStr by remember { mutableStateOf(initialProduct?.sellingPrice?.toString() ?: "0.0") }
    var taxStr by remember { mutableStateOf(initialProduct?.taxRate?.toString() ?: "18.0") }
    var qtyStr by remember { mutableStateOf(initialProduct?.stockQuantity?.toString() ?: "10") }
    var minStockStr by remember { mutableStateOf(initialProduct?.minStockLevel?.toString() ?: "5") }
    var supplier by remember { mutableStateOf(initialProduct?.supplier ?: "") }
    var description by remember { mutableStateOf(initialProduct?.description ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialProduct == null) "Add New Product" else "Edit Product") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("SKU") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("Barcode") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = costStr,
                        onValueChange = { costStr = it },
                        label = { Text("Purchase Cost") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Selling Price *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = qtyStr,
                        onValueChange = { qtyStr = it },
                        label = { Text("Initial Stock") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = minStockStr,
                        onValueChange = { minStockStr = it },
                        label = { Text("Min Alert Level") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = supplier,
                    onValueChange = { supplier = it },
                    label = { Text("Supplier / Distributor") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(error ?: "", color = DangerRose, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        error = "Product name is required."
                        return@Button
                    }
                    val cost = costStr.toDoubleOrNull() ?: 0.0
                    val price = priceStr.toDoubleOrNull() ?: 0.0
                    val tax = taxStr.toDoubleOrNull() ?: 0.0
                    val qty = qtyStr.toIntOrNull() ?: 0
                    val minStock = minStockStr.toIntOrNull() ?: 5
                    onSave(name, sku, barcode, category, cost, price, tax, qty, minStock, supplier, description)
                }
            ) {
                Text("Save Product")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun StockAdjustDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onConfirm: (delta: Int, reason: String) -> Unit
) {
    var deltaStr by remember { mutableStateOf("10") }
    var isAddition by remember { mutableStateOf(true) }
    var reason by remember { mutableStateOf("Restock from supplier") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adjust Stock: ${product.name}") },
        text = {
            Column {
                Text("Current Stock: ${product.stockQuantity} items", style = MaterialTheme.typography.bodyMedium, color = Slate400)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = isAddition,
                        onClick = {
                            isAddition = true
                            reason = "Restock from supplier"
                        },
                        label = { Text("+ Add Stock (In)") }
                    )
                    FilterChip(
                        selected = !isAddition,
                        onClick = {
                            isAddition = false
                            reason = "Damaged / Audit loss"
                        },
                        label = { Text("- Deduct (Out)") }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = deltaStr,
                    onValueChange = { deltaStr = it },
                    label = { Text("Quantity") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for adjustment") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val count = deltaStr.toIntOrNull() ?: 0
                    val delta = if (isAddition) count else -count
                    onConfirm(delta, reason)
                }
            ) {
                Text("Apply Adjustment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun BarcodeScannerModal(
    products: List<ProductEntity>,
    onDismiss: () -> Unit,
    onBarcodeFound: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = BrandPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Laser Barcode Scanner")
            }
        },
        text = {
            Column {
                Text(
                    "Simulated Optical Barcode Scanner. Tap any sample barcode below to instantly scan into POS / Search:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
                Spacer(modifier = Modifier.height(12.dp))
                products.filter { it.barcode.isNotBlank() }.take(6).forEach { p ->
                    BizCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        onClick = { onBarcodeFound(p.barcode) }
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(p.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("||| ${p.barcode}", style = MaterialTheme.typography.labelMedium, color = BrandPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
