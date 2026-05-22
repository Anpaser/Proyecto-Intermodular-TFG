package com.example.proyectointermodulartfg.vista;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.proyectointermodulartfg.R;
import com.example.proyectointermodulartfg.controlador.SupabaseHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pantalla_historial_ventas_realizadas extends AppCompatActivity {

    private ImageButton btnBackHistorialVentas;
    private TextView tvTotalIngresosNum;
    private RecyclerView rvHistorialVentas;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_historial_ventas_realizadas);

        cargarVistas();
        cargarHistorialVentas();

        btnBackHistorialVentas.setOnClickListener(v -> {
            Intent intent = new Intent(Pantalla_historial_ventas_realizadas.this, Pantalla_perfil.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void cargarVistas() {
        btnBackHistorialVentas = findViewById(R.id.btnBackHistorialVentas);
        tvTotalIngresosNum = findViewById(R.id.tvTotalIngresosNum);
        rvHistorialVentas = findViewById(R.id.rvHistorialVentas);
        rvHistorialVentas.setLayoutManager(new LinearLayoutManager(this));
    }

    private void cargarHistorialVentas() {
        executorService.execute(() -> {
            try {
                SharedPreferences prefs = getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE);
                long idUsuario = prefs.getLong("id_usuario", -1);
                String jsonVentas = SupabaseHelper.obtenerVentasDelVendedor(idUsuario);

                if (jsonVentas != null && !jsonVentas.equals("[]")) {
                    try {
                        JSONArray array = new JSONArray(jsonVentas);
                        List<JSONObject> lista = new ArrayList<>();
                        double acumulado = 0.0;

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject venta = array.getJSONObject(i);
                            lista.add(venta);

                            int cant = venta.getInt("cantidad");
                            double precioUnit = venta.getDouble("precio_unitario");
                            acumulado += (cant * precioUnit);
                        }

                        final double totalFinal = acumulado;
                        runOnUiThread(() -> {
                            tvTotalIngresosNum.setText(String.format("%.2f €", totalFinal));
                            rvHistorialVentas.setAdapter(new VentasRealizadasAdapter(lista));
                        });

                    } catch (Exception e) {
                        Log.e("VENTAS", "Error parseando JSON", e);
                    }
                } else {
                    runOnUiThread(() -> tvTotalIngresosNum.setText("0.00 €"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private class VentasRealizadasAdapter extends RecyclerView.Adapter<VentasRealizadasAdapter.ViewHolder> {
        private final List<JSONObject> ventas;

        public VentasRealizadasAdapter(List<JSONObject> ventas) {
            this.ventas = ventas;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item_venta_realizada, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            try {
                JSONObject item = ventas.get(position);


                JSONObject producto = item.optJSONObject("Productos");
                if (producto != null) {
                    holder.tvNombre.setText(producto.optString("nombre", "Producto"));

                    String urlImagen = producto.optString("imagen", "");
                    Glide.with(holder.itemView.getContext())
                            .load(urlImagen)
                            .placeholder(android.R.drawable.ic_menu_report_image)
                            .error(android.R.drawable.ic_menu_close_clear_cancel)
                            .into(holder.ivImagen);
                }

                JSONObject pedido = item.optJSONObject("Pedidos");
                if (pedido != null) {
                    JSONObject comprador = pedido.optJSONObject("Usuarios");
                    if (comprador != null) {
                        String nombreComprador = comprador.optString("nombre", "Cliente");
                        holder.tvComprador.setText("Comprador: " + nombreComprador);
                    } else {
                        holder.tvComprador.setText("Comprador: Desconocido");
                    }
                } else {
                    holder.tvComprador.setText("Comprador: Desconocido");
                }

                int cantidad = item.optInt("cantidad", 1);
                holder.tvCantidad.setText("Unidades: " + cantidad);

                double totalLinea = cantidad * item.optDouble("precio_unitario", 0.0);
                holder.tvPrecio.setText(String.format("+%.2f €", totalLinea));

            } catch (Exception e) {
                Log.e("ADAPTER", "Error vinculando vista", e);
            }
        }

        @Override
        public int getItemCount() {
            return ventas.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvEstado, tvNombre, tvComprador, tvCantidad, tvPrecio;
            ImageView ivImagen;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvEstado = itemView.findViewById(R.id.tvVentaEstadoCobro);
                tvNombre = itemView.findViewById(R.id.tvVentaProductoNombre);
                tvComprador = itemView.findViewById(R.id.tvVentaComprador);
                tvCantidad = itemView.findViewById(R.id.tvVentaCantidad);
                tvPrecio = itemView.findViewById(R.id.tvVentaPrecio);
                ivImagen = itemView.findViewById(R.id.ivVentaProductoImagen);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) executorService.shutdown();
    }
}