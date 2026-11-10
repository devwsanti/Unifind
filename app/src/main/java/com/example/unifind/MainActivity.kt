package com.example.unifind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.unifind.ui.navigation.AppNavigation
import com.example.unifind.ui.theme.UniFindTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniFindTheme {
                AppNavigation()
            }
        }
    }
}
