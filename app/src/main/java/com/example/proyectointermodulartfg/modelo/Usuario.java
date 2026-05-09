package com.example.proyectointermodulartfg.modelo;

public class Usuario {
    private long id;
    private  String nombre;
    private String email;
    private String clave;
    private long id_rol;

    public Usuario(){}

    public Usuario(long id, String nombre, String email, String clave, long id_rol){
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.clave = clave;
        this.id_rol = id_rol;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public long getId_rol() {
        return id_rol;
    }

    public void setId_rol(long id_rol) {
        this.id_rol = id_rol;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                ", clave='" + clave + '\'' +
                ", id_rol=" + id_rol +
                '}';
    }
}
