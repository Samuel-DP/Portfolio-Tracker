
package Modelo;

import java.time.LocalDateTime;

public class Transaccion {
    
    private final int id;
    private final String tipo;
    private final LocalDateTime fecha;
    private final String activo;
    private final double unidades;
    private double precioPorMoneda;
    private  double importe;
    private final String notas;
    
    
    public Transaccion(String tipo, LocalDateTime fecha, String activo, double unidades, double precioPorMoneda, double importe, String notas){
        this.id = 0;
        this.tipo = tipo;
        this.fecha = fecha;
        this.activo = activo;
        this.unidades = unidades;
        this.precioPorMoneda = precioPorMoneda;
        this.importe = importe;
        this.notas = notas;
    }
    
    // COnstructor parametrizado para Transaccion Transferencia
    public Transaccion(String tipo, LocalDateTime fecha, String activo, double unidades, String notas){
        this.id = 0;
        this.tipo = tipo;
        this.fecha = fecha;
        this.activo = activo;
        this.precioPorMoneda = 0;
        this.importe = 0;
        this.unidades = unidades;
        this.notas = notas;
    }
    
    public Transaccion(int id, String tipo, LocalDateTime fecha, String activo, double unidades, double precioPorMoneda, double importe, String notas){
        this.id = id;
        this.tipo = tipo;
        this.fecha = fecha;
        this.activo = activo;
        this.unidades = unidades;
        this.precioPorMoneda = precioPorMoneda;
        this.importe = importe;
        this.notas = notas;
    }
    
    public int getId() { return id; }
    public String getTipo() { return tipo; }
    public LocalDateTime getFecha() { return fecha; }
    public String getActivo() { return activo; }
    public double getUnidades() { return unidades; }
    public double getPrecioPorMoneda() { return precioPorMoneda; }
    public double getImporte() { return importe; }
    public String getNotas() { return notas; }

    
}
