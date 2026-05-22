package com.example.proyectointermodulartfg.vista;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectointermodulartfg.R;
import com.example.proyectointermodulartfg.controlador.SupabaseHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pantalla_de_carga extends AppCompatActivity {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

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
            executorService.execute(() -> {
                String respuesta = SupabaseHelper.obtenerDatosTablas("Usuarios", "correo", correo);
                runOnUiThread(() -> {
                    try {
                        JSONArray array = new JSONArray(respuesta);

                        if (array.length() > 0) {
                            JSONObject objeto = array.getJSONObject(0);

                            long id = objeto.getLong("id");
                            int rol = objeto.getInt("id_rol");
                            String nombre = objeto.optString("nombre", "Usuario");

                            SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putLong("id_usuario", id);
                            editor.putInt("rol_usuario", rol);
                            editor.apply();

                            Toast.makeText(this, "Bienvenido/a " + nombre, Toast.LENGTH_SHORT).show();

                            lanzarSiguientePantalla(rol);
                        } else {
                            irAlLogin();
                        }
                    } catch (Exception e) {
                        Log.e("SPLASH", "Error procesando datos: " + e.getMessage());
                        irAlLogin();
                    }
                });
            });
        }
    }

    private void lanzarSiguientePantalla(int rol) {
        Intent intent;
        if (rol == 1) {
            intent = new Intent(Pantalla_de_carga.this, Pantalla_panel_administrador.class);
        } else {
            intent = new Intent(Pantalla_de_carga.this, Pantalla_principal.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void irAlLogin() {
        Intent intent = new Intent(Pantalla_de_carga.this, Pantalla_login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) executorService.shutdown();
    }
}