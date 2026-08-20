package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.local.entities.ExpenseEntity
import com.example.data.local.entities.PaymentEntity
import com.example.model.ExpenseCategory
import com.example.ui.components.BizCard
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MetricBarVisualizer
import com.example.ui.components.SearchAndFilterBar
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    currency: String,
    expenses: List<ExpenseEntity>,
    onAddExpense: (title: String, category: String, amount: Double, method: String, description: String) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf("ALL") + ExpenseCategory.values().map { it.name }
    val filteredExpenses = expenses.filter { exp ->
        val matchesQuery = exp.title.contains(searchQuery, ignoreCase = true) ||
                exp.description.contains(searchQuery, ignoreCase = true)
        val matchesCat = if (selectedCategory == "ALL") true else exp.category == selectedCategory
        matchesQuery && matchesCat
    }

    val totalExpenseAmount = expenses.sumOf { it.amount }
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = DangerRose,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_expense_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
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
                placeholder = "Search expenses by title or memo..."
            )

            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Total Expense Card
            BizCard(backgroundColor = DangerRose.copy(alpha = 0.08f), borderColor = DangerRose.copy(alpha = 0.3f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Recorded Expenses", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        Text(
                            "$currency${String.format(Locale.US, "%,.2f", totalExpenseAmount)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = DangerRoseDark
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(DangerRose.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Receipt, contentDescription = null, tint = DangerRose)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredExpenses.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.AccountBalanceWallet,
                    title = "No Expenses Logged",
                    description = if (searchQuery.isNotEmpty()) "No expense matched your filter." else "Log operating costs, bills, wages, and vendor payouts.",
                    actionButtonText = if (searchQuery.isEmpty()) "Add First Expense" else null,
                    onActionClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredExpenses, key = { it.id }) { expense ->
                        BizCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(expense.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Slate200.copy(alpha = 0.4f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(expense.category.replace("_", " "), style = MaterialTheme.typography.labelSmall, color = Slate600)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "${dateFormat.format(Date(expense.date))} • ${expense.paymentMethod}${if (expense.description.isNotBlank()) " • " + expense.description else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate400
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "$currency${String.format(Locale.US, "%,.2f", expense.amount)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = DangerRose
                                    )
                                    IconButton(onClick = { onDeleteExpense(expense) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Slate400, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("INVENTORY_PURCHASE") }
        var amountStr by remember { mutableStateOf("") }
        var method by remember { mutableStateOf("UPI") }
        var description by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Log New Expense") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Expense Title *") },
                        placeholder = { Text("e.g. Office Electricity Bill") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Amount ($currency) *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Category", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(ExpenseCategory.values()) { cat ->
                            FilterChip(
                                selected = category == cat.name,
                                onClick = { category = cat.name },
                                label = { Text(cat.label, fontSize = 11.sp) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = method,
                        onValueChange = { method = it },
                        label = { Text("Paid Via (UPI, Bank, Cash)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Notes / Vendor Info") },
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
                        val amt = amountStr.toDoubleOrNull()
                        if (title.isBlank() || amt == null || amt <= 0) {
                            error = "Please enter valid title and amount."
                            return@Button
                        }
                        onAddExpense(title, category, amt, method, description)
                        showAddDialog = false
                    }
                ) {
                    Text("Save Expense")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ---------------- PAYMENTS RECEIVED SCREEN ----------------

@Composable
fun PaymentsScreen(
    currency: String,
    payments: List<PaymentEntity>
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredPayments = payments.filter { p ->
        p.customerName.contains(searchQuery, ignoreCase = true) ||
                p.referenceNumber.contains(searchQuery, ignoreCase = true) ||
                p.paymentMethod.contains(searchQuery, ignoreCase = true)
    }

    val totalCollected = payments.sumOf { it.amount }
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        SearchAndFilterBar(
            searchQuery = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Search payments by customer or UTR reference..."
        )

        Spacer(modifier = Modifier.height(12.dp))

        BizCard(backgroundColor = SuccessEmerald.copy(alpha = 0.08f), borderColor = SuccessEmerald.copy(alpha = 0.3f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Collections Received", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    Text(
                        "$currency${String.format(Locale.US, "%,.2f", totalCollected)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = SuccessEmeraldDark
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SuccessEmerald.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = SuccessEmerald)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredPayments.isEmpty()) {
            EmptyStateView(
                icon = Icons.Outlined.Payment,
                title = "No Payment Receipts",
                description = "All settled customer receipts and POS transactions will appear here."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredPayments, key = { it.id }) { payment ->
                    BizCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(payment.customerName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    "${dateFormat.format(Date(payment.paymentDate))} • ${payment.paymentMethod}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400
                                )
                                if (payment.referenceNumber.isNotBlank()) {
                                    Text("Ref: ${payment.referenceNumber}", style = MaterialTheme.typography.labelSmall, color = Slate500)
                                }
                            }
                            Text(
                                "$currency${String.format(Locale.US, "%,.2f", payment.amount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SuccessEmerald
                            )
                        }
                    }
                }
            }
        }
    }
}
