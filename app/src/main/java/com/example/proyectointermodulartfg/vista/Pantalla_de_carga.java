package com.example.proyectointermodulartfg.vista;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectointermodulartfg.R;
import com.example.proyectointermodulartfg.controlador.SupabaseHelper;

import org.json.JSONArray;
import org.json.JSONObject;

public class Pantalla_de_carga extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pantalla_de_carga);

        comprobarSesion();
    }

    private String obtenerCorreoGuardado() {
        SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
        return prefs.getString("correo_usuario", null);
    }

    private void comprobarSesion() {
        String correo = obtenerCorreoGuardado();

        if (correo == null) {
            irAlLogin();
        } else {
            new Thread(() -> {
                boolean existe = SupabaseHelper.existeUsuario(correo);
                String respuesta = SupabaseHelper.obtenerDatosTablas("Usuarios", "correo", correo);

                runOnUiThread(() -> {
                    try {
                    JSONArray array = new JSONArray(respuesta);

                        if (array.length() > 0) {
                            JSONObject objeto = array.getJSONObject(0);
                            String nombreReal = objeto.getString("nombre");
                            Toast.makeText(this, "Bienvenido/a " + nombreReal, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Bienvenido/a USUARIO", Toast.LENGTH_SHORT).show();
                    }

                    if (existe) {
                        Intent intent = new Intent(Pantalla_de_carga.this, Pantalla_principal.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        irAlLogin();
                    }
                });
            }).start();
        }
    }

    private void irAlLogin() {
        Intent intent = new Intent(Pantalla_de_carga.this, Pantalla_login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}