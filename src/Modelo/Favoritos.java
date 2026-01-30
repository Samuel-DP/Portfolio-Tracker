
package Modelo;


public class Favoritos {
    
    private String tipo;
    private String simboloTicker;
    private String nombreEmpresa;
    private double precio;
    private double porcentaje24h;
    private double marketCap;
    private boolean favorito;

    public Favoritos(String tipo, String simboloTicker, String nombreEmpresa, double precio, double porcentaje24h, double marketCap, boolean favorito) {
        this.tipo = tipo;
        this.simboloTicker = simboloTicker;
        this.nombreEmpresa = nombreEmpresa;
        this.precio = precio;
        this.porcentaje24h = porcentaje24h;
        this.marketCap = marketCap;
        this.favorito = favorito;
    }

    public String getTipo() {
        return tipo;
    }

    public String getSimboloTicker() {
        return simboloTicker;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public double getPrecio() {
        return precio;
    }

    public double getPorcentaje24h() {
        return porcentaje24h;
    }

    public double getMarketCap() {
        return marketCap;
    }

    public boolean isFavorito() {
        return favorito;
    }
    
    public void setFavorito(boolean favorito) {
        this.favorito = favorito;
    }
    
    
}
