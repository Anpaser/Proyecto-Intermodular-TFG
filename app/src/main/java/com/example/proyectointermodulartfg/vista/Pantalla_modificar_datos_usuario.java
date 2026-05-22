package com.example.proyectointermodulartfg.vista;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyectointermodulartfg.R;
import com.example.proyectointermodulartfg.controlador.SupabaseHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pantalla_modificar_datos_usuario extends AppCompatActivity {
    private EditText etNombre, etTelefono, etContrasena, etRepContrasena;
    private Button btnConfirmarCambios;
    private ImageButton ibVolverAlPerfil;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pantalla_modificar_datos_usuario);

        etNombre = findViewById(R.id.etModificarNombre);
        etTelefono = findViewById(R.id.etModificarTelefono);
        etContrasena = findViewById(R.id.etModificarClave);
        etRepContrasena = findViewById(R.id.etModificarClaveRep);
        btnConfirmarCambios = findViewById(R.id.btnGuardarCambios);
        ibVolverAlPerfil = findViewById(R.id.btnBackModificar);

        btnConfirmarCambios.setOnClickListener(v -> modificarDatos());

        ibVolverAlPerfil.setOnClickListener(v -> volverAlPerfil());
    }

    private void volverAlPerfil() {
        Intent intent = new Intent(Pantalla_modificar_datos_usuario.this, Pantalla_perfil.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void modificarDatos() {
        String nombre = etNombre.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String clave = etContrasena.getText().toString().trim();
        String repClave = etRepContrasena.getText().toString().trim();

        SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
        String correoActualUsuario = prefs.getString("correo_usuario", "");

        executorService.execute(() -> {
            boolean algunCambio = false;
            boolean errorClave = false;

            if (!nombre.isEmpty()) {
                algunCambio = SupabaseHelper.modificarDatosUsuario("nombre", nombre, correoActualUsuario);
            }

            if (!telefono.isEmpty()) {
                boolean okTelefono = SupabaseHelper.modificarDatosUsuario("telefono", telefono, correoActualUsuario);
                algunCambio = algunCambio || okTelefono;
            }

            if (!clave.isEmpty()) {
                if (clave.equals(repClave) && clave.length() >= 6) {
                    SupabaseHelper.actualizarPasswordAuth(clave);
                    boolean okTabla = SupabaseHelper.modificarDatosUsuario("clave", clave, correoActualUsuario);
                    algunCambio = algunCambio || okTabla;
                } else {
                    errorClave = true;
                }
            }

            boolean finalAlgunCambio = algunCambio;
            boolean finalErrorClave = errorClave;

            runOnUiThread(() -> {
                if (finalErrorClave) {
                    Toast.makeText(this, "Las claves deben coincidir y tener 6 caracteres", Toast.LENGTH_SHORT).show();
                } else if (finalAlgunCambio) {
                    Toast.makeText(this, "Cambios guardados con éxito", Toast.LENGTH_SHORT).show();
                    volverAlPerfil();
                } else if (nombre.isEmpty() && telefono.isEmpty() && clave.isEmpty()) {
                    Toast.makeText(this, "No has rellenado nada para modificar", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) executorService.shutdown();
    }
}