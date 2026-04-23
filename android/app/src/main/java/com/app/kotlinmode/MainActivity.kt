package com.app.kotlinmode

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.app.kotlinmode.navigation.AppNavGraph
import com.app.kotlinmode.ui.theme.KotlinModeAppTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
        } else {
            Log.w("MainActivity", "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        askNotificationPermission()

        setContent {
            val navController = rememberNavController()
            
            // Handle notification intent if present
            LaunchedEffect(intent) {
                handleNotificationIntent(intent, navController)
            }

            KotlinModeAppTheme {
                AppNavGraph(context = applicationContext, navController = navController)
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun handleNotificationIntent(intent: Intent, navController: NavHostController) {
        val type = intent.getStringExtra("type") ?: return
        Log.d("MainActivity", "Handling notification: $type")
        
        val uri = when (type) {
            "message" -> {
                val convId = intent.getStringExtra("conversationId")
                val senderId = intent.getStringExtra("senderId")
                if (convId != null && senderId != null) {
                    Uri.parse("https://kotlinmode.app/chat/$convId/$senderId")
                } else null
            }
            "follow" -> {
                val userId = intent.getStringExtra("userId")
                if (userId != null) {
                    Uri.parse("https://kotlinmode.app/profile/$userId")
                } else null
            }
            else -> null
        }

        uri?.let { navController.navigate(it) }
    }
}
