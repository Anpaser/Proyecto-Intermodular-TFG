package com.example.proyectointermodulartfg.vista;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectointermodulartfg.R;
import com.example.proyectointermodulartfg.controlador.SupabaseHelper;

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

                runOnUiThread(() -> {
                    if (existe) {
                        Toast.makeText(this, "Bienvenido " + correo, Toast.LENGTH_SHORT).show();
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