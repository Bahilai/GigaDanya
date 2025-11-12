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
    val colorHex: Long
)

object EconomicAgents {
    val MONETARIST = AgentInfo(
        name = "YandexGPT 5 Pro (Latest)",
        id = "fvtpijdcg86cuiuta1s0",
        emoji = "💰",
        colorHex = 0xFFFFD54F
    )
    
    val MARXIST = AgentInfo(
        name = "YandexGPT 5 Lite (Latest)",
        id = "fvtt8samocfafo748ubs",
        emoji = "⚒️",
        colorHex = 0xFFEF5350
    )
    
    val AUSTRIAN_SCHOOL = AgentInfo(
        name = "Qwen3 235B A22B Instruct 2507 FP8 (Latest)",
        id = "fvtk87vgas4rclli76ap",
        emoji = "🎻",
        colorHex = 0xFFAB47BC
    )
    
    val NEOCLASSIC = AgentInfo(
        name = "OpenAI GPT OSS 20B (Latest)",
        id = "fvtkje76q0p5k9qtqtjn",
        emoji = "📊",
        colorHex = 0xFF42A5F5
    )
    
    val KEYNESIAN = AgentInfo(
        name = "OpenAI GPT OSS 120B (Latest)",
        id = "fvtbqlta7fhgog296d1g",
        emoji = "🏛️",
        colorHex = 0xFF66BB6A
    )
    
    val ALL_AGENTS = listOf(
        MONETARIST,
        MARXIST,
        AUSTRIAN_SCHOOL,
        NEOCLASSIC,
        KEYNESIAN
    )
    
    fun getAgentByName(name: String): AgentInfo? {
        return ALL_AGENTS.find { it.name == name }
    }
}
