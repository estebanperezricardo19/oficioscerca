
package com.example.service

import io.github.jan.supabase.auth.SignOutScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object AuthService {

    suspend fun signIn(
        email: String,
        password: String
    ) {
        SupabaseClient.client.auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }
    }

    suspend fun signUp(
        email: String,
        password: String,
        name: String = "",
        phone: String = "",
        role: String = "client"
    ): UserInfo? {
        return SupabaseClient.client.auth.signUpWith(Email) {
            this.email = email.trim()
            this.password = password
            this.data = buildJsonObject {
                if (name.isNotBlank()) put("name", name.trim())
                if (phone.isNotBlank()) put("phone", phone.trim())
                put("role", role.trim().lowercase())
            }
        }
    }

    fun currentUserId(): String? {
        if (!SupabaseClient.isConfigured) return null
        return try {
            SupabaseClient.client.auth.currentUserOrNull()?.id
        } catch (e: Throwable) {
            null
        }
    }

    fun currentSession(): UserSession? {
        if (!SupabaseClient.isConfigured) return null
        return try {
            SupabaseClient.client.auth.currentSessionOrNull()
        } catch (e: Throwable) {
            null
        }
    }

    suspend fun resetPassword(email: String) {
        SupabaseClient.client.auth.resetPasswordForEmail(email.trim())
    }

    suspend fun signOut() {
        if (!SupabaseClient.isConfigured) return
        try {
            SupabaseClient.client.auth.signOut(SignOutScope.GLOBAL)
        } catch (e: Throwable) {
            android.util.Log.w("AuthService", "Global signOut failed, clearing locally: ${e.message}")
            try {
                SupabaseClient.client.auth.signOut(SignOutScope.LOCAL)
            } catch (e2: Throwable) {
                android.util.Log.e("AuthService", "Local signOut failed: ${e2.message}")
            }
        }
    }
}
