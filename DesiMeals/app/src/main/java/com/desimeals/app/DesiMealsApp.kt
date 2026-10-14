package com.desimeals.app

import android.app.Application
import com.desimeals.app.di.AppContainer
import com.desimeals.app.di.AppContainerImpl

class DesiMealsApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(applicationContext)
    }
}
