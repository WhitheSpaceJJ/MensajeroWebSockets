/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ws;

import java.util.Objects;

/**
 *
 * @author josej
 */
public class Mensaje {
    private String nombre;
    private String destinatario;
    private String asunto;
    private String remitente;
    private String mensaje;
    
    public Mensaje(String nombre, String destinatario, String asunto, String remitente, String mensaje) {
        this.nombre = nombre;
        this.destinatario = destinatario;
        this.asunto = asunto;
        this.remitente = remitente;
        this.mensaje = mensaje;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public String getDestinatario() {
        return destinatario;
    }
    
    public String getAsunto() {
        return asunto;
    }
    
    public String getRemitente() {
        return remitente;
    }
    
    public String getMensaje() {
        return mensaje;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.nombre);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Mensaje other = (Mensaje) obj;
        return Objects.equals(this.nombre, other.nombre);
    }
    
}