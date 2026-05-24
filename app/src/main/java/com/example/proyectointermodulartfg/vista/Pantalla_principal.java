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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlin.Pair;
import kotlinx.serialization.json.JsonElement;
import kotlinx.serialization.json.JsonObject;

/**
 * Pantalla principal de la aplicación (Home).
 *
 * Muestra el catálogo de productos, permite búsqueda y filtrado
 * por categoría, y abre el detalle del producto al hacer clic.
 */
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
    private ExecutorService executorService = Executors.newFixedThreadPool(5);

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

        findViewById(R.id.main).setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                if (svBusqueda.hasFocus()) {
                    svBusqueda.clearFocus();
                    hideKeyboard();
                    v.performClick();
                    return true;
                }
            }
            return false;
        });

        obtenerIdUsuarioYNombre();
        configurarListeners();
        cargarProductos("", "Todos");
    }

    private void configurarListeners() {
        svBusqueda.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 2 || newText.isEmpty())
                    cargarProductos(newText, categoriaSeleccionada);
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
                Chip chipSeleccionado = findViewById(checkedIds.get(0));
                categoriaSeleccionada = chipSeleccionado.getText().toString();
            }

            actualizarColoresChips(checkedIds);

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

            executorService.execute(() -> {
                int cantidadInicial = 1;
                boolean ok = SupabaseHelper.agregarAlCarrito(idUsuarioSesion, idProductoSeleccionado, cantidadInicial);

                runOnUiThread(() -> {
                    if (ok) {
                        Toast.makeText(this, "Producto añadido al carrito", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al añadir", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        findViewById(R.id.btnCerrarDetalle).setOnClickListener(v -> layoutDetalle.setVisibility(View.GONE));

        rbUsuarioValoracion.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser && idProductoSeleccionado != -1) {
                executorService.execute(() -> {
                    SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
                    long idUsuario = prefs.getLong("id_usuario", -1);
                    if (idUsuario != -1) {
                        boolean exito = SupabaseHelper.insertarValoracion(idUsuario, idProductoSeleccionado, (int) rating);
                        runOnUiThread(() -> {
                            if (exito) Toast.makeText(this, "¡Valoración guardada!", Toast.LENGTH_SHORT).show();
                            else Toast.makeText(this, "Fallo al guardar en BD", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show());
                    }
                });
            }
        });

        svBusqueda.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                svBusqueda.setBackgroundColor(getResources().getColor(R.color.gray_ligth));
            } else {
                svBusqueda.setBackgroundColor(getResources().getColor(R.color.white));
            }
        });
    }

    private void actualizarColoresChips(List<Integer> checkedIds) {
        int morado = getResources().getColor(android.R.color.holo_purple);
        int blanco = getResources().getColor(android.R.color.white);

        for (int i = 0; i < cgCategorias.getChildCount(); i++) {
            View vista = cgCategorias.getChildAt(i);
            if (vista instanceof Chip) {
                Chip chip = (Chip) vista;

                if (chip.getId() == R.id.chipTodos) {
                    chip.setTextColor(blanco);
                    continue;
                }

                boolean estaSeleccionado = checkedIds.contains(chip.getId());

                if (estaSeleccionado) {
                    chip.setTextColor(morado);
                } else {
                    chip.setTextColor(blanco);
                }
            }
        }
    }

    private void hideKeyboard() {
        android.view.inputmethod.InputMethodManager imm =
                (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void obtenerIdUsuarioYNombre() {
        SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
        String correo = prefs.getString("correo_usuario", "");
        if (correo.isEmpty()) return;

        executorService.execute(() -> {
            String respuesta = SupabaseHelper.obtenerDatosTablas("Usuarios", "correo", correo);
            if (respuesta != null && !respuesta.isEmpty()) {
                try {
                    JSONArray array = new JSONArray(respuesta);
                    if (array.length() > 0) {
                        JSONObject objeto = array.getJSONObject(0);

                        if (objeto.has("id_usuario")) {
                            idUsuarioSesion = objeto.getLong("id_usuario");
                        } else if (objeto.has("id")) {
                            idUsuarioSesion = objeto.getLong("id");
                        }

                        String nombreReal = objeto.getString("nombre");
                        String[] partesNombre = nombreReal.trim().split(" ");
                        String primerNombre = partesNombre[0];
                        runOnUiThread(() -> nombrePantalla.setText("Hola, " + primerNombre + " \uD83D\uDC4B"));
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> nombrePantalla.setText("Hola, Usuario \uD83D\uDC4B"));
                }
            }
        });

    }

    private void cambiarPantalla(Class<?> pantalla) {
        Intent intent = new Intent(Pantalla_principal.this, pantalla);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void cargarProductos(String busqueda, String categoria) {

        executorService.execute(() -> {
            List<JsonObject> productosTienda = SupabaseHelper.buscarProductos(busqueda, categoria);
            runOnUiThread(() -> {
                if (productosTienda != null) {
                    listaProductos.clear();
                    listaProductos.addAll(productosTienda);
                    adapter.notifyDataSetChanged();
                }
            });
        });
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
                } catch (Exception e) {
                    vendedor = "Anónimo";
                }
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
                } catch (Exception e) {
                    categoriaText = "General";
                }
            }

            rbUsuarioValoracion.setRating(0);
            executorService.execute(() -> {
                Pair<Float, Integer> stats = SupabaseHelper.obtenerEstadisticasValoracion(idProductoSeleccionado);
                runOnUiThread(() -> {
                    rbMediaValoracion.setRating(stats.getFirst());
                    tvNumValoraciones.setText("(" + stats.getSecond() + " valoraciones)");
                });
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
            } catch (Exception e) {
                stock = 0;
            }

            detStock.setText("Stock disponible: " + stock + " unidades");
            detAlertaStock.setVisibility((stock > 0 && stock <= 5) ? View.VISIBLE : View.GONE);

            if (!img.isEmpty()) Glide.with(this).load(img).into(detImagen);
            layoutDetalle.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            android.util.Log.e("CRASH_DETALLE", "Error general: " + e.getMessage());
            Toast.makeText(this, "Error al cargar el detalle del producto", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) executorService.shutdown();
    }
}