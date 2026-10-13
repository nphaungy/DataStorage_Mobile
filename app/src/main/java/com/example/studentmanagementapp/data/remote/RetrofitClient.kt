package com.example.studentmanagementapp.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8000/"

    @Volatile
    private var instance: Retrofit? = null

    fun getInstance(tokenStorage: TokenStorage): Retrofit =
        instance ?: synchronized(this) {
            instance ?: create(tokenStorage).also { instance = it }
        }

    fun create(tokenStorage: TokenStorage): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenStorage))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}