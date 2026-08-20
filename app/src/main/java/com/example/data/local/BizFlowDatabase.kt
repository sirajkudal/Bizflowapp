package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AuditLogDao
import com.example.data.local.dao.BusinessDao
import com.example.data.local.dao.CustomerDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.InvoiceDao
import com.example.data.local.dao.NotificationDao
import com.example.data.local.dao.PaymentDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.SaleDao
import com.example.data.local.dao.StaffDao
import com.example.data.local.dao.SubscriptionDao
import com.example.data.local.dao.SupportTicketDao
import com.example.data.local.dao.UserDao
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

@Database(
    entities = [
        UserEntity::class,
        BusinessEntity::class,
        BranchEntity::class,
        CustomerEntity::class,
        ProductEntity::class,
        InventoryMovementEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class,
        PaymentEntity::class,
        ExpenseEntity::class,
        StaffEntity::class,
        SubscriptionPlanEntity::class,
        SubscriptionEntity::class,
        NotificationEntity::class,
        SupportTicketEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BizFlowDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun businessDao(): BusinessDao
    abstract fun customerDao(): CustomerDao
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun paymentDao(): PaymentDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun staffDao(): StaffDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun notificationDao(): NotificationDao
    abstract fun supportTicketDao(): SupportTicketDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: BizFlowDatabase? = null

        fun getInstance(context: Context): BizFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BizFlowDatabase::class.java,
                    "bizflow_production.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
