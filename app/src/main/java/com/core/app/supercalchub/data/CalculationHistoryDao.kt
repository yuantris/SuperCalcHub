package com.core.app.supercalchub.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 计算历史记录DAO
 */
@Dao
interface CalculationHistoryDao {
    
    /**
     * 获取所有历史记录
     */
    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<CalculationHistoryEntity>>
    
    /**
     * 根据类型获取历史记录
     */
    @Query("SELECT * FROM calculation_history WHERE type = :type ORDER BY timestamp DESC")
    fun getHistoryByType(type: String): Flow<List<CalculationHistoryEntity>>
    
    /**
     * 获取最近的历史记录
     */
    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 20): Flow<List<CalculationHistoryEntity>>
    
    /**
     * 搜索历史记录
     */
    @Query("SELECT * FROM calculation_history WHERE expression LIKE '%' || :query || '%' OR result LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchHistory(query: String): Flow<List<CalculationHistoryEntity>>
    
    /**
     * 插入历史记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: CalculationHistoryEntity)
    
    /**
     * 批量插入历史记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(historyList: List<CalculationHistoryEntity>)
    
    /**
     * 删除历史记录
     */
    @Delete
    suspend fun deleteHistory(history: CalculationHistoryEntity)
    
    /**
     * 根据ID删除历史记录
     */
    @Query("DELETE FROM calculation_history WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    /**
     * 清空所有历史记录
     */
    @Query("DELETE FROM calculation_history")
    suspend fun clearAll()
    
    /**
     * 获取历史记录数量
     */
    @Query("SELECT COUNT(*) FROM calculation_history")
    fun getHistoryCount(): Flow<Int>
    
    /**
     * 获取指定日期的历史记录
     */
    @Query("SELECT * FROM calculation_history WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getHistoryByDateRange(startTime: Long, endTime: Long): Flow<List<CalculationHistoryEntity>>
}