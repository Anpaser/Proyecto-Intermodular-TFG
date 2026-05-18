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
        JsonObject producto = listaProductos.get(position);

        String nombre = producto.get("nombre") != null ?
                producto.get("nombre").toString().replace("\"", "") : "Sin nombre";

        String precio = producto.get("precio") != null ?
                producto.get("precio").toString().replace("\"", "") : "0.00";

        String urlImagen = producto.get("imagen") != null ?
                producto.get("imagen").toString().replace("\"", "") : "";

        holder.tvNombre.setText(nombre);
        holder.tvPrecio.setText(precio + " €");

        String catNombre = "General";
        try {
            JsonElement rawCat = producto.get("Categorias");
            if (rawCat != null && !rawCat.toString().equals("null")) {
                String catString = rawCat.toString();

                if (catString.contains("nombre_categoria")) {
                    String[] partes = catString.split(":");
                    if (partes.length > 1) {
                        catNombre = partes[1].replace("}", "").replace("\"", "").trim();
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("SUPABASE_ADAPTER", "Error parseo categoría: " + e.getMessage());
        }

        holder.tvCategoria.setText(catNombre);

        if (!urlImagen.isEmpty() && !urlImagen.equals("null")) {
            Glide.with(holder.itemView.getContext())
                    .load(urlImagen)
                    .into(holder.ivImagen);
        }

        holder.itemView.setOnClickListener(v -> listener.onProductoClick(producto));
    }

    @Override
    public int getItemCount() {
        return listaProductos != null ? listaProductos.size() : 0;
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