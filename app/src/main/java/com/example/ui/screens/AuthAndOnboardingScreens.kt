package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BizCard
import com.example.ui.theme.*

@Composable
fun AuthScreen(
    onLoginSuccess: (String) -> Unit,
    onRegisterSuccess: (String, String) -> Unit,
    onDemoLogin: () -> Unit,
    onBackToLanding: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBackToLanding) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Brand Icon
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(BrandPrimary, BrandSecondary))),
            contentAlignment = Alignment.Center
        ) {
            Text("⚡", fontSize = 26.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isRegisterMode) "Create your BizFlow Account" else "Welcome back to BizFlow",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (isRegisterMode) "Start your 14-day free business trial today" else "Log in to manage your business operations",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate400,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Form Card
        BizCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isRegisterMode) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_fullname_field")
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Business Email") },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_email_field")
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_password_field")
            )

            if (!isRegisterMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showForgotPasswordDialog = true }) {
                        Text("Forgot password?", style = MaterialTheme.typography.bodySmall, color = BrandPrimary)
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = DangerRose,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }

            Button(
                onClick = {
                    if (email.isBlank()) {
                        errorMessage = "Please enter your email."
                        return@Button
                    }
                    if (isRegisterMode) {
                        if (fullName.isBlank()) {
                            errorMessage = "Please enter your name."
                            return@Button
                        }
                        onRegisterSuccess(email, fullName)
                    } else {
                        onLoginSuccess(email)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("auth_submit_btn")
            ) {
                Text(if (isRegisterMode) "Create Free Account" else "Sign In", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Slate200.copy(alpha = 0.4f))
                Text(" OR ", style = MaterialTheme.typography.labelSmall, color = Slate400, modifier = Modifier.padding(horizontal = 8.dp))
                HorizontalDivider(modifier = Modifier.weight(1f), color = Slate200.copy(alpha = 0.4f))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1-Click Demo Login
            OutlinedButton(
                onClick = onDemoLogin,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandSecondary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("demo_login_btn")
            ) {
                Text("⚡ 1-Click Launch Demo Account", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Switch mode button
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isRegisterMode) "Already have an account?" else "Don't have a business account?",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate400
            )
            TextButton(
                onClick = {
                    isRegisterMode = !isRegisterMode
                    errorMessage = null
                },
                modifier = Modifier.testTag("switch_auth_mode_btn")
            ) {
                Text(
                    text = if (isRegisterMode) "Sign In" else "Register Free",
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimary
                )
            }
        }
    }

    if (showForgotPasswordDialog) {
        var resetEmail by remember { mutableStateOf(email) }
        var resetDone by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Reset Password") },
            text = {
                Column {
                    if (resetDone) {
                        Text("Password reset instructions sent to $resetEmail. Check your inbox.")
                    } else {
                        Text("Enter your email address to receive password reset instructions.")
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it },
                            label = { Text("Email Address") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                if (resetDone) {
                    TextButton(onClick = { showForgotPasswordDialog = false }) { Text("Close") }
                } else {
                    Button(onClick = { resetDone = true }) { Text("Send Link") }
                }
            }
        )
    }
}

@Composable
fun OnboardingScreen(
    userEmail: String,
    onComplete: (name: String, owner: String, email: String, phone: String, address: String, country: String, currency: String, taxRate: Double, prefix: String, logoEmoji: String) -> Unit
) {
    var businessName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("India") }
    var selectedCurrency by remember { mutableStateOf("₹") }
    var taxRateStr by remember { mutableStateOf("18.0") }
    var invoicePrefix by remember { mutableStateOf("INV-") }
    var selectedEmoji by remember { mutableStateOf("🏢") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val logoEmojis = listOf("🏢", "⚡", "🛍️", "💻", "📦", "🏪", "☕", "🚚", "🛠️", "🩺")
    val currencies = listOf("₹" to "INR (₹)", "$" to "USD ($)", "€" to "EUR (€)", "£" to "GBP (£)", "AED " to "AED")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Business Setup Wizard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Let's configure your store profile, invoicing details, and tax settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate400
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Logo Emoji Selector
        Text("Choose Business Logo Emoji", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(logoEmojis) { emoji ->
                val isSelected = emoji == selectedEmoji
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) BrandPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) BrandPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { selectedEmoji = emoji },
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 22.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        BizCard {
            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text("Business / Store Name *") },
                placeholder = { Text("e.g. Apex Trading Co.") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboard_bizname_field")
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = ownerName,
                onValueChange = { ownerName = it },
                label = { Text("Owner / Manager Name *") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Business Phone Number *") },
                placeholder = { Text("+91 98765 43210") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Store Address / City *") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Primary Currency", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(currencies) { (sym, label) ->
                    val isSelected = selectedCurrency == sym
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCurrency = sym },
                        label = { Text(label) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = taxRateStr,
                    onValueChange = { taxRateStr = it },
                    label = { Text("Default Tax / GST %") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = invoicePrefix,
                    onValueChange = { invoicePrefix = it },
                    label = { Text("Invoice Prefix") },
                    placeholder = { Text("INV-") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage ?: "",
                color = DangerRose,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (businessName.isBlank() || ownerName.isBlank() || phone.isBlank()) {
                    errorMessage = "Please fill in business name, owner name and phone."
                    return@Button
                }
                val tax = taxRateStr.toDoubleOrNull() ?: 18.0
                onComplete(
                    businessName,
                    ownerName,
                    userEmail,
                    phone,
                    address,
                    country,
                    selectedCurrency,
                    tax,
                    invoicePrefix.ifBlank { "INV-" },
                    selectedEmoji
                )
            },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("onboard_finish_btn")
        ) {
            Text("Launch Business Dashboard", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
