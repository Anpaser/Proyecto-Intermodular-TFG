package com.example.proyectointermodulartfg.vista;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyectointermodulartfg.R;
import com.example.proyectointermodulartfg.controlador.SupabaseHelper;

import org.json.JSONArray;
import org.json.JSONObject;

public class Pantalla_datos_personales_usuario extends AppCompatActivity {
    private TextView tvNombre, tvCorreo, tvTelefono, tvFechaCreacion;
    private ImageButton ibAtras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pantalla_datos_personales_usuario);

        tvNombre = findViewById(R.id.tvDatoNombre);
        tvCorreo = findViewById(R.id.tvDatoCorreo);
        tvTelefono = findViewById(R.id.tvDatoTelefono);
        tvFechaCreacion = findViewById(R.id.tvDatoFecha);
        ibAtras = findViewById(R.id.btnBackDatos);

        ibAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Pantalla_datos_personales_usuario.this, Pantalla_perfil.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        obtenerDatos();
    }

    private void obtenerDatos() {
        SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
        String correo = prefs.getString("correo_usuario", "");

        new Thread(() -> {
            String resultadoConsulta = SupabaseHelper.obtenerDatosTablas("Usuarios", "correo", correo);
            runOnUiThread(() -> {
                if (resultadoConsulta != null) {
                    try {
                        JSONArray array = new JSONArray(resultadoConsulta);
                        JSONObject object = array.getJSONObject(0);

                        String nombre = object.getString("nombre");
                        String telefono = object.getString("telefono");
                        String fecha = object.getString("created_at");

                        tvCorreo.setText(correo);
                        if (!nombre.isEmpty()) tvNombre.setText(nombre); else tvNombre.setText("Nombre desconocido");
                        if (!telefono.isEmpty()) tvTelefono.setText(telefono); else tvTelefono.setText("Teléfono desconocido");
                        if (!fecha.isEmpty()) tvFechaCreacion.setText(fecha); else tvFechaCreacion.setText("Fecha desconocida");

                    } catch (Exception e) {
                        Toast.makeText(this, "Datos de usuario no encontrados", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }).start();
    }
}