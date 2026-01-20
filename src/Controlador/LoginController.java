
package Controlador;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.stage.Stage;

public class LoginController implements Initializable {

    @FXML
    private Button btnLogin;
    @FXML
    private Button btnRegistro;
    @FXML
    private Hyperlink linkRegistro;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }  
    
    @FXML
    private void onLogin(ActionEvent event) throws IOException{
        
        // Carga la vista principal
        Parent vistaPrincipal = FXMLLoader.load(getClass().getResource("/Vista/vistaPrincipal.fxml"));
        
        // Obtiene la ventana donde esta el login 
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        
        // Cambia la escena de login --> vistaPrincipal
        stage.setScene(new Scene(vistaPrincipal));
        
        // Centra la ventana en la pantalla 
        stage.centerOnScreen();
            
    }

    @FXML
    private void registrarse(ActionEvent event) throws IOException {
        
        Parent vistaRegistro = FXMLLoader.load(getClass().getResource("/Vista/vistaRegistro.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(vistaRegistro));
        
    }
    
    
    
}
