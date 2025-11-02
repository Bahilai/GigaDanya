package com.bahilai.gigadanya.data

/**
 * Представляет сообщение в чате
 * @param id Уникальный идентификатор сообщения
 * @param text Текстовое содержимое сообщения (может быть null для сообщений с изображением)
 * @param imageUrl URL изображения (если сообщение содержит изображение)
 * @param isFromUser true если сообщение от пользователя, false если от бота
 * @param timestamp Время отправки сообщения
 */
data class Message(
    val id: String,
    val text: String? = null,
    val imageUrl: String? = null,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

