package com.core.app.supercalchub.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 计算历史记录实体
 */
@Entity(tableName = "calculation_history")
data class CalculationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val expression: String,
    val result: String,
    val type: String, // BASIC, FRACTION, EQUATION, FUNCTION, GEOMETRY, SCIENTIFIC
    val timestamp: Long = System.currentTimeMillis()
)