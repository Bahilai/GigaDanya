package com.bahilai.gigadanya.network

import com.bahilai.gigadanya.data.TokenizeRequest
import com.bahilai.gigadanya.data.TokenizeResponse
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
    
    /**
     * Токенизация текста
     * Документация: https://yandex.cloud/ru/docs/foundation-models/operations/yandexgpt/evaluate-request
     */
    @POST("foundationModels/v1/tokenize")
    suspend fun tokenize(
        @Header("Authorization") authorization: String,
        @Header("x-folder-id") folderId: String,
        @Body request: TokenizeRequest
    ): TokenizeResponse
}

