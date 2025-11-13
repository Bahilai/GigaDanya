package com.bahilai.gigadanya.data

data class Message(
    val id: String,
    val text: String? = null,
    val imageUrl: String? = null,
    val isFromUser: Boolean,
    val agentName: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class AgentInfo(
    val name: String,
    val id: String,
    val emoji: String,
    val colorHex: Long,
    val modelType: String = ""
)

/**
 * Информация о стоимости модели
 * Цены указаны в рублях за 1000 токенов
 */
data class ModelPricing(
    val modelType: String,
    val inputPricePer1kTokens: Double,
    val outputPricePer1kTokens: Double
)

object ModelPricingTable {
    // Актуальные цены на модели Yandex (в рублях за 1000 токенов)
    val YANDEX_GPT_5_PRO = ModelPricing(
        modelType = "yandexgpt-5-pro",
        inputPricePer1kTokens = 1.20,
        outputPricePer1kTokens = 1.20
    )
    
    val YANDEX_GPT_5_LITE = ModelPricing(
        modelType = "yandexgpt-5-lite",
        inputPricePer1kTokens = 0.20,
        outputPricePer1kTokens = 0.20
    )
    
    // Актуальные цены на модели Qwen (в рублях за 1000 токенов)
    val QWEN_235B = ModelPricing(
        modelType = "qwen-235b",
        inputPricePer1kTokens = 0.50,
        outputPricePer1kTokens = 0.50
    )
    
    // Актуальные цены на модели OpenAI OSS (в рублях за 1000 токенов)
    val OPENAI_GPT_OSS_20B = ModelPricing(
        modelType = "openai-gpt-oss-20b",
        inputPricePer1kTokens = 0.10,
        outputPricePer1kTokens = 0.10
    )
    
    val OPENAI_GPT_OSS_120B = ModelPricing(
        modelType = "openai-gpt-oss-120b",
        inputPricePer1kTokens = 0.30,
        outputPricePer1kTokens = 0.30
    )
    
    fun getPricingForModel(modelType: String): ModelPricing? {
        return when (modelType) {
            "yandexgpt-5-pro" -> YANDEX_GPT_5_PRO
            "yandexgpt-5-lite" -> YANDEX_GPT_5_LITE
            "qwen-235b" -> QWEN_235B
            "openai-gpt-oss-20b" -> OPENAI_GPT_OSS_20B
            "openai-gpt-oss-120b" -> OPENAI_GPT_OSS_120B
            else -> null
        }
    }
}

object EconomicAgents {
    val DEFAULT_AGENT = AgentInfo(
        name = "YandexGPT 5 Pro (Latest)",
        id = "fvtpijdcg86cuiuta1s0",
        emoji = "💰",
        colorHex = 0xFFFFD54F,
        modelType = "yandexgpt-5-pro"
    )
    
    val ALL_AGENTS = listOf(DEFAULT_AGENT)
    
    fun getAgentByName(name: String): AgentInfo? {
        return ALL_AGENTS.find { it.name == name }
    }
}
