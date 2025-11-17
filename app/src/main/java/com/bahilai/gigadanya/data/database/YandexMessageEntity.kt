package com.bahilai.gigadanya.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity для хранения истории разговора (YandexMessage) в базе данных
 */
@Entity(tableName = "conversation_history")
data class YandexMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String, // "system", "user", "assistant"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

