package com.example.bearbudget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.bearbudget.ui.navigation.MainNavigation
import com.example.bearbudget.ui.theme.BearBudgetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BearBudgetTheme {
                MainNavigation()
            }
        }
    }
}
