package Controlador;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

public class VistaMercadosController implements Initializable {

    @FXML
    private AnchorPane contenidoMercados;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            vistaCrypto();
        } catch (IOException ex) {
            Logger.getLogger(VistaMercadosController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    private void onFavoritos(ActionEvent event) throws IOException {

        Parent vistaFavs = FXMLLoader.load(getClass().getResource("/Vista/vistaFavoritos.fxml"));
        contenidoMercados.getChildren().setAll(vistaFavs);
    }

    @FXML
    private void onAcciones(ActionEvent event) throws IOException {
        
        Parent vistaAcciones = FXMLLoader.load(getClass().getResource("/Vista/vistaAcciones.fxml"));
        contenidoMercados.getChildren().setAll(vistaAcciones);
    }

    @FXML
    private void onCrypto(ActionEvent event) throws IOException {
        
       Parent vistaCrypto = FXMLLoader.load(getClass().getResource("/Vista/vistaCrypto.fxml"));
       contenidoMercados.getChildren().setAll(vistaCrypto);
       
    }
    
    private void vistaCrypto() throws IOException{
        
       Parent vistaCrypto = FXMLLoader.load(getClass().getResource("/Vista/vistaCrypto.fxml"));
       contenidoMercados.getChildren().setAll(vistaCrypto);
       
    }


    

}
