
package Controlador;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            cargarVistaGeneral();
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
    
    private void cargarVistaGeneral() throws IOException{
        
         Parent vistaGeneralPortfolio = FXMLLoader.load(getClass().getResource("/Vista/vistaPortfolioGeneral.fxml"));
         
         contenidoPortfolio.getChildren().setAll(vistaGeneralPortfolio);
      
    }

    @FXML
    private void onCrearCartera(ActionEvent event) throws IOException {
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vista/vistaNuevaCartera.fxml")); // Defino mi vista de la ventana emergente
        Parent root = loader.load();
        
        VistaNuevaCarteraController controlador = loader.getController(); // Me permite comunicarme con la ventana modal
        
        Scene scena = new Scene(root);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL); // Ventana Emergente, me bloquea lo que tenog por debajo
        stage.setResizable(false); // Esto me quita el boton de maximizar de mi ventana emergente, solo quiero que se pueda cerrar
        stage.setScene(scena);
        stage.showAndWait();
    }
    
    
}
