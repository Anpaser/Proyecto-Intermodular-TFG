package com.example.proyectointermodulartfg.controlador;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.proyectointermodulartfg.R;
import java.util.List;

import kotlinx.serialization.json.JsonArray;
import kotlinx.serialization.json.JsonElement;
import kotlinx.serialization.json.JsonObject;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private List<JsonObject> listaProductos;
    private OnProductoClickListener listener;

    public interface OnProductoClickListener {
        void onProductoClick(JsonObject producto);
    }

    public ProductoAdapter(List<JsonObject> listaProductos, OnProductoClickListener listener) {
        this.listaProductos = listaProductos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item_pantalla_principal, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        // Obtenemos el objeto genérico
        Object objProducto = listaProductos.get(position);
        String jsonCompleto = objProducto.toString();

        android.util.Log.d("JSON_PRUEBA", "Datos: " + jsonCompleto);

        // --- EXTRAER DATOS BÁSICOS (Usando lógica de Strings) ---
        // Si el get devuelve null, usamos un valor por defecto
        String nombre = "Sin nombre";
        if (listaProductos.get(position).get("nombre") != null) {
            nombre = listaProductos.get(position).get("nombre").toString().replace("\"", "");
        }

        String precio = "0.00";
        if (listaProductos.get(position).get("precio") != null) {
            precio = listaProductos.get(position).get("precio").toString().replace("\"", "");
        }

        String urlImagen = "";
        if (listaProductos.get(position).get("imagen") != null) {
            urlImagen = listaProductos.get(position).get("imagen").toString().replace("\"", "");
        }

        holder.tvNombre.setText(nombre);
        holder.tvPrecio.setText(precio + " €");

        String catNombre = "General";

        try {
            Object rawCat = listaProductos.get(position).get("Categorias");

            if (rawCat != null) {
                String catString = rawCat.toString();

                if (catString.contains("nombre_categoria")) {
                    String[] partes = catString.split("nombre_categoria\":\"");
                    if (partes.length > 1) {
                        catNombre = partes[1].split("\"")[0];
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("ERROR_MANUAL", "No se pudo cortar el texto");
        }

        holder.tvCategoria.setText(catNombre);

        // Carga de imagen
        if (!urlImagen.isEmpty() && !urlImagen.equals("null")) {
            Glide.with(holder.itemView.getContext()).load(urlImagen).into(holder.ivImagen);
        }

        holder.itemView.setOnClickListener(v -> listener.onProductoClick(listaProductos.get(position)));
    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    public static class ProductoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvPrecio, tvCategoria;
        ImageView ivImagen;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvProductoNombre);
            tvPrecio = itemView.findViewById(R.id.tvProductoPrecio);
            tvCategoria = itemView.findViewById(R.id.tvProductoCategoria);
            ivImagen = itemView.findViewById(R.id.ivProductoImagen);
        }
    }
}