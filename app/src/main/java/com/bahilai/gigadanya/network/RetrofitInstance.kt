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
    private const val BASE_URL = "https://llm.api.cloud.yandex.net/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val api: YandexGptApi = retrofit.create(YandexGptApi::class.java)
    
    // API credentials из BuildConfig
    val apiKey: String
        get() = "Api-Key ${BuildConfig.YANDEX_API_KEY}"
    
    val folderId: String
        get() = BuildConfig.YANDEX_FOLDER_ID
}

