package Controlador;

import Dao.TransaccionesDAO;
import Modelo.ActivoPortfolioResumen;
import Modelo.PrecioActivoService;
import Modelo.Transaccion;
import java.net.URL;
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

//TAREAS POR HACER:
// Cambiar todo el formato de las tablas en vez de . usar , Poner todas las tablas igual. Ver que hacer con $ si quitarlos o ponerlo en las tabla. FALTA
// Falta hacer tabla cache de activos porque gasto las llamadas de api y se dejan de ver datos de mis estadisticas.IMPORTANTE!!!! HECHO
// Grafico lineal del saldo en mi cartera, me he quedado haciendolo bien!! FALTA HACERLO  BIEN
// CREACION DE DISTINTOS PORTFOLIOS, POR DEFECTO TODO A 0
//OCULTAR DESOCULTAR ACTIVOS Y AJUSTAR EL FORMATO DE LOS NUMEROS A TODO IGUAL

