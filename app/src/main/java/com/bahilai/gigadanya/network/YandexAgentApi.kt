package com.bahilai.gigadanya.network

import com.bahilai.gigadanya.data.AgentRequest
import com.bahilai.gigadanya.data.AgentResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * API интерфейс для взаимодействия с Yandex AI Studio Agent
 */
interface YandexAgentApi {
    @POST("responses")
    suspend fun sendMessage(
        @Header("Authorization") authorization: String,
        @Header("x-folder-id") folderId: String,
        @Body request: AgentRequest
    ): AgentResponse
}

