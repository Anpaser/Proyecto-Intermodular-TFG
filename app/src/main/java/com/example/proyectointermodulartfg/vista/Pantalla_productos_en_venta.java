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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectointermodulartfg.R;
import com.example.proyectointermodulartfg.controlador.SupabaseHelper;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Pantalla_productos_en_venta extends AppCompatActivity {

    private ImageButton btnBackVentas;
    private TextView tvTotalVentasNum, tvGananciasNum;
    private RecyclerView rvMisVentas;
    private ExtendedFloatingActionButton fabNuevoProducto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_productos_en_venta);

        vincularVistas();
        cargarMisProductos();

        btnBackVentas.setOnClickListener(v -> finish());

        fabNuevoProducto.setOnClickListener(v -> {
            Intent intent = new Intent(this, Pantalla_crear_producto.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarMisProductos();
    }

    private void vincularVistas() {
        btnBackVentas = findViewById(R.id.btnBackVentas);
        tvTotalVentasNum = findViewById(R.id.tvTotalVentasNum);
        tvGananciasNum = findViewById(R.id.tvGananciasNum);
        rvMisVentas = findViewById(R.id.rvMisVentas);
        fabNuevoProducto = findViewById(R.id.fabNuevoProducto);

        rvMisVentas.setLayoutManager(new LinearLayoutManager(this));
    }

    private void cargarMisProductos() {
        new Thread(() -> {
            try {
                SharedPreferences prefs = getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE);
                String correoUsuario = prefs.getString("correo_usuario", null);

                String jsonUsuario = SupabaseHelper.obtenerDatosTablas("Usuarios", "correo", correoUsuario);
                long idUsuario = new JSONArray(jsonUsuario).getJSONObject(0).getLong("id");

                String jsonProductos = SupabaseHelper.obtenerDatosTablas("Productos", "id_usuario", String.valueOf(idUsuario));

                runOnUiThread(() -> {
                    if (jsonProductos != null && !jsonProductos.equals("[]")) {
                        try {
                            JSONArray array = new JSONArray(jsonProductos);
                            List<JSONObject> lista = new ArrayList<>();
                            double valorInventario = 0.0;

                            for (int i = 0; i < array.length(); i++) {
                                JSONObject prod = array.getJSONObject(i);
                                lista.add(prod);
                                valorInventario += prod.getDouble("precio") * prod.getInt("stock");
                            }

                            tvTotalVentasNum.setText(String.valueOf(lista.size()));
                            tvGananciasNum.setText(String.format("%.2f €", valorInventario));

                            ProductosEnVentaAdapter adapter = new ProductosEnVentaAdapter(lista);
                            rvMisVentas.setAdapter(adapter);

                        } catch (Exception e) {
                            Log.e("PRODUCTOS", "Error parseando JSON", e);
                        }
                    } else {
                        tvTotalVentasNum.setText("0");
                        tvGananciasNum.setText("0.00 €");
                        rvMisVentas.setAdapter(new ProductosEnVentaAdapter(new ArrayList<>()));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void eliminarProductoDeLaBaseDeDatos(long idProducto) {
        new Thread(() -> {
            boolean exito = SupabaseHelper.eliminarProducto(idProducto);

            runOnUiThread(() -> {
                if (exito) {
                    Toast.makeText(this, "Producto eliminado correctamente", Toast.LENGTH_SHORT).show();
                    cargarMisProductos();
                } else {
                    Toast.makeText(this, "Error al eliminar el producto", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private class ProductosEnVentaAdapter extends RecyclerView.Adapter<ProductosEnVentaAdapter.ViewHolder> {
        private List<JSONObject> productos;

        public ProductosEnVentaAdapter(List<JSONObject> productos) {
            this.productos = productos;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item_productos_en_venta, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            try {
                JSONObject item = productos.get(position);
                holder.tvNombre.setText(item.getString("nombre"));
                holder.tvPrecio.setText(String.format("%.2f €", item.getDouble("precio")));

                holder.btnEliminar.setOnClickListener(v -> {
                    try {
                        long idProducto = item.getLong("id");
                        eliminarProductoDeLaBaseDeDatos(idProducto);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return productos.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNombre, tvPrecio;
            ImageButton btnEliminar;
            ImageView ivImagen;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvNombre = itemView.findViewById(R.id.tvVentaNombre);
                tvPrecio = itemView.findViewById(R.id.tvVentaPrecio);
                btnEliminar = itemView.findViewById(R.id.btnVentaEliminar);
                ivImagen = itemView.findViewById(R.id.ivVentaImagen);
            }
        }
    }
}