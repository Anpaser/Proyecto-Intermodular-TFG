package com.example.proyectointermodulartfg.vista;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectointermodulartfg.R;
import com.example.proyectointermodulartfg.controlador.SupabaseHelper;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Pantalla_panel_administrador_contenido_tabla extends AppCompatActivity {

    private RecyclerView rvContenido;
    private TextView tvTitulo;
    private ImageButton btnBack;
    private String nombreTabla;
    private ContenidoAdapter adapter;
    private List<JSONObject> listaDatos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_panel_administrador_contenido_tabla);

        rvContenido = findViewById(R.id.rvContenidoTabla);
        tvTitulo = findViewById(R.id.tvTituloTablaAdmin);
        btnBack = findViewById(R.id.btnBackAdminContenido);

        nombreTabla = getIntent().getStringExtra("nombre_tabla");
        if (nombreTabla != null) {
            tvTitulo.setText(nombreTabla.toUpperCase());
        }

        rvContenido.setLayoutManager(new GridLayoutManager(this, 1));
        adapter = new ContenidoAdapter(listaDatos);
        rvContenido.setAdapter(adapter);

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(Pantalla_panel_administrador_contenido_tabla.this, Pantalla_panel_administrador.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        cargarDatosDeTabla();
    }

    private void cargarDatosDeTabla() {
        new Thread(() -> {
            try {
                String jsonRespuesta = SupabaseHelper.obtenerTodaLaTabla(nombreTabla);

                if (jsonRespuesta != null) {
                    JSONArray jsonArray = new JSONArray(jsonRespuesta);
                    listaDatos.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        listaDatos.add(jsonArray.getJSONObject(i));
                    }

                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private class ContenidoAdapter extends RecyclerView.Adapter<ContenidoAdapter.ViewHolder> {
        private List<JSONObject> datos;

        public ContenidoAdapter(List<JSONObject> datos) { this.datos = datos; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.recycler_view_item_panel_administrador_contenido_tabla, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            try {
                JSONObject fila = datos.get(position);

                String idFila = fila.optString("id", "N/A");
                holder.tvId.setText("Registro #" + idFila);

                holder.tvJson.setText(fila.toString(4));

                holder.btnEliminar.setOnClickListener(v -> {
                    int posicionActual = holder.getAdapterPosition();

                    if (posicionActual != RecyclerView.NO_POSITION) {
                        eliminarRegistro(fila.optLong("id"), posicionActual);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() { return datos.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvId, tvJson;
            MaterialButton btnEliminar;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvId = itemView.findViewById(R.id.tvIdFilaAdmin);
                tvJson = itemView.findViewById(R.id.tvDatosFilaJSON);
                btnEliminar = itemView.findViewById(R.id.btnAdminEliminarFila);
            }
        }
    }

    private void eliminarRegistro(long id, int position) {
        new Thread(() -> {
            boolean exito = SupabaseHelper.eliminarFilaGenerica(nombreTabla, id);
            runOnUiThread(() -> {
                if (exito) {
                    listaDatos.remove(position);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Eliminado correctamente", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}