package com.example.proyectointermodulartfg.controlador;

import android.content.Context;
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
import java.util.Map;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private List<Map<String, Object>> listaProductos;
    private Context context;

    public ProductoAdapter(List<Map<String, Object>> listaProductos, Context context) {
        this.listaProductos = listaProductos;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item_pantalla_principal, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Map<String, Object> producto = listaProductos.get(position);

        String nombre = producto.get("nombre") != null ? producto.get("nombre").toString() : "Sin nombre";
        Object precioObj = producto.get("precio");
        String precio = (precioObj != null) ? precioObj.toString() : "0.00";

        holder.tvNombre.setText(nombre);
        holder.tvPrecio.setText(precio + " €");

        Map<String, Object> categoriaMap = (Map<String, Object>) producto.get("Categorias");
        if (categoriaMap != null && categoriaMap.get("nombre_categoria") != null) {
            holder.tvCategoria.setText("Categoría: " + categoriaMap.get("nombre_categoria").toString());
        } else {
            holder.tvCategoria.setText("Categoría: General");
        }

        String urlImagen = producto.get("imagen") != null ? producto.get("imagen").toString() : "";

        Glide.with(context)
                .load(urlImagen)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .centerCrop()
                .into(holder.ivImagen);
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
