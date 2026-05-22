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

import com.example.proyectointermodulartfg.R;
import com.example.proyectointermodulartfg.controlador.SupabaseHelper;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pantalla_perfil extends AppCompatActivity {
    private MaterialButton btnHistorialPedidos, btnHistorialVentas, btnDatosUsuario, btnModificarDatos, btnCerrarSesion;
    private TextView tvNombreUsuario, tvCorreoUsuario;
    private ImageButton ibAtras;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pantalla_perfil);

        btnHistorialPedidos = findViewById(R.id.btnHistorialPedidos);
        btnHistorialVentas = findViewById(R.id.btnHistorialVentas);
        btnDatosUsuario = findViewById(R.id.btnDatosUsuario);
        btnModificarDatos = findViewById(R.id.btnModificarDatos);
        btnCerrarSesion = findViewById(R.id.btnLogout);
        tvNombreUsuario = findViewById(R.id.tvNombrePerfil);
        tvCorreoUsuario = findViewById(R.id.tvEmailPerfil);
        ibAtras = findViewById(R.id.btnBackPerfil);

        btnHistorialPedidos.setOnClickListener(v -> moverAOtraPantalla(Pantalla_historial_pedidos_realizados.class));

        btnHistorialVentas.setOnClickListener(v -> moverAOtraPantalla(Pantalla_historial_ventas_realizadas.class));

        btnDatosUsuario.setOnClickListener(v -> moverAOtraPantalla(Pantalla_datos_personales_usuario.class));

        btnModificarDatos.setOnClickListener(v -> moverAOtraPantalla(Pantalla_modificar_datos_usuario.class));

        btnCerrarSesion.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(Pantalla_perfil.this, Pantalla_login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        ibAtras.setOnClickListener(v -> {
            Intent intent = new Intent(Pantalla_perfil.this, Pantalla_principal.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        obtenerDatos();
    }

    private void moverAOtraPantalla(Class<?> destino) {
        Intent intent = new Intent(Pantalla_perfil.this, destino);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void obtenerDatos() {
        SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
        String correo = prefs.getString("correo_usuario", "");

        executorService.execute(() -> {
           String resultadoConsulta = SupabaseHelper.obtenerDatosTablas("Usuarios", "correo", correo);

              if (resultadoConsulta != null) {
                  try {
                      JSONArray array = new JSONArray(resultadoConsulta);
                      JSONObject object = array.getJSONObject(0);

                      String nombre = object.getString("nombre");
                      runOnUiThread(() -> {
                          tvCorreoUsuario.setText(correo);
                          tvNombreUsuario.setText(nombre);
                      });
                  } catch (Exception e) {
                      runOnUiThread(() -> Toast.makeText(this, "Datos de usuario no encontrados", Toast.LENGTH_SHORT).show());
                  }
              }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) executorService.shutdown();
    }
}