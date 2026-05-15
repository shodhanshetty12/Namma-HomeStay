package com.namma.homestay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.namma.homestay.ui.NammaHomeStayApp
import com.namma.homestay.ui.theme.NammaHomeStayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            NammaHomeStayTheme {
                NammaHomeStayApp()
            }
        }
    }
}

