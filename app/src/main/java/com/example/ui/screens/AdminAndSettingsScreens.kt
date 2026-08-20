package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.*
import com.example.ui.components.BizCard
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---------------- ADMIN PANEL SCREEN ----------------

@Composable
fun AdminPanelScreen(
    businesses: List<BusinessEntity>,
    subscriptions: List<SubscriptionEntity>,
    plans: List<SubscriptionPlanEntity>,
    tickets: List<SupportTicketEntity>,
    onUpdatePlan: (SubscriptionPlanEntity) -> Unit,
    onReplyTicket: (SupportTicketEntity, status: String, reply: String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: SaaS Metrics, 1: Plans Editor, 2: Support Tickets
    var editingPlan by remember { mutableStateOf<SubscriptionPlanEntity?>(null) }
    var replyingTicket by remember { mutableStateOf<SupportTicketEntity?>(null) }

    val totalMrr = subscriptions.filter { it.status == "ACTIVE" }.sumOf { it.lastBilledAmount }
    val activeSubCount = subscriptions.count { it.status == "ACTIVE" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text("SaaS Super Admin Control Center", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Manage platform MRR, live tenant businesses, dynamic pricing, and support tickets.", style = MaterialTheme.typography.bodySmall, color = Slate400)

        Spacer(modifier = Modifier.height(14.dp))

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Metrics") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Pricing Plans") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Tickets (${tickets.count { it.status == "OPEN" }})") })
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatCard(
                                title = "Estimated MRR",
                                value = "₹${String.format(Locale.US, "%,.0f", totalMrr)}",
                                subtitle = "$activeSubCount active paid subscriptions",
                                icon = Icons.Outlined.MonetizationOn,
                                iconColor = SuccessEmerald,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Total SaaS Tenants",
                                value = "${businesses.size}",
                                subtitle = "Registered store databases",
                                icon = Icons.Outlined.Store,
                                iconColor = BrandPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Text("Active Business Tenants", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    items(businesses, key = { it.id }) { biz ->
                        BizCard {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(biz.logoEmoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(biz.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text("${biz.ownerName} • ${biz.email}", style = MaterialTheme.typography.bodySmall, color = Slate400)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(BrandPrimary.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(if (biz.isDemo) "DEMO STORE" else "LIVE STORE", style = MaterialTheme.typography.labelSmall, color = BrandPrimaryDark, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Plans & Pricing Editor
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text("Dynamic Plan Configuration (No Code Change Needed)", style = MaterialTheme.typography.bodySmall, color = Slate400)
                    }
                    items(plans, key = { it.id }) { plan ->
                        BizCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(plan.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("₹${String.format(Locale.US, "%.0f", plan.monthlyPrice)} / mo • Prods Limit: ${plan.productLimit} • Cust Limit: ${plan.customerLimit}", style = MaterialTheme.typography.bodySmall, color = Slate400)
                                }
                                Button(
                                    onClick = { editingPlan = plan },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Edit Pricing", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Support Tickets
                if (tickets.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Outlined.SupportAgent,
                        title = "No Support Tickets",
                        description = "Customer questions and support tickets will appear here."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(tickets, key = { it.id }) { tkt ->
                            BizCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(tkt.subject, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            StatusBadge(status = tkt.status)
                                        }
                                        Text("${tkt.userEmail} • Category: ${tkt.category}", style = MaterialTheme.typography.bodySmall, color = Slate400)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(tkt.description, style = MaterialTheme.typography.bodyMedium)
                                        if (tkt.adminReply.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("Admin Reply: ${tkt.adminReply}", style = MaterialTheme.typography.bodySmall, color = SuccessEmeraldDark)
                                        }
                                    }
                                    Button(
                                        onClick = { replyingTicket = tkt },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Respond", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingPlan != null) {
        val plan = editingPlan!!
        var priceStr by remember { mutableStateOf("${plan.monthlyPrice}") }
        var maxProdStr by remember { mutableStateOf("${plan.productLimit}") }
        var maxCustStr by remember { mutableStateOf("${plan.customerLimit}") }

        AlertDialog(
            onDismissRequest = { editingPlan = null },
            title = { Text("Edit Plan: ${plan.name}") },
            text = {
                Column {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Monthly Price (INR)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = maxProdStr,
                        onValueChange = { maxProdStr = it },
                        label = { Text("Max Product SKUs Limit") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = maxCustStr,
                        onValueChange = { maxCustStr = it },
                        label = { Text("Max Customers Limit") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = priceStr.toDoubleOrNull() ?: plan.monthlyPrice
                        val prods = maxProdStr.toIntOrNull() ?: plan.productLimit
                        val custs = maxCustStr.toIntOrNull() ?: plan.customerLimit
                        onUpdatePlan(
                            plan.copy(
                                monthlyPrice = p,
                                productLimit = prods,
                                customerLimit = custs
                            )
                        )
                        editingPlan = null
                    }
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPlan = null }) { Text("Cancel") }
            }
        )
    }

    if (replyingTicket != null) {
        val tkt = replyingTicket!!
        var replyText by remember { mutableStateOf(tkt.adminReply) }
        var status by remember { mutableStateOf("RESOLVED") }

        AlertDialog(
            onDismissRequest = { replyingTicket = null },
            title = { Text("Reply to Ticket: ${tkt.subject}") },
            text = {
                Column {
                    Text("Inquiry from: ${tkt.userEmail}", style = MaterialTheme.typography.bodySmall, color = Slate400)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        label = { Text("Official Admin Response") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = status == "RESOLVED", onClick = { status = "RESOLVED" }, label = { Text("Mark Resolved") })
                        FilterChip(selected = status == "IN_PROGRESS", onClick = { status = "IN_PROGRESS" }, label = { Text("In Progress") })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReplyTicket(tkt, status, replyText)
                        replyingTicket = null
                    }
                ) {
                    Text("Send & Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { replyingTicket = null }) { Text("Cancel") }
            }
        )
    }
}

// ---------------- NOTIFICATIONS SCREEN ----------------

@Composable
fun NotificationsScreen(
    notifications: List<NotificationEntity>,
    onMarkRead: (String) -> Unit,
    onMarkAllRead: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US)

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
            Text("Notification Center", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (notifications.any { !it.isRead }) {
                TextButton(onClick = onMarkAllRead) {
                    Text("Mark All as Read", color = BrandPrimary, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (notifications.isEmpty()) {
            EmptyStateView(
                icon = Icons.Outlined.Notifications,
                title = "No Notifications",
                description = "Low stock alerts, invoice payment notifications, and updates will be shown here."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications, key = { it.id }) { notif ->
                    val (icon, tint) = when (notif.type) {
                        "LOW_STOCK" -> Icons.Default.Warning to DangerRose
                        "PAYMENT_RECEIVED" -> Icons.Default.CheckCircle to SuccessEmerald
                        "SUBSCRIPTION" -> Icons.Default.Stars to BrandPrimary
                        else -> Icons.Default.Info to Slate500
                    }

                    BizCard(
                        backgroundColor = if (notif.isRead) MaterialTheme.colorScheme.surface else BrandPrimary.copy(alpha = 0.05f),
                        onClick = { if (!notif.isRead) onMarkRead(notif.id) }
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(tint.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(notif.title, style = MaterialTheme.typography.titleSmall, fontWeight = if (!notif.isRead) FontWeight.Bold else FontWeight.SemiBold)
                                    if (!notif.isRead) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(BrandPrimary))
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(notif.message, style = MaterialTheme.typography.bodySmall, color = Slate500)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(dateFormat.format(Date(notif.timestamp)), style = MaterialTheme.typography.labelSmall, color = Slate400)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- SUPPORT SCREEN ----------------

@Composable
fun SupportScreen(
    tickets: List<SupportTicketEntity>,
    onCreateTicket: (subject: String, category: String, priority: String, description: String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

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
            Text("Help & Support Desk", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Ticket", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (tickets.isEmpty()) {
            EmptyStateView(
                icon = Icons.Outlined.SupportAgent,
                title = "Need Help with BizFlow?",
                description = "Our engineering support team is available 24/7. Submit a support ticket anytime.",
                actionButtonText = "Submit Support Ticket",
                onActionClick = { showCreateDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(tickets, key = { it.id }) { tkt ->
                    BizCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(tkt.subject, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusBadge(status = tkt.status)
                                }
                                Text("Priority: ${tkt.priority} • Category: ${tkt.category}", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(tkt.description, style = MaterialTheme.typography.bodySmall)

                                if (tkt.adminReply.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SuccessEmerald.copy(alpha = 0.1f))
                                            .padding(8.dp)
                                    ) {
                                        Text("Support Agent Reply: ${tkt.adminReply}", style = MaterialTheme.typography.bodySmall, color = SuccessEmeraldDark)
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
        var subject by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Billing") }
        var priority by remember { mutableStateOf("MEDIUM") }
        var description by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Submit Support Ticket") },
            text = {
                Column {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category (Billing, Invoicing, Inventory, POS)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description of Issue *") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (subject.isNotBlank() && description.isNotBlank()) {
                            onCreateTicket(subject, category, priority, description)
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Submit Ticket")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ---------------- SETTINGS SCREEN ----------------

@Composable
fun SettingsScreen(
    currentBusiness: BusinessEntity?,
    allBusinesses: List<BusinessEntity>,
    onSelectBusiness: (String) -> Unit,
    onUpdateBusiness: (BusinessEntity) -> Unit,
    onResetDemoData: () -> Unit,
    onViewLegal: (String) -> Unit,
    onNavigateToLanding: () -> Unit
) {
    var businessName by remember(currentBusiness) { mutableStateOf(currentBusiness?.name ?: "") }
    var ownerName by remember(currentBusiness) { mutableStateOf(currentBusiness?.ownerName ?: "") }
    var phone by remember(currentBusiness) { mutableStateOf(currentBusiness?.phone ?: "") }
    var email by remember(currentBusiness) { mutableStateOf(currentBusiness?.email ?: "") }
    var address by remember(currentBusiness) { mutableStateOf(currentBusiness?.address ?: "") }
    var currencySymbol by remember(currentBusiness) { mutableStateOf(currentBusiness?.currencySymbol ?: "₹") }
    var invoicePrefix by remember(currentBusiness) { mutableStateOf(currentBusiness?.invoicePrefix ?: "INV-") }
    var showSwitchBizModal by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Business Settings & Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        // Store Switcher Card
        BizCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(currentBusiness?.logoEmoji ?: "🏢", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(currentBusiness?.name ?: "Store", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Active Storefront Profile", style = MaterialTheme.typography.bodySmall, color = Slate400)
                    }
                }
                OutlinedButton(onClick = { showSwitchBizModal = true }) {
                    Text("Switch Store", fontSize = 12.sp)
                }
            }
        }

        // Store Profile Form
        BizCard {
            Text("Store Profile & Invoicing Info", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text("Business Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = ownerName,
                onValueChange = { ownerName = it },
                label = { Text("Owner / Manager Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = currencySymbol,
                    onValueChange = { currencySymbol = it },
                    label = { Text("Currency (₹, $, €)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = invoicePrefix,
                    onValueChange = { invoicePrefix = it },
                    label = { Text("Invoice Prefix") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    if (currentBusiness != null) {
                        onUpdateBusiness(
                            currentBusiness.copy(
                                name = businessName,
                                ownerName = ownerName,
                                phone = phone,
                                email = email,
                                address = address,
                                currencySymbol = currencySymbol,
                                invoicePrefix = invoicePrefix
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
            ) {
                Text("Save Store Settings")
            }
        }

        // Legal & Compliance Card
        BizCard {
            Text("Legal & Compliance Policies", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { onViewLegal("terms") }) { Text("Terms of Service") }
                TextButton(onClick = { onViewLegal("privacy") }) { Text("Privacy Policy") }
                TextButton(onClick = { onViewLegal("refund") }) { Text("Refund Policy") }
            }
        }

        // Demo Actions Card
        BizCard(backgroundColor = DangerRose.copy(alpha = 0.05f)) {
            Text("Developer & Demo Actions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Reload sample data (Products, Invoices, CRM records) to test all features.", style = MaterialTheme.typography.bodySmall, color = Slate400)
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = { showResetConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRose),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reload Sample Demo Data")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onNavigateToLanding,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Public SaaS Landing Page")
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    if (showSwitchBizModal) {
        AlertDialog(
            onDismissRequest = { showSwitchBizModal = false },
            title = { Text("Switch Business Account") },
            text = {
                Column {
                    allBusinesses.forEach { biz ->
                        BizCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            onClick = {
                                onSelectBusiness(biz.id)
                                showSwitchBizModal = false
                            }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(biz.logoEmoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(biz.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(biz.ownerName, style = MaterialTheme.typography.bodySmall, color = Slate400)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSwitchBizModal = false }) { Text("Close") }
            }
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reload Demo Database?") },
            text = { Text("This will replenish sample products, inventory logs, customers, and transactions.") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetDemoData()
                        showResetConfirm = false
                    }
                ) {
                    Text("Confirm Reload")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

// ---------------- LEGAL PAGES VIEWER MODAL ----------------

@Composable
fun LegalDocViewerModal(
    docType: String,
    onDismiss: () -> Unit
) {
    val title = when (docType) {
        "terms" -> "Terms of Service"
        "privacy" -> "Privacy Policy"
        "refund" -> "Refund & Cancellation Policy"
        else -> "Terms & Policies"
    }

    val content = when (docType) {
        "terms" -> """
            BizFlow SaaS Terms of Service
            Last Updated: January 2026

            1. Acceptance of Terms
            By registering for and using BizFlow, you agree to comply with these terms. BizFlow provides business accounting, inventory tracking, invoicing, and POS software for small and medium enterprises.

            2. User Accounts & Security
            You are responsible for safeguarding your login credentials and data stored under your tenant database.

            3. Data Ownership
            All customer records, sales transactions, product catalogs, and invoice histories belong exclusively to your business. We do not sell or monetize tenant data.

            4. Service Availability
            BizFlow guarantees a 99.9% uptime SLA for cloud operations.
        """.trimIndent()
        "privacy" -> """
            BizFlow Privacy Policy
            Last Updated: January 2026

            1. Information We Collect
            We collect store profile info, email addresses, and transaction metadata necessary to execute billing, notifications, and analytics.

            2. Security & Encryption
            All communications are encrypted in transit with TLS 1.3 and at rest with AES-256 standards.

            3. Third-Party Disclosures
            Payment transactions are processed securely via verified gateways (Razorpay, Stripe) and never stored directly on our servers.
        """.trimIndent()
        "refund" -> """
            BizFlow Refund & Cancellation Policy
            Last Updated: January 2026

            1. 14-Day Money Back Guarantee
            If you are not 100% satisfied with any paid BizFlow subscription tier, you may request a full refund within 14 days of upgrade.

            2. Cancellation
            You can downgrade or cancel auto-renewal anytime from the Subscription settings page.
        """.trimIndent()
        else -> "BizFlow is committed to ethical, transparent business software operations."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(content, style = MaterialTheme.typography.bodySmall, lineHeight = 18.sp)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )
}
