package com.example.proyectointermodulartfg.vista;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectointermodulartfg.R;
import com.example.proyectointermodulartfg.controlador.SupabaseHelper;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Pantalla_detalle_pedido extends AppCompatActivity {

    private ImageButton btnBackDetalle;
    private TextView tvIdPedido, tvFechaPedido, tvSubtotalDetalle, tvTotalDetalle;
    private RecyclerView rvProductosPedido;
    private long idPedidoRecibido;
    private double precioTotalRecibido;
    private MaterialButton btnDescargarFactura;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_detalle_pedido);

        vincularVistas();

        idPedidoRecibido = getIntent().getLongExtra("ID_PEDIDO", -1);
        precioTotalRecibido = getIntent().getDoubleExtra("PRECIO_TOTAL", 0.0);
        String fecha = getIntent().getStringExtra("FECHA_PEDIDO");

        if (idPedidoRecibido != -1) {
            rellenarDatosCabecera(fecha);
            cargarProductosDelServidor();
        } else {
            Toast.makeText(this, "Error al cargar el detalle", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnDescargarFactura.setOnClickListener(v -> {descargarFacturaPdf();});

        btnBackDetalle.setOnClickListener(v -> finish());
    }

    private void vincularVistas() {
        btnBackDetalle = findViewById(R.id.btnBackDetalle);
        tvIdPedido = findViewById(R.id.tvIdPedido);
        tvFechaPedido = findViewById(R.id.tvFechaPedido);
        tvSubtotalDetalle = findViewById(R.id.tvSubtotalDetalle);
        tvTotalDetalle = findViewById(R.id.tvTotalDetalle);
        rvProductosPedido = findViewById(R.id.rvProductosPedido);
        btnDescargarFactura = findViewById(R.id.btnDescargarFactura);
        rvProductosPedido.setLayoutManager(new LinearLayoutManager(this));
    }

    private void rellenarDatosCabecera(String fecha) {
        tvIdPedido.setText("Pedido " + idPedidoRecibido);
        tvFechaPedido.setText("Realizado " + (fecha != null ? fecha : "recientemente"));

        double costoEnvio = 5.50;
        double subtotal = precioTotalRecibido - costoEnvio;

        tvSubtotalDetalle.setText(String.format("%.2f €", subtotal));
        tvTotalDetalle.setText(String.format("%.2f €", precioTotalRecibido));
    }

    private void cargarProductosDelServidor() {
        new Thread(() -> {
            String jsonRespuesta = SupabaseHelper.obtenerDatosTablas("Detalle_Pedidos", "id_pedido", String.valueOf(idPedidoRecibido));

            runOnUiThread(() -> {
                if (jsonRespuesta != null) {
                    try {
                        JSONArray datosArray = new JSONArray(jsonRespuesta);
                        List<JSONObject> listaProds = new ArrayList<>();
                        for (int i = 0; i < datosArray.length(); i++) {
                            listaProds.add(datosArray.getJSONObject(i));
                        }

                        DetalleProductosAdapter adapter = new DetalleProductosAdapter(listaProds);
                        rvProductosPedido.setAdapter(adapter);

                    } catch (JSONException e) {
                        Log.e("ERROR_DETALLE", "Error JSON: " + e.getMessage());
                    }
                }
            });
        }).start();
    }

    private void descargarFacturaPdf() {
        String urlFactura = "https://owseadckiffiwwnjgqgj.supabase.co/storage/v1/object/public/Facturas/Factura_pedido_%20" + idPedidoRecibido + ".pdf";

        try {
            Toast.makeText(this, "Abriendo factura...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(urlFactura));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error al intentar abrir el PDF", Toast.LENGTH_SHORT).show();
            Log.e("ERROR_PDF", "Error abriendo el intent: " + e.getMessage());
        }
    }

    private class DetalleProductosAdapter extends RecyclerView.Adapter<DetalleProductosAdapter.ViewHolder> {
        private List<JSONObject> productos;

        public DetalleProductosAdapter(List<JSONObject> productos) {
            this.productos = productos;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item_producto_detalle, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            try {
                JSONObject item = productos.get(position);

                int cantidad = item.getInt("cantidad");
                double precioUnit = item.getDouble("precio_unitario");

                String nombre = item.has("nombre_producto") ?
                        item.getString("nombre_producto") :
                        "Producto ID: " + item.getLong("id_producto");

                holder.tvNombre.setText(nombre);
                holder.tvCantidad.setText("x" + cantidad);
                holder.tvPrecio.setText(String.format("%.2f €", precioUnit));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return productos.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNombre, tvCantidad, tvPrecio;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvNombre = itemView.findViewById(R.id.tvDetalleNombre);
                tvCantidad = itemView.findViewById(R.id.tvDetalleCantidad);
                tvPrecio = itemView.findViewById(R.id.tvDetallePrecio);
            }
        }
    }
}