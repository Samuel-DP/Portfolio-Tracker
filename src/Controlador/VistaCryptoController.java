package Controlador;

import Modelo.Crypto;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.json.JSONArray;
import org.json.JSONObject;

public class VistaCryptoController implements Initializable {

    @FXML
    private Button btnFavoritos;
    @FXML
    private Button btnAcciones;
    @FXML
    private Button btnCrypto;
    @FXML
    private TableView<Crypto> tablaCrypto;
    @FXML
    private TableColumn<Crypto, String> colSimbolo;
    @FXML
    private TableColumn<Crypto, String> colNombre;
    @FXML
    private TableColumn<Crypto, Double> colPrecio;
    @FXML
    private TableColumn<Crypto, Double> col1h;
    @FXML
    private TableColumn<Crypto, Double> col24h;
    @FXML
    private TableColumn<Crypto, Double> col7d;
    @FXML
    private TableColumn<Crypto, Double> colMarketCap;
    @FXML
    private TableColumn<Crypto, Double> colVolumen;
    @FXML
    private TableColumn<Crypto, Double> colSupply;

    private final ObservableList<Crypto> datosCrypto = FXCollections.observableArrayList();

    // Cache simple en memoria
    private static ObservableList<Crypto> cacheCrypto = FXCollections.observableArrayList();
    private static long cacheTimestampMs = 0;

    // Ajuste de tiempo: para volver a llamar a la API 30s / 60s ...
    private static final long CACHE_TTL_MS = 60_000;

    // Para evitar 2 llamadas a la vez si el usuario entra rápido
    private static volatile boolean cargando = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Vinculamos las columnas con los getter de Crypto
        colSimbolo.setCellValueFactory(new PropertyValueFactory<>("symbol"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("price"));
        col1h.setCellValueFactory(new PropertyValueFactory<>("change1h"));
        col24h.setCellValueFactory(new PropertyValueFactory<>("change24h"));
        col7d.setCellValueFactory(new PropertyValueFactory<>("change7d"));
        colMarketCap.setCellValueFactory(new PropertyValueFactory<>("marketCap"));
        colVolumen.setCellValueFactory(new PropertyValueFactory<>("volume24h"));
        colSupply.setCellValueFactory(new PropertyValueFactory<>("circulatingSupply"));

        aplicarFormatoPorcentaje(col1h);
        aplicarFormatoPorcentaje(col24h);
        aplicarFormatoPorcentaje(col7d);

        aplicarFormatoAbreviado(colMarketCap);
        aplicarFormatoAbreviado(colVolumen);
        aplicarFormatoAbreviado(colSupply);

        tablaCrypto.setItems(datosCrypto);

        cargarTop10Criptos();

    }

    // Creo un gestor de hilos y utilizo un solo hilo para no crear 200 threads si cambio de vista
    private static final ExecutorService API_EXECUTOR
            = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("coingecko-api-thread");
                return t;
            });

    private void cargarTop10Criptos() {

        long ahora = System.currentTimeMillis();

        // Si hay cache fresca, la uso y NO llamo a la API
        if (!cacheCrypto.isEmpty() && (ahora - cacheTimestampMs) < CACHE_TTL_MS) {
            datosCrypto.setAll(cacheCrypto);
            return;
        }

        //  Evito llamadas duplicadas si ya se esta cargando
        if (cargando) {
            // Si hay algo en cache lo muéstro mientras
            if (!cacheCrypto.isEmpty()) {
                datosCrypto.setAll(cacheCrypto);
            }
            return;
        }

        cargando = true;

        Task<ObservableList<Crypto>> task = new Task<>() {
            @Override
            protected ObservableList<Crypto> call() throws Exception {
                return fetchListaDesdeCoinGecko(); 
            }
        };
        
        // Si todo va bien
        task.setOnSucceeded(e -> {
            ObservableList<Crypto> lista = task.getValue();

            // Actualizo tabla
            datosCrypto.setAll(lista);

            // Guardo cache
            cacheCrypto.setAll(lista);
            cacheTimestampMs = System.currentTimeMillis();

            cargando = false;
        });
        
        // Si algo falla
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            ex.printStackTrace();

            // MUY IMPORTANTE, No vaciar la tabla.
            // Si tenog cache, me aseguro de que se vea.
            if (!cacheCrypto.isEmpty()) {
                datosCrypto.setAll(cacheCrypto);
            }
            // Si no hay cache y tampoco datos previos, la tabla se quedará como esté.

            cargando = false;
        });

        API_EXECUTOR.submit(task);
    }

    private ObservableList<Crypto> fetchListaDesdeCoinGecko() throws Exception {

        String url = "https://api.coingecko.com/api/v3/coins/markets"
                + "?vs_currency=usd"
                + "&order=market_cap_desc"
                + "&per_page=30"
                + "&page=1"
                + "&sparkline=false"
                + "&price_change_percentage=1h,24h,7d";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        String body = response.body() == null ? "" : response.body().trim();

        if (status != 200) {
            if (status == 429) {
                throw new RuntimeException("Rate limit (429): demasiadas llamadas a CoinGecko.");
            }
            throw new RuntimeException("HTTP " + status + ". Body: " + (body.length() > 200 ? body.substring(0, 200) : body));
        }

        JSONArray array = new JSONArray(body);
        ObservableList<Crypto> lista = FXCollections.observableArrayList();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);

            String symbol = obj.optString("symbol", "").toUpperCase();
            String name = obj.optString("name", "N/A");
            double price = obj.optDouble("current_price", 0);

            double change1h = obj.optDouble("price_change_percentage_1h_in_currency", 0);
            double change24h = obj.optDouble("price_change_percentage_24h_in_currency", 0);
            double change7d = obj.optDouble("price_change_percentage_7d_in_currency", 0);

            double marketCap = obj.optDouble("market_cap", 0);
            double volume24h = obj.optDouble("total_volume", 0);
            double supply = obj.optDouble("circulating_supply", 0);

            lista.add(new Crypto(symbol, name, price, change1h, change24h, change7d, marketCap, volume24h, supply));
        }

        return lista;
    }

    private void aplicarFormatoPorcentaje(TableColumn<Crypto, Double> columna) {
        columna.setCellFactory(col -> new TableCell<Crypto, Double>() {
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

    private void aplicarFormatoAbreviado(TableColumn<Crypto, Double> columna) {
        columna.setCellFactory(col -> new TableCell<Crypto, Double>() {
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
