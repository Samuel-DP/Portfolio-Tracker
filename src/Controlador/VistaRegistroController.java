package Controlador;

import Modelo.Usuario;
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
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import Dao.UsuarioDAO;

public class VistaRegistroController implements Initializable {

    @FXML
    private Hyperlink linkLogin;
    @FXML
    private TextField txt_email;
    @FXML
    private PasswordField txt_passw;
    @FXML
    private Button btn_registrarse;
    @FXML
    private PasswordField txt_confirm_passw;
    @FXML
    private TextField txt_nombre;
    @FXML
    private Label lbl_error;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        txt_email.setOnKeyTyped(e -> limpiarError());
        txt_nombre.setOnKeyTyped(e -> limpiarError());
        txt_passw.setOnKeyTyped(e -> limpiarError());
        txt_confirm_passw.setOnKeyTyped(e -> limpiarError());

    }

    @FXML
    private void volverLogin(ActionEvent event) throws IOException {

        Parent vistaLogin = FXMLLoader.load(getClass().getResource("/Vista/Login.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(vistaLogin));

        // Centra la ventana en la pantalla 
        stage.centerOnScreen();
    }

    @FXML
    private void registrarUsuario(ActionEvent event) {

        lbl_error.setVisible(false);

        String email = txt_email.getText().trim();
        String username = txt_nombre.getText().trim();
        String password = txt_passw.getText();
        String confirmPassword = txt_confirm_passw.getText();

        if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {

            lbl_error.setText("⚠ No puede haber campos vacíos");
            lbl_error.setVisible(true);

            if (email.isEmpty()) {
                txt_email.setStyle("-fx-border-color: red;");
            }
            if (username.isEmpty()) {
                txt_nombre.setStyle("-fx-border-color: red;");
            }
            if (password.isEmpty()) {
                txt_passw.setStyle("-fx-border-color: red;");
            }
            if (confirmPassword.isEmpty()) {
                txt_confirm_passw.setStyle("-fx-border-color: red;");
            }

            return;
        }

        if (!password.equals(confirmPassword)) {

            lbl_error.setText("⚠ Las contraseñas no coinciden");
            lbl_error.setVisible(true);

            txt_passw.setStyle("-fx-border-color: red;");
            txt_confirm_passw.setStyle("-fx-border-color: red;");

            txt_passw.setText("");
            txt_confirm_passw.setText("");

            return;
        }

        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());

        Usuario usuario = new Usuario(email, username, passwordHash);

        boolean insertado = UsuarioDAO.insertarUsuario(usuario);
        
        if (insertado) {
            
            System.out.println("Usuario creado correctamente");
            lbl_error.setStyle("-fx-text-fill: green;");
            lbl_error.setText("✔ Usuario registrado correctamente");
            lbl_error.setVisible(true);
            limpiarCampos();
            
        } else {
            
            lbl_error.setStyle("-fx-text-fill: red;");
            lbl_error.setText("⚠ No se pudo registrar el usuario");
            lbl_error.setVisible(true);
            
        }

    }

    public void limpiarError() {

        lbl_error.setVisible(false);
        lbl_error.setStyle("-fx-text-fill: red;");
        txt_email.setStyle(null);
        txt_nombre.setStyle(null);
        txt_passw.setStyle(null);
        txt_confirm_passw.setStyle(null);

    }
    
    public void limpiarCampos(){
        
        txt_email.setText("");
        txt_nombre.setText("");
        txt_passw.setText("");
        txt_confirm_passw.setText("");
    }
    

}
