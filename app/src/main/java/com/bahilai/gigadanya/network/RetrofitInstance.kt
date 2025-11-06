package com.bahilai.gigadanya.network

import com.bahilai.gigadanya.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton объект для создания экземпляра Retrofit
 */
object RetrofitInstance {
    // URL для Foundation Models API (старый API)
    private const val BASE_URL = "https://llm.api.cloud.yandex.net/"
    
    // URL для AI Studio Agent API (новый API)
    private const val AGENT_BASE_URL = "https://rest-assistant.api.cloud.yandex.net/v1/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    
    // Retrofit для Foundation Models API (старый)
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    // Retrofit для Agent API (новый)
    private val agentRetrofit = Retrofit.Builder()
        .baseUrl(AGENT_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    // API интерфейсы
    val api: YandexGptApi = retrofit.create(YandexGptApi::class.java)
    val agentApi: YandexAgentApi = agentRetrofit.create(YandexAgentApi::class.java)
    
    // API credentials из BuildConfig
    val apiKey: String
        get() = "Api-Key ${BuildConfig.YANDEX_API_KEY}"
    
    val folderId: String
        get() = BuildConfig.YANDEX_FOLDER_ID
    
    val agentId: String
        get() = BuildConfig.YANDEX_AGENT_ID
}

