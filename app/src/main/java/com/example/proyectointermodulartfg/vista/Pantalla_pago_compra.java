package com.example.proyectointermodulartfg.vista;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectointermodulartfg.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class Pantalla_pago_compra extends AppCompatActivity {

    // Vistas de la UI
    private ImageButton btnBackPago;
    private MaterialCardView cvDireccionEnvio, cvMetodoPago;
    private TextView tvNombreUsuarioEnvio, tvDetalleDireccion, tvDetallePago;
    private TextView tvSubtotal, tvCostoEnvio, tvTotalFinal;
    private MaterialButton btnFinalizarCompra;

    private double subtotal = 0.0;
    private double costoEnvio = 5.50;
    private double totalFinal = 0.0;
    private boolean direccionSeleccionada = false;
    private boolean pagoSeleccionado = false;
    private ActivityResultLauncher<Intent> launcherDireccion;
    private ActivityResultLauncher<Intent> launcherPago;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_pago_compra);
        inicializarVistas();
        configurarLaunchers();
        cargarPrecios();
        configurarPulsadores();
    }

    private void inicializarVistas() {
        btnBackPago = findViewById(R.id.btnBackPago);
        cvDireccionEnvio = findViewById(R.id.cvDireccionEnvio);
        cvMetodoPago = findViewById(R.id.cvMetodoPago);
        tvNombreUsuarioEnvio = findViewById(R.id.tvNombreUsuarioEnvio);
        tvDetalleDireccion = findViewById(R.id.tvDetalleDireccion);
        tvDetallePago = findViewById(R.id.tvDetallePago);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvCostoEnvio = findViewById(R.id.tvCostoEnvio);
        tvTotalFinal = findViewById(R.id.tvTotalFinal);
        btnFinalizarCompra = findViewById(R.id.btnFinalizarCompra);
    }

    private void cargarPrecios() {
        subtotal = getIntent().getDoubleExtra("PRECIO_TOTAL", 0.0);
        totalFinal = subtotal + costoEnvio;

        tvSubtotal.setText(String.format("%.2f €", subtotal));
        tvCostoEnvio.setText(String.format("%.2f €", costoEnvio));
        tvTotalFinal.setText(String.format("%.2f €", totalFinal));
    }

    private void configurarLaunchers() {
        launcherDireccion = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String nombre = result.getData().getStringExtra("NOMBRE_DIRECCION");
                        String direccionCompleta = result.getData().getStringExtra("DIRECCION_COMPLETA");

                        tvNombreUsuarioEnvio.setText(nombre);
                        tvDetalleDireccion.setText(direccionCompleta);
                        direccionSeleccionada = true;
                    }
                }
        );

        launcherPago = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String tarjeta = result.getData().getStringExtra("TARJETA_SELECCIONADA");

                        tvDetallePago.setText(tarjeta);
                        pagoSeleccionado = true;
                    }
                }
        );
    }

    private void configurarPulsadores() {
        btnBackPago.setOnClickListener(v -> finish());

        cvDireccionEnvio.setOnClickListener(v -> {
            Intent intent = new Intent(Pantalla_pago_compra.this, Pantalla_seleccionar_direccion.class);
            launcherDireccion.launch(intent);
        });

        cvMetodoPago.setOnClickListener(v -> {
            Intent intent = new Intent(Pantalla_pago_compra.this, Pantalla_seleccionar_pago.class);
            launcherPago.launch(intent);
        });

        btnFinalizarCompra.setOnClickListener(v -> {
            if (!direccionSeleccionada) {
                Toast.makeText(this, "Por favor, selecciona una dirección de envío.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pagoSeleccionado) {
                Toast.makeText(this, "Por favor, selecciona un método de pago.", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Procesando pedido...", Toast.LENGTH_SHORT).show();

            // TODO: Aquí llamaremos a las funciones de Supabase para:
            // 1. Guardar la dirección en BD
            // 2. Crear el Pedido
            // 3. Crear el Detalle_Pedido
            // 4. Vaciar el carrito
            // 5. Generar PDF
        });
    }
}