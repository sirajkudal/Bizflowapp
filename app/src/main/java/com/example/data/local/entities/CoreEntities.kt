package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val fullName: String,
    val role: String = "OWNER",
    val isEmailVerified: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val isSuspended: Boolean = false
)

@Entity(tableName = "businesses")
data class BusinessEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val name: String,
    val ownerName: String,
    val email: String,
    val phone: String,
    val address: String,
    val country: String = "India",
    val currencySymbol: String = "₹",
    val defaultTaxRate: Double = 18.0,
    val invoicePrefix: String = "INV-",
    val logoEmoji: String = "🏢",
    val isDemo: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "branches")
data class BranchEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val name: String,
    val address: String,
    val phone: String,
    val isMainBranch: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val name: String,
    val phone: String,
    val email: String = "",
    val address: String = "",
    val taxId: String = "",
    val notes: String = "",
    val totalPurchases: Double = 0.0,
    val outstandingBalance: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val branchId: String = "main",
    val name: String,
    val sku: String,
    val barcode: String = "",
    val category: String = "General",
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val taxRate: Double = 0.0,
    val stockQuantity: Int = 0,
    val minStockLevel: Int = 10,
    val supplier: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "inventory_movements")
data class InventoryMovementEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val productId: String,
    val productName: String,
    val movementType: String, // IN, OUT, ADJUSTMENT
    val quantityChange: Int,
    val balanceAfter: Int,
    val reason: String,
    val referenceId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
