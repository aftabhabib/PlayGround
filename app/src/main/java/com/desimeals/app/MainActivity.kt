package com.desimeals.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.desimeals.app.ui.navigation.AppNav
import com.desimeals.app.ui.theme.DesiMealsTheme
import com.desimeals.app.ui.viewmodel.AppViewModel
import com.desimeals.app.util.prefsFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: AppViewModel = viewModel(factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory(application))
            val prefs by prefsFlow(applicationContext).collectAsState(initial = com.desimeals.app.util.AppPrefs())
            val dark = when (prefs.theme) {
                "light" -> false
                "dark" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            DesiMealsTheme(darkTheme = dark) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNav(vm)
                }
            }
        }
    }
}
