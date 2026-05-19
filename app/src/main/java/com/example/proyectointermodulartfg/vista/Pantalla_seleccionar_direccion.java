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

public class Pantalla_seleccionar_direccion extends AppCompatActivity {

    private ImageButton btnBackDir;
    private EditText etCalle, etNumero, etLetra, etCP, etCiudad, etProvincia;
    private MaterialButton btnGuardarDireccion;

    private long idUsuarioActual = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_seleccionar_direccion);

        inicializarVistas();
        recuperarIdUsuario();

        btnBackDir.setOnClickListener(v -> finish());

        btnGuardarDireccion.setOnClickListener(v -> {
            if (idUsuarioActual == -1) {
                Toast.makeText(this, "Error: No se pudo identificar al usuario", Toast.LENGTH_SHORT).show();
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

    private void recuperarIdUsuario() {
        SharedPreferences prefs = getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE);
        String correoUsuario = prefs.getString("correo_usuario", null);

        if (correoUsuario != null) {
            String jsonRespuesta = SupabaseHelper.obtenerDatosTablas("Usuarios", "correo", correoUsuario);

            if (jsonRespuesta != null) {
                try {
                    JSONArray jsonArray = new JSONArray(jsonRespuesta);
                    if (jsonArray.length() > 0) {
                        JSONObject objetoUsuario = jsonArray.getJSONObject(0);
                        idUsuarioActual = objetoUsuario.getLong("id");
                        Log.d("SUPABASE_ID", "ID de usuario recuperado: " + idUsuarioActual);
                    }
                } catch (JSONException e) {
                    Log.e("JSON_ERROR", "Error al parsear el usuario: " + e.getMessage());
                }
            }
        }
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

        boolean exito = SupabaseHelper.insertarDireccion(
                idUsuarioActual,
                calle,
                numero,
                letra,
                cp,
                ciudad,
                provincia
        );

        if (exito) {
            Toast.makeText(this, "Dirección guardada correctamente", Toast.LENGTH_SHORT).show();
            enviarResultadoAPantallaPago(calle, numero, letra, cp, ciudad, provincia);
        } else {
            Toast.makeText(this, "Error al guardar en la base de datos", Toast.LENGTH_SHORT).show();
        }
    }

    private void enviarResultadoAPantallaPago(String calle, String numero, String letra, String cp, String ciudad, String provincia) {
        String direccionFormateada = calle + " " + numero;
        if (!letra.isEmpty()) {
            direccionFormateada += ", " + letra;
        }
        String resumenCompleto = direccionFormateada + "\n" + cp + " " + ciudad + " (" + provincia + ")";

        Intent intent = new Intent();
        intent.putExtra("DIRECCION_COMPLETA", resumenCompleto);

        intent.putExtra("CALLE", calle);
        intent.putExtra("NUMERO", numero);
        intent.putExtra("LETRA", letra);
        intent.putExtra("CP", cp);
        intent.putExtra("CIUDAD", ciudad);
        intent.putExtra("PROVINCIA", provincia);

        setResult(RESULT_OK, intent);
        finish();
    }
}