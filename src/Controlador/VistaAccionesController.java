package Controlador;

import Modelo.Crypto;
import Modelo.Stock;
import Modelo.vGlobales;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.json.JSONObject;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.scene.control.TableCell;

public class VistaAccionesController implements Initializable {

    @FXML
    private TableView<Stock> tablaAcciones;
    @FXML
    private TableColumn<Stock, String> colTicker;
    @FXML
    private TableColumn<Stock, String> colEmpresa;
    @FXML
    private TableColumn<Stock, Double> colPrecioAccion;
    @FXML
    private TableColumn<Stock, Double> colAcc24h;
    @FXML
    private TableColumn<Stock, Double> colAccMarketCap;
    @FXML
    private TableColumn<Stock, Void> colFav;

    private final ObservableList<Stock> datosStock = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Configuración de las columnas para Stock
        colTicker.setCellValueFactory(new PropertyValueFactory<>("ticker"));
        colEmpresa.setCellValueFactory(new PropertyValueFactory<>("company"));
        colPrecioAccion.setCellValueFactory(new PropertyValueFactory<>("price"));
        colAcc24h.setCellValueFactory(new PropertyValueFactory<>("change24h"));
        colAccMarketCap.setCellValueFactory(new PropertyValueFactory<>("marketCap"));

        aplicarFormatoPorcentaje(colAcc24h);
        aplicarFormatoAbreviado(colAccMarketCap);

        cargarAcciones();

