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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pantalla_detalle_pedido extends AppCompatActivity {

    private ImageButton btnBackDetalle;
    private TextView tvIdPedido, tvSubtotalDetalle, tvTotalDetalle;
    private RecyclerView rvProductosPedido;
    private long idPedidoRecibido;
    private double precioTotalRecibido;
    private MaterialButton btnDescargarFactura;
    private DetalleProductosAdapter adapter;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_detalle_pedido);

        vincularVistas();

        idPedidoRecibido = getIntent().getLongExtra("ID_PEDIDO", -1);
        precioTotalRecibido = getIntent().getDoubleExtra("PRECIO_TOTAL", 0.0);

        if (idPedidoRecibido != -1) {
            rellenarDatosCabecera();
            cargarProductosDelServidor();
        } else {
            Toast.makeText(this, "Error al cargar el detalle", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnDescargarFactura.setOnClickListener(v -> {
            descargarFacturaPdf();
        });

        btnBackDetalle.setOnClickListener(v -> {
            Intent intent = new Intent(Pantalla_detalle_pedido.this, Pantalla_historial_pedidos_realizados.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void vincularVistas() {
        btnBackDetalle = findViewById(R.id.btnBackDetalle);
        tvIdPedido = findViewById(R.id.tvIdPedido);
        tvSubtotalDetalle = findViewById(R.id.tvSubtotalDetalle);
        tvTotalDetalle = findViewById(R.id.tvTotalDetalle);
        rvProductosPedido = findViewById(R.id.rvProductosPedido);
        btnDescargarFactura = findViewById(R.id.btnDescargarFactura);
        rvProductosPedido.setLayoutManager(new LinearLayoutManager(this));

        rvProductosPedido.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DetalleProductosAdapter(new ArrayList<>());
        rvProductosPedido.setAdapter(adapter);
    }

    private void rellenarDatosCabecera() {
        tvIdPedido.setText("Pedido " + idPedidoRecibido);

        double costoEnvio = 5.50;
        double subtotal = precioTotalRecibido - costoEnvio;

        tvSubtotalDetalle.setText(String.format("%.2f €", subtotal));
        tvTotalDetalle.setText(String.format("%.2f €", precioTotalRecibido));
    }

    private void cargarProductosDelServidor() {
        executorService.execute(() -> {
            String jsonRespuesta = SupabaseHelper.obtenerDetallesPedidoConProductos(idPedidoRecibido);

            Log.d("DETALLE_PEDIDO_APP", "Respuesta de Supabase para los productos: " + jsonRespuesta);

            if (jsonRespuesta != null && !jsonRespuesta.isEmpty()) {
                try {
                    JSONArray datosArray = new JSONArray(jsonRespuesta);
                    List<JSONObject> listaProds = new ArrayList<>();
                    for (int i = 0; i < datosArray.length(); i++) {
                        listaProds.add(datosArray.getJSONObject(i));
                    }

                    runOnUiThread(() -> {
                        adapter.actualizarLista(listaProds);
                    });
                } catch (JSONException e) {
                    Log.e("ERROR_DETALLE", "Error JSON: " + e.getMessage());
                }
            }
        });
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
        private final List<JSONObject> productos;

        public void actualizarLista(List<JSONObject> nuevaLista) {
            if (nuevaLista != null) {
                this.productos.clear();
                this.productos.addAll(nuevaLista);
                notifyDataSetChanged();
            }
        }

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
            JSONObject item = productos.get(position);

            int cantidad = item.optInt("cantidad", 0);
            double precioUnit = item.optDouble("precio_unitario", 0.0);

            String nombre = "Producto desconocido";
            JSONObject infoProducto = item.optJSONObject("Productos");

            if (infoProducto != null) {
                nombre = infoProducto.optString("nombre", "Sin nombre");
            } else {
                nombre = "Producto ID: " + item.optLong("id_producto");
            }

            holder.tvNombre.setText(nombre);
            holder.tvCantidad.setText("x" + cantidad);
            holder.tvPrecio.setText(String.format("%.2f €", precioUnit));
        }

        @Override
        public int getItemCount() {
            return productos != null ? productos.size(): 0;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) executorService.shutdown();
    }
}