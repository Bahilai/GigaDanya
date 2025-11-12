package com.bahilai.gigadanya.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bahilai.gigadanya.data.AgentInfo
import com.bahilai.gigadanya.data.AgentRequest
import com.bahilai.gigadanya.data.AgentStatistics
import com.bahilai.gigadanya.data.CompletionOptions
import com.bahilai.gigadanya.data.EconomicAgents
import com.bahilai.gigadanya.data.Message
import com.bahilai.gigadanya.data.ModelPricingTable
import com.bahilai.gigadanya.data.PromptConfig
import com.bahilai.gigadanya.data.TotalStatistics
import com.bahilai.gigadanya.data.YandexGptRequest
import com.bahilai.gigadanya.data.YandexMessage
import com.bahilai.gigadanya.network.RetrofitInstance
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.system.measureTimeMillis

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
    
    // Статистика по последнему запросу
    val statistics = mutableStateOf<TotalStatistics?>(null)
    
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
            statistics.value = null
            
            val agentStatsList = mutableListOf<AgentStatistics>()
            var totalResponseTime = 0L
            
            try {
                // Формируем входное сообщение из истории разговора
                val inputText = conversationHistory.lastOrNull()?.text ?: ""
                
                // Последовательно опрашиваем всех агентов
                for (agent in EconomicAgents.ALL_AGENTS) {
                    try {
                        var responseTime = 0L
                        var inputTokens = 0
                        var outputTokens = 0
                        var totalTokens = 0
                        
                        // Замеряем время ответа
                        val response = measureTimeMillis {
                            val apiResponse = fetchResponseFromAgent(agent, inputText)
                            
                            // Получаем текст ответа
                            val botText = apiResponse.output?.firstOrNull()?.content?.firstOrNull()?.text ?: ""
                            
                            // Получаем информацию о токенах из API
                            val apiInputTokens = apiResponse.usage?.inputTextTokens ?: 0
                            val apiOutputTokens = apiResponse.usage?.completionTokens ?: 0
                            val apiTotalTokens = apiResponse.usage?.totalTokens ?: 0
                            
                            // ВАЖНО: Yandex AI Studio Agent API возвращает 0 токенов!
                            // Используем Tokenize API для точного подсчета, если Agent API не предоставляет данные
                            if (apiTotalTokens == 0 && (apiInputTokens == 0 || apiOutputTokens == 0)) {
                                // Точный подсчет через Tokenize API
                                // Определяем modelType для токенизации на основе модели агента
                                val modelTypeForTokenize = when {
                                    agent.modelType.contains("yandexgpt", ignoreCase = true) -> agent.modelType
                                    else -> "yandexgpt" // По умолчанию используем yandexgpt для всех моделей
                                }
                                
                                inputTokens = getTokenCount(inputText, modelTypeForTokenize)
                                outputTokens = getTokenCount(botText, modelTypeForTokenize)
                                totalTokens = inputTokens + outputTokens
                                
                                android.util.Log.d("ChatViewModel", "${agent.name}: Tokenize API - in: $inputTokens, out: $outputTokens, total: $totalTokens")
                            } else {
                                // Используем данные от Agent API (если они есть)
                                inputTokens = apiInputTokens
                                outputTokens = apiOutputTokens
                                totalTokens = apiTotalTokens
                                
                                android.util.Log.d("ChatViewModel", "${agent.name}: Agent API tokens - in: $inputTokens, out: $outputTokens, total: $totalTokens")
                            }
                            
                            // Обрабатываем текст ответа
                            if (botText.isNotEmpty()) {
                                processAgentResponse(agent, botText)
                            }
                        }
                        
                        responseTime = response
                        totalResponseTime += responseTime
                        
                        // Вычисляем стоимость
                        val pricing = ModelPricingTable.getPricingForModel(agent.modelType)
                        val cost = if (pricing != null) {
                            (inputTokens / 1000.0 * pricing.inputPricePer1kTokens) +
                            (outputTokens / 1000.0 * pricing.outputPricePer1kTokens)
                        } else {
                            0.0
                        }
                        
                        // Сохраняем статистику агента
                        agentStatsList.add(
                            AgentStatistics(
                                agentInfo = agent,
                                responseTime = responseTime,
                                inputTokens = inputTokens,
                                outputTokens = outputTokens,
                                totalTokens = totalTokens,
                                cost = cost
                            )
                        )
                        
                    } catch (e: Exception) {
                        // Если один агент упал, продолжаем работу с остальными
                        addBotMessage(
                            text = "Ошибка получения ответа: ${e.localizedMessage}",
                            agentName = agent.name
                        )
                        e.printStackTrace()
                    }
                }
                
                // Создаем итоговую статистику
                if (agentStatsList.isNotEmpty()) {
                    val totalTokens = agentStatsList.sumOf { it.totalTokens }
                    val totalCost = agentStatsList.sumOf { it.cost }
                    
                    statistics.value = TotalStatistics(
                        agentStats = agentStatsList,
                        totalResponseTime = totalResponseTime,
                        totalTokens = totalTokens,
                        totalCost = totalCost
                    )
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
        statistics.value = null
        addBotMessage("Чат очищен. Чем могу помочь?")
    }
    
    /**
     * Точный подсчет количества токенов в тексте через Tokenize API
     * 
     * Использует официальный Yandex Cloud Tokenize API для получения
     * точного количества токенов для указанного текста и модели.
     * 
     * Документация: https://yandex.cloud/ru/docs/foundation-models/operations/yandexgpt/evaluate-request
     */
    private suspend fun getTokenCount(text: String, modelType: String = "yandexgpt"): Int {
        if (text.isBlank()) return 0
        
        return try {
            // Формируем modelUri для токенизации
            val modelUri = "gpt://${RetrofitInstance.folderId}/$modelType/latest"
            
            val request = com.bahilai.gigadanya.data.TokenizeRequest(
                modelUri = modelUri,
                text = text
            )
            
            val response = RetrofitInstance.api.tokenize(
                authorization = RetrofitInstance.apiKey,
                folderId = RetrofitInstance.folderId,
                request = request
            )
            
            // Возвращаем количество токенов
            val tokenCount = response.tokens.size
            android.util.Log.d("ChatViewModel", "✓ Tokenize API: $tokenCount tokens for text length ${text.length}")
            tokenCount
            
        } catch (e: Exception) {
            // В случае ошибки используем приблизительную оценку
            android.util.Log.w("ChatViewModel", "⚠️ Tokenize API failed: ${e.message}. Using estimation.")
            estimateTokenCount(text)
        }
    }
    
    /**
     * Приблизительная оценка количества токенов (fallback)
     * 
     * Используется только если Tokenize API недоступен.
     */
    private fun estimateTokenCount(text: String): Int {
        if (text.isBlank()) return 0
        
        // Считаем символы (без пробелов)
        val charCount = text.replace("\\s+".toRegex(), "").length
        
        // Приблизительная формула: 1 токен ≈ 3.5 символа (среднее для русского и английского)
        // Добавляем небольшой запас (10%), чтобы не занижать оценку
        val estimatedTokens = (charCount / 3.5 * 1.1).toInt()
        
        // Минимум 1 токен для непустого текста
        return maxOf(1, estimatedTokens)
    }
}

