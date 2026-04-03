package Controlador;

import Modelo.Transaccion;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class VistaTransaccionVentaController implements Initializable {

    @FXML
    private ComboBox<String> cb_moneda;
    @FXML
    private DatePicker dp_fecha;
    @FXML
    private Button btn_agregarVenta;
    @FXML
    private TextField txt_total_gastado;
    @FXML
    private TextField txt_cantidad;
    @FXML
    private TextField txt_precio;
    @FXML
    private TextField txt_notas;

    private VistaAñadirTransaccionController parent;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        ObservableList<String> activosDisponibles = FXCollections.observableArrayList(
                "Bitcoin BTC", "Ethereum ETH", "Tether USDT", "XRP XRP", "BNB BNB", "USDC USDC", "Solana SOL", "TRON TRX",
                "Figure Heloc FIGR_HELOC", "Dogecoin DOGE", "USDS USDS", "WhiteBIT Coin WBT", "LEO Token LEO", "Cardano ADA", "Bitcoin Cash BCH",
                "Hyperliquid HYPE", "Chainlink LINK", "Monero XMR", "Ethena USDe USDE", "Stellar XLM", "Canton CC", "MemeCore M", "Dai DAI",
                "USD1 USD1", "Litecoin LTC", "PayPal USD PYUSD", "Zcash ZEC", "Hedera HBAR", "Avalanche AVAX", "Rain RAIN",
                "Apple Inc AAPL", "Microsoft Corp MSFT", "Amazon.com Inc AMZN", "Alphabet Inc GOOGL", "Tesla Inc TSLA", "NVIDIA Corp NVDA",
                "Ferrari NV RACE", "BlackRock Inc BLK", "Visa Inc V", "Mastercard Inc MA", "Strategy Inc MSTR", "ASML Holding NV ASML", "AST SpaceMobile Inc ASTS"
        );

        cb_moneda.setEditable(true);
        cb_moneda.setItems(activosDisponibles);

        FilteredList<String> activosFiltrados = new FilteredList<>(activosDisponibles, p -> true);

        cb_moneda.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            String textoBusqueda = newText == null ? "" : newText.trim().toLowerCase();

            activosFiltrados.setPredicate(activo
                    -> textoBusqueda.isBlank() || activo.toLowerCase().contains(textoBusqueda)
            );

            cb_moneda.setItems(activosFiltrados);
            if (!cb_moneda.isShowing()) {
                cb_moneda.show();
            }
        });

        cb_moneda.setOnAction(event -> {
            String seleccionado = cb_moneda.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                cb_moneda.getEditor().setText(seleccionado);
            }
        });

        // Recalculo cuando cambie cantidad o precio
        txt_cantidad.textProperty().addListener((obs, oldV, newV) -> recalcularTotal());
        txt_precio.textProperty().addListener((obs, oldV, newV) -> recalcularTotal());
    }

    public void setParentController(VistaAñadirTransaccionController parent) {
        this.parent = parent;
    }

    @FXML
    private void onAgregarVenta(ActionEvent event) {

        Validaciones v = new Validaciones();
        if (!v.validarCampos(cb_moneda, dp_fecha, txt_cantidad, txt_precio, txt_notas)) {
            return;
        }

        String activo = cb_moneda.getValue();
        double unidades = Double.parseDouble(txt_cantidad.getText());
        double precioPorMoneda = Double.parseDouble(txt_precio.getText());
        String notas = txt_notas.getText();

        LocalTime horaActual = LocalTime.now();
        LocalDate fecha = dp_fecha.getValue();

        LocalDateTime fechaHora = LocalDateTime.of(fecha, horaActual);

        double importe = unidades * precioPorMoneda;

        Transaccion t = new Transaccion("VENTA", fechaHora, activo, unidades, precioPorMoneda, importe, notas);

        parent.setResultadoAndClose(t);

    }

    private void recalcularTotal() {

        String cTxt = txt_cantidad.getText();
        String pTxt = txt_precio.getText();

        if (cTxt == null || cTxt.isBlank() || pTxt == null || pTxt.isBlank()) {
            txt_total_gastado.setText("");
            return;
        }

        try {
            // Si usas coma decimal, esto lo soporta:
            double cantidad = Double.parseDouble(cTxt.replace(",", "."));
            double precio = Double.parseDouble(pTxt.replace(",", "."));

            double total = cantidad * precio;

            txt_total_gastado.setText(String.format("%.2f €", total));

        } catch (NumberFormatException e) {
            // Si el usuario está escribiendo y aún no es un número válido
            txt_total_gastado.setText("");
        }
    }

}
