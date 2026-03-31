package Modelo;

public class PortfolioItem {

    private final int id;
    private final String icono;
    private final String nombre;
    private final String descripcion;
    private final boolean esDefault;

    public PortfolioItem(int id, String icono, String nombre, String descripcion, boolean esDefault) {
        this.id = id;
        this.icono = icono;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.esDefault = esDefault;
    }

    public int getId() {
        return id;
    }

    public String getIcono() {
        return icono;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isEsDefault() {
        return esDefault;
    }

    public String getEtiquetaCompleta() {
        String prefijo = (icono == null || icono.trim().isEmpty()) ? "📊" : icono;
        return prefijo + " " + nombre;
    }
}
