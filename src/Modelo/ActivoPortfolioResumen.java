package Modelo;

public class ActivoPortfolioResumen {

    private final String nombre;
    private final String precio;
    private final String cambio24h;
    private final String inversion;
    private final String unidades;
    private final String precioPromedioCompra;
    private final String gananciaPerdida;
    private final String porcentVariacion;

    public ActivoPortfolioResumen(String nombre, String precio, String cambio24h, String inversion,
            String unidades, String precioPromedioCompra, String gananciaPerdida, String porcentVariacion) {
        this.nombre = nombre;
        this.precio = precio;
        this.cambio24h = cambio24h;
        this.inversion = inversion;
        this.unidades = unidades;
        this.precioPromedioCompra = precioPromedioCompra;
        this.gananciaPerdida = gananciaPerdida;
        this.porcentVariacion = porcentVariacion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getPrecio() {
        return precio;
    }

    public String getCambio24h() {
        return cambio24h;
    }

    public String getInversion() {
        return inversion;
    }

    public String getUnidades() {
        return unidades;
    }

    public String getPrecioPromedioCompra() {
        return precioPromedioCompra;
    }

    public String getGananciaPerdida() {
        return gananciaPerdida;
    }
    
    public String getPorcentVariacion() {
        return porcentVariacion;
    }
    
}
