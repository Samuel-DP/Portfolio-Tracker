package Controlador;

import Controlador.Validaciones;
import Controlador.VistaAñadirTransaccionController;
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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class VistaAñadirTransaccionTransferirController implements Initializable {

    @FXML
    private Button btn_transferir;
    @FXML
    private ComboBox<String> cb_moneda;
    @FXML
    private ComboBox<String> cb_transferencia;
    @FXML
    private TextField txt_cantidad;
    @FXML
    private DatePicker dp_fecha;
    @FXML
    private TextField txt_notas;

    private VistaAñadirTransaccionController parent;
    @FXML
    private Label lbl_errorTransaccion;

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

        cb_transferencia.getItems().addAll("Transferencia entrante", "Transferencia saliente");

        Validaciones v = new Validaciones();
        cb_moneda.getEditor().setOnKeyTyped(e -> v.limpiarError(lbl_errorTransaccion, cb_moneda, cb_transferencia, txt_cantidad, txt_notas, dp_fecha));
        cb_moneda.setOnMouseClicked(e -> v.limpiarError(lbl_errorTransaccion, cb_moneda, cb_transferencia, txt_cantidad, txt_notas, dp_fecha));
        cb_transferencia.setOnAction(e -> v.limpiarError(lbl_errorTransaccion, cb_moneda, cb_transferencia, txt_cantidad, txt_notas, dp_fecha));
        txt_cantidad.setOnKeyTyped(e -> v.limpiarError(lbl_errorTransaccion, cb_moneda, cb_transferencia, txt_cantidad, txt_notas, dp_fecha));
        txt_notas.setOnKeyTyped(e -> v.limpiarError(lbl_errorTransaccion, cb_moneda, cb_transferencia, txt_cantidad, txt_notas, dp_fecha));
        dp_fecha.setOnAction(e -> v.limpiarError(lbl_errorTransaccion, cb_moneda, cb_transferencia, txt_cantidad, txt_notas, dp_fecha));

    }

    public void setParentController(VistaAñadirTransaccionController parent) {
        this.parent = parent;
    }

    public void cargarTransaccion(Transaccion transaccion) {
        if (transaccion == null) {
            return;
        }

        cb_moneda.setValue(transaccion.getActivo());
        cb_moneda.getEditor().setText(transaccion.getActivo());
        cb_transferencia.setValue(transaccion.getTipo());
        txt_cantidad.setText(String.valueOf(transaccion.getUnidades()));
        dp_fecha.setValue(transaccion.getFecha().toLocalDate());
        txt_notas.setText(transaccion.getNotas());
    }

    @FXML
    private void onTransferir(ActionEvent event) {

        Validaciones v = new Validaciones();
        if (!v.validarTransferir(cb_moneda, cb_transferencia, txt_cantidad, txt_notas, dp_fecha, lbl_errorTransaccion)) {
            return;
        }

        String activo = cb_moneda.getValue();
        String tipo = cb_transferencia.getValue();
        String cantidadNormalizada = v.normalizarDecimal(txt_cantidad.getText());
        txt_cantidad.setText(cantidadNormalizada);
        double cantidad = Double.parseDouble(cantidadNormalizada);
        String notas = txt_notas.getText();

        LocalTime horaActual = LocalTime.now();
        LocalDate fecha = dp_fecha.getValue();

        LocalDateTime fechaHora = LocalDateTime.of(fecha, horaActual);

        Transaccion t = new Transaccion(tipo, fechaHora, activo, cantidad, notas);

        parent.setResultadoAndClose(t);

    }

}
