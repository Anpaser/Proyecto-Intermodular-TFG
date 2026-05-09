package com.example.proyectointermodulartfg.controlador

import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.runBlocking

object SupabaseHelper {

    @JvmStatic
    fun obtenerDatos(tabla: String): String {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()
                val response = client.postgrest.from(tabla).select().data
                response
            } catch (e: Exception) {
                "Error: ${e.message}"
            }
        }
    }

    @JvmStatic
    fun existeUsuario(emailBuscado: String): Boolean {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()
                val response = client.postgrest.from("Usuarios")
                    .select { filter { eq("correo", emailBuscado) } }.data
                response != "[]" && response.isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
    }
}