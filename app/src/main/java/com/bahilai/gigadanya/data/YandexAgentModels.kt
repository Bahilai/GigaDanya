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
    val output: List<OutputContent>?,
    
    @SerializedName("status")
    val status: String?,
    
    @SerializedName("error")
    val error: ErrorInfo?,
    
    @SerializedName(value = "usage", alternate = ["usageMetadata", "usage_metadata", "tokenUsage", "token_usage"])
    val usage: UsageInfo?
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

data class UsageInfo(
    // Yandex AI Studio Agent API использует input_tokens (не inputTextTokens!)
    @SerializedName(value = "input_tokens", alternate = ["inputTextTokens", "input_text_tokens", "promptTokens", "prompt_tokens", "inputTokens"])
    val inputTextTokens: Int?,
    
    // Yandex AI Studio Agent API использует output_tokens (не completionTokens!)
    @SerializedName(value = "output_tokens", alternate = ["completionTokens", "completion_tokens", "outputTokens", "generatedTokens", "generated_tokens"])
    val completionTokens: Int?,
    
    // Yandex AI Studio Agent API использует total_tokens (не totalTokens!)
    @SerializedName(value = "total_tokens", alternate = ["totalTokens", "allTokens", "all_tokens"])
    val totalTokens: Int?
)

/**
 * Статистика для отдельного агента
 */
data class AgentStatistics(
    val agentInfo: AgentInfo,
    val responseTime: Long, // в миллисекундах
    val inputTokens: Int,
    val outputTokens: Int,
    val totalTokens: Int,
    val cost: Double // в рублях
)

/**
 * Итоговая статистика по всем агентам
 */
data class TotalStatistics(
    val agentStats: List<AgentStatistics>,
    val totalResponseTime: Long,
    val totalTokens: Int,
    val totalCost: Double
)

