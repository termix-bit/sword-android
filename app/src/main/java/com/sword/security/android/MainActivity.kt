package com.sword.security.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sword.security.android.ui.navigation.SwordNavHost
import com.sword.security.android.ui.theme.SwordTheme
import com.sword.security.android.ui.viewmodel.SwordViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val swordViewModel: SwordViewModel = viewModel()
            val prefs by swordViewModel.preferences.collectAsStateWithLifecycle()
            SwordTheme(themeMode = prefs.themeMode) {
                SwordNavHost(viewModel = swordViewModel)
            }
        }
    }
}
