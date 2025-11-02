package com.bahilai.gigadanya.data

import com.google.gson.annotations.SerializedName

/**
 * Модели данных для взаимодействия с Yandex GPT API
 */

// Запрос к API
data class YandexGptRequest(
    @SerializedName("modelUri")
    val modelUri: String,
    
    @SerializedName("completionOptions")
    val completionOptions: CompletionOptions,
    
    @SerializedName("messages")
    val messages: List<YandexMessage>
)

data class CompletionOptions(
    @SerializedName("stream")
    val stream: Boolean = false,
    
    @SerializedName("temperature")
    val temperature: Double = 0.6,
    
    @SerializedName("maxTokens")
    val maxTokens: Int = 2000
)

data class YandexMessage(
    @SerializedName("role")
    val role: String, // "system", "user", "assistant"
    
    @SerializedName("text")
    val text: String
)

// Ответ от API
data class YandexGptResponse(
    @SerializedName("result")
    val result: Result
)

data class Result(
    @SerializedName("alternatives")
    val alternatives: List<Alternative>,
    
    @SerializedName("usage")
    val usage: Usage,
    
    @SerializedName("modelVersion")
    val modelVersion: String
)

data class Alternative(
    @SerializedName("message")
    val message: YandexMessage,
    
    @SerializedName("status")
    val status: String
)

data class Usage(
    @SerializedName("inputTextTokens")
    val inputTextTokens: Int,
    
    @SerializedName("completionTokens")
    val completionTokens: Int,
    
    @SerializedName("totalTokens")
    val totalTokens: Int
)

