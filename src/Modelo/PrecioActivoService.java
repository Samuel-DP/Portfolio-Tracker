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
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

public class PrecioActivoService {

    private static final Set<String> TICKERS_STOCKS = new HashSet<>(Arrays.asList(
            "AAPL", "MSFT", "AMZN", "GOOGL", "TSLA", "NVDA", "RACE", "BLK", "V", "MA", "MSTR", "ASML", "ASTS"
    ));

    private static final String FINNHUB_BASE = "https://finnhub.io/api/v1";
    private static final String COINGECKO_BASE = "https://api.coingecko.com/api/v3";

    private PrecioActivoService() {
    }

    public static double obtenerPrecioActual(String activoDescripcion) {
        String simbolo = extraerSimbolo(activoDescripcion);
        if (simbolo.isBlank()) {
            return 0;
        }

        try {
            if (esStock(simbolo)) {
                return obtenerPrecioStock(simbolo);
            }
            return obtenerPrecioCrypto(simbolo);
        } catch (Exception e) {
            return 0;
        }
    }

    private static boolean esStock(String simbolo) {
        return TICKERS_STOCKS.contains(simbolo.toUpperCase());
    }

    private static double obtenerPrecioStock(String simbolo) throws Exception {
        String apiKey = vGlobales.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return 0;
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
            return 0;
        }

        JSONObject json = new JSONObject(response.body());
        return json.optDouble("c", 0);
    }

    private static double obtenerPrecioCrypto(String simbolo) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();

        String url = COINGECKO_BASE + "/coins/markets?vs_currency=usd"
                + "&symbols=" + URLEncoder.encode(simbolo.toLowerCase(), StandardCharsets.UTF_8)
                + "&order=market_cap_desc&per_page=1&page=1&sparkline=false";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return 0;
        }

        JSONArray arr = new JSONArray(response.body());
        if (arr.isEmpty()) {
            return 0;
        }

        JSONObject first = arr.getJSONObject(0);
        return first.optDouble("current_price", 0);
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
}
