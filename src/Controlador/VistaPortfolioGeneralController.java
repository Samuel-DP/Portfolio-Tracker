package Controlador;

import Dao.TransaccionesDAO;
import Modelo.ActivoPortfolioResumen;
import Modelo.Transaccion;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;

public class VistaPortfolioGeneralController implements Initializable {

    @FXML
    private Pane contenidoPortfolio;
    @FXML
    private Pane prueba;
    @FXML
    private Pane prueba1;
    @FXML
    private Pane prueba11;
    @FXML
    private Pane prueba2;
    @FXML
    private Pane prueba3;
    @FXML
    private Pane prueba31;
    @FXML
    private Pane prueba4;
    @FXML
    private Label lbl_mejorActivo;
    @FXML
    private Label lbl_beneficioHistorico;
    @FXML
    private Label lbl_cambio24h;
    @FXML
    private Label lbl_saldoActual;
    @FXML
    private LineChart<?, ?> grafico_lineal;
    @FXML
    private PieChart grafico_donut;
    @FXML
    private Label lbl_peorActivo;
    @FXML
    private Pane prueba111;
    @FXML
    private Label lbl_baseDeCosto;

    @FXML
    private TableView<ActivoPortfolioResumen> tbl_activos;
    @FXML
    private TableColumn<ActivoPortfolioResumen, String> col_nombre;
    @FXML
    private TableColumn<ActivoPortfolioResumen, String> col_precio;
    @FXML
    private TableColumn<ActivoPortfolioResumen, String> col_24h;
    @FXML
    private TableColumn<ActivoPortfolioResumen, String> col_inversiones;
    @FXML
    private TableColumn<ActivoPortfolioResumen, String> col_uds;
    @FXML
    private TableColumn<ActivoPortfolioResumen, String> col_precio_prom;
    @FXML
    private TableColumn<ActivoPortfolioResumen, String> col_ganancia_perdida;
    @FXML
    private TableColumn<ActivoPortfolioResumen, String> col_porcentVariacion;

    private final ObservableList<ActivoPortfolioResumen> dataActivos = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTablaActivos();
        cargarActivosDesdeTransacciones();
    }

    private void configurarTablaActivos() {
        col_nombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        col_precio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        col_24h.setCellValueFactory(new PropertyValueFactory<>("cambio24h"));
        col_inversiones.setCellValueFactory(new PropertyValueFactory<>("inversion"));
        col_uds.setCellValueFactory(new PropertyValueFactory<>("unidades"));
        col_precio_prom.setCellValueFactory(new PropertyValueFactory<>("precioPromedioCompra"));
        col_ganancia_perdida.setCellValueFactory(new PropertyValueFactory<>("gananciaPerdida"));
        col_porcentVariacion.setCellValueFactory(new PropertyValueFactory<>("porcentVariacion"));

        tbl_activos.setItems(dataActivos);
    }

    private void cargarActivosDesdeTransacciones() {
        dataActivos.clear();
        dataActivos.addAll(TransaccionesDAO.obtenerResumenActivosPortfolioActual());
        actualizarMejorYPeorActivo();
        actualizarResumenHistorico();
        actualizarSaldoActual();
    }

    private void actualizarMejorYPeorActivo() {
        Comparator<ActivoPortfolioResumen> comparadorRendimiento = Comparator
                .comparingDouble(this::extraerGananciaPerdidaNumerica)
                .thenComparingDouble(this::extraerVariacionNumerica);

        ActivoPortfolioResumen mejorActivo = dataActivos.stream()
                .filter(activo -> activo != null)
                .max(comparadorRendimiento)
                .orElse(null);

        ActivoPortfolioResumen peorActivo = dataActivos.stream()
                .filter(activo -> activo != null)
                .min(comparadorRendimiento)
                .orElse(null);

        lbl_mejorActivo.setText(formatearResumenActivo(mejorActivo));
        lbl_peorActivo.setText(formatearResumenActivo(peorActivo));
    }

    private double extraerVariacionNumerica(ActivoPortfolioResumen activo) {
        String valor = activo.getPorcentVariacion();
        if (valor == null || valor.isBlank() || valor.contains("--")) {
            return 0;
        }

        String limpio = valor.replace("%", "")
                .replace(",", "")
                .trim();

        try {
            return Double.parseDouble(limpio);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private double extraerGananciaPerdidaNumerica(ActivoPortfolioResumen activo) {
        String valor = activo.getGananciaPerdida();
        if (valor == null || valor.isBlank() || valor.contains("--")) {
            return 0;
        }

        String limpio = valor.replace("$", "")
                .replace(",", "")
                .trim();

        try {
            return Double.parseDouble(limpio);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String formatearResumenActivo(ActivoPortfolioResumen activo) {
        if (activo == null) {
            return "Sin datos";
        }

        return String.format("%s\n%s\n%s",
                extraerTicker(activo.getNombre()),
                activo.getPorcentVariacion(),
                activo.getGananciaPerdida());
    }

    private String extraerTicker(String descripcionActivo) {
        if (descripcionActivo == null || descripcionActivo.isBlank()) {
            return "--";
        }

        String limpio = descripcionActivo.trim();
        int ultimoEspacio = limpio.lastIndexOf(' ');

        if (ultimoEspacio < 0 || ultimoEspacio == limpio.length() - 1) {
            return limpio;
        }

        return limpio.substring(ultimoEspacio + 1).trim();
    }

    private void actualizarResumenHistorico() {
        TransaccionesDAO.BeneficioHistoricoResumen resumen = TransaccionesDAO.calcularBeneficioHistoricoPortfolioActual();
        lbl_baseDeCosto.setText(formatearMoneda(resumen.getBaseCosto()));
        lbl_beneficioHistorico.setText(String.format("%s %s",
                formatearMonedaConSigno(resumen.getBeneficioHistorico()),
                formatearPorcentajeConSigno(resumen.getPorcentajeRentabilidad())));
    }

    private void actualizarSaldoActual() {
        double saldoActual = TransaccionesDAO.calcularSaldoActualPortfolioActual();
        lbl_saldoActual.setText(formatearMoneda(saldoActual));
    }

    private String formatearMoneda(double valor) {
        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
        return formatoMoneda.format(valor);
    }

    private String formatearMonedaConSigno(double valor) {
        return String.format("%s%s", valor >= 0 ? "+" : "-", formatearMoneda(Math.abs(valor)));
    }

    private String formatearPorcentajeConSigno(double valor) {
        return String.format("%+.2f%%", valor);
    }

}

// Cambiar todo el formato de las tablas en vez de . usar , Poner todas las tablas igual. Ver que hacer con $ si quitarlos o ponerlo en las tabla
// Hacer las estadisticass del portfolio esta semana ya junto con sus ajustes en tiempo real de saldos
// Best performer worst performer revisar con distintos ejemplos. Hecho
// Añadir Base de costo.  Hecho
//beneficio historico . Hecho
// Falta Hacer bien el saldo actual porque esta mal
//Falta hacer tabla cache de activos porque gasto las llamadas de api y se dejan de ver datos de mis estadisticas.

