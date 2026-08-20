package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.AuditLogEntity
import com.example.data.local.entities.BranchEntity
import com.example.data.local.entities.BusinessEntity
import com.example.data.local.entities.CustomerEntity
import com.example.data.local.entities.ExpenseEntity
import com.example.data.local.entities.InventoryMovementEntity
import com.example.data.local.entities.InvoiceEntity
import com.example.data.local.entities.InvoiceItemEntity
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.PaymentEntity
import com.example.data.local.entities.ProductEntity
import com.example.data.local.entities.SaleEntity
import com.example.data.local.entities.SaleItemEntity
import com.example.data.local.entities.StaffEntity
import com.example.data.local.entities.SubscriptionEntity
import com.example.data.local.entities.SubscriptionPlanEntity
import com.example.data.local.entities.SupportTicketEntity
import com.example.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)
}

@Dao
interface BusinessDao {
    @Query("SELECT * FROM businesses ORDER BY createdAt ASC")
    fun getAllBusinesses(): Flow<List<BusinessEntity>>

    @Query("SELECT * FROM businesses WHERE id = :id LIMIT 1")
    fun getBusinessById(id: String): Flow<BusinessEntity?>

    @Query("SELECT * FROM businesses WHERE id = :id LIMIT 1")
    suspend fun getBusinessByIdDirect(id: String): BusinessEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: BusinessEntity)

    @Update
    suspend fun updateBusiness(business: BusinessEntity)

    @Delete
    suspend fun deleteBusiness(business: BusinessEntity)

    @Query("SELECT * FROM branches WHERE businessId = :businessId")
    fun getBranchesForBusiness(businessId: String): Flow<List<BranchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBranch(branch: BranchEntity)
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE businessId = :businessId ORDER BY name ASC")
    fun getCustomersByBusiness(businessId: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: String): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET totalPurchases = totalPurchases + :amount, outstandingBalance = outstandingBalance + :balanceChange WHERE id = :customerId")
    suspend fun updateCustomerBalances(customerId: String, amount: Double, balanceChange: Double)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE businessId = :businessId ORDER BY name ASC")
    fun getProductsByBusiness(businessId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): ProductEntity?

    @Query("SELECT * FROM products WHERE businessId = :businessId AND barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(businessId: String, barcode: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("UPDATE products SET stockQuantity = stockQuantity + :delta WHERE id = :productId")
    suspend fun adjustStock(productId: String, delta: Int)

    @Query("SELECT * FROM inventory_movements WHERE businessId = :businessId ORDER BY timestamp DESC")
    fun getInventoryMovements(businessId: String): Flow<List<InventoryMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: InventoryMovementEntity)
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales WHERE businessId = :businessId ORDER BY timestamp DESC")
    fun getSalesByBusiness(businessId: String): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getSaleItems(saleId: String): List<SaleItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Delete
    suspend fun deleteSale(sale: SaleEntity)
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun getInvoicesByBusiness(businessId: String): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceById(id: String): InvoiceEntity?

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun getInvoiceItems(invoiceId: String): List<InvoiceItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItemEntity>)

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Delete
    suspend fun deleteInvoice(invoice: InvoiceEntity)

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteInvoiceItems(invoiceId: String)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE businessId = :businessId ORDER BY paymentDate DESC")
    fun getPaymentsByBusiness(businessId: String): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Delete
    suspend fun deletePayment(payment: PaymentEntity)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE businessId = :businessId ORDER BY date DESC")
    fun getExpensesByBusiness(businessId: String): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)
}

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff WHERE businessId = :businessId ORDER BY addedAt ASC")
    fun getStaffByBusiness(businessId: String): Flow<List<StaffEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: StaffEntity)

    @Update
    suspend fun updateStaff(staff: StaffEntity)

    @Delete
    suspend fun deleteStaff(staff: StaffEntity)
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscription_plans")
    fun getAllPlans(): Flow<List<SubscriptionPlanEntity>>

    @Query("SELECT * FROM subscription_plans WHERE id = :id LIMIT 1")
    suspend fun getPlanById(id: String): SubscriptionPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlans(plans: List<SubscriptionPlanEntity>)

    @Update
    suspend fun updatePlan(plan: SubscriptionPlanEntity)

    @Query("SELECT * FROM subscriptions WHERE businessId = :businessId LIMIT 1")
    fun getSubscriptionForBusiness(businessId: String): Flow<SubscriptionEntity?>

    @Query("SELECT * FROM subscriptions")
    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: SubscriptionEntity)

    @Update
    suspend fun updateSubscription(subscription: SubscriptionEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE businessId = :businessId ORDER BY timestamp DESC")
    fun getNotificationsByBusiness(businessId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE businessId = :businessId")
    suspend fun markAllAsRead(businessId: String)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)
}

@Dao
interface SupportTicketDao {
    @Query("SELECT * FROM support_tickets WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun getTicketsByBusiness(businessId: String): Flow<List<SupportTicketEntity>>

    @Query("SELECT * FROM support_tickets ORDER BY createdAt DESC")
    fun getAllTickets(): Flow<List<SupportTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicketEntity)

    @Update
    suspend fun updateTicket(ticket: SupportTicketEntity)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs WHERE businessId = :businessId ORDER BY timestamp DESC LIMIT 100")
    fun getAuditLogs(businessId: String): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity)
}
