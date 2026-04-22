package com.app.kotlinmode

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.app.kotlinmode.navigation.AppNavGraph
import com.app.kotlinmode.ui.theme.KotlinModeAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Full-bleed design — app draws behind system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            KotlinModeAppTheme {
                // AppNavGraph now creates its own SessionManager internally
                AppNavGraph(context = applicationContext)
            }
        }
    }
}
