package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.*
import com.example.service.ReportExportService
import com.example.ui.components.BizCard
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MetricBarVisualizer
import com.example.ui.components.StatCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InventoryScreen(
    currency: String,
    products: List<ProductEntity>,
    movements: List<InventoryMovementEntity>,
    onAdjustStock: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Overview, 1: Movement Ledger
    val totalUnits = products.sumOf { it.stockQuantity }
    val totalCostValue = products.sumOf { it.stockQuantity * it.purchasePrice }
    val totalRetailValue = products.sumOf { it.stockQuantity * it.sellingPrice }
    val lowStockCount = products.count { it.stockQuantity <= it.minStockLevel }

    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Valuation & Alerts") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Stock Movements (${movements.size})") })
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatCard(
                            title = "Total Catalog SKUs",
                            value = "${products.size}",
                            subtitle = "$totalUnits physical items",
                            icon = Icons.Outlined.Inventory2,
                            iconColor = BrandPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Low Stock Warnings",
                            value = "$lowStockCount",
                            subtitle = "Items below min level",
                            icon = Icons.Outlined.Warning,
                            iconColor = if (lowStockCount > 0) DangerRose else SuccessEmerald,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    BizCard {
                        Text("Inventory Asset Valuation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Purchase Cost Valuation", style = MaterialTheme.typography.bodyMedium, color = Slate400)
                            Text("$currency${String.format(Locale.US, "%,.2f", totalCostValue)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Potential Retail Selling Value", style = MaterialTheme.typography.bodyMedium, color = Slate400)
                            Text("$currency${String.format(Locale.US, "%,.2f", totalRetailValue)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SuccessEmerald)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Estimated Gross Margin", style = MaterialTheme.typography.bodyMedium, color = Slate400)
                            val potProfit = totalRetailValue - totalCostValue
                            Text("$currency${String.format(Locale.US, "%,.2f", potProfit)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BrandPrimary)
                        }
                    }
                }

                item {
                    Text("Low Stock Reorder List", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                val lowStockList = products.filter { it.stockQuantity <= it.minStockLevel }
                if (lowStockList.isEmpty()) {
                    item {
                        BizCard(backgroundColor = SuccessEmerald.copy(alpha = 0.08f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessEmerald)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("All catalog items are adequately stocked above minimum levels.", color = SuccessEmeraldDark, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                } else {
                    items(lowStockList, key = { it.id }) { p ->
                        BizCard(borderColor = DangerRose.copy(alpha = 0.4f)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(p.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("SKU: ${p.sku} • Supplier: ${p.supplier.ifBlank { "N/A" }}", style = MaterialTheme.typography.bodySmall, color = Slate400)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(DangerRose.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("${p.stockQuantity} Left (Min: ${p.minStockLevel})", color = DangerRoseDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Movement Ledger
            if (movements.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.History,
                    title = "No Inventory Movements",
                    description = "Sales, stock adjustments, and supplier restocks will be audited here automatically."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(movements, key = { it.id }) { mov ->
                        val isIn = mov.movementType == "IN" || mov.quantityChange > 0
                        BizCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isIn) SuccessEmerald.copy(alpha = 0.15f) else DangerRose.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isIn) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                            contentDescription = null,
                                            tint = if (isIn) SuccessEmerald else DangerRose,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(mov.productName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(
                                            "${dateFormat.format(Date(mov.timestamp))} • ${mov.reason}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate400
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${if (mov.quantityChange >= 0) "+" else ""}${mov.quantityChange}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isIn) SuccessEmerald else DangerRose
                                    )
                                    Text(
                                        text = "Bal: ${mov.balanceAfter}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate400
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

// ---------------- FINANCIAL REPORTS & ANALYTICS ----------------

@Composable
fun ReportsScreen(
    currency: String,
    sales: List<SaleEntity>,
    expenses: List<ExpenseEntity>,
    products: List<ProductEntity>,
    customers: List<CustomerEntity>
) {
    var selectedReportType by remember { mutableStateOf(0) } // 0: P&L, 1: Sales, 2: Expenses, 3: Inventory
    var exportSuccessMessage by remember { mutableStateOf<String?>(null) }

    val reportTypes = listOf("Profit & Loss", "Sales Audit", "Expense Analysis", "Stock Valuation")

    val totalSales = sales.sumOf { it.grandTotal }
    val totalExpenses = expenses.sumOf { it.amount }
    val netProfit = totalSales - totalExpenses
    val margin = if (totalSales > 0) (netProfit / totalSales) * 100 else 0.0

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
            Text("Business Intelligence Reports", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Button(
                onClick = {
                    val csv = if (selectedReportType == 3) {
                        ReportExportService.generateInventoryCsv(products, currency)
                    } else {
                        ReportExportService.generateSalesCsv(sales, currency)
                    }
                    exportSuccessMessage = "Report exported to CSV (${csv.lines().size} rows). Ready to share or open in Excel."
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export CSV", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (exportSuccessMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SuccessEmerald.copy(alpha = 0.15f))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessEmerald, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(exportSuccessMessage ?: "", color = SuccessEmeraldDark, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    IconButton(onClick = { exportSuccessMessage = null }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(reportTypes.indices.toList()) { idx ->
                FilterChip(
                    selected = selectedReportType == idx,
                    onClick = { selectedReportType = idx },
                    label = { Text(reportTypes[idx]) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (selectedReportType) {
                0 -> {
                    // Profit & Loss Report
                    BizCard(backgroundColor = if (netProfit >= 0) SuccessEmerald.copy(alpha = 0.08f) else DangerRose.copy(alpha = 0.08f)) {
                        Text("Executive P&L Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Gross Sales Revenue", style = MaterialTheme.typography.bodyMedium)
                            Text("$currency${String.format(Locale.US, "%,.2f", totalSales)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SuccessEmerald)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Operating Expenses", style = MaterialTheme.typography.bodyMedium)
                            Text("- $currency${String.format(Locale.US, "%,.2f", totalExpenses)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DangerRose)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Net Operating Income", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "$currency${String.format(Locale.US, "%,.2f", netProfit)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (netProfit >= 0) SuccessEmeraldDark else DangerRoseDark
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Net Profit Margin: ${String.format(Locale.US, "%.1f", margin)}%", style = MaterialTheme.typography.labelMedium, color = Slate500)
                    }

                    BizCard {
                        Text("Visual Margin Breakdown", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        MetricBarVisualizer("Revenue", totalSales, maxOf(totalSales, totalExpenses, 1.0), SuccessEmerald, currency)
                        Spacer(modifier = Modifier.height(10.dp))
                        MetricBarVisualizer("Expenses", totalExpenses, maxOf(totalSales, totalExpenses, 1.0), DangerRose, currency)
                    }
                }
                1 -> {
                    // Sales Audit
                    BizCard {
                        Text("Sales Transactions (${sales.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        val paymentMethodGroups = sales.groupBy { it.paymentMethod }
                        paymentMethodGroups.forEach { (method, sList) ->
                            val methodTotal = sList.sumOf { it.grandTotal }
                            MetricBarVisualizer(
                                title = "$method (${sList.size} sales)",
                                amount = methodTotal,
                                maxAmount = totalSales,
                                barColor = BrandPrimary,
                                currencySymbol = currency
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
                2 -> {
                    // Expense Breakdown
                    BizCard {
                        Text("Expenses by Category (${expenses.size} entries)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        val expGroups = expenses.groupBy { it.category }
                        expGroups.forEach { (cat, expList) ->
                            val catTotal = expList.sumOf { it.amount }
                            MetricBarVisualizer(
                                title = "${cat.replace("_", " ")} (${expList.size})",
                                amount = catTotal,
                                maxAmount = totalExpenses,
                                barColor = DangerRose,
                                currencySymbol = currency
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
                3 -> {
                    // Stock Valuation
                    BizCard {
                        Text("Inventory Categories Valuation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        val prodCatGroups = products.groupBy { it.category }
                        val totalStockVal = products.sumOf { it.stockQuantity * it.purchasePrice }
                        prodCatGroups.forEach { (cat, pList) ->
                            val catVal = pList.sumOf { it.stockQuantity * it.purchasePrice }
                            MetricBarVisualizer(
                                title = "$cat (${pList.size} SKUs)",
                                amount = catVal,
                                maxAmount = totalStockVal,
                                barColor = PurpleAccent,
                                currencySymbol = currency
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
