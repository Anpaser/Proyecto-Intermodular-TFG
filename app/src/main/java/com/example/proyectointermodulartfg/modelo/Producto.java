package com.example.proyectointermodulartfg.modelo;

public class Producto {
    private int id;
    private int idUsuario;
    private String nombreVendedor;
    private String nombre;
    private String descripcion;
    private double precio;
    private String imagen;
    private String nombre_categoria;
    private int stock;

    public Producto() {}
    public Producto(int id, int idUsuario, String nombreVendedor, String nombre, String descripcion, double precio, String imagen, String nombre_categoria, int stock) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.nombreVendedor = nombreVendedor;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.imagen = imagen;
        this.nombre_categoria = nombre_categoria;
        this.stock = stock;
    }

    // Getters y Setters necesarios
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public double getPrecio() { return precio; }
    public String getImagen() { return imagen; }
    public String getNombre_categoria() { return nombre_categoria; }
    public String getNombreVendedor() { return nombreVendedor; }
    public int getStock() { return stock; }

    public void setStock(int stock) { this.stock = stock; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}