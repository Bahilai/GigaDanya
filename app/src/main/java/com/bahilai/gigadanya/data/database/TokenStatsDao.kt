package com.bahilai.gigadanya.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы со статистикой токенов
 */
@Dao
interface TokenStatsDao {
    @Query("SELECT * FROM token_stats WHERE id = 1")
    fun getTokenStats(): Flow<TokenStatsEntity?>
    
    @Query("SELECT * FROM token_stats WHERE id = 1")
    suspend fun getTokenStatsSync(): TokenStatsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTokenStats(stats: TokenStatsEntity)
    
    @Query("DELETE FROM token_stats")
    suspend fun deleteTokenStats()
}

