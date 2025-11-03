package com.bahilai.gigadanya.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bahilai.gigadanya.data.CompletionOptions
import com.bahilai.gigadanya.data.Message
import com.bahilai.gigadanya.data.YandexGptRequest
import com.bahilai.gigadanya.data.YandexMessage
import com.bahilai.gigadanya.network.RetrofitInstance
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel для управления состоянием чата
 */
class ChatViewModel : ViewModel() {
    // Список сообщений в чате
    val messages = mutableStateListOf<Message>()
    
    // Состояние загрузки
    val isLoading = mutableStateOf(false)
    
    // Состояние ошибки
    val errorMessage = mutableStateOf<String?>(null)
    
    // История сообщений для контекста API
    private val conversationHistory = mutableListOf<YandexMessage>()
    
    init {
        // Добавляем приветственное сообщение от бота
        addBotMessage("Привет! Я GigaDanya, твой личный бешеный мопед. Чем могу помочь?")
    }
    
    /**
     * Отправка сообщения пользователя
     */
    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank() || isLoading.value) return
        
        // Добавляем сообщение пользователя
        val userMsg = Message(
            id = UUID.randomUUID().toString(),
            text = userMessage,
            isFromUser = true
        )
        messages.add(userMsg)
        
        // Добавляем в историю для контекста
        conversationHistory.add(YandexMessage(role = "user", text = userMessage))
        
        // Отправляем запрос к API
        fetchBotResponse()
    }
    
    /**
     * Получение ответа от Yandex GPT
     */
    private fun fetchBotResponse() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            
            try {
                val modelUri = "agent://${RetrofitInstance.folderId}/${RetrofitInstance.agentId}"
                Log.d("ChatViewModel", "Model URI: $modelUri")
                Log.d("ChatViewModel", "API Key exists: ${RetrofitInstance.apiKey.isNotEmpty()}")
                Log.d("ChatViewModel", "Folder ID: ${RetrofitInstance.folderId}")
                Log.d("ChatViewModel", "Agent ID: ${RetrofitInstance.agentId}")
                
                val request = YandexGptRequest(
                    modelUri = modelUri,
                    completionOptions = CompletionOptions(
                        stream = false,
                        temperature = 0.6,
                        maxTokens = 2000
                    ),
                    messages = conversationHistory.toList()
                )
                
                val response = RetrofitInstance.api.sendMessage(
                    authorization = RetrofitInstance.apiKey,
                    folderId = RetrofitInstance.folderId,
                    request = request
                )
                
                // Получаем текст ответа
                val botText = response.result.alternatives.firstOrNull()?.message?.text
                
                if (botText != null) {
                    // Добавляем ответ в историю
                    conversationHistory.add(YandexMessage(role = "assistant", text = botText))
                    
                    // Проверяем, содержит ли ответ URL изображения
                    val imageUrl = extractImageUrl(botText)
                    
                    if (imageUrl != null) {
                        // Если есть изображение, создаем сообщение с изображением
                        val textWithoutUrl = botText.replace(imageUrl, "").trim()
                        
                        if (textWithoutUrl.isNotEmpty()) {
                            addBotMessage(textWithoutUrl)
                        }
                        
                        addBotImage(imageUrl)
                    } else {
                        // Обычное текстовое сообщение
                        addBotMessage(botText)
                    }
                } else {
                    errorMessage.value = "Не удалось получить ответ от бота"
                }
                
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("failed to connect") == true -> 
                        "Ошибка подключения. Проверьте интернет-соединение"
                    e.message?.contains("timeout") == true -> 
                        "Превышено время ожидания. Попробуйте еще раз"
                    e.message?.contains("401") == true || e.message?.contains("403") == true -> 
                        "Ошибка авторизации. Проверьте API ключи"
                    else -> "Ошибка: ${e.localizedMessage ?: e.message}"
                }
                errorMessage.value = errorMsg
                Log.e("ChatViewModel", "Error details: ", e)
            } finally {
                isLoading.value = false
            }
        }
    }
    
    /**
     * Извлечение URL изображения из текста
     * Поддерживаются форматы: http://, https://
     */
    private fun extractImageUrl(text: String): String? {
        val urlPattern = Regex("https?://[^\\s]+\\.(jpg|jpeg|png|gif|webp)", RegexOption.IGNORE_CASE)
        return urlPattern.find(text)?.value
    }
    
    /**
     * Добавление текстового сообщения от бота
     */
    private fun addBotMessage(text: String) {
        val botMsg = Message(
            id = UUID.randomUUID().toString(),
            text = text,
            isFromUser = false
        )
        messages.add(botMsg)
    }
    
    /**
     * Добавление изображения от бота
     */
    private fun addBotImage(imageUrl: String) {
        val imageMsg = Message(
            id = UUID.randomUUID().toString(),
            imageUrl = imageUrl,
            isFromUser = false
        )
        messages.add(imageMsg)
    }
    
    /**
     * Очистка истории чата
     */
    fun clearChat() {
        messages.clear()
        conversationHistory.clear()
        addBotMessage("Чат очищен. Чем могу помочь?")
    }
}

