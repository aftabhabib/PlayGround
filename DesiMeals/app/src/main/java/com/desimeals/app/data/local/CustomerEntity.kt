package com.desimeals.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val monthlyRate: Double = 0.0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
