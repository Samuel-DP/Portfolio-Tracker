package Controlador;

import Dao.UsuarioDAO;
import Modelo.FavoritesService;
import Modelo.PortfolioService;
import Modelo.Usuario;
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
import org.mindrot.jbcrypt.BCrypt;

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

        lbl_errorCredenciales.setVisible(false);
        txt_email.setStyle(null);
        txt_passw.setStyle(null);

        String datoLogin = txt_email.getText().trim(); // puede ser email o username
        String password = txt_passw.getText();

        if (datoLogin.isEmpty() || password.isEmpty()) {
            lbl_errorCredenciales.setText("⚠ No puede haber campos vacíos");
            lbl_errorCredenciales.setVisible(true);

            if (datoLogin.isEmpty()) {
                txt_email.setStyle("-fx-border-color: red;");
            }
            if (password.isEmpty()) {
                txt_passw.setStyle("-fx-border-color: red;");
            }
            return;
        }

        Usuario usuario = UsuarioDAO.obtenerUsuarioPorEmailOUsername(datoLogin);

        if (usuario == null) {
            lbl_errorCredenciales.setText("⚠ Usuario no encontrado");
            lbl_errorCredenciales.setVisible(true);

            txt_email.setStyle("-fx-border-color: red;");
            txt_passw.setStyle("-fx-border-color: red;");
            txt_email.clear();
            txt_passw.clear();
            return;
        }

        if (!usuario.IsActive()) {
            lbl_errorCredenciales.setText("⚠ Usuario inactivo");
            lbl_errorCredenciales.setVisible(true);
            return;
        }

        // Compruebo la contraseña con BCrypt
        boolean passwordCorrecta = BCrypt.checkpw(password, usuario.getPasswordHash());

        if (passwordCorrecta) {
            // Guardo el id del usuario logeado 
            vGlobales.setUsuarioIdActual(usuario.getId());
            
            //Cargo los favs del usuario
            FavoritesService.loadForCurrentUser();
            
            // Cargo el portfolio del usuario
            PortfolioService.loadForCurrentUser();
            
            Parent vistaPrincipal = FXMLLoader.load(getClass().getResource("/Vista/vistaPrincipal.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(vistaPrincipal));
            stage.centerOnScreen();
        } else {
            lbl_errorCredenciales.setText("⚠ Email/usuario o contraseña incorrectos");
            lbl_errorCredenciales.setVisible(true);

            txt_email.setStyle("-fx-border-color: red;");
            txt_passw.setStyle("-fx-border-color: red;");
            txt_email.clear();
            txt_passw.clear();
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
