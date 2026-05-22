package com.example.proyectointermodulartfg.vista;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectointermodulartfg.R;
import com.example.proyectointermodulartfg.controlador.SupabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pantalla_historial_pedidos_realizados extends AppCompatActivity {

    private ImageButton btnBackHistorial;
    private RecyclerView rvHistorial;
    private Chip chipTodos, chipEnCamino, chipEntregados;
    private List<JSONObject> listaCompletaPedidos = new ArrayList<>();
    private HistorialAdapter adapter;
    private long idUsuarioActual = -1;
    private ExecutorService executorService = Executors.newFixedThreadPool(4);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_historial_pedidos_realizados);

        inicializarVistas();
        recuperarIdUsuario();

        btnBackHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(Pantalla_historial_pedidos_realizados.this, Pantalla_perfil.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });


        configurarFiltros();
    }

    private void inicializarVistas() {
        btnBackHistorial = findViewById(R.id.btnBackHistorial);
        rvHistorial = findViewById(R.id.rvHistorial);
        chipTodos = findViewById(R.id.chipTodos);
        chipEnCamino = findViewById(R.id.chipEnCamino);
        chipEntregados = findViewById(R.id.chipEntregados);

        if (rvHistorial != null) {
            rvHistorial.setLayoutManager(new LinearLayoutManager(this));
            adapter = new HistorialAdapter(new ArrayList<>());
            rvHistorial.setAdapter(adapter);
        } else {
            Log.e("HISTORIAL_APP", "El RecyclerView es null. Revisa el layout.");
        }


    }

    private void recuperarIdUsuario() {
        SharedPreferences prefs = getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE);
        idUsuarioActual = prefs.getLong("id_usuario", -1);

        if (idUsuarioActual != -1) {
            cargarPedidosDesdeBD();
        }
    }

    private void cargarPedidosDesdeBD() {
        executorService.execute(() -> {
            String jsonRespuesta = SupabaseHelper.obtenerDatosTablas("Pedidos", "id_usuario", String.valueOf(idUsuarioActual));
            Log.d("HISTORIAL_APP", "Pedidos obtenidos de la BD: " + jsonRespuesta);

                if (jsonRespuesta != null && !jsonRespuesta.isEmpty()) {
                    try {
                        JSONArray arrayPedidos = new JSONArray(jsonRespuesta);
                        listaCompletaPedidos.clear();
                        for (int i = 0; i < arrayPedidos.length(); i++) {
                            listaCompletaPedidos.add(arrayPedidos.getJSONObject(i));
                        }
                        runOnUiThread(() -> {
                            filtrarPedidos("Todos");
                        });
                    } catch (JSONException e) {
                        Log.e("HISTORIAL_APP", "Error parseando pedidos", e);
                    }
                }
        });
    }

    private void configurarFiltros() {
        if (chipTodos != null) chipTodos.setOnClickListener(v -> filtrarPedidos("Todos"));
        if (chipEnCamino != null) chipEnCamino.setOnClickListener(v -> filtrarPedidos("En camino"));
        if (chipEntregados != null)
            chipEntregados.setOnClickListener(v -> filtrarPedidos("Entregados"));
    }

    private void filtrarPedidos(String filtro) {
        if (chipTodos != null) chipTodos.setChecked(filtro.equals("Todos"));
        if (chipEnCamino != null) chipEnCamino.setChecked(filtro.equals("En camino"));
        if (chipEntregados != null) chipEntregados.setChecked(filtro.equals("Entregados"));

        List<JSONObject> listaFiltrada = new ArrayList<>();

        if (filtro.equals("Todos")) {
            listaFiltrada.addAll(listaCompletaPedidos);
        } else {
            for (JSONObject pedido : listaCompletaPedidos) {
                String estado = pedido.optString("estado_pedido", "").toLowerCase();

                if (estado.contains(filtro.toLowerCase().replace("s", ""))) {
                    listaFiltrada.add(pedido);
                }
            }
        }

        if (adapter != null) {
            adapter.actualizarLista(listaFiltrada);
        }
    }

    private class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.ViewHolder> {
        private List<JSONObject> pedidos;

        public HistorialAdapter(List<JSONObject> pedidos) {
            this.pedidos = pedidos;
        }

        public void actualizarLista(List<JSONObject> nuevaLista) {
            this.pedidos = nuevaLista;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item_pedido, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            JSONObject pedido = pedidos.get(position);

            long idPedido = pedido.optLong("id", 0);
            String estado = pedido.optString("estado_pedido", "Desconocido");
            String fecha = pedido.optString("fecha", "Sin fecha");
            double precioTotal = pedido.optDouble("precio_total", 0.0);

            holder.tvPedidoId.setText("Pedido #" + idPedido);
            holder.tvPedidoEstado.setText(estado.toUpperCase());
            holder.tvPedidoFecha.setText("Realizado el " + fecha);
            holder.tvPedidoPrecioTotal.setText(String.format("%.2f €", precioTotal));

            if (estado.toLowerCase().contains("entregado")) {
                holder.tvPedidoEstado.setTextColor(Color.parseColor("#2E7D32"));
            } else {
                holder.tvPedidoEstado.setTextColor(Color.parseColor("#F57C00"));
            }

            if (holder.tvPedidoItems != null) {
                holder.tvPedidoItems.setText("Productos del pedido");
            }

            if (holder.btnVerDetalles != null) {
                holder.btnVerDetalles.setOnClickListener(v -> {
                    Intent intent = new Intent(Pantalla_historial_pedidos_realizados.this, Pantalla_detalle_pedido.class);
                    intent.putExtra("ID_PEDIDO", idPedido);
                    intent.putExtra("PRECIO_TOTAL", precioTotal);
                    startActivity(intent);
                });
            }
        }

        @Override
        public int getItemCount() {
            return pedidos != null ? pedidos.size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvPedidoId, tvPedidoEstado, tvPedidoFecha, tvPedidoItems, tvPedidoPrecioTotal;
            MaterialButton btnVerDetalles;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvPedidoId = itemView.findViewById(R.id.tvPedidoId);
                tvPedidoEstado = itemView.findViewById(R.id.tvPedidoEstado);
                tvPedidoFecha = itemView.findViewById(R.id.tvPedidoFecha);
                tvPedidoItems = itemView.findViewById(R.id.tvPedidoItems);
                tvPedidoPrecioTotal = itemView.findViewById(R.id.tvPedidoPrecioTotal);
                btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) executorService.shutdown();
    }
}