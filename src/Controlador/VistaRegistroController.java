
package Controlador;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.stage.Stage;

public class VistaRegistroController implements Initializable {

    @FXML
    private Hyperlink linkLogin;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void volverLogin(ActionEvent event) throws IOException {
        
        Parent vistaLogin = FXMLLoader.load(getClass().getResource("/Vista/Login.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(vistaLogin));
        
        // Centra la ventana en la pantalla 
        stage.centerOnScreen();
    }
    
}
