package com.example.proyectointermodulartfg.controlador

import android.util.Patterns
import com.example.proyectointermodulartfg.modelo.Usuario
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.runBlocking
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object SupabaseHelper {

    // AUTENTICACIÓN Y SESIÓN
    @JvmStatic
    fun logearUsuario(usuario: Usuario): Boolean {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()
                client.auth.signInWith(io.github.jan.supabase.gotrue.providers.builtin.Email) {
                    email = usuario.correo
                    password = usuario.clave
                }
                val response = client.postgrest.from("Usuarios")
                    .select { filter { eq("correo", usuario.correo) } }.data
                response != "[]" && response.isNotEmpty()
            } catch (e: Exception) {
                android.util.Log.e("SUPABASE_LOGIN", "Error en login sincronizado: ${e.message}")
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
                    provider = Google
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    @JvmStatic
    fun enviarCodigoRecuperacion(correo: String): Boolean {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()
                client.auth.resetPasswordForEmail(email = correo)
                true
            } catch (e: Exception) {
                android.util.Log.e("SUPABASE_AUTH", "Error: ${e.message}")
                false
            }
        }
    }

    @JvmStatic
    fun verificarCodigoResetearClave(correo: String, codigo: String, nuevaClave: String): Boolean {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()
                client.auth.verifyEmailOtp(type = OtpType.Email.RECOVERY, email = correo, token = codigo)
                client.auth.updateUser { password = nuevaClave }
                client.postgrest.from("Usuarios")
                    .update({ set("clave", nuevaClave) }) {
                        filter { eq("correo", correo) }
                    }
                true
            } catch (e: Exception) {
                android.util.Log.e("SUPABASE_RESET", "Error al resetear: ${e.message}")
                false
            }
        }
    }

    @JvmStatic
    fun actualizarPasswordAuth(nuevaClave: String): Boolean {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()
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

    // GESTIÓN DE USUARIOS Y PERFIL
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
                client.auth.signUpWith(io.github.jan.supabase.gotrue.providers.builtin.Email) {
                    email = nuevoUsuario.correo
                    password = nuevoUsuario.clave
                }
                val datosJson = buildJsonObject {
                    put("nombre", nuevoUsuario.nombre)
                    put("correo", nuevoUsuario.correo)
                    put("clave", nuevoUsuario.clave)
                    put("telefono", nuevoUsuario.telefono)
                    put("id_rol", 2)
                }
                client.postgrest.from("Usuarios").insert(datosJson)
                true
            } catch (e: Exception) {
                android.util.Log.e("SUPABASE_REGISTRO", "ERROR DETALLADO: ${e.message}")
                e.printStackTrace()
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

    // GESTIÓN DE PRODUCTOS
    @JvmStatic
    fun buscarProductos(nombreBusqueda: String, categoria: String): List<JsonObject> {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()

                val columnasRelacionadas = Columns.raw("*, Categorias!inner(nombre_categoria), Usuarios(nombre)")
                val respuesta = client.postgrest.from("Productos").select(columns = columnasRelacionadas) {
                    filter {
                        if (nombreBusqueda.isNotEmpty()) {
                            ilike("nombre", "%$nombreBusqueda%")
                        }
                        if (categoria != "Todos" && categoria.isNotEmpty()) {
                            eq("Categorias.nombre_categoria", categoria)
                        }
                    }
                }

                respuesta.decodeList<JsonObject>()
            } catch (e: Exception) {
                android.util.Log.e("SUPABASE_ERROR", "Error en la query: ${e.message}")
                emptyList()
            }
        }
    }

    // UTILIDADES Y CONSULTAS GENÉRICAS
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
        return correo != null && Patterns.EMAIL_ADDRESS.matcher(correo).matches()
    }
}