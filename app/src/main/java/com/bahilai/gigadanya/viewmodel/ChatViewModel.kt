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
    
    // Формат ответа: true = JSON, false = Text
    val isJsonFormat = mutableStateOf(false)
    
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
     * Получение ответа от AI Studio Agent
     */
    private fun fetchBotResponse() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            
            try {
                // Формируем входное сообщение из истории разговора
                val inputText = conversationHistory.lastOrNull()?.text ?: ""
                
                // Логирование для отладки
                android.util.Log.d("ChatViewModel", "Sending message to agent: $inputText")
                android.util.Log.d("ChatViewModel", "Agent ID: ${RetrofitInstance.agentId}")
                android.util.Log.d("ChatViewModel", "Folder ID: ${RetrofitInstance.folderId}")
                android.util.Log.d("ChatViewModel", "API Key length: ${RetrofitInstance.apiKey.length}")
                
                val request = AgentRequest(
                    prompt = PromptConfig(
                        id = RetrofitInstance.agentId,
                        variables = null
                    ),
                    input = inputText,
                    stream = false
                )
                
                android.util.Log.d("ChatViewModel", "Request prepared: $request")
                
                val response = RetrofitInstance.agentApi.sendMessage(
                    authorization = RetrofitInstance.apiKey,
                    folderId = RetrofitInstance.folderId,
                    request = request
                )
                
                android.util.Log.d("ChatViewModel", "Response received: $response")
                
                // Обрабатываем ответ агента
                if (response.error != null) {
                    errorMessage.value = "Ошибка агента: ${response.error.message} (код: ${response.error.code})"
                    android.util.Log.e("ChatViewModel", "API Error: ${response.error.message}, code: ${response.error.code}")
                    return@launch
                }
                
                // Пытаемся извлечь текст из разных возможных форматов ответа
                val botText = extractBotText(response)
                
                if (botText.isNullOrEmpty()) {
                    android.util.Log.e("ChatViewModel", "No text found in response: $response")
                    errorMessage.value = "Не удалось получить ответ от агента. Ответ пуст."
                    return@launch
                }
                
                android.util.Log.d("ChatViewModel", "Extracted bot text: $botText")
                
                if (isJsonFormat.value) {
                    // JSON формат - отображаем полный JSON ответ
                    val jsonResponse = com.google.gson.GsonBuilder()
                        .setPrettyPrinting()
                        .create()
                        .toJson(response)
                    
                    // Добавляем текст в историю контекста
                    conversationHistory.add(YandexMessage(role = "assistant", text = botText))
                    
                    addBotMessage(jsonResponse)
                } else {
                    // Текстовый формат - извлекаем только текст из ответа
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
                }
                
            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "Exception occurred", e)
                
                val errorMsg = when {
                    e is java.net.SocketTimeoutException -> 
                        "Превышено время ожидания. Проверьте интернет-соединение."
                    e is java.net.UnknownHostException -> 
                        "Не удалось подключиться к серверу. Проверьте интернет-соединение."
                    e is java.net.ConnectException -> 
                        "Ошибка подключения. Проверьте интернет-соединение и попробуйте позже."
                    e is retrofit2.HttpException -> {
                        val code = e.code()
                        val errorBody = e.response()?.errorBody()?.string()
                        android.util.Log.e("ChatViewModel", "HTTP Error $code: $errorBody")
                        "Ошибка HTTP $code: ${errorBody ?: e.message()}"
                    }
                    e.message?.contains("Failed to connect") == true ->
                        "Не удалось подключиться к серверу. Проверьте интернет и попробуйте позже."
                    else -> {
                        android.util.Log.e("ChatViewModel", "Unknown error: ${e.message}", e)
                        "Ошибка: ${e.localizedMessage ?: e.message ?: "Неизвестная ошибка"}"
                    }
                }
                errorMessage.value = errorMsg
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }
    
    /**
     * Извлечение текста бота из различных форматов ответа
     */
    private fun extractBotText(response: com.bahilai.gigadanya.data.AgentResponse): String? {
        // Пытаемся извлечь из output (формат AI Studio Agent)
        response.output?.firstOrNull()?.content?.firstOrNull()?.text?.let {
            if (it.isNotEmpty()) return it
        }
        
        // Пытаемся извлечь из result.alternatives (формат Foundation Models)
        response.result?.alternatives?.firstOrNull()?.message?.text?.let {
            if (it.isNotEmpty()) return it
        }
        
        return null
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
     * Переключение формата ответа
     */
    fun toggleFormat() {
        isJsonFormat.value = !isJsonFormat.value
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

