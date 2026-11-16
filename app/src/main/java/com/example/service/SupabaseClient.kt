package com.example.service

import com.example.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.realtime.Realtime

object SupabaseClient {

    /**
     * Sanitizes the Supabase URL in case it has trailing slashes or subpaths like /rest/v1.
     * Supabase SDK expects the root project URL: https://<project-ref>.supabase.co
     */
    private val normalizedUrl: String by lazy {
        val raw = BuildConfig.SUPABASE_URL.trim()
        if (raw.isEmpty()) return@lazy ""
        val withoutSubpath = if (raw.contains("/rest/v1")) {
            raw.substringBefore("/rest/v1")
        } else {
            raw
        }
        withoutSubpath.trimEnd('/')
    }

    val isConfigured: Boolean
        get() = normalizedUrl.isNotEmpty() &&
                !normalizedUrl.startsWith("YOUR_") &&
                BuildConfig.SUPABASE_PUBLISHABLE_KEY.isNotBlank() &&
                !BuildConfig.SUPABASE_PUBLISHABLE_KEY.startsWith("YOUR_")

    val client by lazy {
        createSupabaseClient(
            supabaseUrl = normalizedUrl,
            supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY.trim()
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
            install(Realtime)
        }
    }
}