package com.andychen.notimind.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.andychen.notimind.core.database.entity.SummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SummaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: SummaryEntity)
    
    @Query("SELECT * FROM summaries WHERE id = :id")
    suspend fun getSummaryById(id: String): SummaryEntity?
    
    @Query("SELECT * FROM summaries WHERE date = :date ORDER BY generatedAt DESC")
    fun getSummariesByDate(date: Long): Flow<List<SummaryEntity>>
    
    @Query("SELECT * FROM summaries WHERE date = :date AND period = :period ORDER BY generatedAt DESC LIMIT 1")
    fun getLatestSummaryByDateAndPeriod(date: Long, period: String): Flow<SummaryEntity?>
    
    @Query("SELECT * FROM summaries WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC, generatedAt DESC")
    fun getSummariesByDateRange(startDate: Long, endDate: Long): Flow<List<SummaryEntity>>
    
    @Query("DELETE FROM summaries WHERE date < :date")
    suspend fun deleteSummariesOlderThan(date: Long): Int
    
    @Query("DELETE FROM summaries")
    suspend fun deleteAllSummaries()
}