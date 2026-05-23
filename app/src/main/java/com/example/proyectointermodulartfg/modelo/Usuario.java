package com.example.proyectointermodulartfg.modelo;

import kotlinx.serialization.Serializable;

/**
 * Clase modelo que representa a un usuario de la aplicación.
 *
 * Contiene los datos personales y de autenticación. Soporta
 * tanto usuarios normales como vendedores (id_rol = 1).
 *
 * @author Angel Paredes Serrano
 * @version 1.0
 */
@Serializable
public class Usuario {
    private long id;
    private  String nombre;
    private String correo;
    private String clave;
    private long id_rol;
    private String telefono;

    public Usuario(){}

    public Usuario(String correo, String clave){
        this.correo = correo;
        this.clave = clave;
    }

    public Usuario(String nombre, String correo, String clave, String telefono){
        this.nombre = nombre;
        this.correo = correo;
        this.clave = clave;
        this.id_rol = 2;
        this.telefono = telefono;
    }

    public Usuario(long id, String nombre, String correo, long id_rol, String clave, String telefono){
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.clave = clave;
        this.id_rol = id_rol;
        this.telefono = telefono;

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

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getClave() { return clave; }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public long getId_rol() {
        return id_rol;
    }

    public void setId_rol(long id_rol) {
        this.id_rol = id_rol;
    }

    public String getTelefono() { return telefono; }

    public void setTelefono(String telefono) { this.telefono = telefono; }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", correo='" + correo + '\'' +
                ", clave='" + clave + '\'' +
                ", id_rol=" + id_rol +
                ", telefono='" + telefono + '\'' +
                '}';
    }
}
