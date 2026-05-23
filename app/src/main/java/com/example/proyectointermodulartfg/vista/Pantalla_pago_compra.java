package com.example.proyectointermodulartfg.vista;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectointermodulartfg.R;
import com.example.proyectointermodulartfg.controlador.SupabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity responsable del proceso final de compra.
 *
 * Gestiona la selección de dirección, método de pago,
 * creación del pedido en Supabase y la generación automática
 * de la factura en PDF.
 */
public class Pantalla_pago_compra extends AppCompatActivity {

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
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

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
        btnBackPago.setOnClickListener(v -> {
            Intent intent = new Intent(Pantalla_pago_compra.this, Pantalla_carrito_productos.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        cvDireccionEnvio.setOnClickListener(v -> {
            Intent intent = new Intent(this, Pantalla_seleccionar_direccion.class);
            launcherDireccion.launch(intent);
        });

        cvMetodoPago.setOnClickListener(v -> {
            Intent intent = new Intent(this, Pantalla_seleccionar_pago.class);
            launcherPago.launch(intent);
        });

        btnFinalizarCompra.setOnClickListener(v -> {
            if (!direccionSeleccionada || !pagoSeleccionado) {
                Toast.makeText(this, "Por favor, completa los datos de envío y pago.", Toast.LENGTH_SHORT).show();
                return;
            }
            procesarCompra();
        });
    }

    /**
     * Procesa la compra completa: guarda dirección, crea pedido,
     * inserta detalles, actualiza stock y genera la factura PDF.
     */
    private void procesarCompra() {
        btnFinalizarCompra.setEnabled(false);
        btnFinalizarCompra.setText("Procesando...");

        SharedPreferences prefs = getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE);
        long idUsuario = prefs.getLong("id_usuario", -1);

        executorService.execute(() -> {
            try {
                if (idUsuario == -1) {
                    throw new Exception("No hay un usuario logueado. Inicia sesión de nuevo.");
                }

                long idDireccion = SupabaseHelper.insertarDireccion(
                        idUsuario,
                        tvDetalleDireccion.getText().toString(),
                        "S/N", "", "00000", "Ciudad", "Provincia"
                );
                if (idDireccion == -1) throw new Exception("Error al guardar la dirección");

                long idPedido = SupabaseHelper.crearPedido(idUsuario, idDireccion, totalFinal);
                if (idPedido == -1) throw new Exception("Error al crear el pedido");

                String carritoJson = SupabaseHelper.obtenerCarritoConProductos(idUsuario);
                if (carritoJson == null || carritoJson.equals("[]")) throw new Exception("El carrito está vacío");

                JSONArray arrayCarrito = new JSONArray(carritoJson);
                JSONArray detallesParaEnviar = new JSONArray();

                for (int i = 0; i < arrayCarrito.length(); i++) {
                    JSONObject item = arrayCarrito.getJSONObject(i);
                    JSONObject producto = item.getJSONObject("Productos");

                    JSONObject detalle = new JSONObject();
                    detalle.put("id_pedido", idPedido);
                    detalle.put("id_producto", item.getLong("id_producto"));
                    detalle.put("cantidad", item.getInt("cantidad_seleccionada"));
                    detalle.put("precio_unitario", producto.getDouble("precio"));

                    detallesParaEnviar.put(detalle);
                }

                boolean detallesOk = SupabaseHelper.insertarDetallesDesdeJson(detallesParaEnviar.toString());
                if (!detallesOk) throw new Exception("Error al insertar los detalles del pedido");

                boolean stockOk = SupabaseHelper.actualizarStockProductos(arrayCarrito);
                if (!stockOk) {
                    Log.w("COMPRA", "El pedido se creó, pero hubo un problema actualizando el stock.");
                }

                SupabaseHelper.vaciarCarritoCompleto(idUsuario);

                generarFacturaPDF(idPedido, arrayCarrito);

                runOnUiThread(() -> {
                    Toast.makeText(this, "¡Compra realizada con éxito!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Pantalla_pago_compra.this, Pantalla_historial_pedidos_realizados.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnFinalizarCompra.setEnabled(true);
                    btnFinalizarCompra.setText("Finalizar Compra");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("ERROR_COMPRA", "Fallo en procesarCompra", e);
                });
            }
        });
    }

    private void generarFacturaPDF(long idPedido, JSONArray itemsCarrito) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        paint.setTextSize(14f);
        paint.setFakeBoldText(true);
        canvas.drawText("TIENDA TFG - FACTURA", 60, 40, paint);

        paint.setFakeBoldText(false);
        paint.setTextSize(10f);
        canvas.drawText("Pedido: #CH-" + idPedido, 20, 70, paint);
        canvas.drawText("Cliente: " + tvNombreUsuarioEnvio.getText().toString(), 20, 85, paint);
        canvas.drawText("Dirección: " + tvDetalleDireccion.getText().toString(), 20, 100, paint);

        canvas.drawText("------------------------------------------", 20, 120, paint);

        int yPos = 140;
        try {
            for (int i = 0; i < itemsCarrito.length(); i++) {
                JSONObject item = itemsCarrito.getJSONObject(i);
                JSONObject prod = item.getJSONObject("Productos");
                String linea = item.getInt("cantidad_seleccionada") + "x " + prod.getString("nombre") + " - " + prod.getDouble("precio") + "€";
                canvas.drawText(linea, 20, yPos, paint);
                yPos += 20;
            }
        } catch (Exception ignored) {
            Log.e("PDF", "Error leyendo items del carrito para el PDF");
        }

        canvas.drawText("------------------------------------------", 20, yPos + 10, paint);
        paint.setFakeBoldText(true);
        canvas.drawText("TOTAL: " + String.format("%.2f €", totalFinal), 20, yPos + 30, paint);

        document.finishPage(page);

        File dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, "Factura_" + idPedido + ".pdf");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            document.writeTo(fos);
            Log.d("PDF", "Factura guardada en: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e("PDF", "Error al guardar el PDF", e);
        } finally {
            document.close();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}