package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val branchId: String = "main",
    val customerId: String = "",
    val customerName: String = "Walk-in Customer",
    val subtotal: Double,
    val discountAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val grandTotal: Double,
    val paymentMethod: String,
    val invoiceId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sale_items")
data class SaleItemEntity(
    @PrimaryKey val id: String,
    val saleId: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val invoiceNumber: String,
    val customerId: String,
    val customerName: String,
    val customerEmail: String = "",
    val customerPhone: String = "",
    val customerAddress: String = "",
    val issueDate: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis() + (14L * 24 * 60 * 60 * 1000), // +14 days default
    val subtotal: Double,
    val discountAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val grandTotal: Double,
    val paidAmount: Double = 0.0,
    val status: String = "DRAFT", // DRAFT, SENT, PARTIALLY_PAID, PAID, OVERDUE, CANCELLED
    val notes: String = "Thank you for doing business with us.",
    val terms: String = "Payment due within 14 days of issue date.",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "invoice_items")
data class InvoiceItemEntity(
    @PrimaryKey val id: String,
    val invoiceId: String,
    val productId: String = "",
    val description: String,
    val quantity: Int,
    val unitPrice: Double,
    val taxRate: Double = 0.0,
    val totalAmount: Double
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val invoiceId: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val amount: Double,
    val paymentDate: Long = System.currentTimeMillis(),
    val paymentMethod: String = "UPI",
    val referenceNumber: String = "",
    val notes: String = ""
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val title: String,
    val category: String, // Rent, Salary, Electricity, Internet, Transport, Marketing, Supplies, Other
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val paymentMethod: String = "Cash",
    val description: String = "",
    val receiptUri: String = ""
)

@Entity(tableName = "staff")
data class StaffEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: String, // OWNER, MANAGER, STAFF, ACCOUNTANT
    val isActive: Boolean = true,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "subscription_plans")
data class SubscriptionPlanEntity(
    @PrimaryKey val id: String, // FREE, STARTER, PROFESSIONAL, BUSINESS
    val name: String,
    val monthlyPrice: Double,
    val productLimit: Int,
    val customerLimit: Int,
    val featuresJson: String,
    val isMultiBranchAllowed: Boolean,
    val isStaffAllowed: Boolean,
    val isReportsAllowed: Boolean,
    val trialDays: Int = 14
)

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val planId: String, // FREE, STARTER, PROFESSIONAL, BUSINESS
    val status: String = "ACTIVE", // ACTIVE, TRIAL, EXPIRED, CANCELLED
    val startDate: Long = System.currentTimeMillis(),
    val renewalDate: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
    val lastBilledAmount: Double = 0.0,
    val autoRenew: Boolean = true
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val title: String,
    val message: String,
    val type: String, // LOW_STOCK, OVERDUE_INVOICE, PAYMENT_RECEIVED, STAFF, SUBSCRIPTION, SYSTEM
    val isRead: Boolean = false,
    val actionRoute: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val userEmail: String,
    val subject: String,
    val category: String, // BILLING, TECHNICAL, FEATURE_REQUEST, ACCOUNT, GENERAL
    val priority: String, // LOW, MEDIUM, HIGH, URGENT
    val description: String,
    val status: String = "OPEN", // OPEN, IN_PROGRESS, RESOLVED, CLOSED
    val adminReply: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val action: String,
    val entityType: String,
    val details: String,
    val performedBy: String = "Admin / Owner",
    val timestamp: Long = System.currentTimeMillis()
)
