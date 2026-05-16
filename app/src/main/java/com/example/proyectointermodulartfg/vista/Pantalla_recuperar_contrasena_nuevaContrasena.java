package com.example.proyectointermodulartfg.vista;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyectointermodulartfg.R;
import com.example.proyectointermodulartfg.controlador.SupabaseHelper;

public class Pantalla_recuperar_contrasena_nuevaContrasena extends AppCompatActivity {
    private EditText etContrasena, etRepContrasena;
    private Button btnModificar;
    private TextView tvVolverLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pantalla_recuperar_contrasena_nueva_contrasena);

        etContrasena = findViewById(R.id.etNuevaContrasena);
        etRepContrasena = findViewById(R.id.etNuevaContrasenaRep);
        btnModificar = findViewById(R.id.btnCambiarContrasena);
        tvVolverLogin = findViewById(R.id.tvVolverLoginNuevaContrasena);

        btnModificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modificarContrasena();
            }
        });

        tvVolverLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                volverLogin();
            }
        });
    }

    private void volverLogin() {
        Intent intent = new Intent(Pantalla_recuperar_contrasena_nuevaContrasena.this, Pantalla_login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void modificarContrasena() {
        String nuevaContrasena = etContrasena.getText().toString().trim();
        String repNuevaContrasena = etRepContrasena.getText().toString().trim();
        String correo = getIntent().getStringExtra("correo_recuperacion");
        String codigo = getIntent().getStringExtra("codigo_verificacion");

        if (nuevaContrasena.isEmpty() || repNuevaContrasena.isEmpty()) {
            Toast.makeText(this, "Debes rellenar ambos campos para modificar la contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!nuevaContrasena.equals(repNuevaContrasena)) {
            Toast.makeText(this, "Las contraseñas deben ser iguales", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            boolean modificacionExitosa = SupabaseHelper.verificarCodigoResetearClave(correo, codigo, nuevaContrasena);
            runOnUiThread(() -> {
                if (modificacionExitosa) {
                    Toast.makeText(this, "¡Contraseña actualizada! Inicia sesión", Toast.LENGTH_SHORT).show();
                    volverLogin();
                } else {
                    Toast.makeText(this, "Error al modificar al contraseña", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}