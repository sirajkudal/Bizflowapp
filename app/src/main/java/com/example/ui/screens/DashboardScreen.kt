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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.*
import com.example.model.DateRangeFilter
import com.example.ui.components.BizCard
import com.example.ui.components.MetricBarVisualizer
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    business: BusinessEntity?,
    sales: List<SaleEntity>,
    expenses: List<ExpenseEntity>,
    customers: List<CustomerEntity>,
    products: List<ProductEntity>,
    invoices: List<InvoiceEntity>,
    selectedRange: DateRangeFilter,
    onSelectRange: (DateRangeFilter) -> Unit,
    onNavigateToPos: () -> Unit,
    onNavigateToInvoice: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToReports: () -> Unit
) {
    val currency = business?.currencySymbol ?: "₹"
    val now = System.currentTimeMillis()
    val cutoff = now - (selectedRange.days.toLong() * 24 * 60 * 60 * 1000)

    val rangeSales = sales.filter { it.timestamp >= cutoff }
    val rangeExpenses = expenses.filter { it.date >= cutoff }

    val totalSalesAmt = rangeSales.sumOf { it.grandTotal }
    val totalExpensesAmt = rangeExpenses.sumOf { it.amount }
    val netProfit = totalSalesAmt - totalExpensesAmt
    val profitMargin = if (totalSalesAmt > 0) (netProfit / totalSalesAmt) * 100 else 0.0

    val oneDayCutoff = now - (24 * 60 * 60 * 1000)
    val todaySalesAmt = sales.filter { it.timestamp >= oneDayCutoff }.sumOf { it.grandTotal }

    val outstandingTotal = invoices
        .filter { it.status in listOf("SENT", "PARTIALLY_PAID", "OVERDUE") }
        .sumOf { (it.grandTotal - it.paidAmount).coerceAtLeast(0.0) }

    val lowStockProducts = products.filter { it.stockQuantity <= it.minStockLevel }

    // Top Products
    val topProducts = products.sortedByDescending { it.sellingPrice * it.stockQuantity }.take(4)
    // Top Customers
    val topCustomers = customers.sortedByDescending { it.totalPurchases }.take(4)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date Range Selector Filter
        item {
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(DateRangeFilter.values()) { range ->
                    val isSelected = range == selectedRange
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectRange(range) },
                        label = { Text(range.label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Low Stock Alert Banner (if any)
        if (lowStockProducts.isNotEmpty()) {
            item {
                BizCard(
                    backgroundColor = DangerRose.copy(alpha = 0.08f),
                    borderColor = DangerRose.copy(alpha = 0.4f),
                    onClick = onNavigateToInventory
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(DangerRose.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRose, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${lowStockProducts.size} Items at or below Reorder Level!",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DangerRoseDark
                                )
                                Text(
                                    text = "Tap to view inventory movements & reorder.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate500
                                )
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = DangerRoseDark)
                    }
                }
            }
        }

        // Quick Action Shortcuts Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onNavigateToPos,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("dashboard_pos_btn")
                ) {
                    Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Sale (POS)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onNavigateToInvoice,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("dashboard_new_inv_btn")
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Invoice", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Key Financial KPI Stat Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "${selectedRange.label} Revenue",
                        value = "$currency${String.format(Locale.US, "%,.0f", totalSalesAmt)}",
                        subtitle = "${rangeSales.size} completed transactions",
                        icon = Icons.Outlined.TrendingUp,
                        iconColor = SuccessEmerald,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Net Profit",
                        value = "$currency${String.format(Locale.US, "%,.0f", netProfit)}",
                        subtitle = "${String.format(Locale.US, "%.1f", profitMargin)}% net margin",
                        icon = Icons.Outlined.AccountBalanceWallet,
                        iconColor = if (netProfit >= 0) SuccessEmerald else DangerRose,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Expenses",
                        value = "$currency${String.format(Locale.US, "%,.0f", totalExpensesAmt)}",
                        subtitle = "${rangeExpenses.size} recorded costs",
                        icon = Icons.Outlined.Receipt,
                        iconColor = DangerRose,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Outstanding Due",
                        value = "$currency${String.format(Locale.US, "%,.0f", outstandingTotal)}",
                        subtitle = "Pending customer balance",
                        icon = Icons.Outlined.HourglassEmpty,
                        iconColor = WarningAmber,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Catalog Items",
                        value = "${products.size}",
                        subtitle = "${lowStockProducts.size} low stock",
                        icon = Icons.Outlined.Inventory2,
                        iconColor = BrandPrimary,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToInventory() }
                    )
                    StatCard(
                        title = "Active Customers",
                        value = "${customers.size}",
                        subtitle = "CRM directory",
                        icon = Icons.Outlined.People,
                        iconColor = PurpleAccent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToCustomers() }
                    )
                }
            }
        }

        // Financial Cashflow Visualizer Card
        item {
            BizCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Cash Flow Breakdown (${selectedRange.label})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onNavigateToReports) {
                        Text("Full Reports", style = MaterialTheme.typography.labelMedium, color = BrandPrimary)
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                val maxBarVal = maxOf(totalSalesAmt, totalExpensesAmt, 1.0)
                MetricBarVisualizer(
                    title = "Gross Revenue",
                    amount = totalSalesAmt,
                    maxAmount = maxBarVal,
                    barColor = SuccessEmerald,
                    currencySymbol = currency
                )
                Spacer(modifier = Modifier.height(12.dp))
                MetricBarVisualizer(
                    title = "Operational Expenses",
                    amount = totalExpensesAmt,
                    maxAmount = maxBarVal,
                    barColor = DangerRose,
                    currencySymbol = currency
                )
            }
        }

        // Top Selling Products
        item {
            BizCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Top Catalog Products", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onNavigateToInventory) {
                        Text("View All", style = MaterialTheme.typography.labelMedium, color = BrandPrimary)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (topProducts.isEmpty()) {
                    Text("No products added yet.", style = MaterialTheme.typography.bodySmall, color = Slate400)
                } else {
                    topProducts.forEach { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("SKU: ${p.sku} • Stock: ${p.stockQuantity}", style = MaterialTheme.typography.bodySmall, color = Slate400)
                            }
                            Text(
                                "$currency${String.format(Locale.US, "%,.2f", p.sellingPrice)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrandPrimary
                            )
                        }
                    }
                }
            }
        }

        // Top Customers
        item {
            BizCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Top Valued Clients", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onNavigateToCustomers) {
                        Text("CRM List", style = MaterialTheme.typography.labelMedium, color = BrandPrimary)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (topCustomers.isEmpty()) {
                    Text("No customers registered yet.", style = MaterialTheme.typography.bodySmall, color = Slate400)
                } else {
                    topCustomers.forEach { c ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(c.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(c.phone, style = MaterialTheme.typography.bodySmall, color = Slate400)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "$currency${String.format(Locale.US, "%,.0f", c.totalPurchases)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessEmerald
                                )
                                if (c.outstandingBalance > 0) {
                                    Text(
                                        "Due: $currency${String.format(Locale.US, "%,.0f", c.outstandingBalance)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DangerRose
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
