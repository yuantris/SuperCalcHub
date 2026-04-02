package com.core.app.supercalchub.ui.navigation

import androidx.navigation.NavController
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 导航工具类 - 防止快速点击导致的白屏问题
 */
object NavigationUtils {
    private val isNavigating = AtomicBoolean(false)
    
    /**
     * 安全的返回操作
     */
    fun safePopBack(navController: NavController): Boolean {
        if (isNavigating.getAndSet(true)) {
            return false
        }
        
        return try {
            val result = navController.popBackStack()
            result
        } catch (e: Exception) {
            false
        } finally {
            // 延迟重置导航状态，防止快速连续点击
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                isNavigating.set(false)
            }, 300)
        }
    }
    
    /**
     * 安全的导航操作
     */
    fun safeNavigate(
        navController: NavController,
        route: String,
        singleTop: Boolean = true
    ) {
        if (isNavigating.getAndSet(true)) {
            return
        }
        
        try {
            navController.navigate(route) {
                if (singleTop) {
                    launchSingleTop = true
                }
            }
        } catch (e: Exception) {
            // 忽略导航异常
        } finally {
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                isNavigating.set(false)
            }, 300)
        }
    }
}