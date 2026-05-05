package com.example.proyectointermodulartfg

import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

object SupabaseHelper {

    @JvmStatic
    fun obtenerDatos(tabla: String): String {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()
                // Esto hace la magia que Java no puede hacer solo
                val response = client.postgrest.from(tabla).select().data
                response
            } catch (e: Exception) {
                "Error: ${e.message}"
            }
        }
    }
}