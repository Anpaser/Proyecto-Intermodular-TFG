package com.example.proyectointermodulartfg.vista;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectointermodulartfg.R;
import com.google.android.material.button.MaterialButton;

public class Pantalla_seleccionar_pago extends AppCompatActivity {

    private ImageButton btnBackPago;
    private EditText etTitularTarjeta, etNumeroTarjeta, etFechaExp, etCVV;
    private MaterialButton btnUsarTarjeta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_seleccionar_pago);

        inicializarVistas();
        cargarPagoGuardado();

        btnBackPago.setOnClickListener(v -> {
            Intent intent = new Intent(Pantalla_seleccionar_pago.this, Pantalla_pago_compra.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        btnUsarTarjeta.setOnClickListener(v -> {
            String titular = etTitularTarjeta.getText().toString().trim();
            String numero = etNumeroTarjeta.getText().toString().trim();
            String fecha = etFechaExp.getText().toString().trim();
            String cvv = etCVV.getText().toString().trim();

            if (titular.isEmpty() || numero.length() < 16 || fecha.isEmpty() || cvv.length() < 3) {
                Toast.makeText(this, "Por favor, introduce datos válidos", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("pago_titular", titular);
            editor.putString("pago_numero", numero);
            editor.putString("pago_fecha", fecha);
            editor.apply();

            String ultimosDigitos = numero.substring(numero.length() - 4);
            String tarjetaSeleccionada = "Tarjeta (**** " + ultimosDigitos + ")";

            Intent resultIntent = new Intent();
            resultIntent.putExtra("TARJETA_SELECCIONADA", tarjetaSeleccionada);

            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void inicializarVistas() {
        btnBackPago = findViewById(R.id.btnBackPago);
        etTitularTarjeta = findViewById(R.id.etTitularTarjeta);
        etNumeroTarjeta = findViewById(R.id.etNumeroTarjeta);
        etFechaExp = findViewById(R.id.etFechaExp);
        etCVV = findViewById(R.id.etCVV);
        btnUsarTarjeta = findViewById(R.id.btnUsarTarjeta);
    }

    private void cargarPagoGuardado() {
        SharedPreferences prefs = getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE);
        etTitularTarjeta.setText(prefs.getString("pago_titular", ""));
        etNumeroTarjeta.setText(prefs.getString("pago_numero", ""));
        etFechaExp.setText(prefs.getString("pago_fecha", ""));
        etCVV.setText("");
    }
}