package Controlador;

import Modelo.Crypto;
import Modelo.Stock;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.scene.control.TableCell;

import org.json.JSONArray;
import org.json.JSONObject;

public class VistaMercadosController implements Initializable {

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

    @FXML
    private TableView<Stock> tablaAcciones;
    @FXML
    private TableColumn<Stock, String> colTicker;
    @FXML
    private TableColumn<Stock, String> colEmpresa;
    @FXML
    private TableColumn<Stock, Double> colPrecioAccion;
    @FXML
    private TableColumn<Stock, Double> colAcc1h;
    @FXML
    private TableColumn<Stock, Double> colAcc24h;
    @FXML
    private TableColumn<Stock, Double> colAcc7d;
    @FXML
    private TableColumn<Stock, Double> colAccMarketCap;
    @FXML
    private TableColumn<Stock, Double> colAccVolumen;

    private final ObservableList<Stock> datosStock = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // AL iniciar la vista mercados que cargue por defecto las cryptos
        mostrarCrypto();

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

        // Configuración de las columnas para Stock
        colTicker.setCellValueFactory(new PropertyValueFactory<>("ticker"));
        colEmpresa.setCellValueFactory(new PropertyValueFactory<>("company"));
        colPrecioAccion.setCellValueFactory(new PropertyValueFactory<>("price"));
        colAcc24h.setCellValueFactory(new PropertyValueFactory<>("change24h"));
        colAccVolumen.setCellValueFactory(new PropertyValueFactory<>("volume24h"));
        colAccMarketCap.setCellValueFactory(new PropertyValueFactory<>("marketCap"));

        aplicarFormatoPorcentaje(col1h);
        aplicarFormatoPorcentaje(col24h);
        aplicarFormatoPorcentaje(col7d);

        aplicarFormatoAbreviado(colMarketCap);
        aplicarFormatoAbreviado(colVolumen);
        aplicarFormatoAbreviado(colSupply);

        tablaCrypto.setItems(datosCrypto);

        cargarTop10Criptos(); // Para que cargue al iniciar
    }

    @FXML
    private void onFavoritos(ActionEvent event) {

    }

    @FXML
    private void onAcciones(ActionEvent event) {
        mostrarAcciones();
        cargarAcciones();
    }

    @FXML
    private void onCrypto(ActionEvent event) {
        mostrarCrypto();
        cargarTop10Criptos();
    }

    private void mostrarCrypto() {
        this.tablaCrypto.setVisible(true);
        this.tablaCrypto.setManaged(true);

        this.tablaAcciones.setVisible(false);
        this.tablaAcciones.setManaged(false);

    }

    private void mostrarAcciones() {
        this.tablaAcciones.setVisible(true);
        this.tablaAcciones.setManaged(true);

        this.tablaCrypto.setVisible(false);
        this.tablaCrypto.setManaged(false); // Evita que la tabla oculta deje huecos

    }

    private void cargarTop10Criptos() {
        Task<ObservableList<Crypto>> task = new Task<>() {
            @Override
            protected ObservableList<Crypto> call() throws Exception {

                String url = "https://api.coingecko.com/api/v3/coins/markets"
                        + "?vs_currency=usd"
                        + "&order=market_cap_desc"
                        + "&per_page=30"
                        + "&page=1"
                        + "&sparkline=false"
                        + "&price_change_percentage=1h,24h,7d";

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                JSONArray array = new JSONArray(response.body());

                ObservableList<Crypto> lista = FXCollections.observableArrayList();

                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);

                    String symbol = obj.getString("symbol").toUpperCase();
                    String name = obj.getString("name");
                    double price = obj.getDouble("current_price");
                    double change1h = obj.optDouble("price_change_percentage_1h_in_currency", 0);
                    double change24h = obj.optDouble("price_change_percentage_24h_in_currency", 0);
                    double change7d = obj.optDouble("price_change_percentage_7d_in_currency", 0);
                    double marketCap = obj.getDouble("market_cap");
                    double volume24h = obj.getDouble("total_volume");
                    double supply = obj.optDouble("circulating_supply", 0);

                    lista.add(new Crypto(symbol, name, price, change1h, change24h, change7d, marketCap, volume24h, supply));
                }

                return lista;
            }
        };

        task.setOnSucceeded(e -> {
            datosCrypto.setAll(task.getValue());
        });

        task.setOnFailed(e -> {
            task.getException().printStackTrace();
        });

        new Thread(task).start();
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

    private static final String POLYGON_KEY = "cHaYMa_WTylZY_zAVEbV_P0KZmuUtYJ9"; // Mi API KEY
    private static final String BASE = "https://api.polygon.io";

    public void cargarAcciones() {

        Task<ObservableList<Stock>> task = new Task<>() {
            @Override
            protected ObservableList<Stock> call() throws Exception {

                String[] tickers = {"AAPL", "MSFT", "AMZN", "GOOGL", "TSLA"};

                HttpClient client = HttpClient.newHttpClient();
                ObservableList<Stock> lista = FXCollections.observableArrayList();

                for (String t : tickers) {

                    // 1️⃣ INFO empresa + market cap
                    String urlInfo = BASE + "/v3/reference/tickers/" + t + "?apiKey=" + POLYGON_KEY;

                    HttpRequest reqInfo = HttpRequest.newBuilder()
                            .uri(URI.create(urlInfo))
                            .GET()
                            .build();

                    HttpResponse<String> resInfo = client.send(reqInfo, HttpResponse.BodyHandlers.ofString());
                    JSONObject infoRoot = new JSONObject(resInfo.body());
                    JSONObject info = infoRoot.getJSONObject("results");

                    String company = info.optString("name", t);
                    double marketCap = info.optDouble("market_cap", 0);

                    // 2️⃣ DAILY aggs → precio, %24h, volumen
                    LocalDate end = LocalDate.now();
                    LocalDate start = end.minusDays(5); // margen fines de semana

                    String urlDaily = BASE + "/v2/aggs/ticker/" + t + "/range/1/day/"
                            + start + "/" + end
                            + "?adjusted=true&sort=asc&limit=50000"
                            + "&apiKey=" + POLYGON_KEY;

                    HttpRequest reqDaily = HttpRequest.newBuilder()
                            .uri(URI.create(urlDaily))
                            .GET()
                            .build();

                    HttpResponse<String> resDaily = client.send(reqDaily, HttpResponse.BodyHandlers.ofString());
                    JSONObject dailyRoot = new JSONObject(resDaily.body());
                    JSONArray daily = dailyRoot.optJSONArray("results");

                    double price = 0;
                    double volume24h = 0;
                    double change24h = 0;

                    if (daily != null && daily.length() >= 2) {
                        JSONObject last = daily.getJSONObject(daily.length() - 1);
                        JSONObject prev = daily.getJSONObject(daily.length() - 2);

                        double closeToday = last.optDouble("c", 0);
                        double closePrev = prev.optDouble("c", 0);

                        price = closeToday;
                        volume24h = last.optDouble("v", 0);

                        if (closePrev != 0) {
                            change24h = (closeToday - closePrev) / closePrev * 100.0;
                        }
                    }

                    // 3️⃣ Añadir fila (SIN 1h y SIN 7d)
                    lista.add(new Stock(
                            t,
                            company,
                            price,
                            change24h,
                            volume24h,
                            marketCap
                    ));
                }

                return lista;
            }
        };

        task.setOnSucceeded(e -> {
            datosStock.setAll(task.getValue());
            tablaAcciones.setItems(datosStock);
        });

        task.setOnFailed(e -> task.getException().printStackTrace());

        new Thread(task).start();
    }

}
