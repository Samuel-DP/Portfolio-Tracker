package Controlador;

import Modelo.PortfolioItem;
import Modelo.PortfolioService;
import Modelo.vGlobales;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class VistaPortfolioController implements Initializable {

    @FXML
    private Button btn_transacciones;
    @FXML
    private Pane PanelHorizontal;
    @FXML
    private Pane PanelVertical;
    @FXML
    private Pane contenidoPortfolio;
    @FXML
    private Button btn_vista_general;
    @FXML
    private Button btn_crear_cartera;
    @FXML
    private ScrollPane scrollPortfolios;
    @FXML
    private VBox vboxPortfolios;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            cargarVistaGeneral();
            PortfolioService.loadForCurrentUser();
            refrescarVistaPortfolios();
        } catch (IOException ex) {
            Logger.getLogger(VistaPortfolioController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void onTransacciones(ActionEvent event) throws IOException {

        Parent vistaTransacciones = FXMLLoader.load(getClass().getResource("/Vista/vistaTransacciones.fxml"));

        // Meto dentro de mi panel de contenidoPortfolio mi vistaTransacciones
        contenidoPortfolio.getChildren().setAll(vistaTransacciones);

    }

    @FXML
    private void OnVistaGeneral(ActionEvent event) throws IOException {
        cargarVistaGeneral();
    }

    private void cargarVistaGeneral() throws IOException {

        Parent vistaGeneralPortfolio = FXMLLoader.load(getClass().getResource("/Vista/vistaPortfolioGeneral.fxml"));

        contenidoPortfolio.getChildren().setAll(vistaGeneralPortfolio);

    }

    public void agregarPortfolio(String emoji, String nombre) {
        PortfolioService.add(emoji, nombre);
        refrescarVistaPortfolios();
    }

    private void refrescarVistaPortfolios() {
        vboxPortfolios.getChildren().clear();

        for (PortfolioItem portfolio : PortfolioService.getPortfolios()) {
            HBox filaPortfolio = new HBox(4);

            Button botonPortfolio = new Button(portfolio.getEtiquetaCompleta());
            botonPortfolio.setPrefHeight(25.0);
            botonPortfolio.setStyle("-fx-background-color: #2256fb;");
            botonPortfolio.setTextFill(javafx.scene.paint.Color.WHITE);
            botonPortfolio.setWrapText(true);
            botonPortfolio.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(botonPortfolio, Priority.ALWAYS);

            if (!portfolio.isEsDefault()) {
                Button btnEliminar = new Button("🗑");
                btnEliminar.setPrefHeight(25.0);
                btnEliminar.setPrefWidth(25.0);
                btnEliminar.setStyle("-fx-background-color:  #3e4349;");
                btnEliminar.setTextFill(javafx.scene.paint.Color.WHITE);
                btnEliminar.setOnAction(e -> confirmarYEliminarPortfolio(portfolio));
                filaPortfolio.getChildren().addAll(botonPortfolio, btnEliminar);
            } else {
                filaPortfolio.getChildren().add(botonPortfolio);
            }

            vboxPortfolios.getChildren().add(filaPortfolio);
        }
    }

    private void confirmarYEliminarPortfolio(PortfolioItem portfolio) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar portfolio");
        confirmacion.setHeaderText("¿Seguro que quieres eliminar este portfolio?");
        confirmacion.setContentText(portfolio.getEtiquetaCompleta());

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            PortfolioService.remove(portfolio);
            refrescarVistaPortfolios();
        }
    }

    @FXML
    private void onCrearCartera(ActionEvent event) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vista/vistaNuevaCartera.fxml")); // Defino mi vista de la ventana emergente
        Parent root = loader.load();

        VistaNuevaCarteraController controlador = loader.getController(); // Me permite comunicarme con la ventana modal

        controlador.setVistaPortfolioController(this);

        Scene scena = new Scene(root);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL); // Ventana Emergente, me bloquea lo que tenog por debajo
        stage.setResizable(false); // Esto me quita el boton de maximizar de mi ventana emergente, solo quiero que se pueda cerrar
        stage.setScene(scena);
        stage.showAndWait();
    }

}
