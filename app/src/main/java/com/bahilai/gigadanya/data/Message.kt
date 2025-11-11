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
        name = "Температура 0.2",
        id = "fvtpijdcg86cuiuta1s0",
        emoji = "💰",
        colorHex = 0xFFFFD54F
    )
    
    val MARXIST = AgentInfo(
        name = "Температура 0.4",
        id = "fvtt8samocfafo748ubs",
        emoji = "⚒️",
        colorHex = 0xFFEF5350
    )
    
    val AUSTRIAN_SCHOOL = AgentInfo(
        name = "Температура 0.6",
        id = "fvtk87vgas4rclli76ap",
        emoji = "🎻",
        colorHex = 0xFFAB47BC
    )
    
    val NEOCLASSIC = AgentInfo(
        name = "Температура 0.8",
        id = "fvtkje76q0p5k9qtqtjn",
        emoji = "📊",
        colorHex = 0xFF42A5F5
    )
    
    val KEYNESIAN = AgentInfo(
        name = "Температура 1",
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
