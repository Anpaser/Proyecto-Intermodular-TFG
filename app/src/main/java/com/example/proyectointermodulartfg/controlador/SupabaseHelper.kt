package com.example.proyectointermodulartfg.controlador

import android.util.Log
import android.util.Patterns
import com.example.proyectointermodulartfg.modelo.Direccion
import com.example.proyectointermodulartfg.modelo.Usuario
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.runBlocking
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.json.JSONArray
import org.json.JSONObject

/**
 * Clase principal de comunicación con Supabase.
 *
 * Contiene todas las operaciones de base de datos (CRUD) y autenticación
 * de la aplicación Comprahoy. Utiliza corrutinas y runBlocking para
 * ejecutar las consultas de forma síncrona desde Java.
 *
 * @author Angel Paredes Serrano
 * @version 1.0
 */

object SupabaseHelper {

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
                client.auth.verifyEmailOtp(
                    type = OtpType.Email.RECOVERY,
                    email = correo,
                    token = codigo
                )
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

    @JvmStatic
    fun buscarProductos(nombreBusqueda: String, categoria: String): List<JsonObject> {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()

                val columnasRelacionadas =
                    Columns.raw("*, Categorias!inner(nombre_categoria), Usuarios(nombre)")
                val respuesta =
                    client.postgrest.from("Productos").select(columns = columnasRelacionadas) {
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

    @JvmStatic
    fun insertarValoracion(idUsuario: Long, idProducto: Long, nota: Int): Boolean {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()

                val datosJson = buildJsonObject {
                    put("id_usuario", idUsuario)
                    put("id_producto", idProducto)
                    put("valoracion", nota)
                    put("comentario", "")
                }

                client.postgrest.from("Valoraciones").upsert(
                    value = datosJson,
                    onConflict = "id_usuario,id_producto"
                )

                true
            } catch (e: Exception) {
                android.util.Log.e("SUPABASE_VALORAR", "Error en upsert: ${e.message}")
                false
            }
        }
    }

