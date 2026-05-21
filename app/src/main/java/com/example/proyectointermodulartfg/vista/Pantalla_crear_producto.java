package com.example.proyectointermodulartfg.vista;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectointermodulartfg.R;
import com.example.proyectointermodulartfg.controlador.SupabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

public class Pantalla_crear_producto extends AppCompatActivity {

    private TextInputEditText etNombre, etDescripcion, etPrecio, etStock, etImagen;
    private AutoCompleteTextView autoCategoria;
    private MaterialButton btnGuardar;
    private ImageButton btnVolver;
    private TextView tvTitulo;

    private final String[] CATEGORIAS = {"Moda", "Hogar", "Belleza", "Electrónica"};

    private boolean esModoEdicion = false;
    private long idProductoAEditar = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_crear_producto);

        inicializarVistas();
        configurarDesplegable();
        comprobarModoEdicion();

        btnVolver.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> validarYGuardarProducto());
    }

    private void inicializarVistas() {
        btnVolver = findViewById(R.id.btnVolverProductos);
        etNombre = findViewById(R.id.etNombreProducto);
        etDescripcion = findViewById(R.id.etDescripcionProducto);
        etPrecio = findViewById(R.id.etPrecioProducto);
        etStock = findViewById(R.id.etStockProducto);
        autoCategoria = findViewById(R.id.autoCategoria);
        etImagen = findViewById(R.id.etImagenProducto);
        btnGuardar = findViewById(R.id.btnGuardarProducto);
        tvTitulo = findViewById(android.R.id.text1);
    }

    private void configurarDesplegable() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, CATEGORIAS);
        autoCategoria.setAdapter(adapter);
    }

    private void comprobarModoEdicion() {
        if (getIntent().hasExtra("modo_edicion")) {
            esModoEdicion = true;
            try {
                JSONObject prod = new JSONObject(getIntent().getStringExtra("datos_producto"));

                idProductoAEditar = prod.getLong("id");
                etNombre.setText(prod.getString("nombre"));
                etDescripcion.setText(prod.getString("descripcion"));
                etPrecio.setText(String.valueOf(prod.getDouble("precio")));
                etStock.setText(String.valueOf(prod.getInt("stock")));
                etImagen.setText(prod.optString("imagen", ""));

                long idCat = prod.getLong("id_categoria");
                autoCategoria.setText(CATEGORIAS[(int)idCat - 1], false);

                btnGuardar.setText("Actualizar Producto");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void validarYGuardarProducto() {
        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();
        String stockStr = etStock.getText().toString().trim();
        String categoriaSeleccionada = autoCategoria.getText().toString();
        String imagenUrl = etImagen.getText().toString().trim();

        if (nombre.isEmpty() || precioStr.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(this, "Rellena los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double precio = Double.parseDouble(precioStr);
            int stock = Integer.parseInt(stockStr);
            long idCategoria = obtenerIdDesdeNombre(categoriaSeleccionada);

            btnGuardar.setEnabled(false);
            ejecutarOperacion(nombre, descripcion, precio, stock, idCategoria, imagenUrl);

        } catch (Exception e) {
            Toast.makeText(this, "Error en los datos numéricos", Toast.LENGTH_SHORT).show();
            btnGuardar.setEnabled(true);
        }
    }

    private void ejecutarOperacion(String nombre, String desc, double precio, int stock, long idCat, String img) {
        new Thread(() -> {
            try {
                boolean exito;
                if (esModoEdicion) {
                    // ACTUALIZAR FILA EXISTENTE
                    exito = SupabaseHelper.actualizarProducto(idProductoAEditar, idCat, nombre, desc, precio, img, stock);
                } else {
                    // INSERTAR NUEVA FILA
                    SharedPreferences prefs = getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE);
                    String correo = prefs.getString("correo_usuario", null);
                    String jsonUser = SupabaseHelper.obtenerDatosTablas("Usuarios", "correo", correo);
                    long idUser = new JSONArray(jsonUser).getJSONObject(0).getLong("id");

                    exito = SupabaseHelper.insertarProducto(idUser, idCat, nombre, desc, precio, img, stock);
                }

                runOnUiThread(() -> {
                    if (exito) {
                        Toast.makeText(this, "¡Operación realizada!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        btnGuardar.setEnabled(true);
                        Toast.makeText(this, "Error en la base de datos", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> btnGuardar.setEnabled(true));
            }
        }).start();
    }

    private long obtenerIdDesdeNombre(String nombre) {
        switch (nombre) {
            case "Moda": return 1;
            case "Hogar": return 2;
            case "Belleza": return 3;
            case "Electrónica": return 4;
            default: return 1;
        }
    }
}