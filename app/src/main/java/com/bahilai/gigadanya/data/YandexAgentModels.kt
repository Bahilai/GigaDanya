package com.bahilai.gigadanya.data

import com.google.gson.annotations.SerializedName

/**
 * Модели данных для взаимодействия с Yandex AI Studio Agent API
 */

// Запрос к Agent API
data class AgentRequest(
    @SerializedName("prompt")
    val prompt: PromptConfig,
    
    @SerializedName("input")
    val input: String,
    
    @SerializedName("stream")
    val stream: Boolean = false
)

data class PromptConfig(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("variables")
    val variables: Map<String, String>? = null
)

// Ответ от Agent API
data class AgentResponse(
    @SerializedName("output")
    val output: List<OutputContent>? = null,
    
    @SerializedName("status")
    val status: String? = null,
    
    @SerializedName("error")
    val error: ErrorInfo? = null,
    
    @SerializedName("result")
    val result: AgentResult? = null
)

data class OutputContent(
    @SerializedName("content")
    val content: List<ContentItem>?
)

data class ContentItem(
    @SerializedName("text")
    val text: String?,
    
    @SerializedName("type")
    val type: String?
)

data class ErrorInfo(
    @SerializedName("message")
    val message: String?,
    
    @SerializedName("code")
    val code: String?
)

data class AgentResult(
    @SerializedName("alternatives")
    val alternatives: List<Alternative>? = null,
    
    @SerializedName("usage")
    val usage: Usage? = null,
    
    @SerializedName("modelVersion")
    val modelVersion: String? = null
)

data class Alternative(
    @SerializedName("message")
    val message: AlternativeMessage? = null,
    
    @SerializedName("status")
    val status: String? = null
)

data class AlternativeMessage(
    @SerializedName("role")
    val role: String?,
    
    @SerializedName("text")
    val text: String?
)

data class Usage(
    @SerializedName("inputTextTokens")
    val inputTextTokens: String?,
    
    @SerializedName("completionTokens")
    val completionTokens: String?,
    
    @SerializedName("totalTokens")
    val totalTokens: String?
)

