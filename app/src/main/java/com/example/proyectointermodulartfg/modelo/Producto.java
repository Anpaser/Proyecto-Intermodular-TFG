package com.example.proyectointermodulartfg.modelo;

public class Producto {
    private int id;
    private int idUsuario;
    private String nombre;
    private String descripcion;
    private double precio;
    private String imagen;
    private String nombre_categoria;

    public Producto(int id, int idUsuario, String nombre, double precio, String imagen, String nombre_categoria) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.precio = precio;
        this.imagen = imagen;
        this.nombre_categoria = nombre_categoria;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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

    public String getNombre_categoria() {
        return nombre_categoria;
    }

    public void setNombre_categoria(String nombre_categoria) {
        this.nombre_categoria = nombre_categoria;
    }
}
