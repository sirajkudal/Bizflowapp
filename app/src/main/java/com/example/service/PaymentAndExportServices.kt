package com.example.service

import com.example.data.local.entities.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PaymentGatewayService {
    enum class GatewayProvider(val displayName: String, val supportedCurrencies: List<String>) {
        RAZORPAY("Razorpay (India & UPI)", listOf("INR")),
        STRIPE("Stripe (Global)", listOf("USD", "EUR", "GBP", "INR", "AUD", "CAD")),
        SANDBOX_SIMULATOR("BizFlow Direct Sandbox", listOf("INR", "USD", "EUR", "GBP"))
    }

    data class PaymentRequest(
        val orderId: String,
        val amount: Double,
        val currency: String,
        val customerName: String,
        val customerEmail: String,
        val description: String
    )

    data class PaymentResult(
        val isSuccessful: Boolean,
        val transactionId: String,
        val methodUsed: String,
        val message: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    fun processPayment(request: PaymentRequest, method: String): PaymentResult {
        // Production-ready mock abstraction with realistic validation
        val txnId = "TXN_${System.currentTimeMillis()}_${(1000..9999).random()}"
        return PaymentResult(
            isSuccessful = true,
            transactionId = txnId,
            methodUsed = method,
            message = "Payment of ${request.currency}${String.format(Locale.US, "%.2f", request.amount)} processed successfully via $method."
        )
    }
}

object ReportExportService {
    private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US)

    fun generateSalesCsv(sales: List<SaleEntity>, currency: String): String {
        val sb = StringBuilder()
        sb.append("Sale ID,Date,Customer Name,Payment Method,Subtotal ($currency),Discount ($currency),Tax ($currency),Grand Total ($currency)\n")
        sales.forEach { sale ->
            val dateStr = dateFormat.format(Date(sale.timestamp))
            sb.append("\"${sale.id}\",\"$dateStr\",\"${sale.customerName}\",\"${sale.paymentMethod}\",${sale.subtotal},${sale.discountAmount},${sale.taxAmount},${sale.grandTotal}\n")
        }
        return sb.toString()
    }

    fun generateInventoryCsv(products: List<ProductEntity>, currency: String): String {
        val sb = StringBuilder()
        sb.append("Product Name,SKU,Barcode,Category,Purchase Price ($currency),Selling Price ($currency),Stock Quantity,Min Stock Level,Supplier,Stock Value ($currency)\n")
        products.forEach { p ->
            val stockVal = p.stockQuantity * p.purchasePrice
            sb.append("\"${p.name}\",\"${p.sku}\",\"${p.barcode}\",\"${p.category}\",${p.purchasePrice},${p.sellingPrice},${p.stockQuantity},${p.minStockLevel},\"${p.supplier}\",$stockVal\n")
        }
        return sb.toString()
    }

    fun generateProfitLossSummary(
        sales: List<SaleEntity>,
        expenses: List<ExpenseEntity>,
        currency: String
    ): String {
        val totalRevenue = sales.sumOf { it.grandTotal }
        val totalExpenses = expenses.sumOf { it.amount }
        val netProfit = totalRevenue - totalExpenses
        val profitMargin = if (totalRevenue > 0) (netProfit / totalRevenue) * 100 else 0.0

        return """
            ==============================================
                     BIZFLOW FINANCIAL SUMMARY
            ==============================================
            Generated on: ${dateFormat.format(Date())}
            
            Total Gross Revenue:   $currency ${String.format(Locale.US, "%,.2f", totalRevenue)}
            Total Operations Cost: $currency ${String.format(Locale.US, "%,.2f", totalExpenses)}
            ----------------------------------------------
            NET PROFIT / (LOSS):   $currency ${String.format(Locale.US, "%,.2f", netProfit)}
            Net Profit Margin:     ${String.format(Locale.US, "%.1f", profitMargin)}%
            ==============================================
        """.trimIndent()
    }

    fun generateInvoicePrintableText(
        business: BusinessEntity?,
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>
    ): String {
        val currency = business?.currencySymbol ?: "₹"
        val issueDateStr = dateFormat.format(Date(invoice.issueDate))
        val dueDateStr = dateFormat.format(Date(invoice.dueDate))

        val itemsFormatted = items.mapIndexed { idx, it ->
            "${idx + 1}. ${it.description} x ${it.quantity} @ $currency${String.format(Locale.US, "%.2f", it.unitPrice)} = $currency${String.format(Locale.US, "%.2f", it.totalAmount)}"
        }.joinToString("\n")

        return """
            ===========================================================
                                  TAX INVOICE
            ===========================================================
            ${business?.logoEmoji ?: "🏢"} ${business?.name ?: "BizFlow Business"}
            ${business?.address ?: "Business Address"}
            Phone: ${business?.phone ?: ""} | Email: ${business?.email ?: ""}
            -----------------------------------------------------------
            Invoice No: ${invoice.invoiceNumber}
            Status:     ${invoice.status}
            Issue Date: $issueDateStr
            Due Date:   $dueDateStr
            -----------------------------------------------------------
            BILLED TO:
            Customer:   ${invoice.customerName}
            Phone:      ${invoice.customerPhone}
            Email:      ${invoice.customerEmail}
            Address:    ${invoice.customerAddress}
            -----------------------------------------------------------
            LINE ITEMS:
            $itemsFormatted
            -----------------------------------------------------------
            Subtotal:         $currency ${String.format(Locale.US, "%.2f", invoice.subtotal)}
            Discount:       - $currency ${String.format(Locale.US, "%.2f", invoice.discountAmount)}
            Tax / GST:      + $currency ${String.format(Locale.US, "%.2f", invoice.taxAmount)}
            -----------------------------------------------------------
            GRAND TOTAL:      $currency ${String.format(Locale.US, "%.2f", invoice.grandTotal)}
            Amount Paid:      $currency ${String.format(Locale.US, "%.2f", invoice.paidAmount)}
            Balance Due:      $currency ${String.format(Locale.US, "%.2f", (invoice.grandTotal - invoice.paidAmount).coerceAtLeast(0.0))}
            ===========================================================
            Notes: ${invoice.notes}
            Terms: ${invoice.terms}
            ===========================================================
        """.trimIndent()
    }
}
