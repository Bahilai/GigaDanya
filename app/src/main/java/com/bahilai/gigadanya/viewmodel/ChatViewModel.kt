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
    // Порог для сжатия истории (количество сообщений перед сжатием)
    private val COMPRESSION_THRESHOLD = 10
    
    // Список сообщений в чате
    val messages = mutableStateListOf<Message>()
    
    // Состояние загрузки
    val isLoading = mutableStateOf(false)
    
    // Состояние ошибки
    val errorMessage = mutableStateOf<String?>(null)
    
    // История сообщений для контекста API
    private val conversationHistory = mutableListOf<YandexMessage>()
    
    // Статистика по токенам
    val tokenStatistics = mutableStateOf<TokenStats?>(null)
    
    data class TokenStats(
        val totalInputTokens: Int = 0,
        val totalOutputTokens: Int = 0,
        val compressionCount: Int = 0,
        val savedTokens: Int = 0
    )
    
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
                // Формируем полный контекст разговора из всей истории
                val inputText = buildConversationContext()
                
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
     * Формирование полного контекста разговора из истории
     * Объединяет все сообщения в один текст для передачи в API
     */
    private fun buildConversationContext(): String {
        if (conversationHistory.isEmpty()) {
            return ""
        }
        
        // Формируем контекст, объединяя все сообщения из истории
        // Каждое сообщение добавляется с указанием роли для лучшего понимания контекста
        val context = conversationHistory.joinToString(separator = "\n\n") { message ->
            when (message.role) {
                "user" -> "Пользователь: ${message.text}"
                "assistant" -> "Ассистент: ${message.text}"
                else -> message.text
            }
        }
        
        // Логируем для отладки (только в debug режиме)
        android.util.Log.d("ChatViewModel", "Контекст разговора (${conversationHistory.size} сообщений):\n$context")
        
        return context
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
        
        // Обновляем статистику токенов (если доступна)
        response.usage?.let { usage ->
            val currentStats = tokenStatistics.value ?: TokenStats()
            val inputTokens = usage.inputTextTokens ?: 0
            val outputTokens = usage.completionTokens ?: 0
            
            tokenStatistics.value = currentStats.copy(
                totalInputTokens = currentStats.totalInputTokens + inputTokens,
                totalOutputTokens = currentStats.totalOutputTokens + outputTokens
            )
            
            android.util.Log.d("ChatViewModel", "Токены: входные=$inputTokens, выходные=$outputTokens, всего=${usage.totalTokens ?: 0}")
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
        
        // Проверяем, нужно ли сжимать историю
        if (shouldCompressHistory()) {
            viewModelScope.launch {
                compressConversationHistory()
            }
        }
        
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
     * Проверка, нужно ли сжимать историю
     */
    private fun shouldCompressHistory(): Boolean {
        // Сжимаем, если в истории больше порогового количества сообщений
        // И только если последнее сообщение было от ассистента (чтобы не сжимать в середине диалога)
        val lastMessage = conversationHistory.lastOrNull()
        return conversationHistory.size >= COMPRESSION_THRESHOLD && 
               lastMessage?.role == "assistant"
    }
    
    /**
     * Сжатие истории разговора через создание summary
     */
    private suspend fun compressConversationHistory() {
        try {
            android.util.Log.d("ChatViewModel", "Начинаем сжатие истории (${conversationHistory.size} сообщений)")
            
            // Берем первые сообщения для сжатия (оставляем последние 4 для контекста)
            // Это гарантирует, что последний обмен (вопрос-ответ) останется в полном виде
            val messagesToCompress = conversationHistory.size - 4
            if (messagesToCompress < 4) return // Нечего сжимать, слишком мало сообщений
            
            val messagesForCompression = conversationHistory.take(messagesToCompress)
            val recentMessages = conversationHistory.takeLast(4)
            
            // Проверяем, нет ли уже summary в истории (чтобы не создавать summary из summary)
            val hasExistingSummary = messagesForCompression.any { it.role == "system" && it.text.startsWith("Резюме предыдущего разговора") }
            if (hasExistingSummary && messagesForCompression.size < 8) {
                // Если уже есть summary и мало сообщений, не сжимаем повторно
                return
            }
            
            // Формируем текст для сжатия
            val conversationText = messagesForCompression.joinToString(separator = "\n\n") { message ->
                when (message.role) {
                    "user" -> "Пользователь: ${message.text}"
                    "assistant" -> "Ассистент: ${message.text}"
                    else -> message.text
                }
            }
            
            // Создаем summary через YandexGPT API
            val summary = createSummary(conversationText)
            
            if (summary.isNotEmpty()) {
                // Подсчитываем экономию токенов (приблизительно)
                val originalTokens = estimateTokens(conversationText)
                val summaryTokens = estimateTokens(summary)
                val savedTokens = originalTokens - summaryTokens
                
                // Заменяем старые сообщения на summary
                conversationHistory.clear()
                conversationHistory.add(YandexMessage(role = "system", text = "Резюме предыдущего разговора: $summary"))
                conversationHistory.addAll(recentMessages)
                
                // Обновляем статистику
                val currentStats = tokenStatistics.value ?: TokenStats()
                tokenStatistics.value = currentStats.copy(
                    compressionCount = currentStats.compressionCount + 1,
                    savedTokens = currentStats.savedTokens + savedTokens
                )
                
                android.util.Log.d("ChatViewModel", "История сжата: ${messagesForCompression.size} сообщений → summary. Сэкономлено токенов: ~$savedTokens")
            }
            
        } catch (e: Exception) {
            android.util.Log.e("ChatViewModel", "Ошибка при сжатии истории: ${e.message}", e)
            // Не прерываем работу приложения, если сжатие не удалось
        }
    }
    
    /**
     * Создание summary разговора через YandexGPT API
     */
    private suspend fun createSummary(conversationText: String): String {
        val modelUri = "gpt://${RetrofitInstance.folderId}/yandexgpt/latest"
        
        val summaryPrompt = """
            Создай краткое резюме следующего диалога, сохраняя ключевую информацию и контекст:
            
            $conversationText
            
            Резюме должно быть кратким, но содержательным, сохраняя важные детали для продолжения разговора.
        """.trimIndent()
        
        val request = YandexGptRequest(
            modelUri = modelUri,
            completionOptions = CompletionOptions(
                stream = false,
                temperature = 0.3, // Низкая температура для более точного summary
                maxTokens = 500 // Ограничиваем размер summary
            ),
            messages = listOf(
                YandexMessage(role = "system", text = "Ты помощник, который создает краткие и информативные резюме диалогов."),
                YandexMessage(role = "user", text = summaryPrompt)
            )
        )
        
        val response = RetrofitInstance.api.sendMessage(
            authorization = RetrofitInstance.apiKey,
            folderId = RetrofitInstance.folderId,
            request = request
        )
        
        val summary = response.result.alternatives.firstOrNull()?.message?.text ?: ""
        
        // Обновляем статистику токенов
        val currentStats = tokenStatistics.value ?: TokenStats()
        val inputTokens = response.result.usage.inputTextTokens
        val outputTokens = response.result.usage.completionTokens
        
        tokenStatistics.value = currentStats.copy(
            totalInputTokens = currentStats.totalInputTokens + inputTokens,
            totalOutputTokens = currentStats.totalOutputTokens + outputTokens
        )
        
        return summary
    }
    
    /**
     * Приблизительная оценка количества токенов
     */
    private fun estimateTokens(text: String): Int {
        if (text.isBlank()) return 0
        // Приблизительная формула: 1 токен ≈ 3.5 символа
        return (text.length / 3.5).toInt()
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
        tokenStatistics.value = null
        addBotMessage("Чат очищен. Чем могу помочь?")
    }
}

