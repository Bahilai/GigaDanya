package com.bahilai.gigadanya.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с историей разговора
 */
@Dao
interface YandexMessageDao {
    @Query("SELECT * FROM conversation_history ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<YandexMessageEntity>>
    
    @Query("SELECT * FROM conversation_history ORDER BY timestamp ASC")
    suspend fun getAllMessagesSync(): List<YandexMessageEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: YandexMessageEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<YandexMessageEntity>)
    
    @Query("DELETE FROM conversation_history")
    suspend fun deleteAllMessages()
    
    @Delete
    suspend fun deleteMessage(message: YandexMessageEntity)
}

