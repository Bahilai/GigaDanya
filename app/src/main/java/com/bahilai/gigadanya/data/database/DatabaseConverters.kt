package com.bahilai.gigadanya.data.database

import com.bahilai.gigadanya.data.Message
import com.bahilai.gigadanya.data.YandexMessage
import com.bahilai.gigadanya.viewmodel.ChatViewModel

/**
 * Функции преобразования между Entity и обычными классами данных
 */

// Message <-> MessageEntity
fun Message.toEntity(): MessageEntity {
    return MessageEntity(
        id = this.id,
        text = this.text,
        imageUrl = this.imageUrl,
        isFromUser = this.isFromUser,
        agentName = this.agentName,
        timestamp = this.timestamp
    )
}

fun MessageEntity.toMessage(): Message {
    return Message(
        id = this.id,
        text = this.text,
        imageUrl = this.imageUrl,
        isFromUser = this.isFromUser,
        agentName = this.agentName,
        timestamp = this.timestamp
    )
}

// YandexMessage <-> YandexMessageEntity
fun YandexMessage.toEntity(): YandexMessageEntity {
    return YandexMessageEntity(
        id = 0, // Будет сгенерировано автоматически
        role = this.role,
        text = this.text,
        timestamp = System.currentTimeMillis()
    )
}

fun YandexMessageEntity.toYandexMessage(): YandexMessage {
    return YandexMessage(
        role = this.role,
        text = this.text
    )
}

// TokenStats <-> TokenStatsEntity
fun ChatViewModel.TokenStats.toEntity(): TokenStatsEntity {
    return TokenStatsEntity(
        id = 1,
        totalInputTokens = this.totalInputTokens,
        totalOutputTokens = this.totalOutputTokens,
        compressionCount = this.compressionCount,
        savedTokens = this.savedTokens
    )
}

fun TokenStatsEntity.toTokenStats(): ChatViewModel.TokenStats {
    return ChatViewModel.TokenStats(
        totalInputTokens = this.totalInputTokens,
        totalOutputTokens = this.totalOutputTokens,
        compressionCount = this.compressionCount,
        savedTokens = this.savedTokens
    )
}

