package com.bahilai.gigadanya.network

import com.bahilai.gigadanya.data.YandexGptRequest
import com.bahilai.gigadanya.data.YandexGptResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * API интерфейс для взаимодействия с Yandex GPT
 */
interface YandexGptApi {
    @POST("foundationModels/v1/completion")
    suspend fun sendMessage(
        @Header("Authorization") authorization: String,
        @Header("x-folder-id") folderId: String,
        @Body request: YandexGptRequest
    ): YandexGptResponse
}

