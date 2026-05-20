package com.example.proyectointermodulartfg.vista;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectointermodulartfg.R;
import com.example.proyectointermodulartfg.controlador.SupabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;

public class Pantalla_crear_producto extends AppCompatActivity {

    private TextInputEditText etNombre, etDescripcion, etPrecio, etStock, etCategoria, etImagen;
    private MaterialButton btnGuardar;
    private ImageButton btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_crear_producto);

        inicializarVistas();

        btnVolver.setOnClickListener(v -> finish());

        btnGuardar.setOnClickListener(v -> validarYGuardarProducto());
    }

    private void inicializarVistas() {
        btnVolver = findViewById(R.id.btnVolverProductos);
        etNombre = findViewById(R.id.etNombreProducto);
        etDescripcion = findViewById(R.id.etDescripcionProducto);
        etPrecio = findViewById(R.id.etPrecioProducto);
        etStock = findViewById(R.id.etStockProducto);
        etCategoria = findViewById(R.id.etIdCategoria);
        etImagen = findViewById(R.id.etImagenProducto);
        btnGuardar = findViewById(R.id.btnGuardarProducto);
    }

    private void validarYGuardarProducto() {
        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();
        String stockStr = etStock.getText().toString().trim();
        String categoriaStr = etCategoria.getText().toString().trim();
        String imagenUrl = etImagen.getText().toString().trim();

        if (nombre.isEmpty() || descripcion.isEmpty() || precioStr.isEmpty() || stockStr.isEmpty() || categoriaStr.isEmpty()) {
            Toast.makeText(this, "Por favor, rellena todos los campos obligatorios.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double precio = Double.parseDouble(precioStr);
            int stock = Integer.parseInt(stockStr);
            long idCategoria = Long.parseLong(categoriaStr);

            btnGuardar.setEnabled(false);

            guardarEnBaseDeDatos(nombre, descripcion, precio, stock, idCategoria, imagenUrl);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Asegúrate de que el precio, stock y categoría sean números válidos.", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarEnBaseDeDatos(String nombre, String desc, double precio, int stock, long idCat, String img) {
        new Thread(() -> {
            try {
                SharedPreferences prefs = getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE);
                String correoUsuario = prefs.getString("correo_usuario", null);

                if (correoUsuario == null) {
                    throw new Exception("No hay sesión activa.");
                }

                String jsonUsuario = SupabaseHelper.obtenerDatosTablas("Usuarios", "correo", correoUsuario);

                if (jsonUsuario == null || jsonUsuario.equals("[]")) {
                    throw new Exception("No se encontró el usuario en la base de datos.");
                }

                JSONArray arrayUsuario = new JSONArray(jsonUsuario);
                long idUsuario = arrayUsuario.getJSONObject(0).getLong("id");

                boolean exito = SupabaseHelper.insertarProducto(idUsuario, idCat, nombre, desc, precio, img, stock);

                runOnUiThread(() -> {
                    btnGuardar.setEnabled(true);
                    if (exito) {
                        Toast.makeText(Pantalla_crear_producto.this, "¡Producto guardado con éxito!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(Pantalla_crear_producto.this, "Error al guardar el producto en la base de datos.", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnGuardar.setEnabled(true);
                    Toast.makeText(Pantalla_crear_producto.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}