package com.example.studentmanagementapp.data.remote
import android.content.Context

class TokenStorage  (context: Context){
    private val preferences = context.applicationContext.getSharedPreferences("auth_preferences",
        Context.MODE_PRIVATE)

    fun saveToken(token: String){
        preferences.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }
    fun getToken(): String? = preferences.getString(KEY_ACCESS_TOKEN, null)

    fun clearToken() {
        preferences.edit().remove(KEY_ACCESS_TOKEN).apply()
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
    }
}
