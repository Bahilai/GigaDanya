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
    val id: String
)

object EconomicAgents {
    val MONETARIST = AgentInfo(
        name = "Монетарист",
        id = "fvtpijdcg86cuiuta1s0"
    )
    
    val MARXIST = AgentInfo(
        name = "Марксист",
        id = "fvtt8samocfafo748ubs"
    )
    
    val AUSTRIAN_SCHOOL = AgentInfo(
        name = "Австрийская школа",
        id = "fvtk87vgas4rc11i76ap"
    )
    
    val NEOCLASSIC = AgentInfo(
        name = "Неоклассик",
        id = "fvtkje76q0p5k9qtqtjn"
    )
    
    val KEYNESIAN = AgentInfo(
        name = "Кейнсианец",
        id = "fvtbqlta7fhgog296d1g"
    )
    
    val ALL_AGENTS = listOf(
        MONETARIST,
        MARXIST,
        AUSTRIAN_SCHOOL,
        NEOCLASSIC,
        KEYNESIAN
    )
}
