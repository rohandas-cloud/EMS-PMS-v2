package com.example.myapplication.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Dummy API Client - Using JSONPlaceholder for testing
 * Base URL: https://jsonplaceholder.typicode.com/
 */
object DummyApiClient {
    
    // Free dummy API for testing
    private const val DUMMY_BASE_URL = "https://jsonplaceholder.typicode.com/"
    
    private fun createDummyHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }
    
    val dummyApi: EmsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(DUMMY_BASE_URL)
            .client(createDummyHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EmsApiService::class.java)
    }
}
