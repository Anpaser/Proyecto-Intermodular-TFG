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

public class Pantalla_recuperar_contrasena_codigoVerificacion extends AppCompatActivity {
    private EditText etCodigo;
    private Button btnValidar;
    private TextView tvVolverLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pantalla_recuperar_contrasena_codigo_verificacion);

        etCodigo = findViewById(R.id.etCodigoRecuperar);
        btnValidar = findViewById(R.id.btnValidarCodigo);
        tvVolverLogin = findViewById(R.id.tvVolverLoginCodigo);

        btnValidar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarCodigo();
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
        Intent intent = new Intent(Pantalla_recuperar_contrasena_codigoVerificacion.this, Pantalla_login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void validarCodigo() {
        String correo = getIntent().getStringExtra("correo");
        String codigo = etCodigo.getText().toString().trim();

        if (correo == null || correo.isEmpty()) {
            Toast.makeText(this, "Error en el correo electronico", Toast.LENGTH_SHORT).show();
            return;
        }

        if (codigo.isEmpty() || codigo.length() < 6) {
            Toast.makeText(this, "Debes introducir los 6 dígitos del código para continuar", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Pantalla_recuperar_contrasena_codigoVerificacion.this, Pantalla_recuperar_contrasena_nuevaContrasena.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("correo_recuperacion", correo);
        intent.putExtra("codigo_verificacion", codigo);
        startActivity(intent);
    }
}
