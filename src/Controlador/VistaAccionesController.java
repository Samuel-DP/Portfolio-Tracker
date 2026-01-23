package Controlador;

import Modelo.Stock;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

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
    private TableColumn<Stock, Double> colAccVolumen;

    private final ObservableList<Stock> datosStock = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Configuración de las columnas para Stock
        colTicker.setCellValueFactory(new PropertyValueFactory<>("ticker"));
        colEmpresa.setCellValueFactory(new PropertyValueFactory<>("company"));
        colPrecioAccion.setCellValueFactory(new PropertyValueFactory<>("price"));
        colAcc24h.setCellValueFactory(new PropertyValueFactory<>("change24h"));
        colAccVolumen.setCellValueFactory(new PropertyValueFactory<>("volume24h"));
        colAccMarketCap.setCellValueFactory(new PropertyValueFactory<>("marketCap"));
        
        /*aplicarFormatoPorcentaje(colAcc24h);
        aplicarFormatoAbreviado(colAccMarketCap);
        aplicarFormatoAbreviado(colAccVolumen);*/
        
        cargarAcciones();

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
