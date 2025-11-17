package com.bahilai.gigadanya.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity для хранения статистики токенов в базе данных
 */
@Entity(tableName = "token_stats")
data class TokenStatsEntity(
    @PrimaryKey
    val id: Int = 1, // Всегда один экземпляр
    val totalInputTokens: Int = 0,
    val totalOutputTokens: Int = 0,
    val compressionCount: Int = 0,
    val savedTokens: Int = 0
)

