package com.example.proyectointermodulartfg.vista;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectointermodulartfg.R;
import com.example.proyectointermodulartfg.controlador.CarritoAdapter;
import com.example.proyectointermodulartfg.controlador.SupabaseHelper;
import com.example.proyectointermodulartfg.modelo.ProductoCarrito;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Pantalla_carrito_productos extends AppCompatActivity {

    private ImageButton btnBackCarrito;
    private RecyclerView rvCarrito;
    private TextView tvTotalPrecio;
    private MaterialButton btnPagar;

    private CarritoAdapter adapter;
    private List<ProductoCarrito> listaCarrito = new ArrayList<>();
    private long idUsuarioSesion = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_carrito_productos);

        btnBackCarrito = findViewById(R.id.btnBackCarrito);
        rvCarrito = findViewById(R.id.rvCarrito);
        tvTotalPrecio = findViewById(R.id.tvTotalPrecio);
        btnPagar = findViewById(R.id.btnPagar);

        rvCarrito.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CarritoAdapter(listaCarrito, new CarritoAdapter.OnCarritoClickListener() {
            @Override
            public void onUpdate(ProductoCarrito item) {
                actualizarTotal();
                actualizarCantidadEnBD(item);
            }

            @Override
            public void onDelete(ProductoCarrito item) {
                eliminarProductoDeBD(item);
            }
        });
        rvCarrito.setAdapter(adapter);

        btnBackCarrito.setOnClickListener(v -> finish());

        btnPagar.setOnClickListener(v -> {
            if (listaCarrito.isEmpty()) {
                Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show();
            } else {
                double total = 0.0;
                for (ProductoCarrito item : listaCarrito) {
                    total += (item.getPrecio() * item.getCantidad_seleccionada());
                }

                Intent intent = new Intent(Pantalla_carrito_productos.this, Pantalla_pago_compra.class);
                intent.putExtra("PRECIO_TOTAL", total);
                startActivity(intent);
            }
        });

        obtenerIdUsuarioYCargarCarrito();
    }

    private void obtenerIdUsuarioYCargarCarrito() {
        SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
        String correo = prefs.getString("correo_usuario", "");

        if (correo.isEmpty()) {
            Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            String respuesta = SupabaseHelper.obtenerDatosTablas("Usuarios", "correo", correo);
            try {
                if (respuesta != null && !respuesta.isEmpty()) {
                    JSONArray array = new JSONArray(respuesta);
                    if (array.length() > 0) {
                        JSONObject userObj = array.getJSONObject(0);
                        if (userObj.has("id_usuario")) idUsuarioSesion = userObj.getLong("id_usuario");
                        else if (userObj.has("id")) idUsuarioSesion = userObj.getLong("id");

                        if (idUsuarioSesion != -1) {
                            cargarDatosCarritoDeBD();
                        }
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("CARRITO_USER", "Error: " + e.getMessage());
            }
        }).start();
    }

    private void cargarDatosCarritoDeBD() {
        new Thread(() -> {
            String jsonRespuesta = SupabaseHelper.obtenerCarritoConProductos(idUsuarioSesion);

            runOnUiThread(() -> {
                if (jsonRespuesta != null && !jsonRespuesta.isEmpty() && !jsonRespuesta.equals("[]")) {
                    try {
                        listaCarrito.clear();
                        JSONArray jsonArray = new JSONArray(jsonRespuesta);

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);

                            long idProducto = obj.getLong("id_producto");
                            int cantidad = obj.getInt("cantidad_seleccionada");

                            JSONObject productoObj = obj.getJSONObject("Productos");
                            String nombre = productoObj.getString("nombre");
                            double precio = productoObj.getDouble("precio");
                            String imagen = productoObj.getString("imagen");

                            ProductoCarrito item = new ProductoCarrito(
                                    -1,
                                    idUsuarioSesion,
                                    idProducto,
                                    cantidad,
                                    nombre,
                                    precio,
                                    imagen
                            );
                            listaCarrito.add(item);
                        }
                        adapter.notifyDataSetChanged();
                        actualizarTotal();

                    } catch (Exception e) {
                        android.util.Log.e("CARRITO_PARSE", "Error parseando carrito: " + e.getMessage());
                        Toast.makeText(this, "Error al procesar los datos del carrito", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    listaCarrito.clear();
                    adapter.notifyDataSetChanged();
                    actualizarTotal();
                }
            });
        }).start();
    }

    private void actualizarTotal() {
        double total = 0.0;
        for (ProductoCarrito item : listaCarrito) {
            total += (item.getPrecio() * item.getCantidad_seleccionada());
        }
        tvTotalPrecio.setText(String.format(java.util.Locale.US, "%.2f €", total));
    }

    private void actualizarCantidadEnBD(ProductoCarrito item) {
        new Thread(() -> {
            boolean ok = SupabaseHelper.agregarAlCarrito(item.getId_usuario(), item.getId_producto(), item.getCantidad_seleccionada());
            if (!ok) {
                runOnUiThread(() -> Toast.makeText(this, "Error de sincronización con el servidor", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void eliminarProductoDeBD(ProductoCarrito item) {
        new Thread(() -> {
            boolean ok = SupabaseHelper.eliminarDelCarrito(item.getId_usuario(), item.getId_producto());
            runOnUiThread(() -> {
                if (ok) {
                    listaCarrito.remove(item);
                    adapter.notifyDataSetChanged();
                    actualizarTotal();
                } else {
                    Toast.makeText(this, "Error al eliminar de la base de datos", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}