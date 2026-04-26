package Modelo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheMercadoService {

    private static final long TTL_MS = 15 * 60 * 1000L; // 15 minutos
    private static final Map<String, PrecioCacheado> CACHE_PRECIOS = new ConcurrentHashMap<>();

    private CacheMercadoService() {
    }

    public static void guardarCotizacion(String simbolo, double precio, double cambio24h) {
        if (simbolo == null || simbolo.isBlank() || precio <= 0) {
            return;
        }

        CACHE_PRECIOS.put(simbolo.trim().toUpperCase(), new PrecioCacheado(precio, cambio24h, System.currentTimeMillis()));
    }

    public static PrecioActivoService.CotizacionActivo obtenerCotizacion(String simbolo) {
        if (simbolo == null || simbolo.isBlank()) {
            return PrecioActivoService.CotizacionActivo.vacia();
        }

        PrecioCacheado cache = CACHE_PRECIOS.get(simbolo.trim().toUpperCase());
        if (cache == null) {
            return PrecioActivoService.CotizacionActivo.vacia();
        }

        if ((System.currentTimeMillis() - cache.timestampMs) > TTL_MS) {
            CACHE_PRECIOS.remove(simbolo.trim().toUpperCase());
            return PrecioActivoService.CotizacionActivo.vacia();
        }

        return new PrecioActivoService.CotizacionActivo(cache.precio, cache.cambio24h);
    }

    private static class PrecioCacheado {

        private final double precio;
        private final double cambio24h;
        private final long timestampMs;

        private PrecioCacheado(double precio, double cambio24h, long timestampMs) {
            this.precio = precio;
            this.cambio24h = cambio24h;
            this.timestampMs = timestampMs;
        }
    }
}
