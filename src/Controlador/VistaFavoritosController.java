package Controlador;

import Modelo.Favoritos;
import Modelo.vGlobales;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import org.json.JSONArray;
import org.json.JSONObject;

public class VistaFavoritosController implements Initializable {

    @FXML
    private TableView<Favoritos> tablaFavoritos;
    @FXML
    private TableColumn<Favoritos, String> colFav;
    @FXML
    private TableColumn<Favoritos, String> colTipo;
    @FXML
    private TableColumn<Favoritos, String> colSimbolTicker;
    @FXML
    private TableColumn<Favoritos, String> colNombre;
    @FXML
    private TableColumn<Favoritos, Double> colPrecio;
    @FXML
    private TableColumn<Favoritos, Double> col24h;
    @FXML
    private TableColumn<Favoritos, Double> colMarketCap;

    private final ObservableList<Favoritos> data = FXCollections.observableArrayList();
    
    private static final String FINNHUB_KEY = vGlobales.getApiKey();
    private static final String FINNHUB_BASE = "https://finnhub.io/api/v1";

    private static final ExecutorService API_EXECUTOR
            = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("favoritos-api-thread");
                return t;
            });

    private static final long CACHE_TTL_MS = 60_000;
    private static final Map<String, double[]> cacheMercadoFavoritos = new ConcurrentHashMap<>();
    private static volatile long cacheTimestampMs = 0;
    private static volatile boolean cargando = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colSimbolTicker.setCellValueFactory(new PropertyValueFactory<>("simboloTicker"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreEmpresa"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        col24h.setCellValueFactory(new PropertyValueFactory<>("porcentaje24h"));
        colMarketCap.setCellValueFactory(new PropertyValueFactory<>("marketCap"));

        aplicarFormatoPorcentaje(col24h);
        aplicarFormatoAbreviado(colMarketCap);

        colFav.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(""));

        tablaFavoritos.setItems(Modelo.FavoritesService.getFavoritos());
        actualizarDatosMercadoFavoritos();

        // Dentro de la colFav metemos el toogle de favoritos
        colFav.setCellFactory(tc -> new TableCell<>() {
            private final ToggleButton btn = new ToggleButton();

            {
                btn.getStyleClass().add("fav-toggle");
                btn.setFocusTraversable(false);
                btn.setOnAction(e -> {
                    Favoritos row = getTableView().getItems().get(getIndex());

                    boolean selected = btn.isSelected();
                    row.setFavorito(btn.isSelected());
                    btn.setText(btn.isSelected() ? "★" : "☆"); // 

                    // si lo desmarco de favoritos, lo quito de la lista
                    if (!selected) {
                        Modelo.FavoritesService.remove(row.getTipo(), row.getSimboloTicker());
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Favoritos row = getTableView().getItems().get(getIndex());
                btn.setSelected(row.isFavorito());
                btn.setText(row.isFavorito() ? "★" : "☆");
                setGraphic(btn);
            }
        });

    }

    // Añadimos los datos de precio %24h y Mcap que faltan a favoritos
    private void actualizarDatosMercadoFavoritos() {
        ObservableList<Favoritos> favoritos = Modelo.FavoritesService.getFavoritos();
        if (favoritos == null || favoritos.isEmpty()) {
            return;
        }

        long ahora = System.currentTimeMillis();

        // Si la cache sigue fresca, no hacemos llamadas a APIs.
        if (!cacheMercadoFavoritos.isEmpty() && (ahora - cacheTimestampMs) < CACHE_TTL_MS) {
            aplicarCacheAFavoritos(favoritos);
            tablaFavoritos.refresh();
            return;
        }

        // Evita disparar varias cargas si haces click rápido entre vistas.
        if (cargando) {
            if (!cacheMercadoFavoritos.isEmpty()) {
                aplicarCacheAFavoritos(favoritos);
                tablaFavoritos.refresh();
            }
            return;
        }

        cargando = true;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(8))
                        .build();

                Map<String, double[]> cryptoMap = obtenerTopCriptos(client);
                Map<String, double[]> nuevaCache = new HashMap<>();

                for (Favoritos fav : favoritos) {
                    if ("CRYPTO".equalsIgnoreCase(fav.getTipo())) {
                        double[] datos = cryptoMap.get(fav.getSimboloTicker().toUpperCase());
                        if (datos != null) {
                            nuevaCache.put(key(fav.getTipo(), fav.getSimboloTicker()), datos);
                            fav.setPrecio(datos[0]);
                            fav.setPorcentaje24h(datos[1]);
                            fav.setMarketCap(datos[2]);
                        }
                    } else if ("STOCK".equalsIgnoreCase(fav.getTipo())) {
                        double[] datos = obtenerDatosStock(client, fav.getSimboloTicker());
                        nuevaCache.put(key(fav.getTipo(), fav.getSimboloTicker()), datos);
                        fav.setPrecio(datos[0]);
                        fav.setPorcentaje24h(datos[1]);
                        fav.setMarketCap(datos[2]);
                    }
                }

                cacheMercadoFavoritos.clear();
                cacheMercadoFavoritos.putAll(nuevaCache);
                cacheTimestampMs = System.currentTimeMillis();

                return null;
            }
        };

        task.setOnSucceeded(e -> {
            tablaFavoritos.refresh();
            cargando = false;
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            if (ex != null) {
                ex.printStackTrace();
            }
            cargando = false;
        });

        API_EXECUTOR.submit(task);
    }

    private static String key(String tipo, String simbolo) {
        return tipo.toUpperCase() + ":" + simbolo.toUpperCase();
    }

    private void aplicarCacheAFavoritos(ObservableList<Favoritos> favoritos) {
        for (Favoritos fav : favoritos) {
            double[] datos = cacheMercadoFavoritos.get(key(fav.getTipo(), fav.getSimboloTicker()));
            if (datos == null) {
                continue;
            }
            fav.setPrecio(datos[0]);
            fav.setPorcentaje24h(datos[1]);
            fav.setMarketCap(datos[2]);
        }
    }

    private Map<String, double[]> obtenerTopCriptos(HttpClient client) throws Exception {
        String url = "https://api.coingecko.com/api/v3/coins/markets"
                + "?vs_currency=usd"
                + "&order=market_cap_desc"
                + "&per_page=250"
                + "&page=1"
                + "&sparkline=false"
                + "&price_change_percentage=24h";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(12))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new RuntimeException("HTTP " + res.statusCode() + " en CoinGecko.");
        }

        Map<String, double[]> map = new HashMap<>();
        JSONArray array = new JSONArray(res.body());
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            String symbol = obj.optString("symbol", "").toUpperCase();
            double precio = obj.optDouble("current_price", 0);
            double change24h = obj.optDouble("price_change_percentage_24h_in_currency", 0);
            double marketCap = obj.optDouble("market_cap", 0);
            map.put(symbol, new double[]{precio, change24h, marketCap});
        }

        return map;
    }

    private double[] obtenerDatosStock(HttpClient client, String ticker) throws Exception {
        String quoteUrl = FINNHUB_BASE + "/quote?symbol=" + ticker + "&token=" + FINNHUB_KEY;
        HttpRequest quoteReq = HttpRequest.newBuilder()
                .uri(URI.create(quoteUrl))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> quoteRes = client.send(quoteReq, HttpResponse.BodyHandlers.ofString());
        if (quoteRes.statusCode() != 200) {
            throw new RuntimeException("HTTP " + quoteRes.statusCode() + " en quote de " + ticker);
        }

        JSONObject quote = new JSONObject(quoteRes.body());
        double precio = quote.optDouble("c", 0);
        double change24h = quote.optDouble("dp", 0);

        String profileUrl = FINNHUB_BASE + "/stock/profile2?symbol=" + ticker + "&token=" + FINNHUB_KEY;
        HttpRequest profileReq = HttpRequest.newBuilder()
                .uri(URI.create(profileUrl))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> profileRes = client.send(profileReq, HttpResponse.BodyHandlers.ofString());
        if (profileRes.statusCode() != 200) {
            throw new RuntimeException("HTTP " + profileRes.statusCode() + " en profile2 de " + ticker);
        }

        JSONObject profile = new JSONObject(profileRes.body());
        double marketCap = profile.optDouble("marketCapitalization", 0);

        return new double[]{precio, change24h, marketCap};
    }

    private void aplicarFormatoPorcentaje(TableColumn<Favoritos, Double> columna) {
        columna.setCellFactory(col -> new TableCell<Favoritos, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f%%", value));

                    if (value >= 0) {
                        setStyle("-fx-text-fill: #17b070;");
                    } else {
                        setStyle("-fx-text-fill: #ec3c41;");
                    }
                }
            }
        });
    }

    private String abreviarNumero(double num) {
        if (num >= 1_000_000_000) {
            return String.format("%.2fB", num / 1_000_000_000);
        }
        if (num >= 1_000_000) {
            return String.format("%.2fM", num / 1_000_000);
        }
        if (num >= 1_000) {
            return String.format("%.2fK", num / 1_000);
        }
        return String.format("%.2f", num);
    }

    private void aplicarFormatoAbreviado(TableColumn<Favoritos, Double> columna) {
        columna.setCellFactory(col -> new TableCell<Favoritos, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(abreviarNumero(value));
                }
            }
        });
    }

}
