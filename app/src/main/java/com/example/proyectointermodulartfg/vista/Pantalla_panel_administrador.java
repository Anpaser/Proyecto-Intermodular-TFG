package com.example.proyectointermodulartfg.vista;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectointermodulartfg.R;
import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.List;

public class Pantalla_panel_administrador extends AppCompatActivity {
    private MaterialButton btnCerrarSesion;
    private RecyclerView rvTablas;
    private final List<String> listaTablas = Arrays.asList(
            "Usuarios", "Productos", "Categorias", "Pedidos", "Detalle_Pedidos", "Direcciones", "Valoraciones", "Carrito", "Facturas", "Roles"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_panel_administrador);

        rvTablas = findViewById(R.id.rvTablasBaseDatos);
        rvTablas.setLayoutManager(new LinearLayoutManager(this));

        TablasAdapter adapter = new TablasAdapter(listaTablas);
        rvTablas.setAdapter(adapter);

        btnCerrarSesion = findViewById(R.id.btnLogoutAdmin);

        btnCerrarSesion.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, Pantalla_login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private class TablasAdapter extends RecyclerView.Adapter<TablasAdapter.ViewHolder> {
        private final List<String> tablas;

        public TablasAdapter(List<String> tablas) {
            this.tablas = tablas;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item_panel_administrador, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String nombreTabla = tablas.get(position);
            holder.tvNombreTabla.setText(nombreTabla);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(Pantalla_panel_administrador.this, Pantalla_panel_administrador_contenido_tabla.class);
                intent.putExtra("nombre_tabla", nombreTabla);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return tablas.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNombreTabla;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvNombreTabla = itemView.findViewById(R.id.tvNombreTablaAdmin);
            }
        }
    }
}