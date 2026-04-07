package Controlador;

import Dao.TransaccionesDAO;
import Modelo.Transaccion;
import Modelo.vGlobales;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class VistaTransaccionesController implements Initializable {

    @FXML
    private Button btn_añadir_transaccion;
    @FXML
    private Button btn_eliminar_transaccion;
    @FXML
    private Pane saldoActual;

    @FXML
    private TableView<Transaccion> tbl_transacciones;
    @FXML
    private TableColumn<Transaccion, String> colTipo;
    @FXML
    private TableColumn<Transaccion, LocalDateTime> colFecha;
    @FXML
    private TableColumn<Transaccion, String> colActivos;
    @FXML
    private TableColumn<Transaccion, Double> colUnidades;
    @FXML
    private TableColumn<Transaccion, Double> colPrecioPorMoneda;
    @FXML
    private TableColumn<Transaccion, Double> colImporte;
    @FXML
    private TableColumn<Transaccion, String> colNotas;

    private ObservableList<Transaccion> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colActivos.setCellValueFactory(new PropertyValueFactory<>("activo"));
        colUnidades.setCellValueFactory(new PropertyValueFactory<>("unidades"));
        colPrecioPorMoneda.setCellValueFactory(new PropertyValueFactory<>("precioPorMoneda"));
        colImporte.setCellValueFactory(new PropertyValueFactory<>("importe"));
        colNotas.setCellValueFactory(new PropertyValueFactory<>("notas"));

        tbl_transacciones.setItems(data);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        colFecha.setCellFactory(col -> new TableCell<Transaccion, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(formatter));
            }
        });

        cargarTransaccionesDesdeDB();

    }

    @FXML
    private void OnEliminarTransacciones(ActionEvent event) {
        Transaccion seleccionada = tbl_transacciones.getSelectionModel().getSelectedItem();
        Integer usuarioId = vGlobales.getUsuarioIdActual();

        if (seleccionada == null || usuarioId == null || seleccionada.getId() <= 0) {
            return;
        }

        boolean eliminado = TransaccionesDAO.eliminarTransaccion(usuarioId, seleccionada.getId());
        if (eliminado) {
            data.remove(seleccionada);
        }
    }

    @FXML
    private void OnAñadirTransacciones(ActionEvent event) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vista/vistaAñadirTransaccion.fxml")); // Defino mi vista de la ventana emergente
        Parent root = loader.load();

        VistaAñadirTransaccionController controlador = loader.getController(); // Me permite comunicarme con la ventana modal

        Scene scena = new Scene(root);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL); // Ventana Emergente, me bloquea lo que tengo por debajo
        stage.setResizable(false); // Esto me quita el boton de maximizar de mi ventana emergente, solo quiero que se pueda cerrar
        stage.setScene(scena);
        stage.showAndWait();

        Transaccion nueva = controlador.getResultado();
        if (nueva != null) {
            Integer portfolioId = TransaccionesDAO.obtenerPortfolioActualId();
            if (portfolioId == null) {
                return;
            }

            Transaccion guardada = TransaccionesDAO.insertarTransaccion(portfolioId, nueva);
            if (guardada != null) {
                data.add(0, guardada);
                tbl_transacciones.refresh();
            }
        }

    }

    private void cargarTransaccionesDesdeDB() {
        Integer portfolioId = TransaccionesDAO.obtenerPortfolioActualId();
        data.clear();

        if (portfolioId == null) {
            return;
        }

        data.addAll(TransaccionesDAO.obtenerTransaccionesPorPortfolio(portfolioId));
    }

}
