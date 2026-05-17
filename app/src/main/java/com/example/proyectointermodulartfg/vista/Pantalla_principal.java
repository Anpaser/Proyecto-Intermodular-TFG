package com.example.proyectointermodulartfg.vista;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
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

import java.util.ArrayList;
import java.util.List;

import kotlinx.serialization.json.JsonObject;
import kotlinx.serialization.json.JsonElement;
import kotlinx.serialization.json.JsonKt;

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

        rvProductos.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductoAdapter(listaProductos, this);
        rvProductos.setAdapter(adapter);

        cargarProductos("", "Todos");

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

        ivPerfilUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cambiarPantalla(Pantalla_perfil.class);
            }
        });

        fabVentas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cambiarPantalla(Pantalla_productos_en_venta.class);
            }
        });

        fabCarrito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cambiarPantalla(Pantalla_carrito_productos.class);
            }
        });

        obtenerNombrePantalla();
    }

    private void obtenerNombrePantalla() {
        SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
        String correo = prefs.getString("correo_usuario", "");
        if (correo.isEmpty()) return;
        new Thread(() -> {
           String respuesta = SupabaseHelper.obtenerDatosTablas("Usuarios", "correo", correo);
           runOnUiThread(() -> {
               if (respuesta != null && !respuesta.isEmpty()) {
                   try {
                       org.json.JSONArray array = new org.json.JSONArray(respuesta);

                       if (array.length() > 0) {
                           org.json.JSONObject objeto = array.getJSONObject(0);
                           String nombreReal = objeto.getString("nombre");
                           nombrePantalla.setText("Hola, " + nombreReal + " \uD83D\uDC4B");
                       }
                   } catch (org.json.JSONException e) {
                       android.util.Log.e("ERROR_JSON", "Error parseando: " + e.getMessage());
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
        new Thread(() -> {
            List<JsonObject> productosTienda = SupabaseHelper.buscarProductos(busqueda, categoria);
            runOnUiThread(() -> {
                if (productosTienda != null) {
                    listaProductos.clear();
                    listaProductos.addAll(productosTienda);
                    adapter.notifyDataSetChanged();
                }
            });
        }).start();
    }

    @Override
    public void onProductoClick(JsonObject producto) {
        String nombre = producto.get("nombre").toString().replace("\"", "");
        String precio = producto.get("precio").toString().replace("\"", "");
        String desc = (producto.get("descripcion") != null && !producto.get("descripcion").toString().equals("null"))
                ? producto.get("descripcion").toString().replace("\"", "") : "Sin descripción";
        String img = producto.get("imagen").toString().replace("\"", "");

        String vendedor = "Anónimo";
        if (producto.containsKey("Usuarios") && !producto.get("Usuarios").toString().equals("null")) {
            JsonElement userElem = producto.get("Usuarios");
            if (userElem instanceof JsonObject) {
                vendedor = ((JsonObject) userElem).get("nombre").toString().replace("\"", "");
            } else if (userElem instanceof kotlinx.serialization.json.JsonArray) {
                kotlinx.serialization.json.JsonArray array = (kotlinx.serialization.json.JsonArray) userElem;
                if (array.size() > 0) {
                    vendedor = ((JsonObject) array.get(0)).get("nombre").toString().replace("\"", "");
                }
            }
        }

        String categoriaText = "General";
        if (producto.containsKey("Categorias") && !producto.get("Categorias").toString().equals("null")) {
            JsonElement catElem = producto.get("Categorias");
            if (catElem instanceof JsonObject) {
                categoriaText = ((JsonObject) catElem).get("nombre_categoria").toString().replace("\"", "");
            } else if (catElem instanceof kotlinx.serialization.json.JsonArray) {
                kotlinx.serialization.json.JsonArray array = (kotlinx.serialization.json.JsonArray) catElem;
                if (array.size() > 0) {
                    categoriaText = ((JsonObject) array.get(0)).get("nombre_categoria").toString().replace("\"", "");
                }
            }
        }

        detNombre.setText(nombre);
        detPrecio.setText(precio + " €");
        detDescripcion.setText(desc);
        detVendedor.setText("Vendido por: " + vendedor);
        detCategoria.setText(categoriaText.toUpperCase());

        int stock = 0;
        try {
            stock = (int) Double.parseDouble(producto.get("stock").toString().replace("\"", ""));
        } catch (Exception e) { stock = 0; }
        detStock.setText("Stock disponible: " + stock + " unidades");
        detAlertaStock.setVisibility((stock > 0 && stock <= 5) ? View.VISIBLE : View.GONE);

        Glide.with(this).load(img).into(detImagen);
        layoutDetalle.setVisibility(View.VISIBLE);

        findViewById(R.id.btnCerrarDetalle).setOnClickListener(v -> layoutDetalle.setVisibility(View.GONE));
    }
}