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
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
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
    @FXML
    private TableColumn<Transaccion, Void> colAcciones;

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
        configurarColumnaAcciones();

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

    private void eliminarTransaccion(Transaccion transaccion) {
        Integer usuarioId = vGlobales.getUsuarioIdActual();

        if (transaccion == null || usuarioId == null || transaccion.getId() <= 0) {
            return;
        }

        boolean eliminado = TransaccionesDAO.eliminarTransaccion(usuarioId, transaccion.getId());
        if (eliminado) {
            data.remove(transaccion);
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

    private void configurarColumnaAcciones() {
        Image iconoEditar = new Image(getClass().getResourceAsStream("/Imagenes/lapiz.png"));
        Image iconoEliminar = new Image(getClass().getResourceAsStream("/Imagenes/basura.png"));

        colAcciones.setCellFactory(col -> new TableCell<Transaccion, Void>() {
            private final Button btnEditar = crearBotonAccion(iconoEditar, "Editar");
            private final Button btnEliminar = crearBotonAccion(iconoEliminar, "Eliminar");
            private final HBox contenedor = new HBox(10, btnEditar, btnEliminar);

            {
                contenedor.setAlignment(Pos.CENTER);
                btnEditar.setOnAction(event -> {
                    Transaccion transaccion = getTableView().getItems().get(getIndex());
                    tbl_transacciones.getSelectionModel().select(transaccion);
                });

                btnEliminar.setOnAction(event -> {
                    Transaccion transaccion = getTableView().getItems().get(getIndex());
                    if (confirmarEliminacion(transaccion)) {
                        eliminarTransaccion(transaccion);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });
    }

    private Button crearBotonAccion(Image icono, String tooltip) {
        ImageView imageView = new ImageView(icono);
        imageView.setFitHeight(16);
        imageView.setFitWidth(16);
        imageView.setPreserveRatio(true);

        Button boton = new Button();
        boton.setGraphic(imageView);
        boton.setCursor(Cursor.HAND);
        boton.setStyle("-fx-background-color: transparent; -fx-padding: 2;");
        boton.setAccessibleText(tooltip);
        return boton;
    }

    private boolean confirmarEliminacion(Transaccion transaccion) {
        if (transaccion == null) {
            return false;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Seguro que quieres eliminar esta transacción?");

        return alert.showAndWait()
                .filter(buttonType -> buttonType == ButtonType.OK)
                .isPresent();
    }

}


// Me he quedado haciendo la ventana modal de actualizar cuando le doy al boton de mi lapicero e intentando entender bien esta ultima implementacion