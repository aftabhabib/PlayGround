package com.desimeals.app.di

import android.content.Context
import androidx.room.Room
import com.desimeals.app.data.local.AppDatabase
import com.desimeals.app.data.repository.CustomerRepository
import com.desimeals.app.data.repository.LedgerRepository
import com.desimeals.app.data.repository.MealRepository

interface AppContainer {
    val customerRepository: CustomerRepository
    val ledgerRepository: LedgerRepository
    val mealRepository: MealRepository
}

class AppContainerImpl(context: Context) : AppContainer {
    private val db: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "desi_meals.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    override val customerRepository: CustomerRepository by lazy {
        CustomerRepository(db.customerDao())
    }
    override val ledgerRepository: LedgerRepository by lazy {
        LedgerRepository(db.transactionDao(), db.customerDao())
    }
    override val mealRepository: MealRepository by lazy {
        MealRepository(db.mealDao())
    }
}