        colFav.setCellFactory(tc -> new TableCell<>() {
            private final javafx.scene.control.ToggleButton btn = new javafx.scene.control.ToggleButton();

            {
                btn.getStyleClass().add("fav-toggle");
                btn.setFocusTraversable(false);

                btn.setOnAction(e -> {
                    Stock row = getTableView().getItems().get(getIndex());

                    Modelo.Favoritos fav = new Modelo.Favoritos(
                            "STOCK",
                            row.getTicker(),
                            row.getCompany(),
                            row.getPrice(),
                            row.getChange24h(), 
                            row.getMarketCap(),
                            true
                    );

                    Modelo.FavoritesService.toggle(fav);

                    boolean isFav = Modelo.FavoritesService.isFavorite("STOCK", row.getTicker());
                    btn.setSelected(isFav);
                    btn.setText(isFav ? "★" : "☆");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                Stock row = getTableView().getItems().get(getIndex());
                boolean isFav = Modelo.FavoritesService.isFavorite("STOCK", row.getTicker());

                btn.setSelected(isFav);
                btn.setText(isFav ? "★" : "☆");
                setGraphic(btn);
            }
        });

    }

    private static final String FINNHUB_KEY = vGlobales.getApiKey();
    private static final String BASE = "https://finnhub.io/api/v1";

    // Executor 1 hilo
    private static final ExecutorService API_EXECUTOR
            = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("finnhub-api-thread");
                return t;
            });

    // ME HE QUEDADO INTENTANOD ENTENDER ESTE CODIGO Y PONERLO PARECIDO AL DE CRYPTO
    // Cache
    private static ObservableList<Stock> cacheStocks = FXCollections.observableArrayList();
    private static long cacheQuoteTsMs = 0;
    private static final long QUOTE_TTL_MS = 60_000; // 60s

    // Cache de perfiles por ticker 
    private static final Map<String, StockProfile> profileCache = new ConcurrentHashMap<>();
    private static final long PROFILE_TTL_MS = 24 * 60 * 60 * 1000L; // 24h

    private static volatile boolean cargando = false;

    private static class StockProfile {

        final String company;
        final double marketCap;
        final long tsMs;

        StockProfile(String company, double marketCap, long tsMs) {
            this.company = company;
            this.marketCap = marketCap;
            this.tsMs = tsMs;
        }
    }

    public void cargarAcciones() {

        long ahora = System.currentTimeMillis();

        // Si la cache de QUOTES está fresca, la uso y no llamo a la Api
        if (!cacheStocks.isEmpty() && (ahora - cacheQuoteTsMs) < QUOTE_TTL_MS) {
            datosStock.setAll(cacheStocks);
            tablaAcciones.setItems(datosStock);
            return;
        }

        // Evito doble carga
        if (cargando) {
            if (!cacheStocks.isEmpty()) {
                datosStock.setAll(cacheStocks);
                tablaAcciones.setItems(datosStock);
            }
            return;
        }

        cargando = true;

        Task<ObservableList<Stock>> task = new Task<>() {
            @Override
            protected ObservableList<Stock> call() throws Exception {

                if (FINNHUB_KEY == null || FINNHUB_KEY.isBlank()) {
                    throw new RuntimeException("FINNHUB_KEY no está configurada en variables de entorno.");
                }

                String[] tickers = {"AAPL", "MSFT", "AMZN", "GOOGL", "TSLA", "NVDA", "RACE", "BLK",
                    "V", "MA", "MSTR", "ASML", "ASTS", "NVO", "JD", "KOS", "GLNG", "PBR", "VAL"};

                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(8))
                        .build();

                ObservableList<Stock> lista = FXCollections.observableArrayList();

                for (String t : tickers) {

                    // PERFIL (cache 24h)
                    StockProfile profile = getProfileCached(client, t);

                    // QUOTE (precio y %)
                    QuoteData q = getQuote(client, t);

                    lista.add(new Stock(
                            t,
                            profile.company,
                            q.price,
                            q.change24h,
                            profile.marketCap
                    ));
                }

                return lista;
            }
        };

        task.setOnSucceeded(e -> {
            ObservableList<Stock> lista = task.getValue();

            // UI
            datosStock.setAll(lista);
            tablaAcciones.setItems(datosStock);

            // Cache quotes
            cacheStocks.setAll(lista);
            cacheQuoteTsMs = System.currentTimeMillis();

            cargando = false;
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            ex.printStackTrace();

            // NO vaciar tabla: usa cache si existe
            if (!cacheStocks.isEmpty()) {
                datosStock.setAll(cacheStocks);
                tablaAcciones.setItems(datosStock);
            }

            cargando = false;
        });

        API_EXECUTOR.submit(task);
    }

    private StockProfile getProfileCached(HttpClient client, String ticker) throws Exception {
        long now = System.currentTimeMillis();

        StockProfile cached = profileCache.get(ticker);
        if (cached != null && (now - cached.tsMs) < PROFILE_TTL_MS) {
            return cached;
        }

        String urlInfo = BASE + "/stock/profile2?symbol=" + ticker + "&token=" + FINNHUB_KEY;
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(urlInfo))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        String body = res.body() == null ? "" : res.body().trim();

        if (res.statusCode() != 200) {
            if (res.statusCode() == 429) {
                throw new RuntimeException("Rate limit (429) en profile2.");
            }
            throw new RuntimeException("HTTP " + res.statusCode() + " en profile2: " + recortar(body, 200));
        }

        JSONObject info = new JSONObject(body);

        String company = info.optString("name", ticker);
        double marketCap = info.optDouble("marketCapitalization", 0); // ojo: suele venir en millones

        StockProfile p = new StockProfile(company, marketCap, now);
        profileCache.put(ticker, p);
        return p;
    }

    private static class QuoteData {

        final double price;
        final double change24h;

        QuoteData(double price, double change24h) {
            this.price = price;
            this.change24h = change24h;
        }
    }

    private QuoteData getQuote(HttpClient client, String ticker) throws Exception {
        String urlQuote = BASE + "/quote?symbol=" + ticker + "&token=" + FINNHUB_KEY;

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(urlQuote))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        String body = res.body() == null ? "" : res.body().trim();

        if (res.statusCode() != 200) {
            if (res.statusCode() == 429) {
                throw new RuntimeException("Rate limit (429) en quote.");
            }
            throw new RuntimeException("HTTP " + res.statusCode() + " en quote: " + recortar(body, 200));
        }

        JSONObject quote = new JSONObject(body);
        double price = quote.optDouble("c", 0);
        double change24h = quote.optDouble("dp", 0);

        return new QuoteData(price, change24h);
    }

    // Simplifico los mensajes de errores
    private static String recortar(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    private void aplicarFormatoPorcentaje(TableColumn<Stock, Double> columna) {
        columna.setCellFactory(col -> new TableCell<Stock, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f%%", value));

                    if (value >= 0) {
                        setStyle("-fx-text-fill: #17b070;"); // verde
                    } else {
                        setStyle("-fx-text-fill: #ec3c41;"); // rojo
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

    private void aplicarFormatoAbreviado(TableColumn<Stock, Double> columna) {
        columna.setCellFactory(col -> new TableCell<Stock, Double>() {
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
