package org.poli;

import javax.crypto.SecretKey;
import java.net.InetSocketAddress;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Objects;

public class Usuario {
    private InetSocketAddress direccion;
    private String nombre;

    private SecretKey secretKey;
    private PublicKey pubKey;

    public Usuario(InetSocketAddress direccion, String nombre, PublicKey pubKey, SecretKey secretKey ) {
        this.direccion = direccion;
        this.nombre = nombre;
        this.secretKey = secretKey;
        this.pubKey = pubKey;
    }

    public PublicKey getPubKey() {
        return pubKey;
    }

    public String getSecretKeyEncoded() {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    public SecretKey getSecretKey() {
        return this.secretKey;
    }

    public void setSecretKey(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    public void setPubKey(PublicKey pubKey) {
        this.pubKey = pubKey;
    }

    public InetSocketAddress getDireccion() {
        return direccion;
    }

    public void setDireccion(InetSocketAddress direccion) {
        this.direccion = direccion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String toString(){
        return nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return direccion.equals(usuario.direccion) && nombre.equals(usuario.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(direccion, nombre);
    }
}
