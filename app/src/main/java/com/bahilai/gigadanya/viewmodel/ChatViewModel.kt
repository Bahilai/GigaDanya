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
import org.json.JSONArray
import org.json.JSONObject

/**
 * Data class для хранения JSON ответа
 */
data class JsonResponse(
    val rawJson: String,
    val formattedText: String
)

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
                when (responseFormat.value) {
                    com.bahilai.gigadanya.data.ResponseFormat.TEXT -> {
                        // Используем Agent API для текстового формата
                        val botText = fetchAgentResponse()
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
                    }
                    com.bahilai.gigadanya.data.ResponseFormat.JSON -> {
                        // Используем прямой YandexGPT API для JSON формата
                        val jsonResponse = fetchGptJsonResponse()
                        if (jsonResponse != null) {
                            // Добавляем форматированный текст в историю
                            conversationHistory.add(YandexMessage(role = "assistant", text = jsonResponse.formattedText))
                            
                            // Добавляем сообщение с JSON
                            addBotJsonMessage(jsonResponse.formattedText, jsonResponse.rawJson)
                        } else {
                            errorMessage.value = "Не удалось получить ответ"
                        }
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
     * Получение ответа от Agent API (текстовый формат)
     */
    private suspend fun fetchAgentResponse(): String? {
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
            errorMessage.value = "Ошибка агента: ${response.error.message}"
            return null
        }
        
        return response.output?.firstOrNull()?.content?.firstOrNull()?.text
    }
    
    /**
     * Получение ответа от YandexGPT API (JSON формат)
     */
    private suspend fun fetchGptJsonResponse(): JsonResponse? {
        // Создаем копию истории с системным промптом для JSON формата
        val messagesWithJsonPrompt = mutableListOf<YandexMessage>()
        
        // Добавляем системное сообщение для JSON формата
        messagesWithJsonPrompt.add(
            YandexMessage(
                role = "system",
                text = "Отвечай в формате JSON. Представь результат в виде объекта JSON. Не используй разметку Markdown!"
            )
        )
        
        // Добавляем историю разговора
        messagesWithJsonPrompt.addAll(conversationHistory)
        
        val request = YandexGptRequest(
            modelUri = "gpt://${RetrofitInstance.folderId}/yandexgpt/latest",
            completionOptions = CompletionOptions(
                stream = false,
                temperature = 0.6,
                maxTokens = 2000
            ),
            messages = messagesWithJsonPrompt,
            jsonObject = true
        )
        
        val response = RetrofitInstance.api.sendMessage(
            authorization = RetrofitInstance.apiKey,
            folderId = RetrofitInstance.folderId,
            request = request
        )
        
        val rawText = response.result.alternatives.firstOrNull()?.message?.text
        
        // Если формат JSON, преобразуем в JsonResponse
        return if (rawText != null) {
            val formattedText = convertJsonToText(rawText)
            JsonResponse(
                rawJson = formatJsonForDisplay(rawText),
                formattedText = formattedText
            )
        } else {
            null
        }
    }
    
    /**
     * Форматирование JSON для красивого отображения
     */
    private fun formatJsonForDisplay(jsonText: String): String {
        return try {
            val cleanJson = jsonText.trim()
            
            // Пытаемся парсить как объект или массив
            val jsonObject = if (cleanJson.startsWith("{")) {
                JSONObject(cleanJson)
            } else if (cleanJson.startsWith("[")) {
                JSONArray(cleanJson).toString(2)
                return JSONArray(cleanJson).toString(2)
            } else {
                return cleanJson
            }
            
            jsonObject.toString(2)
        } catch (e: Exception) {
            // Если не удается парсить, возвращаем исходный текст
            jsonText
        }
    }
    
    /**
     * Преобразование JSON в читаемый текст
     */
    private fun convertJsonToText(jsonText: String): String {
        return try {
            val cleanJson = jsonText.trim()
            
            // Пытаемся парсить JSON
            if (cleanJson.startsWith("{")) {
                val jsonObject = JSONObject(cleanJson)
                buildString {
                    appendLine("📋 JSON Ответ:")
                    appendLine()
                    parseJsonObject(jsonObject, this, 0)
                }
            } else if (cleanJson.startsWith("[")) {
                val jsonArray = JSONArray(cleanJson)
                buildString {
                    appendLine("📋 JSON Ответ (Массив):")
                    appendLine()
                    parseJsonArray(jsonArray, this, 0)
                }
            } else {
                // Не JSON, возвращаем как есть
                cleanJson
            }
        } catch (e: Exception) {
            // Если не удается парсить, возвращаем исходный текст
            jsonText
        }
    }
    
    /**
     * Рекурсивный парсинг JSON объекта
     */
    private fun parseJsonObject(jsonObject: JSONObject, builder: StringBuilder, indent: Int) {
        val indentStr = "  ".repeat(indent)
        
        jsonObject.keys().forEach { key ->
            val value = jsonObject.get(key)
            
            when (value) {
                is JSONObject -> {
                    builder.appendLine("$indentStr• $key:")
                    parseJsonObject(value, builder, indent + 1)
                }
                is JSONArray -> {
                    builder.appendLine("$indentStr• $key:")
                    parseJsonArray(value, builder, indent + 1)
                }
                else -> {
                    builder.appendLine("$indentStr• $key: $value")
                }
            }
        }
    }
    
    /**
     * Рекурсивный парсинг JSON массива
     */
    private fun parseJsonArray(jsonArray: JSONArray, builder: StringBuilder, indent: Int) {
        val indentStr = "  ".repeat(indent)
        
        for (i in 0 until jsonArray.length()) {
            val value = jsonArray.get(i)
            
            when (value) {
                is JSONObject -> {
                    builder.appendLine("$indentStr${i + 1}.")
                    parseJsonObject(value, builder, indent + 1)
                }
                is JSONArray -> {
                    builder.appendLine("$indentStr${i + 1}.")
                    parseJsonArray(value, builder, indent + 1)
                }
                else -> {
                    builder.appendLine("$indentStr${i + 1}. $value")
                }
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
     * Добавление JSON сообщения от бота
     */
    private fun addBotJsonMessage(formattedText: String, rawJson: String) {
        // Добавляем форматированный текст
        val textMsg = Message(
            id = UUID.randomUUID().toString(),
            text = formattedText,
            isFromUser = false,
            rawJson = null
        )
        messages.add(textMsg)
        
        // Добавляем сырой JSON в отдельном сообщении
        val jsonMsg = Message(
            id = UUID.randomUUID().toString(),
            text = "🔍 Полный JSON ответ:\n\n$rawJson",
            isFromUser = false,
            rawJson = rawJson
        )
        messages.add(jsonMsg)
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

