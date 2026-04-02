# SuperCalcHub - 多功能计算器

## 📱 项目简介

SuperCalcHub 是一款功能丰富的 Android 计算器应用，基于 Jetpack Compose 构建。除了标准计算功能外，还提供了科学计算、分数计算、函数绘制等多种数学工具，适合学生和数学爱好者使用。

### 技术栈
- **UI 框架**: Jetpack Compose
- **架构**: MVVM
- **数据库**: Room + KSP
- **数学计算**: exp4j + 自定义表达式解析器
- **图形绘制**: Canvas API (自定义 View)
- **导航**: Compose Navigation

---

## ✅ 已实现功能

### 1. 标准计算器
- ✅ 基本四则运算（加、减、乘、除）
- ✅ 实时预计算（输入时显示结果）
- ✅ 自动补全括号
- ✅ 百分比计算
- ✅ 连续计算（用结果继续运算）

### 2. 科学计算器
- ✅ 切换按钮（标准 ↔ 科学）
- ✅ 三角函数：sin、cos、tan、asin、acos、atan
- ✅ 对数函数：log（常用对数）、ln（自然对数）
- ✅ 指数函数：x²、x^n、e^x
- ✅ 其他函数：√x、|x|、1/x
- ✅ 常数：π、e
- ✅ 角度/弧度模式切换

### 3. 分数计算器
- ✅ 分数四则运算
- ✅ 自定义键盘（a/b 按钮输入分数）
- ✅ 分数线样式显示（不是斜杠）
- ✅ 分数与小数同时显示
- ✅ 特殊情况处理（如 1/3 + 2/3 = 1）

### 4. 竖式计算
- ✅ 展示竖式计算过程
- ✅ 支持加、减、乘、除
- ✅ 显示进位/借位步骤

### 5. 函数绘制
- ✅ exp4j 表达式解析
- ✅ 正确的数学坐标系（unitSize = 50px）
- ✅ 数学网格（按1单位绘制）
- ✅ 函数断裂处理（1/x、tan(x)等）
- ✅ 动态采样精度
- ✅ 零点检测（绿色标记）
- ✅ 极值点检测（橙色标记）
- ✅ 点击显示函数值
- ✅ 惯性滑动
- ✅ 双指缩放（以焦点为中心）
- ✅ 预设函数快捷按钮

### 6. 计算历史
- ✅ Room 数据库持久化存储
- ✅ 查看历史记录
- ✅ 删除单条记录
- ✅ 清空所有历史
- ✅ 显示计算类型、时间

### 7. 解方程模块
- ✅ 一元一次方程（ax + b = 0）
- ✅ 一元二次方程（ax² + bx + c = 0）
- ✅ 二元一次方程组
- ✅ 显示求解步骤
- ✅ 支持复数解

### 8. 主题设置
- ✅ 6种主题颜色选择
- ✅ 深色模式开关
- ✅ 使用 EncryptedSharedPreferences 加密存储

### 9. 隐私加密
- ✅ 启用/禁用隐私加密
- ✅ 设置隐私密码
- ✅ 加密存储敏感数据

### 10. 导航系统
- ✅ 侧边栏菜单
- ✅ 页面间跳转

---

## ❌ 未实现功能

### 1. 趣味游戏模块
- ❌ 数独
- ❌ 2048
- ❌ 24点
- ❌ 猜数字

### 2. 几何模块
- ❌ 几何画板（绘制点、线、多边形）
- ❌ 几何手册（公式、定理）
- ❌ 几何计算（面积、周长等）

### 3. 数学白板
- ❌ 手写输入
- ❌ 画笔工具
- ❌ 橡皮擦
- ❌ 形状工具

### 4. 其他
- ❌ 公式库
- ❌ 计算记录导出

---

## 📁 项目结构

```
app/src/main/java/com/core/app/supercalchub/
├── core/
│   ├── calculator/
│   │   ├── BaseCalculator.kt      # 计算器实现（含表达式解析）
│   │   ├── ExpressionParser.kt    # 表达式解析器
│   │   └── ResultFormatter.kt     # 结果格式化
│   └── math/
│       ├── Fraction.kt            # 分数类
│       ├── FractionCalculator.kt  # 分数计算器
│       └── EquationSolver.kt      # 方程求解器
├── data/
│   ├── AppDatabase.kt             # Room 数据库
│   ├── CalculationHistoryDao.kt   # 历史记录 DAO
│   └── CalculationHistoryEntity.kt # 历史记录实体
├── features/
│   ├── functionplot/
│   │   └── FunctionPlotView.kt    # 函数绘制自定义 View
│   ├── verticalcalc/
│   │   └── VerticalCalculator.kt  # 竖式计算器
│   └── ...
├── ui/
│   ├── navigation/
│   │   └── AppNavigation.kt       # 导航配置
│   ├── screens/
│   │   ├── CalculatorScreen.kt    # 计算器界面
│   │   ├── FractionScreen.kt      # 分数计算界面
│   │   ├── EquationScreen.kt      # 解方程界面
│   │   ├── VerticalCalcScreen.kt  # 竖式计算界面
│   │   ├── FunctionPlotScreen.kt  # 函数绘制界面
│   │   ├── HistoryScreen.kt       # 历史记录界面
│   │   └── SettingsScreen.kt      # 设置界面
│   └── theme/
│       ├── Color.kt               # 颜色定义
│       ├── Theme.kt               # 主题配置
│       └── Type.kt                # 字体配置
└── MainActivity.kt                # 主活动
```

---

## 🚀 使用说明

### 安装
1. 下载 `app-debug.apk`
2. 在 Android 设备上安装
3. 最低支持 Android 7.0 (API 24)

### 功能入口
点击左上角菜单按钮打开侧边栏，选择需要的功能：
- 计算器（标准/科学）
- 分数计算
- 解方程（一元一次、一元二次、二元一次方程组）
- 竖式计算
- 函数绘制
- 计算历史
- 设置（主题、隐私）

### 快捷操作
- 计算器：输入表达式后点击 `=` 或等待预计算
- 分数计算：点击 `a/b` 按钮输入分数
- 解方程：选择方程类型，输入系数，点击"求解"
- 函数绘制：点击预设函数或手动输入，点击"绘制"
- 设置：选择主题颜色，开启深色模式，启用隐私加密

---

## 🔧 开发环境

- Android Studio Panda 2 | 2025.3.2
- Kotlin 2.2.10
- Gradle 9.3.1
- compileSdk 36
- minSdk 24

---

## 📄 License

本项目仅供学习和参考使用。