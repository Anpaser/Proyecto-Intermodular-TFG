package com.example.proyectointermodulartfg.controlador;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.proyectointermodulartfg.R;
import com.example.proyectointermodulartfg.modelo.ProductoCarrito;

import java.util.List;

public class CarritoAdapter extends RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder> {

    private List<ProductoCarrito> items;
    private OnCarritoClickListener listener;

    public interface OnCarritoClickListener {
        void onUpdate(ProductoCarrito item);
        void onDelete(ProductoCarrito item);
    }

    public CarritoAdapter(List<ProductoCarrito> items, OnCarritoClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CarritoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item_pantalla_carrito, parent, false);
        return new CarritoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CarritoViewHolder holder, int position) {
        ProductoCarrito item = items.get(position);

        holder.tvNombre.setText(item.getNombre());
        holder.tvPrecio.setText(item.getPrecio() + " €");
        holder.tvCantidad.setText(String.valueOf(item.getCantidad_seleccionada()));

        Glide.with(holder.itemView.getContext())
                .load(item.getImagen())
                .into(holder.ivImagen);

        holder.btnSumar.setOnClickListener(v -> {
            item.setCantidad_seleccionada(item.getCantidad_seleccionada() + 1);
            notifyItemChanged(holder.getAdapterPosition());
            if (listener != null) listener.onUpdate(item);
        });

        holder.btnRestar.setOnClickListener(v -> {
            if (item.getCantidad_seleccionada() > 1) {
                item.setCantidad_seleccionada(item.getCantidad_seleccionada() - 1);
                notifyItemChanged(holder.getAdapterPosition());
                if (listener != null) listener.onUpdate(item);
            }
        });

        holder.btnEliminar.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (listener != null) listener.onDelete(item);
            items.remove(currentPos);
            notifyItemRemoved(currentPos);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class CarritoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImagen;
        TextView tvNombre, tvPrecio, tvCantidad;
        ImageButton btnSumar, btnRestar, btnEliminar;

        public CarritoViewHolder(@NonNull View view) {
            super(view);
            ivImagen = view.findViewById(R.id.ivCarritoImagen);
            tvNombre = view.findViewById(R.id.tvCarritoNombre);
            tvPrecio = view.findViewById(R.id.tvCarritoPrecio);
            tvCantidad = view.findViewById(R.id.tvCarritoCantidad);
            btnSumar = view.findViewById(R.id.btnCarritoSumar);
            btnRestar = view.findViewById(R.id.btnCarritoRestar);
            btnEliminar = view.findViewById(R.id.btnCarritoEliminar);
        }
    }
}
