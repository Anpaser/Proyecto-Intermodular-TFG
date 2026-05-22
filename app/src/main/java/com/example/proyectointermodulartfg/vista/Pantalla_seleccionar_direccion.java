package com.example.proyectointermodulartfg.vista;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectointermodulartfg.R;
import com.example.proyectointermodulartfg.controlador.SupabaseHelper;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pantalla_seleccionar_direccion extends AppCompatActivity {

    private ImageButton btnBackDir;
    private EditText etCalle, etNumero, etLetra, etCP, etCiudad, etProvincia;
    private MaterialButton btnGuardarDireccion;
    private long idUsuarioActual = -1;
    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_seleccionar_direccion);

        inicializarVistas();

        SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
        idUsuarioActual = prefs.getLong("id_usuario", -1);

        cargarDireccionGuardada();

        btnBackDir.setOnClickListener(v -> {
            Intent intent = new Intent(Pantalla_seleccionar_direccion.this, Pantalla_pago_compra.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        btnGuardarDireccion.setOnClickListener(v -> {
            if (idUsuarioActual == -1) {
                Toast.makeText(this, "Error: No se pudo identificar al usuario. Inicia sesión de nuevo.", Toast.LENGTH_SHORT).show();
                return;
            }
            ejecutarGuardado();
        });
    }

    private void inicializarVistas() {
        btnBackDir = findViewById(R.id.btnBackDir);
        etCalle = findViewById(R.id.etCalle);
        etNumero = findViewById(R.id.etNumero);
        etLetra = findViewById(R.id.etLetra);
        etCP = findViewById(R.id.etCP);
        etCiudad = findViewById(R.id.etCiudad);
        etProvincia = findViewById(R.id.etProvincia);
        btnGuardarDireccion = findViewById(R.id.btnGuardarDireccion);
    }

    private void ejecutarGuardado() {
        String calle = etCalle.getText().toString().trim();
        String numero = etNumero.getText().toString().trim();
        String letra = etLetra.getText().toString().trim();
        String cp = etCP.getText().toString().trim();
        String ciudad = etCiudad.getText().toString().trim();
        String provincia = etProvincia.getText().toString().trim();

        if (calle.isEmpty() || cp.isEmpty() || ciudad.isEmpty() || provincia.isEmpty()) {
            Toast.makeText(this, "Por favor, rellena los campos principales", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("dir_calle", calle);
        editor.putString("dir_numero", numero);
        editor.putString("dir_letra", letra);
        editor.putString("dir_cp", cp);
        editor.putString("dir_ciudad", ciudad);
        editor.putString("dir_provincia", provincia);
        editor.apply();

        executorService.execute(() -> {
            boolean exito = SupabaseHelper.insertarDireccionBoolean(idUsuarioActual, calle, numero, letra, cp, ciudad, provincia);

            runOnUiThread(() -> {
                if (exito) {
                    Toast.makeText(this, "Dirección guardada", Toast.LENGTH_SHORT).show();
                    enviarResultadoAPantallaPago(calle, numero, letra, cp, ciudad, provincia);
                } else {
                    Toast.makeText(this, "Error al guardar en la base de datos", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void enviarResultadoAPantallaPago(String calle, String numero, String letra, String cp, String ciudad, String provincia) {
        String direccionFormateada = calle + " " + numero;
        if (!letra.isEmpty()) {
            direccionFormateada += ", " + letra;
        }
        String resumenCompleto = direccionFormateada + "\n" + cp + " " + ciudad + " (" + provincia + ")";

        Intent intent = new Intent();
        intent.putExtra("NOMBRE_DIRECCION", "Envío a domicilio");
        intent.putExtra("DIRECCION_COMPLETA", resumenCompleto);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void cargarDireccionGuardada() {
        SharedPreferences prefs = getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE);
        etCalle.setText(prefs.getString("dir_calle", ""));
        etNumero.setText(prefs.getString("dir_numero", ""));
        etLetra.setText(prefs.getString("dir_letra", ""));
        etCP.setText(prefs.getString("dir_cp", ""));
        etCiudad.setText(prefs.getString("dir_ciudad", ""));
        etProvincia.setText(prefs.getString("dir_provincia", ""));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) executorService.shutdown();
    }
}