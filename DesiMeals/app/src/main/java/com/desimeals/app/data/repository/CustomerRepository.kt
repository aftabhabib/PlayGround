package com.desimeals.app.data.repository

import com.desimeals.app.data.local.CustomerDao
import com.desimeals.app.data.local.CustomerEntity
import kotlinx.coroutines.flow.Flow

class CustomerRepository(private val dao: CustomerDao) {
    fun observeAll(): Flow<List<CustomerEntity>> = dao.observeAll()
    fun observeActive(): Flow<List<CustomerEntity>> = dao.observeActive()
    fun observeById(id: Long): Flow<CustomerEntity?> = dao.observeById(id)
    fun observeActiveCount(): Flow<Int> = dao.observeActiveCount()

    suspend fun upsert(customer: CustomerEntity): Long {
        require(customer.name.isNotBlank()) { "Customer name is required" }
        return dao.upsert(customer)
    }

    suspend fun delete(customer: CustomerEntity) = dao.delete(customer)
    suspend fun setActive(id: Long, active: Boolean) = dao.setActive(id, active)
}
