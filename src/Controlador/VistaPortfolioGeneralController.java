package Controlador;

import Dao.TransaccionesDAO;
import Modelo.ActivoPortfolioResumen;
import Modelo.PrecioActivoService;
import Modelo.Transaccion;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

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
    private LineChart<String, Number> grafico_lineal;
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

    private static final Locale LOCALE_ES = Locale.forLanguageTag("es-ES");
    private static final DecimalFormat FORMATO_NUMERICO_ES;

    static {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(LOCALE_ES);
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        FORMATO_NUMERICO_ES = new DecimalFormat("#,##0.00", symbols);
    }

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

        aplicarFormatoNumerico(col_precio, false, false);
        aplicarFormatoNumerico(col_24h, true, true);
        aplicarFormatoNumerico(col_inversiones, false, false);
        aplicarFormatoNumerico(col_uds, false, false);
        aplicarFormatoNumerico(col_precio_prom, false, false);
        aplicarFormatoNumerico(col_ganancia_perdida, false, true);
        aplicarFormatoNumerico(col_porcentVariacion, true, true);

        tbl_activos.setItems(dataActivos);
    }

    private void aplicarFormatoNumerico(TableColumn<ActivoPortfolioResumen, String> columna, boolean esPorcentaje, boolean colorearSigno) {
        columna.setCellFactory(col -> new TableCell<ActivoPortfolioResumen, String>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null || value.isBlank()) {
                    setText(null);
                    setStyle("");
                    return;
                }

                Double numero = extraerNumeroFlexible(value);
                if (numero == null) {
                    setText(value);
                    setStyle("");
                    return;
                }

                String texto = FORMATO_NUMERICO_ES.format(numero);
                if (esPorcentaje) {
                    texto += "%";
                }
                setText(texto);

                if (colorearSigno) {
                    if (numero >= 0) {
                        setStyle("-fx-text-fill: #17b070;");
                    } else {
                        setStyle("-fx-text-fill: #ec3c41;");
                    }
                } else {
                    setStyle("");
                }
            }
        });
    }

    private Double extraerNumeroFlexible(String textoOriginal) {
        String limpio = textoOriginal.replace("%", "")
                .replace("$", "")
                .replace("€", "")
                .replaceAll("\\s+", "")
                .trim();

        if (limpio.isBlank() || limpio.contains("--")) {
            return null;
        }

        int idxComa = limpio.lastIndexOf(',');
        int idxPunto = limpio.lastIndexOf('.');
        char decimalSep = idxComa > idxPunto ? ',' : '.';

        if (decimalSep == ',') {
            limpio = limpio.replace(".", "");
            limpio = limpio.replace(",", ".");
        } else {
            limpio = limpio.replace(",", "");
        }

        try {
            return Double.parseDouble(limpio);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void cargarActivosDesdeTransacciones() {
        dataActivos.clear();
        dataActivos.addAll(TransaccionesDAO.obtenerResumenActivosPortfolioActual());
        actualizarMejorYPeorActivo();
        actualizarResumenHistorico();
        actualizarSaldoActual();
        actualizarGraficoDistribucionActivos();
        actualizarGraficoLinealSaldo();
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

        aplicarResumenEnLabel(lbl_mejorActivo, mejorActivo);
        aplicarResumenEnLabel(lbl_peorActivo, peorActivo);
    }

    private void aplicarResumenEnLabel(Label label, ActivoPortfolioResumen activo) {
        if (activo == null) {
            label.setText("Sin datos");
            label.setTextFill(Color.BLACK);
            return;
        }

        Double variacion = extraerNumeroFlexible(activo.getPorcentVariacion());
        Double gananciaPerdida = extraerNumeroFlexible(activo.getGananciaPerdida());

        String variacionFmt = variacion == null ? "--" : FORMATO_NUMERICO_ES.format(variacion) + "%";
        String gananciaFmt = gananciaPerdida == null ? "--" : "$" + FORMATO_NUMERICO_ES.format(gananciaPerdida);

        label.setText(String.format("%s\n%s\n%s", extraerTicker(activo.getNombre()), variacionFmt, gananciaFmt));
        if (gananciaPerdida != null && gananciaPerdida >= 0) {
            label.setTextFill(Color.web("#17b070"));
        } else if (gananciaPerdida != null) {
            label.setTextFill(Color.web("#ec3c41"));
        } else {
            label.setTextFill(Color.BLACK);
        }
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

    private void actualizarGraficoDistribucionActivos() {
        ObservableList<PieChart.Data> datosPie = FXCollections.observableArrayList();
        Map<String, Double> inversionesPorTicker = new LinkedHashMap<>();
        double totalInversion = 0;

        for (ActivoPortfolioResumen activo : dataActivos) {
            if (activo == null) {
                continue;
            }

            double inversion = extraerInversionNumerica(activo.getInversion());
            if (inversion <= 0) {
                continue;
            }

            String ticker = extraerTicker(activo.getNombre());
            inversionesPorTicker.merge(ticker, inversion, Double::sum);
            totalInversion += inversion;
        }

        if (totalInversion <= 0) {
            grafico_donut.setData(datosPie);
            return;
        }

        for (Map.Entry<String, Double> entry : inversionesPorTicker.entrySet()) {
            double porcentaje = (entry.getValue() / totalInversion) * 100;
            String etiqueta = String.format("%s  %.2f%%", entry.getKey(), porcentaje);
            datosPie.add(new PieChart.Data(etiqueta, entry.getValue()));
        }

        grafico_donut.setData(datosPie);
        grafico_donut.setLabelsVisible(false);
        grafico_donut.setLegendVisible(true);
    }

    private double extraerInversionNumerica(String inversionTexto) {
        if (inversionTexto == null || inversionTexto.isBlank()) {
            return 0;
        }

        String limpio = inversionTexto.replaceAll("[^\\d.-]", "");
        if (limpio.isBlank()) {
            return 0;
        }

        try {
            return Double.parseDouble(limpio);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private void actualizarResumenHistorico() {
        TransaccionesDAO.BeneficioHistoricoResumen resumen = TransaccionesDAO.calcularBeneficioHistoricoPortfolioActual();
        lbl_baseDeCosto.setText(formatearMoneda(resumen.getBaseCosto()));
        lbl_beneficioHistorico.setText(String.format("%s %s",
                formatearMonedaConSigno(resumen.getBeneficioHistorico()),
                formatearPorcentajeConSigno(resumen.getPorcentajeRentabilidad())));
        if (resumen.getBeneficioHistorico() >= 0) {
            lbl_beneficioHistorico.setTextFill(Color.web("#17b070"));
        } else {
            lbl_beneficioHistorico.setTextFill(Color.web("#ec3c41"));
        }
    }

    private void actualizarSaldoActual() {
        double saldoActual = TransaccionesDAO.calcularSaldoActualPortfolioActual();
        lbl_saldoActual.setText(formatearMoneda(saldoActual));
    }

    private void actualizarGraficoLinealSaldo() {
        grafico_lineal.getData().clear();
        Integer portfolioId = TransaccionesDAO.obtenerPortfolioActualId();
        if (portfolioId == null) {
            return;
        }

        List<Transaccion> transacciones = TransaccionesDAO.obtenerTransaccionesPorPortfolio(portfolioId);
        if (transacciones.isEmpty()) {
            return;
        }

        transacciones.sort(Comparator.comparing(Transaccion::getFecha));
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM");
        XYChart.Series<String, Number> serieValorCartera = new XYChart.Series<>();
        serieValorCartera.setName("Valor cartera");

        Map<String, Double> unidadesPorActivo = new HashMap<>();
        Map<String, Double> ultimoPrecioPorActivo = new HashMap<>();
        Map<LocalDate, Double> valorPorFecha = new LinkedHashMap<>();

        for (Transaccion transaccion : transacciones) {
            if (transaccion == null || transaccion.getFecha() == null) {
                continue;
            }

            String activo = transaccion.getActivo();
            if (activo == null || activo.isBlank()) {
                continue;
            }

            actualizarUnidadesSegunTipo(transaccion, activo, unidadesPorActivo);
            ultimoPrecioPorActivo.put(activo, Math.abs(transaccion.getPrecioPorMoneda()));

            double valorTotal = calcularValorTotalCartera(unidadesPorActivo, ultimoPrecioPorActivo);
            LocalDate fecha = transaccion.getFecha().toLocalDate();

            if (valorTotal <= 0.000001d) {
                valorPorFecha.clear();
                unidadesPorActivo.clear();
                ultimoPrecioPorActivo.clear();
                valorPorFecha.put(fecha, 0.0);
                continue;
            }

            valorPorFecha.put(fecha, valorTotal);
        }

        if (valorPorFecha.isEmpty()) {
            return;
        }

        for (Map.Entry<LocalDate, Double> entry : valorPorFecha.entrySet()) {
            serieValorCartera.getData().add(new XYChart.Data<>(entry.getKey().format(formatoFecha), entry.getValue()));
        }

        LocalDate ultimaFechaHistorica = valorPorFecha.keySet().stream().reduce((first, second) -> second).orElse(LocalDate.now());
        LocalDate hoy = LocalDate.now();
        if (!ultimaFechaHistorica.equals(hoy)) {
            double valorActual = calcularValorActualConPreciosDeMercado(unidadesPorActivo, ultimoPrecioPorActivo);
            serieValorCartera.getData().add(new XYChart.Data<>(hoy.format(formatoFecha), valorActual));
        }

        grafico_lineal.setAnimated(false);
        grafico_lineal.setLegendVisible(false);
        grafico_lineal.getData().add(serieValorCartera);
    }

    private void actualizarUnidadesSegunTipo(Transaccion transaccion, String activo, Map<String, Double> unidadesPorActivo) {
        String tipo = transaccion.getTipo();
        double unidadesActuales = unidadesPorActivo.getOrDefault(activo, 0.0);
        double unidadesTransaccion = Math.abs(transaccion.getUnidades());

        if ("COMPRA".equalsIgnoreCase(tipo) || "Transferencia entrante".equalsIgnoreCase(tipo)) {
            unidadesPorActivo.put(activo, unidadesActuales + unidadesTransaccion);
            return;
        }

        if ("VENTA".equalsIgnoreCase(tipo) || "Transferencia saliente".equalsIgnoreCase(tipo)) {
            unidadesPorActivo.put(activo, Math.max(0, unidadesActuales - unidadesTransaccion));
        }
    }

    private double calcularValorTotalCartera(Map<String, Double> unidadesPorActivo, Map<String, Double> precioPorActivo) {
        double total = 0;

        for (Map.Entry<String, Double> entry : unidadesPorActivo.entrySet()) {
            double unidades = entry.getValue();
            if (unidades <= 0) {
                continue;
            }

            double precio = precioPorActivo.getOrDefault(entry.getKey(), 0.0);
            total += unidades * precio;
        }

        return total;
    }

    private double calcularValorActualConPreciosDeMercado(Map<String, Double> unidadesPorActivo, Map<String, Double> ultimoPrecioPorActivo) {
        Map<String, Double> preciosActuales = new HashMap<>(ultimoPrecioPorActivo);

        for (String activo : unidadesPorActivo.keySet()) {
            double precioActual = PrecioActivoService.obtenerPrecioActual(activo);
            if (precioActual > 0) {
                preciosActuales.put(activo, precioActual);
            }
        }

        return calcularValorTotalCartera(unidadesPorActivo, preciosActuales);
    }

    private String formatearMoneda(double valor) {
         return "$" + FORMATO_NUMERICO_ES.format(valor);
    }

    private String formatearMonedaConSigno(double valor) {
        return String.format("%s%s", valor >= 0 ? "+" : "-", formatearMoneda(Math.abs(valor)));
    }

    private String formatearPorcentajeConSigno(double valor) {
        return String.format(LOCALE_ES, "%+.2f%%", valor);
    }

}

//TAREAS POR HACER:
// Grafico lineal del saldo en mi cartera, HECHO
// Cambiar  todo el formato de las tablas en vez de . usar , Poner todas las tablas igual. FALTA
//Ajustar formato en transacciones 
// CREACION DE DISTINTOS PORTFOLIOS, POR DEFECTO TODO A 0
//OCULTAR DESOCULTAR ACTIVOS Y AJUSTAR EL FORMATO DE LOS NUMEROS A TODO IGUAL

