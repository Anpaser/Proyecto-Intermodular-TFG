package com.example.proyectointermodulartfg.controlador

import com.example.proyectointermodulartfg.modelo.Usuario
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.runBlocking

object SupabaseHelper {

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

    @JvmStatic
    fun registrarUsuario(nuevoUsuario: Usuario): Boolean {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()
                client.postgrest.from("Usuarios").insert(nuevoUsuario)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    @JvmStatic
    fun logearUsuario(usuario: Usuario): Boolean {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()
                val response = client.postgrest.from("Usuarios")
                    .select { filter {
                        eq("correo", usuario.correo)
                        eq("clave", usuario.clave)
                    }
                    }.data
                response != "[]" && response.isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
    }

    @JvmStatic
    fun logearConGoogle(idTokenRecibido: String): Boolean {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()
                client.auth.signInWith(IDToken) {
                    idToken = idTokenRecibido
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    @JvmStatic
    fun obtenerDatosTablas(tabla: String, columna: String, valorFiltro: String): String? {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()
                val respuesta = client.postgrest.from(tabla)
                    .select { filter {
                        eq(columna, valorFiltro)
                    }
                }
                if (respuesta.data == "[]") null else respuesta.data
            } catch (e: Exception) {
                null
            }
        }
    }
}