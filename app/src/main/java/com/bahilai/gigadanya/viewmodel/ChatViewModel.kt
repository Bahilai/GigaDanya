package com.bahilai.gigadanya.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bahilai.gigadanya.data.AgentRequest
import com.bahilai.gigadanya.data.CompletionOptions
import com.bahilai.gigadanya.data.Message
import com.bahilai.gigadanya.data.PromptConfig
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
    
    // Формат ответа (TEXT или JSON)
    val responseFormat = mutableStateOf(com.bahilai.gigadanya.data.ResponseFormat.TEXT)
    
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
     * Получение ответа от AI Studio Agent или YandexGPT API
     */
    private fun fetchBotResponse() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            
            try {
                val botText = when (responseFormat.value) {
                    com.bahilai.gigadanya.data.ResponseFormat.TEXT -> {
                        // Используем Agent API для текстового формата
                        fetchAgentResponse()
                    }
                    com.bahilai.gigadanya.data.ResponseFormat.JSON -> {
                        // Используем прямой YandexGPT API для JSON формата
                        fetchGptJsonResponse()
                    }
                }
                
                if (botText != null && botText.isNotEmpty()) {
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
                    errorMessage.value = "Не удалось получить ответ"
                }
                
            } catch (e: Exception) {
                errorMessage.value = "Ошибка: ${e.localizedMessage}"
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }
    
    /**
     * Получение ответа от Agent API (текстовый формат)
     * С fallback на YandexGPT API при ошибке
     */
    private suspend fun fetchAgentResponse(): String? {
        return try {
            val inputText = conversationHistory.lastOrNull()?.text ?: ""
            
            val request = AgentRequest(
                prompt = PromptConfig(
                    id = RetrofitInstance.agentId,
                    variables = null
                ),
                input = inputText,
                stream = false
            )
            
            val response = RetrofitInstance.agentApi.sendMessage(
                authorization = RetrofitInstance.apiKey,
                folderId = RetrofitInstance.folderId,
                request = request
            )
            
            if (response.error != null) {
                errorMessage.value = "Ошибка агента: ${response.error.message}. Используем YandexGPT API."
                // Fallback на обычный API
                return fetchGptTextResponse()
            }
            
            response.output?.firstOrNull()?.content?.firstOrNull()?.text
        } catch (e: Exception) {
            // Если Agent API недоступен, используем обычный YandexGPT API
            errorMessage.value = "Agent API недоступен. Используем YandexGPT API."
            fetchGptTextResponse()
        }
    }
    
    /**
     * Получение ответа от YandexGPT API в текстовом режиме (fallback)
     */
    private suspend fun fetchGptTextResponse(): String? {
        val messagesForText = mutableListOf<YandexMessage>()
        
        messagesForText.add(
            YandexMessage(
                role = "system",
                text = "Ты - умный ассистент."
            )
        )
        
        messagesForText.addAll(conversationHistory)
        
        val request = YandexGptRequest(
            modelUri = "gpt://${RetrofitInstance.folderId}/yandexgpt/latest",
            completionOptions = CompletionOptions(
                stream = false,
                temperature = 0.6,
                maxTokens = 2000
            ),
            messages = messagesForText,
            jsonObject = null
        )
        
        val response = RetrofitInstance.api.sendMessage(
            authorization = RetrofitInstance.apiKey,
            folderId = RetrofitInstance.folderId,
            request = request
        )
        
        return response.result.alternatives.firstOrNull()?.message?.text
    }
    
    /**
     * Получение ответа от YandexGPT API (JSON формат)
     * Возвращает чистый отформатированный JSON
     */
    private suspend fun fetchGptJsonResponse(): String? {
        // Создаем список сообщений для JSON режима
        val messagesForJson = mutableListOf<YandexMessage>()
        
        // Добавляем системное сообщение для JSON формата
        messagesForJson.add(
            YandexMessage(
                role = "system",
                text = "Ты - умный ассистент."
            )
        )
        
        // Добавляем историю разговора
        messagesForJson.addAll(conversationHistory)
        
        val request = YandexGptRequest(
            modelUri = "gpt://${RetrofitInstance.folderId}/yandexgpt/latest",
            completionOptions = CompletionOptions(
                stream = false,
                temperature = 0.6,
                maxTokens = 2000
            ),
            messages = messagesForJson,
            jsonObject = true
        )
        
        val response = RetrofitInstance.api.sendMessage(
            authorization = RetrofitInstance.apiKey,
            folderId = RetrofitInstance.folderId,
            request = request
        )
        
        val rawText = response.result.alternatives.firstOrNull()?.message?.text
        
        // В JSON режиме возвращаем чистый отформатированный JSON
        return rawText?.let { formatJson(it) }
    }
    
    /**
     * Форматирование JSON для красивого отображения
     * Убирает escape-последовательности
     */
    private fun formatJson(jsonText: String): String {
        return jsonText
            .replace("\\n", "\n")
            .replace("\\\"", "\"")
            .replace("\\t", "  ")
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
    
    /**
     * Переключение формата ответа
     */
    fun toggleResponseFormat() {
        responseFormat.value = when (responseFormat.value) {
            com.bahilai.gigadanya.data.ResponseFormat.TEXT -> com.bahilai.gigadanya.data.ResponseFormat.JSON
            com.bahilai.gigadanya.data.ResponseFormat.JSON -> com.bahilai.gigadanya.data.ResponseFormat.TEXT
        }
    }
}

