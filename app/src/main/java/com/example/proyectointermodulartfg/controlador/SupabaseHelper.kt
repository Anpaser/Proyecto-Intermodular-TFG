package com.example.proyectointermodulartfg.controlador

import android.media.MediaCodec
import android.util.Patterns
import com.example.proyectointermodulartfg.modelo.Usuario
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.runBlocking
import java.util.regex.Pattern

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

    @JvmStatic
    fun estructuraCorreoValida(correo: String?): Boolean {
        return runBlocking { correo != null && Patterns.EMAIL_ADDRESS.matcher(correo).matches() }
    }

    @JvmStatic
    fun enviarCodigoRecuperacion(correo: String): Boolean {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()
                client.auth.resendEmail(email = correo, type = OtpType.Email.RECOVERY)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    @JvmStatic
    fun verificarCodigoResetearClave(correo: String, codigo: String, nuevaClave: String): Boolean {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance();
                client.auth.verifyEmailOtp(type = OtpType.Email.RECOVERY, email = correo, token = codigo)
                client.auth.updateUser { password = nuevaClave }
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    @JvmStatic
    fun modificarDatosUsuario(columna: String, valor: String, correo: String): Boolean {
        if (valor.isEmpty()) {
            return false
        }

        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()
                client.postgrest.from("Usuarios").update({ set(columna, valor) })
                { filter { eq("correo", correo) } }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    @JvmStatic
    fun actualizarPasswordAuth(nuevaClave: String): Boolean {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()
                // Esto actualiza la contraseña en el sistema de seguridad de Supabase
                client.auth.updateUser {
                    password = nuevaClave
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}