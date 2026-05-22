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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pantalla_datos_personales_usuario extends AppCompatActivity {
    private TextView tvNombre, tvCorreo, tvTelefono, tvFechaCreacion;
    private ImageButton ibAtras;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

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

        ibAtras.setOnClickListener(v -> {
            Intent intent = new Intent(Pantalla_datos_personales_usuario.this, Pantalla_perfil.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        obtenerDatos();
    }

    private void obtenerDatos() {
        SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
        String correo = prefs.getString("correo_usuario", "");

        executorService.execute(() -> {
            String resultadoConsulta = SupabaseHelper.obtenerDatosTablas("Usuarios", "correo", correo);
            runOnUiThread(() -> {
                if (resultadoConsulta != null) {
                    try {
                        JSONArray array = new JSONArray(resultadoConsulta);
                        JSONObject object = array.getJSONObject(0);

                        String nombre = object.optString("nombre", "Nombre desconocido");
                        String telefono = object.optString("telefono", "Teléfono desconocido");
                        String fecha = object.optString("created_at", "Fecha desconocida");

                        if (nombre.equals("null")) nombre = "Nombre desconocido";
                        if (telefono.equals("null")) telefono = "Teléfono desconocido";

                        tvCorreo.setText(correo);
                        tvNombre.setText(nombre);
                        tvTelefono.setText(telefono);

                        tvFechaCreacion.setText(fecha.split("T")[0]);

                    } catch (Exception e) {
                        Toast.makeText(this, "Datos de usuario no encontrados", Toast.LENGTH_SHORT).show();
                    }
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