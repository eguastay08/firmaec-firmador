/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.firmadigital.firmador;


/**
 * Datos del usuario para contruir la validacion CMS.
 * 
 * @author Ricardo Arguello <ricardo.arguello@soportelibre.com>
 */
public class DatosUsuario {

    private String nombre;
    private String apellido;
    private String institucion = "";
    private String cargo = "";
    private String serial;
    private String fechaFirmaArchivo;
    private String crl;
    private String archivo64;
    private String entidadCertificadora;
    private String mensaje;

    public DatosUsuario() {
    }

    private String cedula;

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getInstitucion() {
        return institucion;
    }

    public void setInstitucion(String institucion) {
        this.institucion = institucion;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getFechaFirmaArchivo() {
        return fechaFirmaArchivo;
    }

    public void setFechaFirmaArchivo(String fechaFirmaArchivo) {
        this.fechaFirmaArchivo = fechaFirmaArchivo;
    }

    public String getCrl() {
        return crl;
    }

    public void setCrl(String crl) {
        this.crl = crl;
    }

    public String getArchivo64() {
        return archivo64;
    }

    public void setArchivo64(String archivo64) {
        this.archivo64 = archivo64;
    }

    public String getEntidadCertificadora() {
        return entidadCertificadora;
    }

    public void setEntidadCertificadora(String entidadCertificadora) {
        this.entidadCertificadora = entidadCertificadora;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}