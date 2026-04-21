package Controlador;

import Dao.TransaccionesDAO;
import Modelo.ActivoPortfolioResumen;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
    }

}

// Me falta añadir que las transferencias entrantes me aparezcan en mi tabla de activos en Portfolio general  y que el precio promedio sea 00 y el % de variacion sea --
// Tambien me falta añadir un % de variación en la tabla de activos.
// Cambiar todo el formato de la tabla en vez de , usar .  como esta hecho todo en el resto de tablas y ver que hacer con $ si quitarlos o ponerlo en las tablas
