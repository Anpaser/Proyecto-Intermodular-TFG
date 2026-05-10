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
import com.example.proyectointermodulartfg.modelo.Usuario;

public class Pantalla_registrarse extends AppCompatActivity {
    private EditText etNombreUsuario, etCorreoElectronico, etTelefono, etClave, etClaveRepetida;
    private TextView tvInicioSesion;
    private Button btnRegistrarse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pantalla_registrarse);

        etNombreUsuario = findViewById(R.id.etNombre);
        etCorreoElectronico = findViewById(R.id.etEmailRegistro);
        etTelefono = findViewById(R.id.etTelefono);
        etClave = findViewById(R.id.etPasswordRegistro);
        etClaveRepetida = findViewById(R.id.etPasswordRegistroRepetida);
        tvInicioSesion = findViewById(R.id.tvBackToLogin);
        btnRegistrarse = findViewById(R.id.btnFinalizarRegistro);

        tvInicioSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                irAlLogin();
            }
        });

        btnRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarse();
            }
        });
    }

    private void irAlLogin() {
        Intent intent = new Intent(Pantalla_registrarse.this, Pantalla_login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void registrarse() {
        String nombre = etNombreUsuario.getText().toString().trim();
        String correo = etCorreoElectronico.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String clave = etClave.getText().toString().trim();
        String claveRep = etClaveRepetida.getText().toString().trim();

        if (nombre.isEmpty() || correo.isEmpty() || telefono.isEmpty() || clave.isEmpty() || claveRep.isEmpty()) {
            Toast.makeText(this, "Debes rellenar todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!clave.equals(claveRep)) {
            Toast.makeText(this, "La contraseña y su confirmación deben ser iguales", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            Usuario usuario = new Usuario(nombre, correo, telefono, clave);
            boolean resultado = SupabaseHelper.registrarUsuario(usuario);
            runOnUiThread(() -> {
                if (resultado) {
                    Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();
                    irAlLogin();
                } else {
                    Toast.makeText(this, "Error al registrar al usuario", Toast.LENGTH_SHORT).show();
                    etNombreUsuario.setText("");
                    etCorreoElectronico.setText("");
                    etTelefono.setText("");
                    etClave.setText("");
                    etClaveRepetida.setText("");
                }
            });
        }).start();
    }
}