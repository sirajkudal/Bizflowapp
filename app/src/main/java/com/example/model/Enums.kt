package com.example.model

enum class PlanTier(
    val title: String,
    val defaultPriceInr: Double,
    val productLimit: Int,
    val customerLimit: Int,
    val multiBranch: Boolean,
    val staffAccounts: Boolean,
    val advancedReports: Boolean,
    val dataExport: Boolean,
    val prioritySupport: Boolean,
    val apiAccess: Boolean
) {
    FREE("Free", 0.0, 50, 50, false, false, false, false, false, false),
    STARTER("Starter", 299.0, 500, Int.MAX_VALUE, false, false, false, false, false, false),
    PROFESSIONAL("Professional", 699.0, Int.MAX_VALUE, Int.MAX_VALUE, false, true, true, true, true, false),
    BUSINESS("Business", 1499.0, Int.MAX_VALUE, Int.MAX_VALUE, true, true, true, true, true, true)
}

enum class StaffRole(val title: String, val description: String) {
    OWNER("Owner", "Full access to everything, billing, staff, multi-business & admin controls"),
    MANAGER("Manager", "Business operations, inventory, sales, customers and reports"),
    STAFF("Staff", "Create sales, handle customer accounts & point-of-sale"),
    ACCOUNTANT("Accountant", "Invoices, payment reconciliation, expenses & financial reports")
}

enum class InvoiceStatus(val label: String) {
    DRAFT("Draft"),
    SENT("Sent"),
    PARTIALLY_PAID("Partially Paid"),
    PAID("Paid"),
    OVERDUE("Overdue"),
    CANCELLED("Cancelled")
}

enum class PaymentMethod(val label: String) {
    CASH("Cash"),
    UPI("UPI"),
    CARD("Card"),
    BANK_TRANSFER("Bank Transfer"),
    OTHER("Other")
}

enum class ExpenseCategory(val label: String) {
    RENT("Rent"),
    SALARY("Salary"),
    ELECTRICITY("Electricity"),
    INTERNET("Internet"),
    TRANSPORT("Transport"),
    MARKETING("Marketing"),
    SUPPLIES("Supplies"),
    OTHER("Other")
}

enum class StockMovementType(val label: String) {
    IN("Stock In (Purchase)"),
    OUT("Stock Out (Sale)"),
    ADJUSTMENT("Manual Adjustment")
}

enum class DateRangeFilter(val label: String, val days: Int) {
    TODAY("Today", 1),
    DAYS_7("7 Days", 7),
    DAYS_30("30 Days", 30),
    MONTHS_3("3 Months", 90),
    MONTHS_12("12 Months", 365),
    ALL_TIME("All Time", 3650)
}

enum class TicketPriority(val label: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    URGENT("Urgent")
}

enum class TicketStatus(val label: String) {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved"),
    CLOSED("Closed")
}

enum class TicketCategory(val label: String) {
    BILLING("Billing & Plans"),
    TECHNICAL("Technical Issue"),
    FEATURE_REQUEST("Feature Request"),
    ACCOUNT("Account & Multi-User"),
    GENERAL("General Inquiry")
}
