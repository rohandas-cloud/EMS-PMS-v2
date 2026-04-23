package com.example.myapplication.data.api

import com.example.myapplication.MyApplication
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Base URLs - AWS Production URLs
    private const val PMS_BASE_URL = "https://dd6gzv507q8ms.cloudfront.net/"  // PMS AWS URL
    private const val EMS_BASE_URL = "https://d3lpelprx5afbv.cloudfront.net/"  // EMS AWS URL

    // ============================
    // PMS OKHTTP CLIENT
    // ============================
    private fun createPmsHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)

            // PMS AUTH INTERCEPTOR
            .addInterceptor(createPmsAuthInterceptor())

            // LOGGING
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }

    // ============================
    // EMS OKHTTP CLIENT
    // ============================
    private fun createEmsHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)

            // EMS AUTH INTERCEPTOR
            .addInterceptor(createEmsAuthInterceptor())

            // LOGGING
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }

    // ============================
    // PMS AUTH INTERCEPTOR
    // ============================
    private fun createPmsAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val builder = request.newBuilder()

            val path = request.url.encodedPath

            // Don't attach token for login endpoints
            if (!path.contains("auth/login")) {
                val token = MyApplication.sessionManager.fetchPmsToken()

                token?.let {
                    builder.addHeader("Authorization", "Bearer $it")
                }
            }

            builder.addHeader("Accept", "application/json")
            builder.addHeader("Content-Type", "application/json")

            chain.proceed(builder.build())
        }
    }

    // ============================
    // EMS AUTH INTERCEPTOR
    // ============================
    private fun createEmsAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val builder = request.newBuilder()

            val path = request.url.encodedPath

            // Don't attach token for login endpoints
            if (!path.contains("auth/login")) {
                val token = MyApplication.sessionManager.fetchEmsToken()

                token?.let {
                    builder.addHeader("Authorization", "Bearer $it")
                }
            }

            builder.addHeader("Accept", "application/json")
            builder.addHeader("Content-Type", "application/json")

            chain.proceed(builder.build())
        }
    }

    // ============================
    // PMS API CLIENT
    // ============================
    val pmsApi: PmsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(PMS_BASE_URL)
            .client(createPmsHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PmsApiService::class.java)
    }

    // ============================
    // EMS API CLIENT
    // ============================
    val emsApi: EmsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(EMS_BASE_URL)
            .client(createEmsHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EmsApiService::class.java)
    }

    // ============================
    // DEPRECATED: Legacy API access (for migration)
    // ============================
    @Deprecated("Use pmsApi instead", ReplaceWith("pmsApi"))
    val api: PmsApiService
        get() = pmsApi

    @Deprecated("Use emsApi instead", ReplaceWith("emsApi"))
    val secondaryApi: EmsApiService
        get() = emsApi
}