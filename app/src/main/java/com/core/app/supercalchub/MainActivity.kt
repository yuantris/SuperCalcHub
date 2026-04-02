package com.core.app.supercalchub

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.core.app.supercalchub.data.AppDatabase
import com.core.app.supercalchub.ui.navigation.AppNavigation
import com.core.app.supercalchub.ui.theme.SuperCalcHubTheme

class MainActivity : ComponentActivity() {
    private lateinit var database: AppDatabase
    
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化数据库
        database = AppDatabase.getInstance(this)
        
        // 启用Edge-to-Edge
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT
            )
        )
        window.isNavigationBarContrastEnforced = false
        
        setContent {
            // 读取主题设置
            val prefs = remember { getEncryptedPrefs(this@MainActivity) }
            val themeId = remember { mutableStateOf(prefs.getString("theme", "blue") ?: "blue") }
            val isDarkMode = remember { mutableStateOf(prefs.getBoolean("dark_mode", false)) }
            
            SuperCalcHubTheme(
                darkTheme = isDarkMode.value,
                themeId = themeId.value
            ) {
                SuperCalcHubApp(
                    database = database,
                    onThemeChanged = { newThemeId ->
                        themeId.value = newThemeId
                    },
                    onDarkModeChanged = { newDarkMode ->
                        isDarkMode.value = newDarkMode
                    }
                )
            }
        }
    }
}

@Composable
fun SuperCalcHubApp(
    database: AppDatabase,
    onThemeChanged: (String) -> Unit = {},
    onDarkModeChanged: (Boolean) -> Unit = {}
) {
    val navController = rememberNavController()
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        AppNavigation(
            navController = navController,
            database = database,
            onThemeChanged = onThemeChanged,
            onDarkModeChanged = onDarkModeChanged,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

/**
 * 获取加密的 SharedPreferences
 */
fun getEncryptedPrefs(context: Context): android.content.SharedPreferences {
    return try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        EncryptedSharedPreferences.create(
            context,
            "supercalchub_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // 如果加密失败，回退到普通 SharedPreferences
        context.getSharedPreferences("supercalchub_prefs", Context.MODE_PRIVATE)
    }
}