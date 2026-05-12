package com.example.proyectointermodulartfg.vista;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectointermodulartfg.R;
import com.example.proyectointermodulartfg.controlador.SupabaseHelper;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

public class Pantalla_perfil extends AppCompatActivity {
    private MaterialButton btnHistorialPedidos, btnHistorialVentas, btnModificarDatos, btnCerrarSesion;
    private TextView tvNombreUsuario, tvCorreoUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pantalla_perfil);

        btnHistorialPedidos = findViewById(R.id.btnHistorialPedidos);
        btnHistorialVentas = findViewById(R.id.btnHistorialVentas);
        btnModificarDatos = findViewById(R.id.btnModificarDatos);
        btnCerrarSesion = findViewById(R.id.btnLogout);
        tvNombreUsuario = findViewById(R.id.tvNombrePerfil);
        tvCorreoUsuario = findViewById(R.id.tvEmailPerfil);

        btnHistorialPedidos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moverAOtraPantalla(Pantalla_historial_pedidos_realizados.class);
            }
        });

        btnHistorialVentas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moverAOtraPantalla(Pantalla_historial_ventas_realizadas.class);
            }
        });

        btnModificarDatos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moverAOtraPantalla(Pantalla_modificar_datos_usuario.class);
            }
        });

        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moverAOtraPantalla(Pantalla_login.class);
                finish();
            }
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

        new Thread(() -> {
           String resultadoConsulta = SupabaseHelper.obtenerDatosTablas("Usuarios", "correo", correo);
           runOnUiThread(() -> {
              if (resultadoConsulta != null) {
                  try {
                      JSONArray array = new JSONArray(resultadoConsulta);
                      JSONObject object = array.getJSONObject(0);

                      String nombre = object.getString("nombre");

                      tvCorreoUsuario.setText(correo);
                      tvNombreUsuario.setText(nombre);
                  } catch (Exception e) {
                      Toast.makeText(this, "Datos de usuario no encontrados", Toast.LENGTH_SHORT).show();
                  }
              }
           });
        }).start();
    }
}