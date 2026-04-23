package Controlador;

import Dao.TransaccionesDAO;
import Modelo.Transaccion;
import Modelo.vGlobales;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class VistaTransaccionesController implements Initializable {

    @FXML
    private Button btn_añadir_transaccion;
    @FXML
    private Button btn_exportar_csv;
    @FXML
    private Pane saldoActual;
    @FXML
    private Label lbl_saldo;
    @FXML
    private Pane prueba111;
    @FXML
    private Label lbl_baseDeCosto;

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
            actualizarSaldoActual();
            actualizarBaseDeCosto();
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
                actualizarSaldoActual();
                actualizarBaseDeCosto();
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
        actualizarSaldoActual();
        actualizarBaseDeCosto();
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
                    editarTransaccion(transaccion);
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

    private void editarTransaccion(Transaccion original) {
        Integer usuarioId = vGlobales.getUsuarioIdActual();
        if (original == null || usuarioId == null || original.getId() <= 0) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vista/vistaAñadirTransaccion.fxml"));
            Parent root = loader.load();

            VistaAñadirTransaccionController controlador = loader.getController();
            controlador.setTransaccionParaEditar(original);

            Scene scena = new Scene(root);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setScene(scena);
            stage.showAndWait();

            Transaccion editada = controlador.getResultado();
            if (editada == null) {
                return;
            }

            Transaccion actualizada = TransaccionesDAO.actualizarTransaccion(usuarioId, original.getId(), editada);
            if (actualizada == null) {
                return;
            }

            int indice = data.indexOf(original);
            if (indice >= 0) {
                data.set(indice, actualizada);
            }
            tbl_transacciones.refresh();
            actualizarSaldoActual();
            actualizarBaseDeCosto();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void actualizarSaldoActual() {
        double capitalTotal = 0;

        for (Transaccion transaccion : data) {
            if (transaccion == null || transaccion.getTipo() == null) {
                continue;
            }

            String tipo = transaccion.getTipo().trim();
            double importe = obtenerImporteSeguro(transaccion);

            switch (tipo) {
                case "COMPRA":
                case "Transferencia entrante":
                    capitalTotal += importe;
                    break;
                case "VENTA":
                case "Transferencia saliente":
                    capitalTotal -= importe;
                    break;
                default:
                    break;
            }
        }

        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
        lbl_saldo.setText(formatoMoneda.format(capitalTotal));
    }

    private double obtenerImporteSeguro(Transaccion transaccion) {
        double importe = transaccion.getImporte();

        if (Double.isNaN(importe) || Double.isInfinite(importe) || importe == 0) {
            importe = transaccion.getUnidades() * transaccion.getPrecioPorMoneda();
        }

        return Math.abs(importe);
    }

    private void actualizarBaseDeCosto() {
        double baseDeCosto = 0;

        for (Transaccion transaccion : data) {
            if (transaccion == null || transaccion.getTipo() == null) {
                continue;
            }

            String tipo = transaccion.getTipo().trim();
            if (!"COMPRA".equalsIgnoreCase(tipo)) {
                continue;
            }

            double precio = Math.abs(transaccion.getPrecioPorMoneda());
            double unidades = Math.abs(transaccion.getUnidades());

            if (Double.isNaN(precio) || Double.isInfinite(precio)
                    || Double.isNaN(unidades) || Double.isInfinite(unidades)) {
                continue;
            }

            baseDeCosto += precio * unidades;
        }

        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
        lbl_baseDeCosto.setText(formatoMoneda.format(baseDeCosto));
    }

    @FXML
    private void onExportarCSV(ActionEvent event) {
        if (data.isEmpty()) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Sin datos", "No hay transacciones para exportar.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar transacciones en CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo CSV", "*.csv"));
        fileChooser.setInitialFileName("transacciones.csv");

        Stage stage = (Stage) tbl_transacciones.getScene().getWindow();
        File archivo = fileChooser.showSaveDialog(stage);

        if (archivo == null) {
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
            writer.write("Tipo,Fecha,Activo,Unidades,Precio por moneda,Importe,Notas");
            writer.newLine();

            for (Transaccion transaccion : data) {
                String fila = String.join(",",
                        escaparCsv(transaccion.getTipo()),
                        escaparCsv(transaccion.getFecha() != null ? transaccion.getFecha().format(formatter) : ""),
                        escaparCsv(transaccion.getActivo()),
                        String.valueOf(transaccion.getUnidades()),
                        String.valueOf(transaccion.getPrecioPorMoneda()),
                        String.valueOf(transaccion.getImporte()),
                        escaparCsv(transaccion.getNotas())
                );

                writer.write(fila);
                writer.newLine();
            }

            mostrarAlerta(Alert.AlertType.INFORMATION, "Exportación completada", "CSV exportado correctamente en:\n" + archivo.getAbsolutePath());
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al exportar", "No se pudo exportar el archivo CSV.");
        }
    }

    private String escaparCsv(String valor) {
        if (valor == null) {
            return "\"\"";
        }
        String valorEscapado = valor.replace("\"", "\"\"");
        return "\"" + valorEscapado + "\"";
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

}
