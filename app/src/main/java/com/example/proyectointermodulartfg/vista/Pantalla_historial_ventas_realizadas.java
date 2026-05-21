package com.example.proyectointermodulartfg.vista;

import android.content.Context;
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

public class Pantalla_historial_ventas_realizadas extends AppCompatActivity {

    private ImageButton btnBackHistorialVentas;
    private TextView tvTotalIngresosNum;
    private RecyclerView rvHistorialVentas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_historial_ventas_realizadas);

        vincularVistas();
        cargarHistorialVentas();

        btnBackHistorialVentas.setOnClickListener(v -> finish());
    }

    private void vincularVistas() {
        btnBackHistorialVentas = findViewById(R.id.btnBackHistorialVentas);
        tvTotalIngresosNum = findViewById(R.id.tvTotalIngresosNum);
        rvHistorialVentas = findViewById(R.id.rvHistorialVentas);
        rvHistorialVentas.setLayoutManager(new LinearLayoutManager(this));
    }

    private void cargarHistorialVentas() {
        new Thread(() -> {
            try {
                SharedPreferences prefs = getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE);
                String correoUsuario = prefs.getString("correo_usuario", null);

                String jsonUsuario = SupabaseHelper.obtenerDatosTablas("Usuarios", "correo", correoUsuario);
                long idUsuario = new JSONArray(jsonUsuario).getJSONObject(0).getLong("id");

                String jsonVentas = SupabaseHelper.obtenerVentasDelVendedor(idUsuario);

                runOnUiThread(() -> {
                    if (jsonVentas != null && !jsonVentas.equals("[]")) {
                        try {
                            JSONArray array = new JSONArray(jsonVentas);
                            List<JSONObject> lista = new ArrayList<>();
                            double totalIngresos = 0.0;

                            for (int i = 0; i < array.length(); i++) {
                                JSONObject venta = array.getJSONObject(i);
                                lista.add(venta);

                                int cant = venta.getInt("cantidad");
                                double precioUnit = venta.getDouble("precio_unitario");
                                totalIngresos += (cant * precioUnit);
                            }

                            tvTotalIngresosNum.setText(String.format("%.2f €", totalIngresos));
                            rvHistorialVentas.setAdapter(new VentasRealizadasAdapter(lista));

                        } catch (Exception e) {
                            Log.e("VENTAS", "Error parseando JSON", e);
                        }
                    } else {
                        tvTotalIngresosNum.setText("0.00 €");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private class VentasRealizadasAdapter extends RecyclerView.Adapter<VentasRealizadasAdapter.ViewHolder> {
        private List<JSONObject> ventas;

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

                // 4. Cantidad y Precio
                int cantidad = item.optInt("cantidad", 1);
                holder.tvCantidad.setText("Unidades: " + cantidad);

                double totalLinea = cantidad * item.optDouble("precio_unitario", 0.0);
                holder.tvPrecio.setText(String.format("+%.2f €", totalLinea));

            } catch (Exception e) {
                Log.e("ADAPTER", "Error vinculando vista", e);
            }
        }

        @Override
        public int getItemCount() { return ventas.size(); }

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
}