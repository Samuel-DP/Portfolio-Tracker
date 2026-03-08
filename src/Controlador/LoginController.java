package Controlador;

import Dao.ConexionDB;
import Modelo.vGlobales;
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
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController implements Initializable {

    @FXML
    private Button btnLogin;
    @FXML
    private Hyperlink linkRegistro;
    @FXML
    private TextField txt_email;
    @FXML
    private PasswordField txt_passw;
    @FXML
    private Label lbl_errorCredenciales;
    @FXML
    private Button btn_ocultar;
    @FXML
    private TextField txt_passw_visible;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        txt_email.setOnKeyTyped(e -> limpiarError());
        txt_passw.setOnKeyTyped(e -> limpiarError());

        // Mantiene ambos campos sincronizados tanto mi passwordfiled como mi text field para mostrar la contraseña
        txt_passw_visible.textProperty().bindBidirectional(txt_passw.textProperty());

    }

    @FXML
    private void onLogin(ActionEvent event) throws IOException {

        ConexionDB conexion = new ConexionDB();

        lbl_errorCredenciales.setVisible(false);
        txt_email.setStyle(null);
        txt_passw.setStyle(null);

        vGlobales.USER = txt_email.getText();
        vGlobales.PASSWORD = txt_passw.getText();

        if (conexion.getConexion(vGlobales.getCadena(), vGlobales.USER, vGlobales.PASSWORD) != null) {

            // Carga la vista principal
            Parent vistaPrincipal = FXMLLoader.load(getClass().getResource("/Vista/vistaPrincipal.fxml"));

            // Obtiene la ventana donde esta el login 
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Cambia la escena de login --> vistaPrincipal
            stage.setScene(new Scene(vistaPrincipal));

            // Centra la ventana en la pantalla 
            stage.centerOnScreen();

        } else {

            lbl_errorCredenciales.setText("⚠ Email o contraseña incorrectos");
            lbl_errorCredenciales.setVisible(true);

            txt_email.setStyle("-fx-border-color: red;");
            txt_passw.setStyle("-fx-border-color: red;");

            txt_email.setText("");
            txt_passw.setText("");

        }

    }

    @FXML
    private void registrarse(ActionEvent event) throws IOException {

        Parent vistaRegistro = FXMLLoader.load(getClass().getResource("/Vista/vistaRegistro.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(vistaRegistro));

    }

    public void limpiarError() {

        lbl_errorCredenciales.setVisible(false);
        txt_email.setStyle(null);
        txt_passw.setStyle(null);

    }

    @FXML
    private void togglePassword(ActionEvent event) {

        boolean mostrar = txt_passw.isVisible();

        txt_passw.setVisible(!mostrar);
        txt_passw.setManaged(!mostrar);

        txt_passw_visible.setVisible(mostrar);
        txt_passw_visible.setManaged(mostrar);
    }

}
