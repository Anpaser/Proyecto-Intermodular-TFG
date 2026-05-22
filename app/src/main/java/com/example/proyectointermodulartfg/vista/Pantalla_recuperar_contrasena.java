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

import com.example.proyectointermodulartfg.R;
import com.example.proyectointermodulartfg.controlador.SupabaseHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pantalla_recuperar_contrasena extends AppCompatActivity {
    private EditText etCorreoRecuperar;
    private Button btnEnviarCorreo;
    private TextView tvVolver;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pantalla_recuperar_contrasena);

        etCorreoRecuperar = findViewById(R.id.etEmailRecuperar);
        btnEnviarCorreo = findViewById(R.id.btnEnviarCodigoVerificacion);
        tvVolver = findViewById(R.id.tvVolverLogin);

        obtenerCorreo();

        btnEnviarCorreo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarCodigo();
            }
        });

        tvVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                volverLogin();
            }
        });
    }

    private void volverLogin() {
        Intent intent = new Intent(Pantalla_recuperar_contrasena.this, Pantalla_login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void enviarCodigo() {
        String correo = etCorreoRecuperar.getText().toString().trim();

        if (correo.isEmpty()) {
            Toast.makeText(this, "Debes rellenar el campo email para continuar", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Por favor, introduce un formato de email válido", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            boolean verificacionEnviar = SupabaseHelper.enviarCodigoRecuperacion(correo);
            runOnUiThread(() -> {
                if (verificacionEnviar) {
                    Intent intent = new Intent(Pantalla_recuperar_contrasena.this, Pantalla_recuperar_contrasena_codigoVerificacion.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("correo", correo);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Fallo en el proceso de envio del código", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void obtenerCorreo() {
        String correoLog = getIntent().getStringExtra("correo");
        if (correoLog != null && !correoLog.isEmpty()) {
            etCorreoRecuperar.setText(correoLog);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) executorService.shutdown();
    }
}