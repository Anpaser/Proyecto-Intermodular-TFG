package com.example.proyectointermodulartfg.modelo;

public class Pedido {
    private long id;
    private long id_usuario;
    private long id_direccion;
    private double precio_total;
    private String estado_pedido;
    private String fecha;

    public Pedido() {}

    public Pedido(long id_usuario, long id_direccion, double precio_total, String estado_pedido) {
        this.id_usuario = id_usuario;
        this.id_direccion = id_direccion;
        this.precio_total = precio_total;
        this.estado_pedido = estado_pedido;
    }

    // Getters y Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getId_usuario() { return id_usuario; }
    public void setId_usuario(long id_usuario) { this.id_usuario = id_usuario; }

    public long getId_direccion() { return id_direccion; }
    public void setId_direccion(long id_direccion) { this.id_direccion = id_direccion; }

    public double getPrecio_total() { return precio_total; }
    public void setPrecio_total(double precio_total) { this.precio_total = precio_total; }

    public String getEstado_pedido() { return estado_pedido; }
    public void setEstado_pedido(String estado_pedido) { this.estado_pedido = estado_pedido; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
}