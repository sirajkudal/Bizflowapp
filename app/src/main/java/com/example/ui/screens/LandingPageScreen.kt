package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.SubscriptionPlanEntity
import com.example.ui.components.BizCard
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun LandingPageScreen(
    plans: List<SubscriptionPlanEntity>,
    onStartFree: () -> Unit,
    onViewDemo: () -> Unit,
    onSignIn: () -> Unit,
    onViewLegal: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Navigation Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(listOf(BrandPrimary, BrandSecondary))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚡", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "BizFlow",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Simple SaaS for SMBs",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate400
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onSignIn,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("landing_signin_button"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Sign In", fontSize = 12.sp)
                    }
                }
            }
        }

        // 2. Hero Section
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(BrandPrimary.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🚀 All-in-One Business OS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Run your business from one simple dashboard.",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 34.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Simple business management for retail shops, wholesalers, freelancers, and service agencies. Track inventory, send GST/Tax invoices, record sales, and monitor net profit in real-time.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate400,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = onStartFree,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("hero_start_free_btn")
                        ) {
                            Text("Start Free", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        FilledTonalButton(
                            onClick = onViewDemo,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("hero_view_demo_btn")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View Demo", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // 3. Live Preview Card
        item {
            BizCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(SuccessEmerald)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "LIVE EXECUTIVE PREVIEW",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SuccessEmerald
                        )
                    }
                    Text("Apex Trading Co.", style = MaterialTheme.typography.labelMedium, color = Slate400)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("30-Day Sales", style = MaterialTheme.typography.bodySmall, color = Slate400)
                        Text("₹3,42,850", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SuccessEmerald)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Net Margin", style = MaterialTheme.typography.bodySmall, color = Slate400)
                        Text("+64.2%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = BrandPrimary)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { 0.78f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = BrandPrimary,
                    trackColor = Slate200.copy(alpha = 0.3f)
                )
            }
        }

        // 4. Core Features Grid
        item {
            Spacer(modifier = Modifier.height(36.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Everything your business needs",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Engineered for speed, reliability and clean record keeping.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
                Spacer(modifier = Modifier.height(16.dp))

                val features = listOf(
                    FeatureItem(Icons.Outlined.People, "Customer CRM", "Maintain balances, purchase histories, GSTINs, and direct WhatsApp contact."),
                    FeatureItem(Icons.Outlined.Inventory2, "Inventory & Barcodes", "Automatic low-stock alerts, SKU management, stock in/out audits, and CSV imports."),
                    FeatureItem(Icons.Outlined.ReceiptLong, "Invoicing & Billing", "Generate professional PDF/thermal invoices with tax, discounts, and custom terms."),
                    FeatureItem(Icons.Outlined.PointOfSale, "Rapid POS Sales", "Barcode-ready quick checkout with cash, UPI, card, and split payment tracking."),
                    FeatureItem(Icons.Outlined.AccountBalanceWallet, "Expense Tracking", "Categorized operating costs, rent, electricity, vendor payouts, and receipt audits."),
                    FeatureItem(Icons.Outlined.Assessment, "Financial Reports", "Sales over time, profit & loss statement, inventory valuation, and GST exports."),
                    FeatureItem(Icons.Outlined.Badge, "Staff & Roles", "Role-based accounts for Owners, Managers, Cashiers, and Accountants."),
                    FeatureItem(Icons.Outlined.Store, "Multi-Branch & Multi-Biz", "Manage multiple independent branches and stores under a single subscription.")
                )

                features.forEach { feat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BrandPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(feat.icon, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(feat.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(feat.description, style = MaterialTheme.typography.bodySmall, color = Slate400)
                        }
                    }
                }
            }
        }

        // 5. How It Works
        item {
            Spacer(modifier = Modifier.height(28.dp))
            BizCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                backgroundColor = Slate900
            ) {
                Text(
                    text = "How It Works",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(14.dp))
                StepRow("1", "Quick Setup", "Add your business name, currency, tax rates & invoice prefix in 60 seconds.")
                Spacer(modifier = Modifier.height(10.dp))
                StepRow("2", "Add Products & Customers", "Input items manually or import via CSV catalog. Set reorder levels.")
                Spacer(modifier = Modifier.height(10.dp))
                StepRow("3", "Transact & Grow", "Process sales, send instant invoices, track payment dues and watch net profits grow.")
            }
        }

        // 6. Pricing Plans (Dynamic from DB)
        item {
            Spacer(modifier = Modifier.height(36.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Transparent SaaS Pricing",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Start free and scale effortlessly as your sales volume grows.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
                Spacer(modifier = Modifier.height(16.dp))

                plans.forEach { plan ->
                    val isPopular = plan.id == "PROFESSIONAL"
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPopular) BrandPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (isPopular) BrandPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(plan.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                if (isPopular) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(BrandPrimary)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("MOST POPULAR", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    "₹${String.format(Locale.US, "%.0f", plan.monthlyPrice)}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(" / month", style = MaterialTheme.typography.bodySmall, color = Slate400)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            plan.featuresJson.split(",").forEach { feat ->
                                Row(
                                    modifier = Modifier.padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = SuccessEmerald, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(feat.trim(), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onStartFree,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isPopular) BrandPrimary else Slate800
                                )
                            ) {
                                Text(if (plan.monthlyPrice == 0.0) "Get Started" else "Select Plan")
                            }
                        }
                    }
                }
            }
        }

        // 7. Frequently Asked Questions
        item {
            Spacer(modifier = Modifier.height(28.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Frequently Asked Questions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))

                FaqItem("Does BizFlow work on mobile phones and tablets?", "Yes! BizFlow is designed mobile-first with adaptive layouts that fit smartphones, tablets, and desktop browsers seamlessly.")
                FaqItem("Can I export data for my chartered accountant?", "Absolutely. BizFlow allows 1-click export of Sales, Expenses, Inventory, and Profit & Loss reports in CSV and formatted text formats.")
                FaqItem("Can I run multiple shops under one account?", "Yes, the Business plan supports multi-branch operations with centralized and branch-specific stock, staff, and sales analytics.")
            }
        }

        // 8. Footer with Legal Links
        item {
            Spacer(modifier = Modifier.height(36.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("BizFlow SaaS OS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Simple business management for small businesses.", style = MaterialTheme.typography.bodySmall, color = Slate400)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(onClick = { onViewLegal("terms") }) { Text("Terms of Service", fontSize = 11.sp) }
                        TextButton(onClick = { onViewLegal("privacy") }) { Text("Privacy Policy", fontSize = 11.sp) }
                        TextButton(onClick = { onViewLegal("refund") }) { Text("Refund Policy", fontSize = 11.sp) }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("© 2026 BizFlow Inc. All rights reserved.", style = MaterialTheme.typography.labelSmall, color = Slate500)
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

private data class FeatureItem(
    val icon: ImageVector,
    val title: String,
    val description: String
)

@Composable
private fun StepRow(number: String, title: String, description: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(BrandPrimary),
            contentAlignment = Alignment.Center
        ) {
            Text(number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Text(description, style = MaterialTheme.typography.bodySmall, color = Slate300)
        }
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }
    BizCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = { expanded = !expanded }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = question,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = Slate400
            )
        }
        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = answer,
                style = MaterialTheme.typography.bodySmall,
                color = Slate400
            )
        }
    }
}
