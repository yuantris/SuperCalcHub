package com.core.app.supercalchub.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.core.app.supercalchub.data.AppDatabase
import com.core.app.supercalchub.ui.screens.CalculatorScreen
import com.core.app.supercalchub.ui.screens.VerticalCalcScreen
import com.core.app.supercalchub.ui.screens.FractionScreen
import com.core.app.supercalchub.ui.screens.FunctionPlotScreen
import com.core.app.supercalchub.ui.screens.HistoryScreen
import com.core.app.supercalchub.ui.screens.EquationScreen
import com.core.app.supercalchub.ui.screens.SettingsScreen

/**
 * 应用导航路由
 */
object AppRoutes {
    const val CALCULATOR = "calculator"
    const val VERTICAL_CALC = "vertical_calc"
    const val FRACTION = "fraction"
    const val EQUATION = "equation"
    const val FUNCTION_PLOT = "function_plot"
    const val GAMES = "games"
    const val GEOMETRY = "geometry"
    const val SETTINGS = "settings"
    const val HISTORY = "history"
}

/**
 * 应用导航组件
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    database: AppDatabase,
    onThemeChanged: (String) -> Unit = {},
    onDarkModeChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.CALCULATOR,
        modifier = modifier
    ) {
        // 主计算器界面
        composable(AppRoutes.CALCULATOR) {
            CalculatorScreen(navController, database)
        }
        
        // 竖式计算界面
        composable(AppRoutes.VERTICAL_CALC) {
            VerticalCalcScreen(navController)
        }
        
        // 分数计算界面
        composable(AppRoutes.FRACTION) {
            FractionScreen(navController)
        }
        
        // 解方程界面
        composable(AppRoutes.EQUATION) {
            EquationScreen(navController)
        }
        
        // 函数绘制界面
        composable(AppRoutes.FUNCTION_PLOT) {
            FunctionPlotScreen(navController)
        }
        
        // 游戏界面
        composable(AppRoutes.GAMES) {
            // GamesScreen(navController)
        }
        
        // 几何画板界面
        composable(AppRoutes.GEOMETRY) {
            // GeometryScreen(navController)
        }
        
        // 设置界面
        composable(AppRoutes.SETTINGS) {
            SettingsScreen(
                navController = navController,
                onThemeChanged = onThemeChanged,
                onDarkModeChanged = onDarkModeChanged
            )
        }
        
        // 历史记录界面
        composable(AppRoutes.HISTORY) {
            HistoryScreen(navController, database)
        }
    }
}

/**
 * 功能模块数据
 */
data class FeatureModule(
    val route: String,
    val title: String,
    val description: String,
    val icon: String
)

/**
 * 获取所有功能模块
 */
fun getAllFeatures(): List<FeatureModule> {
    return listOf(
        FeatureModule(
            route = AppRoutes.CALCULATOR,
            title = "标准计算器",
            description = "基本四则运算",
            icon = "calculate"
        ),
        FeatureModule(
            route = AppRoutes.VERTICAL_CALC,
            title = "竖式计算",
            description = "展示竖式计算过程",
            icon = "view_stream"
        ),
        FeatureModule(
            route = AppRoutes.FRACTION,
            title = "分数计算",
            description = "分数四则运算",
            icon = "fraction"
        ),
        FeatureModule(
            route = AppRoutes.EQUATION,
            title = "解方程",
            description = "求解各类方程",
            icon = "functions"
        ),
        FeatureModule(
            route = AppRoutes.FUNCTION_PLOT,
            title = "函数绘制",
            description = "绘制函数图像",
            icon = "show_chart"
        ),
        FeatureModule(
            route = AppRoutes.GAMES,
            title = "趣味游戏",
            description = "数字益智游戏",
            icon = "games"
        ),
        FeatureModule(
            route = AppRoutes.GEOMETRY,
            title = "几何画板",
            description = "绘制几何图形",
            icon = "gesture"
        ),
        FeatureModule(
            route = AppRoutes.HISTORY,
            title = "计算历史",
            description = "查看历史记录",
            icon = "history"
        ),
        FeatureModule(
            route = AppRoutes.SETTINGS,
            title = "设置",
            description = "应用设置",
            icon = "settings"
        )
    )
}