package com.example.proyectointermodulartfg.vista;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.proyectointermodulartfg.R;
import com.example.proyectointermodulartfg.controlador.ProductoAdapter;
import com.example.proyectointermodulartfg.controlador.SupabaseHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import kotlin.Pair;
import kotlinx.serialization.json.JsonElement;
import kotlinx.serialization.json.JsonObject;

public class Pantalla_principal extends AppCompatActivity implements ProductoAdapter.OnProductoClickListener {

    private SearchView svBusqueda;
    private ChipGroup cgCategorias;
    private RecyclerView rvProductos;
    private ProductoAdapter adapter;
    private List<JsonObject> listaProductos = new ArrayList<>();
    private String categoriaSeleccionada = "Todos";
    private View layoutDetalle;
    private TextView detNombre, detPrecio, detDescripcion, detVendedor, detStock, detAlertaStock, detCategoria, nombrePantalla;
    private ImageView detImagen, ivPerfilUsuario;
    private FloatingActionButton fabVentas, fabCarrito;
    private RatingBar rbMediaValoracion, rbUsuarioValoracion;
    private TextView tvNumValoraciones;
    private Button btnAgregarCarrito;
    private long idProductoSeleccionado = -1;
    private long idUsuarioSesion = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_principal);

        svBusqueda = findViewById(R.id.searchProducts);
        cgCategorias = findViewById(R.id.chipGroupCategorias);
        rvProductos = findViewById(R.id.rvProducts);
        layoutDetalle = findViewById(R.id.detalle_producto_overlay);

        detNombre = findViewById(R.id.tvDetalleNombre);
        detPrecio = findViewById(R.id.tvDetallePrecio);
        detDescripcion = findViewById(R.id.tvDetalleDescripcion);
        detVendedor = findViewById(R.id.tvDetalleVendedor);
        detStock = findViewById(R.id.tvDetalleStock);
        detAlertaStock = findViewById(R.id.tvAlertaStock);
        detCategoria = findViewById(R.id.tvDetalleCategoria);
        detImagen = findViewById(R.id.ivDetalleImagen);
        ivPerfilUsuario = findViewById(R.id.ivUserProfile);
        fabVentas = findViewById(R.id.fabSell);
        fabCarrito = findViewById(R.id.fabCart);
        nombrePantalla = findViewById(R.id.tvBienvenida);
        btnAgregarCarrito = findViewById(R.id.btnAgregarAlCarrito);

        rbMediaValoracion = findViewById(R.id.rbMediaValoracion);
        rbUsuarioValoracion = findViewById(R.id.rbUsuarioValoracion);
        tvNumValoraciones = findViewById(R.id.tvNumValoraciones);

        rvProductos.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductoAdapter(listaProductos, this);
        rvProductos.setAdapter(adapter);

        obtenerIdUsuarioYNombre();
        configurarListeners();
        cargarProductos("", "Todos");
    }

    private void configurarListeners() {
        svBusqueda.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 2 || newText.isEmpty()) cargarProductos(newText, categoriaSeleccionada);
                return true;
            }
            @Override
            public boolean onQueryTextSubmit(String query) {
                cargarProductos(query, categoriaSeleccionada);
                return true;
            }
        });

        cgCategorias.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (layoutDetalle.getVisibility() == View.VISIBLE) {
                layoutDetalle.setVisibility(View.GONE);
            }

            if (checkedIds.isEmpty()) {
                categoriaSeleccionada = "Todos";
            } else {
                Chip chip = findViewById(checkedIds.get(0));
                categoriaSeleccionada = chip.getText().toString();
            }
            cargarProductos(svBusqueda.getQuery().toString(), categoriaSeleccionada);
        });

        ivPerfilUsuario.setOnClickListener(v -> cambiarPantalla(Pantalla_perfil.class));
        fabVentas.setOnClickListener(v -> cambiarPantalla(Pantalla_productos_en_venta.class));
        fabCarrito.setOnClickListener(v -> cambiarPantalla(Pantalla_carrito_productos.class));

        btnAgregarCarrito.setOnClickListener(v -> {
            if (idProductoSeleccionado == -1) {
                Toast.makeText(this, "Error: No hay producto seleccionado", Toast.LENGTH_SHORT).show();
                return;
            }

            if (idUsuarioSesion == -1) {
                Toast.makeText(this, "Error: Sesión de usuario no válida", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                int cantidadInicial = 1;
                boolean ok = SupabaseHelper.agregarAlCarrito(idUsuarioSesion, idProductoSeleccionado, cantidadInicial);

                runOnUiThread(() -> {
                    if (ok) {
                        Toast.makeText(this, "Producto añadido al carrito", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al añadir", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });

        findViewById(R.id.btnCerrarDetalle).setOnClickListener(v -> layoutDetalle.setVisibility(View.GONE));
    }

    private void obtenerIdUsuarioYNombre() {
        SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
        String correo = prefs.getString("correo_usuario", "");
        if (correo.isEmpty()) return;

        new Thread(() -> {
            String respuesta = SupabaseHelper.obtenerDatosTablas("Usuarios", "correo", correo);
            runOnUiThread(() -> {
                if (respuesta != null && !respuesta.isEmpty()) {
                    try {
                        JSONArray array = new JSONArray(respuesta);
                        if (array.length() > 0) {
                            JSONObject objeto = array.getJSONObject(0);

                            // Guardamos la ID del usuario en la variable global para usarla en el carrito
                            if (objeto.has("id_usuario")) {
                                idUsuarioSesion = objeto.getLong("id_usuario");
                            } else if (objeto.has("id")) {
                                idUsuarioSesion = objeto.getLong("id");
                            }

                            String nombreReal = objeto.getString("nombre");
                            nombrePantalla.setText("Hola, " + nombreReal + " \uD83D\uDC4B");
                        }
                    } catch (Exception e) {
                        nombrePantalla.setText("Hola, Usuario \uD83D\uDC4B");
                    }
                }
            });
        }).start();
    }

    private void cambiarPantalla(Class<?> pantalla) {
        Intent intent = new Intent(Pantalla_principal.this, pantalla);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void cargarProductos(String busqueda, String categoria) {
        listaProductos.clear();
        adapter.notifyDataSetChanged();
        new Thread(() -> {
            List<JsonObject> productosTienda = SupabaseHelper.buscarProductos(busqueda, categoria);
            runOnUiThread(() -> {
                if (productosTienda != null) {
                    listaProductos.addAll(productosTienda);
                    adapter.notifyDataSetChanged();
                }
            });
        }).start();
    }

    @Override
    public void onProductoClick(JsonObject producto) {
        try {
            JsonElement idElem = producto.get("id_producto");
            if (idElem == null) idElem = producto.get("id");

            if (idElem == null) {
                Toast.makeText(this, "Error: Este producto no tiene ID", Toast.LENGTH_SHORT).show();
                return;
            }

            idProductoSeleccionado = Long.parseLong(idElem.toString().replace("\"", ""));

            String nombre = (producto.get("nombre") != null) ? producto.get("nombre").toString().replace("\"", "") : "Producto sin nombre";
            String precio = (producto.get("precio") != null) ? producto.get("precio").toString().replace("\"", "") : "0.00";
            String desc = (producto.get("descripcion") != null && !producto.get("descripcion").toString().equals("null"))
                    ? producto.get("descripcion").toString().replace("\"", "") : "Sin descripción";
            String img = (producto.get("imagen") != null) ? producto.get("imagen").toString().replace("\"", "") : "";

            String vendedor = "Anónimo";
            if (producto.containsKey("Usuarios") && producto.get("Usuarios") != null) {
                JsonElement userElem = producto.get("Usuarios");
                try {
                    if (userElem instanceof JsonObject) {
                        vendedor = ((JsonObject) userElem).get("nombre").toString().replace("\"", "");
                    } else if (userElem instanceof kotlinx.serialization.json.JsonArray) {
                        kotlinx.serialization.json.JsonArray array = (kotlinx.serialization.json.JsonArray) userElem;
                        if (array.size() > 0) {
                            vendedor = ((JsonObject) array.get(0)).get("nombre").toString().replace("\"", "");
                        }
                    }
                } catch (Exception e) { vendedor = "Anónimo"; }
            }

            String categoriaText = "General";
            if (producto.containsKey("Categorias") && producto.get("Categorias") != null) {
                JsonElement catElem = producto.get("Categorias");
                try {
                    if (catElem instanceof JsonObject) {
                        categoriaText = ((JsonObject) catElem).get("nombre_categoria").toString().replace("\"", "");
                    } else if (catElem instanceof kotlinx.serialization.json.JsonArray) {
                        kotlinx.serialization.json.JsonArray array = (kotlinx.serialization.json.JsonArray) catElem;
                        if (array.size() > 0) {
                            categoriaText = ((JsonObject) array.get(0)).get("nombre_categoria").toString().replace("\"", "");
                        }
                    }
                } catch (Exception e) { categoriaText = "General"; }
            }

            rbUsuarioValoracion.setRating(0);
            new Thread(() -> {
                Pair<Float, Integer> stats = SupabaseHelper.obtenerEstadisticasValoracion(idProductoSeleccionado);
                runOnUiThread(() -> {
                    rbMediaValoracion.setRating(stats.getFirst());
                    tvNumValoraciones.setText("(" + stats.getSecond() + " valoraciones)");
                });
            }).start();

            rbUsuarioValoracion.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                if (fromUser) {
                    new Thread(() -> {
                        SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
                        String correo = prefs.getString("correo_usuario", "");

                        if (correo.isEmpty()) return;

                        String userData = SupabaseHelper.obtenerDatosTablas("Usuarios", "correo", correo);
                        if (userData != null && !userData.equals("[]")) {
                            try {
                                JSONArray array = new JSONArray(userData);
                                if (array.length() > 0) {
                                    JSONObject userObj = array.getJSONObject(0);

                                    long idUsuario = -1;
                                    if (userObj.has("id_usuario")) {
                                        idUsuario = userObj.getLong("id_usuario");
                                    } else if (userObj.has("id")) {
                                        idUsuario = userObj.getLong("id");
                                    }

                                    if (idUsuario != -1) {
                                        boolean exito = SupabaseHelper.insertarValoracion(idUsuario, idProductoSeleccionado, (int) rating);
                                        runOnUiThread(() -> {
                                            if (exito) Toast.makeText(this, "¡Valoración guardada!", Toast.LENGTH_SHORT).show();
                                            else Toast.makeText(this, "Fallo al guardar en BD", Toast.LENGTH_SHORT).show();
                                        });
                                    } else {
                                        runOnUiThread(() -> Toast.makeText(this, "Error: No se encontró la ID del usuario", Toast.LENGTH_SHORT).show());
                                    }
                                }
                            } catch (Exception e) {
                                android.util.Log.e("VALORACION_USER", "Error al leer JSON: " + e.getMessage());
                            }
                        }
                    }).start();
                }
            });

            detNombre.setText(nombre);
            detPrecio.setText(precio + " €");
            detDescripcion.setText(desc);
            detVendedor.setText("Vendido por: " + vendedor);
            detCategoria.setText(categoriaText.toUpperCase());

            int stock = 0;
            try {
                if (producto.get("stock") != null) {
                    stock = (int) Double.parseDouble(producto.get("stock").toString().replace("\"", ""));
                }
            } catch (Exception e) { stock = 0; }

            detStock.setText("Stock disponible: " + stock + " unidades");
            detAlertaStock.setVisibility((stock > 0 && stock <= 5) ? View.VISIBLE : View.GONE);

            if (!img.isEmpty()) Glide.with(this).load(img).into(detImagen);
            layoutDetalle.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            android.util.Log.e("CRASH_DETALLE", "Error general: " + e.getMessage());
            Toast.makeText(this, "Error al cargar el detalle del producto", Toast.LENGTH_SHORT).show();
        }
    }
}