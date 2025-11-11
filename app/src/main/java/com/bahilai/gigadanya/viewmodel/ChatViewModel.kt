package com.bahilai.gigadanya.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bahilai.gigadanya.data.AgentInfo
import com.bahilai.gigadanya.data.AgentRequest
import com.bahilai.gigadanya.data.CompletionOptions
import com.bahilai.gigadanya.data.EconomicAgents
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
    
    // История сообщений для контекста API
    private val conversationHistory = mutableListOf<YandexMessage>()
    
    init {
        // Добавляем приветственное сообщение от бота
        addBotMessage("Привет! Задай вопрос для 5 разных экспертов от самого предсказуемого до неординарного")
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
     * Получение ответов от всех 5 AI Studio Agents
     */
    private fun fetchBotResponse() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            
            try {
                // Формируем входное сообщение из истории разговора
                val inputText = conversationHistory.lastOrNull()?.text ?: ""
                
                // Последовательно опрашиваем всех агентов
                for (agent in EconomicAgents.ALL_AGENTS) {
                    try {
                        val response = fetchResponseFromAgent(agent, inputText)
                        if (response != null) {
                            processAgentResponse(agent, response)
                        }
                    } catch (e: Exception) {
                        // Если один агент упал, продолжаем работу с остальными
                        addBotMessage(
                            text = "Ошибка получения ответа: ${e.localizedMessage}",
                            agentName = agent.name
                        )
                        e.printStackTrace()
                    }
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
    private suspend fun fetchResponseFromAgent(agent: AgentInfo, inputText: String): String? {
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
        
        // Обрабатываем ответ агента
        if (response.error != null) {
            throw Exception(response.error.message)
        }
        
        // Получаем текст ответа из структуры агента
        return response.output?.firstOrNull()?.content?.firstOrNull()?.text
    }
    
    /**
     * Обработка ответа от агента
     */
    private fun processAgentResponse(agent: AgentInfo, botText: String) {
        if (botText.isEmpty()) {
            return
        }
        
        // Проверяем, содержит ли ответ URL изображения
        val imageUrl = extractImageUrl(botText)
        
        if (imageUrl != null) {
            // Если есть изображение, создаем сообщение с изображением
            val textWithoutUrl = botText.replace(imageUrl, "").trim()
            
            if (textWithoutUrl.isNotEmpty()) {
                addBotMessage(textWithoutUrl, agent.name)
            }
            
            addBotImage(imageUrl, agent.name)
        } else {
            // Обычное текстовое сообщение
            addBotMessage(botText, agent.name)
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
    private fun addBotMessage(text: String, agentName: String? = null) {
        val botMsg = Message(
            id = UUID.randomUUID().toString(),
            text = text,
            isFromUser = false,
            agentName = agentName
        )
        messages.add(botMsg)
    }
    
    /**
     * Добавление изображения от бота
     */
    private fun addBotImage(imageUrl: String, agentName: String? = null) {
        val imageMsg = Message(
            id = UUID.randomUUID().toString(),
            imageUrl = imageUrl,
            isFromUser = false,
            agentName = agentName
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

