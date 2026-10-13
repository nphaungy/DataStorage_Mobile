package com.example.studentmanagementapp.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenStorage: TokenStorage
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenStorage.getToken()
        val target = "${originalRequest.method} ${originalRequest.url.encodedPath}"

        val newRequest = if (token != null) {
            // บันทึกเฉพาะ 6 ตัวแรกกับความยาว ไม่เปิดเผย token จริง
            Log.d(TAG, "$target  Authorization: Bearer ${token.take(6)}... (ยาว ${token.length} ตัวอักษร)")
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            Log.d(TAG, "$target  (ไม่มี Authorization header)")
            originalRequest
        }

        return chain.proceed(newRequest)
    }

    private companion object {
        const val TAG = "AuthInterceptor"
    }
}