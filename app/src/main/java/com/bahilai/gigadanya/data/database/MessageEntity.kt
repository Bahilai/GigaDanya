package com.bahilai.gigadanya.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity для хранения сообщений в базе данных
 */
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val text: String? = null,
    val imageUrl: String? = null,
    val isFromUser: Boolean,
    val agentName: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

