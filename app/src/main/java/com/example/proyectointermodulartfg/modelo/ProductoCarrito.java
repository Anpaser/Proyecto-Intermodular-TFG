package com.example.proyectointermodulartfg.modelo;

public class ProductoCarrito {
    private long id;
    private long id_usuario;
    private long id_producto;
    private int cantidad_seleccionada;
    private String nombre;
    private double precio;
    private String imagen;

    public ProductoCarrito() {}

    public ProductoCarrito(long id, long id_usuario, long id_producto, int cantidad_seleccionada, String nombre, double precio, String imagen) {
        this.id = id;
        this.id_usuario = id_usuario;
        this.id_producto = id_producto;
        this.cantidad_seleccionada = cantidad_seleccionada;
        this.nombre = nombre;
        this.precio = precio;
        this.imagen = imagen;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(long id_usuario) {
        this.id_usuario = id_usuario;
    }

    public long getId_producto() {
        return id_producto;
    }

    public void setId_producto(long id_producto) {
        this.id_producto = id_producto;
    }

    public int getCantidad_seleccionada() {
        return cantidad_seleccionada;
    }

    public void setCantidad_seleccionada(int cantidad_seleccionada) {
        this.cantidad_seleccionada = cantidad_seleccionada;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
}
