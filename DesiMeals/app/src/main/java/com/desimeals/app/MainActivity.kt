package com.desimeals.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.desimeals.app.navigation.DesiMealsNavHost
import com.desimeals.app.ui.theme.DesiMealsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as DesiMealsApp).container
        setContent {
            DesiMealsTheme {
                DesiMealsNavHost(container = container)
            }
        }
    }
}