    @JvmStatic
    fun obtenerEstadisticasValoracion(idProducto: Long): Pair<Float, Int> {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()
                val respuesta = client.postgrest.from("Valoraciones")
                    .select { filter { eq("id_producto", idProducto) } }

                val lista = respuesta.decodeList<JsonObject>()

                if (lista.isEmpty()) return@runBlocking Pair(0f, 0)

                val suma = lista.sumOf { it["valoracion"]?.toString()?.toInt() ?: 0 }
                val media = suma.toFloat() / lista.size

                Pair(media, lista.size)
            } catch (e: Exception) {
                Pair(0f, 0)
            }
        }
    }

    @JvmStatic
    fun obtenerDatosTablas(tabla: String, columna: String, valorFiltro: String): String? {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()
                val respuesta = client.postgrest.from(tabla)
                    .select {
                        filter {
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

    @JvmStatic
    fun agregarAlCarrito(idUsuario: Long, idProducto: Long, cantidad: Int): Boolean {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()

                val datosJson = buildJsonObject {
                    put("id_usuario", idUsuario)
                    put("id_producto", idProducto)
                    put("cantidad_seleccionada", cantidad)
                }

                client.postgrest.from("Carrito").upsert(
                    value = datosJson,
                    onConflict = "id_usuario,id_producto"
                )

                true
            } catch (e: Exception) {
                android.util.Log.e("SUPABASE_CARRITO_ADD", e.message ?: "Error desconocido")
                false
            }
        }
    }

    @JvmStatic
    fun obtenerCarritoConProductos(idUsuario: Long): String? {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()

                val result = client.postgrest.from("Carrito")
                    .select(columns = io.github.jan.supabase.postgrest.query.Columns.raw("*, Productos(*)")) {
                        filter {
                            eq("id_usuario", idUsuario)
                        }
                    }
                result.data
            } catch (e: Exception) {
                android.util.Log.e("SUPABASE_CARRITO_GET", "Error: ${e.message}")
                null
            }
        }
    }

    @JvmStatic
    fun eliminarDelCarrito(idUsuario: Long, idProducto: Long): Boolean {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()
                client.postgrest.from("Carrito").delete {
                    filter {
                        eq("id_usuario", idUsuario)
                        eq("id_producto", idProducto)
                    }
                }
                true
            } catch (e: Exception) {
                android.util.Log.e("SUPABASE_CARRITO_DEL", "Error: ${e.message}")
                false
            }
        }
    }

    @JvmStatic
    fun insertarDireccion(idUsuario: Long, calle: String, numero: String, letra: String, cp: String, ciudad: String, provincia: String): Long {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()

                val datosJson = buildJsonObject {
                    put("id_usuario", idUsuario)
                    put("calle", calle)
                    put("numero", numero)
                    put("letra", letra)
                    put("codigo_postal", cp)
                    put("ciudad", ciudad)
                    put("provincia", provincia)
                }

                val direccionExistente = client.postgrest.from("Direcciones")
                    .select { filter { eq("id_usuario", idUsuario) } }
                    .decodeList<JsonObject>()

                if (direccionExistente.isNotEmpty()) {
                    client.postgrest.from("Direcciones").update(datosJson) {
                        filter { eq("id_usuario", idUsuario) }
                    }
                    return@runBlocking direccionExistente[0]["id"]?.toString()?.toLong() ?: -1L
                } else {
                    val respuesta = client.postgrest.from("Direcciones").insert(datosJson) {
                        select()
                    }.decodeSingle<JsonObject>()

                    return@runBlocking respuesta["id"]?.toString()?.toLong() ?: -1L
                }

            } catch (e: Exception) {
                android.util.Log.e("ERROR_DB", "Fallo al procesar dirección: ${e.message}")
                -1L
            }
        }
    }

    @JvmStatic
    fun insertarDireccionBoolean(idUsuario: Long, calle: String, numero: String, letra: String, cp: String, ciudad: String, provincia: String): Boolean {
        val idResult = insertarDireccion(idUsuario, calle, numero, letra, cp, ciudad, provincia)
        return idResult != -1L
    }

    /**
     * Crea un nuevo pedido en la base de datos.
     *
     * @param idUsuario ID del usuario que realiza el pedido
     * @param idDireccion ID de la dirección de envío
     * @param precioTotal Precio final del pedido (incluyendo envío)
     * @return ID del pedido creado o -1 si hubo error
     */
    @JvmStatic
    fun crearPedido(idUsuario: Long, idDireccion: Long, precioTotal: Double): Long {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()

                val datosJson = buildJsonObject {
                    put("id_usuario", idUsuario)
                    put("id_direccion", idDireccion)
                    put("precio_total", precioTotal)
                    put("estado_pedido", "En camino")
                }

                val response = client.postgrest.from("Pedidos").insert(datosJson) {
                    select()
                }

                val jsonArray = org.json.JSONArray(response.data)
                jsonArray.getJSONObject(0).getLong("id")

            } catch (e: Exception) {
                android.util.Log.e("ERROR_DB", "Fallo al crear pedido: ${e.message}")
                -1L
            }
        }
    }

    @JvmStatic
    fun vaciarCarritoCompleto(idUsuario: Long): Boolean {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()
                client.postgrest.from("Carrito").delete {
                    filter { eq("id_usuario", idUsuario) }
                }
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    @JvmStatic
    fun insertarDetallesDesdeJson(jsonArrayString: String): Boolean {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()
                val listaDetalles = Json.decodeFromString<List<JsonObject>>(jsonArrayString)

                client.postgrest.from("Detalle_Pedidos").insert(listaDetalles)
                true
            } catch (e: Exception) {
                android.util.Log.e("SUPABASE_DETALLE", "Error: ${e.message}")
                false
            }
        }
    }

    @JvmStatic
    fun actualizarStockProductos(carritoItems: JSONArray): Boolean {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()

                for (i in 0 until carritoItems.length()) {
                    val item = carritoItems.getJSONObject(i)
                    val idProducto = item.getLong("id_producto")
                    val cantidadComprada = item.getInt("cantidad_seleccionada")

                    val jsonProducto = obtenerDatosTablas("Productos", "id", idProducto.toString())

                    if (!jsonProducto.isNullOrEmpty() && jsonProducto != "[]") {
                        val array = JSONArray(jsonProducto)
                        val stockActual = array.getJSONObject(0).getInt("stock")

                        val nuevoStock = maxOf(0, stockActual - cantidadComprada)

                        client.postgrest.from("Productos").update(
                            { set("stock", nuevoStock) }
                        ) {
                            filter { eq("id", idProducto) }
                        }
                    }
                }
                true
            } catch (e: Exception) {
                Log.e("SupabaseHelper", "Error actualizando stock: ${e.message}")
                false
            }
        }
    }

    @JvmStatic
    fun insertarProducto(idUsuario: Long, idCategoria: Long, nombre: String, descripcion: String, precio: Double, imagenUrl: String, stock: Int)
    : Boolean {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()

                val datosJson = buildJsonObject {
                    put("id_usuario", idUsuario)
                    put("id_categoria", idCategoria)
                    put("nombre", nombre)
                    put("descripcion", descripcion)
                    put("precio", precio)
                    put("imagen", imagenUrl)
                    put("stock", stock)
                }

                client.postgrest.from("Productos").insert(datosJson)
                true
            } catch (e: Exception) {
                android.util.Log.e("SUPABASE_PRODUCTO", "Error insertando producto: ${e.message}")
                false
            }
        }
    }

    @JvmStatic
    fun eliminarProducto(idProducto: Long): Boolean {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()
                client.postgrest.from("Productos").delete {
                    filter {
                        eq("id", idProducto)
                    }
                }
                true
            } catch (e: Exception) {
                android.util.Log.e("SUPABASE_PRODUCTO_DEL", "Error al borrar: ${e.message}")
                false
            }
        }
    }

    @JvmStatic
    fun obtenerVentasDelVendedor(idVendedor: Long): String? {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()

                val columnas = io.github.jan.supabase.postgrest.query.Columns.raw(
                    "*, Productos!inner(*), Pedidos!inner(*, Usuarios(*))"
                )

                val respuesta = client.postgrest.from("Detalle_Pedidos")
                    .select(columns = columnas) {
                        filter {
                            eq("Productos.id_usuario", idVendedor)
                        }
                    }

                if (respuesta.data == "[]") null else respuesta.data
            } catch (e: Exception) {
                android.util.Log.e("SUPABASE_VENTAS", "Error al obtener ventas: ${e.message}")
                null
            }
        }
    }

    @JvmStatic
    fun actualizarProducto(idProducto: Long, idCategoria: Long, nombre: String, descripcion: String, precio: Double, imagenUrl: String, stock: Int)
    : Boolean {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()
                val datosJson = buildJsonObject {
                    put("id_categoria", idCategoria)
                    put("nombre", nombre)
                    put("descripcion", descripcion)
                    put("precio", precio)
                    put("imagen", imagenUrl)
                    put("stock", stock)
                }

                client.postgrest.from("Productos").update(datosJson) {
                    filter { eq("id", idProducto) }
                }
                true
            } catch (e: Exception) {
                android.util.Log.e("SUPABASE_UPDATE", "Error: ${e.message}")
                false
            }
        }
    }

    @JvmStatic
    fun obtenerTodaLaTabla(nombreTabla: String): String? {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()

                val respuesta = client.postgrest.from(nombreTabla).select()

                if (respuesta.data == "[]") null else respuesta.data
            } catch (e: Exception) {
                android.util.Log.e("SUPABASE_ADMIN", "Error al cargar tabla $nombreTabla: ${e.message}")
                null
            }
        }
    }

    @JvmStatic
    fun eliminarFilaGenerica(nombreTabla: String, id: Long): Boolean {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()

                client.postgrest.from(nombreTabla).delete {
                    filter {
                        eq("id", id)
                    }
                }
                true
            } catch (e: Exception) {
                android.util.Log.e("SUPABASE_ADMIN", "Error al borrar registro $id de $nombreTabla: ${e.message}")
                false
            }
        }
    }

    @JvmStatic
    fun obtenerRolUsuario(correo: String): Int {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()
                val respuesta = client.postgrest.from("Usuarios")
                    .select { filter { eq("correo", correo) } }

                val lista = respuesta.decodeList<JsonObject>()

                if (lista.isNotEmpty()) {
                    lista[0]["id_rol"]?.toString()?.toInt() ?: 2
                } else {
                    2
                }
            } catch (e: Exception) {
                android.util.Log.e("SUPABASE_ROL", "Error al obtener rol: ${e.message}")
                2
            }
        }
    }

    @JvmStatic
    fun obtenerDetallesPedidoConProductos(idPedido: Long): String? {
        return runBlocking {
            try {
                val client = SupabaseManager.getInstance()

                val columnas = io.github.jan.supabase.postgrest.query.Columns.raw("*, Productos(*)")

                val result = client.postgrest.from("Detalle_Pedidos")
                    .select(columns = columnas) {
                        filter {
                            eq("id_pedido", idPedido)
                        }
                    }
                if (result.data == "[]") null else result.data
            } catch (e: Exception) {
                android.util.Log.e("SUPABASE_DETALLE_GET", "Fallo crítico: ${e.message}")
                null
            }
        }
    }
}