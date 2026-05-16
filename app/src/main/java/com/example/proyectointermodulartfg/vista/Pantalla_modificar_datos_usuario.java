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

public class Pantalla_modificar_datos_usuario extends AppCompatActivity {
    private EditText etNombre, etTelefono, etContrasena, etRepContrasena;
    private Button btnConfirmarCambios;
    private ImageButton ibVolverAlPerfil;

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

        btnConfirmarCambios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modificarDatos();
            }
        });

        ibVolverAlPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                volverAlPerfil();
            }
        });
    }

    private void volverAlPerfil() {
        Intent intent = new Intent(Pantalla_modificar_datos_usuario.this, Pantalla_perfil.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void modificarDatos() {
        SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
        String correoActualUsuario = prefs.getString("correo_usuario", "");

        String nombre = etNombre.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String clave = etContrasena.getText().toString().trim();
        String repClave = etRepContrasena.getText().toString().trim();

        new Thread(() -> {
            boolean algunCambio = false;
            boolean errorClave = false;

            if (!nombre.isEmpty()) {
                algunCambio = SupabaseHelper.modificarDatosUsuario("nombre", nombre, correoActualUsuario);
            }

            if (!telefono.isEmpty()) {
                boolean okTel = SupabaseHelper.modificarDatosUsuario("telefono", telefono, correoActualUsuario);
                algunCambio = algunCambio || okTel;
            }

            if (!clave.isEmpty()) {
                if (clave.equals(repClave) && clave.length() >= 6) {
                    boolean okAuth = SupabaseHelper.actualizarPasswordAuth(clave);
                    boolean okTabla = SupabaseHelper.modificarDatosUsuario("clave", clave, correoActualUsuario);
                    algunCambio = algunCambio || (okAuth && okTabla);
                } else {
                    errorClave = true;
                }
            }

            boolean finalAlgunCambio = algunCambio;
            boolean finalErrorClave = errorClave;

            runOnUiThread(() -> {
                if (finalErrorClave) {
                    Toast.makeText(this, "Las claves deben coincidir y tener 6 caracteres", Toast.LENGTH_SHORT).show();
                }
                if (finalAlgunCambio) {
                    Toast.makeText(this, "Cambios guardados con éxito", Toast.LENGTH_SHORT).show();
                    volverAlPerfil();
                }
            });
        }).start();
    }
}