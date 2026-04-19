package Modelo;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONArray;
import org.json.JSONObject;

public class PrecioActivoService {

    private static final Set<String> TICKERS_STOCKS = new HashSet<>(Arrays.asList(
            "AAPL", "MSFT", "AMZN", "GOOGL", "TSLA", "NVDA", "RACE", "BLK", "V", "MA", "MSTR", "ASML", "ASTS"
    ));

    private static final String FINNHUB_BASE = "https://finnhub.io/api/v1";
    private static final String COINGECKO_BASE = "https://api.coingecko.com/api/v3";
    private static final long CACHE_TTL_MS = 60_000;
    private static final Map<String, CacheCotizacion> CACHE_COTIZACIONES = new ConcurrentHashMap<>();
    private static final Set<String> CARGANDO_SIMBOLOS = ConcurrentHashMap.newKeySet();

    private PrecioActivoService() {
    }

    public static double obtenerPrecioActual(String activoDescripcion) {
        return obtenerCotizacion(activoDescripcion).getPrecioActual();
    }

    public static CotizacionActivo obtenerCotizacion(String activoDescripcion) {
        String simbolo = extraerSimbolo(activoDescripcion);
        if (simbolo.isBlank()) {
            return CotizacionActivo.vacia();
        }

        long ahora = System.currentTimeMillis();
        CacheCotizacion cache = CACHE_COTIZACIONES.get(simbolo);
        if (cache != null && (ahora - cache.timestampMs) < CACHE_TTL_MS) {
            return cache.cotizacion;
        }

        if (!CARGANDO_SIMBOLOS.add(simbolo)) {
            return cache != null ? cache.cotizacion : CotizacionActivo.vacia();
        }

        try {
            CotizacionActivo cotizacion = obtenerCotizacionDesdeApi(simbolo);
            CACHE_COTIZACIONES.put(simbolo, new CacheCotizacion(cotizacion, System.currentTimeMillis()));
            return cotizacion;
        } catch (Exception e) {
            return cache != null ? cache.cotizacion : CotizacionActivo.vacia();
        } finally {
            CARGANDO_SIMBOLOS.remove(simbolo);
        }
    }

    private static CotizacionActivo obtenerCotizacionDesdeApi(String simbolo) throws Exception {
        if (esStock(simbolo)) {
            return obtenerCotizacionStock(simbolo);
        }

        return obtenerCotizacionCrypto(simbolo);
    }

    private static boolean esStock(String simbolo) {
        return TICKERS_STOCKS.contains(simbolo.toUpperCase());
    }

    private static CotizacionActivo obtenerCotizacionStock(String simbolo) throws Exception {
        String apiKey = vGlobales.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return CotizacionActivo.vacia();
        }

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();

        String url = FINNHUB_BASE + "/quote?symbol=" + URLEncoder.encode(simbolo, StandardCharsets.UTF_8)
                + "&token=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return CotizacionActivo.vacia();
        }

        JSONObject json = new JSONObject(response.body());
        double precioActual = json.optDouble("c", 0);
        double precioPrevio = json.optDouble("pc", 0);
        double cambio24h = 0;

        if (precioPrevio > 0) {
            cambio24h = ((precioActual - precioPrevio) / precioPrevio) * 100;
        }

        return new CotizacionActivo(precioActual, cambio24h);
    }

    private static CotizacionActivo obtenerCotizacionCrypto(String simbolo) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();

        String url = COINGECKO_BASE + "/coins/markets?vs_currency=usd"
                + "&symbols=" + URLEncoder.encode(simbolo.toLowerCase(), StandardCharsets.UTF_8)
                + "&order=market_cap_desc&per_page=1&page=1&sparkline=false"
                + "&price_change_percentage=24h";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return CotizacionActivo.vacia();
        }

        JSONArray arr = new JSONArray(response.body());
        if (arr.isEmpty()) {
            return CotizacionActivo.vacia();
        }

        JSONObject first = arr.getJSONObject(0);
        double precioActual = first.optDouble("current_price", 0);
        double cambio24h = first.optDouble("price_change_percentage_24h", 0);

        return new CotizacionActivo(precioActual, cambio24h);
    }

    private static String extraerSimbolo(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            return "";
        }

        String valor = descripcion.trim();
        int ultimoEspacio = valor.lastIndexOf(' ');

        if (ultimoEspacio < 0 || ultimoEspacio == valor.length() - 1) {
            return valor.toUpperCase();
        }

        return valor.substring(ultimoEspacio + 1).trim().toUpperCase();
    }

    public static class CotizacionActivo {

        private final double precioActual;
        private final double cambioPorcentual24h;

        public CotizacionActivo(double precioActual, double cambioPorcentual24h) {
            this.precioActual = precioActual;
            this.cambioPorcentual24h = cambioPorcentual24h;
        }

        public double getPrecioActual() {
            return precioActual;
        }

        public double getCambioPorcentual24h() {
            return cambioPorcentual24h;
        }

        public static CotizacionActivo vacia() {
            return new CotizacionActivo(0, 0);
        }
    }

    private static class CacheCotizacion {

        private final CotizacionActivo cotizacion;
        private final long timestampMs;

        private CacheCotizacion(CotizacionActivo cotizacion, long timestampMs) {
            this.cotizacion = cotizacion;
            this.timestampMs = timestampMs;
        }
    }

}
