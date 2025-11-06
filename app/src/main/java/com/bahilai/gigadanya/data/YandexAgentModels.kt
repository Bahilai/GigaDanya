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
    val error: ErrorInfo?
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

/**
 * Форматы вывода для AI-агента
 */
enum class ResponseFormat(
    val displayName: String,
    val promptInstruction: String
) {
    PLAIN(
        displayName = "Обычный текст",
        promptInstruction = "Отвечай обычным текстом с разметкой Markdown."
    ),
    
    JSON(
        displayName = "JSON",
        promptInstruction = "Представь результат в форме объекта JSON. Нужны только данные без вводных фраз и объяснений. Не используй разметку Markdown!"
    ),
    
    XML(
        displayName = "XML",
        promptInstruction = "Представь результат в формате XML. Нужны только данные без вводных фраз и объяснений. Не используй разметку Markdown!"
    ),
    
    WITH_EMOJI(
        displayName = "С эмодзи",
        promptInstruction = "Отвечай обычным текстом, но обязательно используй подходящие эмодзи для большей выразительности."
    ),
    
    STRUCTURED_LIST(
        displayName = "Структурированный список",
        promptInstruction = "Представь результат в виде структурированного списка с нумерацией и подпунктами. Используй разметку Markdown."
    ),
    
    TABLE(
        displayName = "Таблица",
        promptInstruction = "Представь результат в виде таблицы в формате Markdown, если это возможно."
    )
}

