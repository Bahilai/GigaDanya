package com.bahilai.gigadanya.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bahilai.gigadanya.data.AgentInfo
import com.bahilai.gigadanya.data.AgentRequest
import com.bahilai.gigadanya.data.EconomicAgents
import com.bahilai.gigadanya.data.Message
import com.bahilai.gigadanya.data.PromptConfig
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
        addBotMessage("Привет! Чем могу помочь?")
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
     * Получение ответа от AI агента
     */
    private fun fetchBotResponse() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            
            try {
                // Формируем входное сообщение из истории разговора
                val inputText = conversationHistory.lastOrNull()?.text ?: ""
                
                // Получаем единственного агента
                val agent = EconomicAgents.DEFAULT_AGENT
                
                // Запрашиваем ответ от агента
                val apiResponse = fetchResponseFromAgent(agent, inputText)
                
                // Получаем текст ответа
                val botText = apiResponse.output?.firstOrNull()?.content?.firstOrNull()?.text ?: ""
                
                // Обрабатываем текст ответа
                if (botText.isNotEmpty()) {
                    processAgentResponse(botText)
                } else {
                    errorMessage.value = "Получен пустой ответ от агента"
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
     * Запрос ответа от конкретного агента
     */
    private suspend fun fetchResponseFromAgent(agent: AgentInfo, inputText: String): com.bahilai.gigadanya.data.AgentResponse {
        val request = AgentRequest(
            prompt = PromptConfig(
                id = agent.id,
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
        
        // Обрабатываем ошибки
        if (response.error != null) {
            throw Exception(response.error.message)
        }
        
        return response
    }
    
    /**
     * Обработка ответа от агента
     */
    private fun processAgentResponse(botText: String) {
        if (botText.isEmpty()) {
            return
        }
        
        // Добавляем ответ в историю для контекста
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

