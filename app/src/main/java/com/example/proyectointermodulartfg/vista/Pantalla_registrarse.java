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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pantalla_registrarse extends AppCompatActivity {
    private EditText etNombreUsuario, etCorreoElectronico, etTelefono, etClave, etClaveRepetida;
    private TextView tvInicioSesion;
    private Button btnRegistrarse;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

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
        finish();
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

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "El formato del correo no es válido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (telefono.length() != 9) {
            Toast.makeText(this, "El teléfono debe tener 9 dígitos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!clave.equals(claveRep)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            Usuario usuario = new Usuario(nombre, correo, clave, telefono);
            boolean usuarioYaRegistrado = SupabaseHelper.existeUsuario(correo);
            if (usuarioYaRegistrado) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Este correo ya está registrado en la app", Toast.LENGTH_SHORT).show();
                });
            } else {
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
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) executorService.shutdown();
    }
}