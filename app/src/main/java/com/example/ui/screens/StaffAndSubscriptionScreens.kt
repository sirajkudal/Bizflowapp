package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.StaffEntity
import com.example.data.local.entities.SubscriptionEntity
import com.example.data.local.entities.SubscriptionPlanEntity
import com.example.model.StaffRole
import com.example.service.PaymentGatewayService
import com.example.ui.components.BizCard
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffScreen(
    staffList: List<StaffEntity>,
    onAddStaff: (name: String, email: String, phone: String, role: String) -> Unit,
    onUpdateRole: (StaffEntity, String) -> Unit,
    onRemoveStaff: (StaffEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BrandPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_staff_fab")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Staff")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Text("Staff & Team Access Control", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Manage roles, permissions, and cashier accounts for your business.", style = MaterialTheme.typography.bodySmall, color = Slate400)

            Spacer(modifier = Modifier.height(14.dp))

            // Role Legend
            BizCard(backgroundColor = MaterialTheme.colorScheme.surfaceVariant) {
                Text("Role Capabilities Overview", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                StaffRole.values().forEach { role ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text("• ${role.title}: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        Text(role.description, style = MaterialTheme.typography.labelSmall, color = Slate500)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (staffList.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.Badge,
                    title = "No Staff Members",
                    description = "Add team members to assign store access and track POS sales by cashier.",
                    actionButtonText = "Invite Staff Member",
                    onActionClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(staffList, key = { it.id }) { staff ->
                        var showRoleMenu by remember { mutableStateOf(false) }
                        BizCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(BrandPrimary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(staff.fullName.take(1).uppercase(), fontWeight = FontWeight.Bold, color = BrandPrimary)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(staff.fullName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(staff.email.ifBlank { staff.phone }, style = MaterialTheme.typography.bodySmall, color = Slate400)
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box {
                                        FilterChip(
                                            selected = true,
                                            onClick = { showRoleMenu = true },
                                            label = { Text(staff.role) },
                                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
                                        )
                                        DropdownMenu(
                                            expanded = showRoleMenu,
                                            onDismissRequest = { showRoleMenu = false }
                                        ) {
                                            StaffRole.values().forEach { role ->
                                                DropdownMenuItem(
                                                    text = { Text(role.title) },
                                                    onClick = {
                                                        onUpdateRole(staff, role.name)
                                                        showRoleMenu = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    IconButton(onClick = { onRemoveStaff(staff) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = Slate400, modifier = Modifier.size(18.dp))
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
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var selectedRole by remember { mutableStateOf("STAFF") }
        var error by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Invite Staff Member") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name *") },
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
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Select Role", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(StaffRole.values()) { role ->
                            FilterChip(
                                selected = selectedRole == role.name,
                                onClick = { selectedRole = role.name },
                                label = { Text(role.title, fontSize = 11.sp) }
                            )
                        }
                    }
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
                        onAddStaff(name, email, phone, selectedRole)
                        showAddDialog = false
                    }
                ) {
                    Text("Add Staff")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ---------------- SUBSCRIPTION & BILLING SCREEN ----------------

@Composable
fun SubscriptionScreen(
    currentSubscription: SubscriptionEntity?,
    plans: List<SubscriptionPlanEntity>,
    onUpgradePlan: (planId: String, amount: Double) -> Unit
) {
    var selectedPlanForCheckout by remember { mutableStateOf<SubscriptionPlanEntity?>(null) }
    var selectedGateway by remember { mutableStateOf(PaymentGatewayService.GatewayProvider.RAZORPAY) }
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("SaaS Plan & Subscriptions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        // Current Active Plan Banner
        BizCard(backgroundColor = BrandPrimary.copy(alpha = 0.08f), borderColor = BrandPrimary.copy(alpha = 0.4f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Current Active Plan", style = MaterialTheme.typography.labelSmall, color = Slate400)
                    Text(
                        currentSubscription?.planId ?: "FREE PLAN",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = BrandPrimary
                    )
                    if (currentSubscription != null) {
                        Text(
                            "Next Billing: ${dateFormat.format(Date(currentSubscription.renewalDate))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                }
                StatusBadge(status = currentSubscription?.status ?: "ACTIVE")
            }
        }

        Text("Available Subscription Tiers", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

        plans.forEach { plan ->
            val isCurrent = currentSubscription?.planId == plan.id
            BizCard(
                borderColor = if (isCurrent) SuccessEmerald else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(plan.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "₹${String.format(Locale.US, "%.0f", plan.monthlyPrice)} / month",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimary
                        )
                    }
                    if (isCurrent) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SuccessEmerald.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("CURRENT PLAN", color = SuccessEmeraldDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                plan.featuresJson.split(",").forEach { feat ->
                    Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = SuccessEmerald, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(feat.trim(), style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!isCurrent) {
                    Button(
                        onClick = { selectedPlanForCheckout = plan },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Upgrade to ${plan.name}")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    if (selectedPlanForCheckout != null) {
        val plan = selectedPlanForCheckout!!
        var processing by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!processing) selectedPlanForCheckout = null },
            title = { Text("Checkout: ${plan.name} Plan") },
            text = {
                Column {
                    Text("Monthly Subscription Fee: ₹${String.format(Locale.US, "%.0f", plan.monthlyPrice)}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Select Payment Gateway Provider", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))

                    PaymentGatewayService.GatewayProvider.values().forEach { gw ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedGateway == gw,
                                onClick = { selectedGateway = gw }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(gw.displayName, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Includes recurring 30-day auto-renewal. Cancel anytime from settings.", style = MaterialTheme.typography.labelSmall, color = Slate400)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        processing = true
                        val req = PaymentGatewayService.PaymentRequest(
                            orderId = "ORD_${System.currentTimeMillis()}",
                            amount = plan.monthlyPrice,
                            currency = "INR",
                            customerName = "Store Owner",
                            customerEmail = "owner@store.com",
                            description = "Subscription to ${plan.name} Plan"
                        )
                        val res = PaymentGatewayService.processPayment(req, selectedGateway.displayName)
                        if (res.isSuccessful) {
                            onUpgradePlan(plan.id, plan.monthlyPrice)
                            selectedPlanForCheckout = null
                        }
                    }
                ) {
                    Text("Authorize ₹${String.format(Locale.US, "%.0f", plan.monthlyPrice)}")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPlanForCheckout = null }) { Text("Cancel") }
            }
        )
    }
}
