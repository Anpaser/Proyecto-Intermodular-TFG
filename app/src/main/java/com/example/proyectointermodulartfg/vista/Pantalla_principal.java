package com.example.proyectointermodulartfg.vista;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectointermodulartfg.R;
import com.example.proyectointermodulartfg.controlador.ProductoAdapter;
import com.example.proyectointermodulartfg.controlador.SupabaseHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Pantalla_principal extends AppCompatActivity {

    private TextView tvBienvenida;
    private SearchView searchView;
    private ChipGroup chipGroup;
    private RecyclerView rvProductos;
    private FloatingActionButton fabCart, fabSell;

    private ProductoAdapter adapter;
    private List<Map<String, Object>> listaProductos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_principal);

        tvBienvenida = findViewById(R.id.tvBienvenida);
        searchView = findViewById(R.id.searchProducts);
        chipGroup = findViewById(R.id.chipGroupCategorias);
        rvProductos = findViewById(R.id.rvProducts);
        fabCart = findViewById(R.id.fabCart);
        fabSell = findViewById(R.id.fabSell);

        SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
        String nombre = prefs.getString("nombre_usuario", "Usuario");
        tvBienvenida.setText("Hola, " + nombre + " 👋");

        rvProductos.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductoAdapter(listaProductos, this);
        rvProductos.setAdapter(adapter);

        cargarDatos("", "Todos");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String categoriaActual = obtenerCategoriaSeleccionada();
                cargarDatos(query, categoriaActual);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    cargarDatos("", obtenerCategoriaSeleccionada());
                }
                return false;
            }
        });

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            String busquedaActual = searchView.getQuery().toString();
            if (checkedIds.isEmpty()) {
                cargarDatos(busquedaActual, "Todos");
            } else {
                Chip chip = findViewById(checkedIds.get(0));
                cargarDatos(busquedaActual, chip.getText().toString());
            }
        });

        fabSell.setOnClickListener(v -> {
            Toast.makeText(this, "Ir a publicar producto", Toast.LENGTH_SHORT).show();
        });
    }

    private void cargarDatos(String consulta, String categoria) {
        new Thread(() -> {
            List<Map<String, Object>> resultados = SupabaseHelper.buscarProductos(consulta, categoria);

            runOnUiThread(() -> {
                if (resultados != null) {
                    listaProductos.clear();
                    listaProductos.addAll(resultados);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(this, "Error al cargar productos", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private String obtenerCategoriaSeleccionada() {
        int idSeleccionado = chipGroup.getCheckedChipId();
        if (idSeleccionado != -1) {
            Chip chip = findViewById(idSeleccionado);
            return chip.getText().toString();
        }
        return "Todos";
    }
}