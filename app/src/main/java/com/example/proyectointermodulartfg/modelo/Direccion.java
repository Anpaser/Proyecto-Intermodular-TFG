package com.example.proyectointermodulartfg.modelo;

import kotlinx.serialization.Serializable;

@Serializable
public class Direccion {
    private long id;
    private long id_usuario;
    private String ciudad;
    private String provincia;
    private String codigo_postal;
    private String calle;
    private String numero;
    private String letra;

    public Direccion() {
    }

    public Direccion(long id, long id_usuario, String ciudad, String provincia, String calle, String codigo_postal, String numero, String letra) {
        this.id = id;
        this.id_usuario = id_usuario;
        this.ciudad = ciudad;
        this.provincia = provincia;
        this.calle = calle;
        this.codigo_postal = codigo_postal;
        this.numero = numero;
        this.letra = letra;
    }

    public long getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(long id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getCodigo_postal() {
        return codigo_postal;
    }

    public void setCodigo_postal(String codigo_postal) {
        this.codigo_postal = codigo_postal;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getLetra() {
        return letra;
    }

    public void setLetra(String letra) {
        this.letra = letra;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
