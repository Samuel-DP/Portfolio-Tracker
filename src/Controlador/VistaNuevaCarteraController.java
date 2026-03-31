package Controlador;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class VistaNuevaCarteraController implements Initializable {

    @FXML
    private TextField txtNombrePortfolio;
    @FXML
    private ChoiceBox<String> cbEmojis;
    @FXML
    private Button btnCrear;

    private VistaPortfolioController vistaPortfolioController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        cbEmojis.setItems(FXCollections.observableArrayList("📊", "💼", "📈", "💰", "🚀"));
        cbEmojis.setValue("📊");

    }

    public void setVistaPortfolioController(VistaPortfolioController vistaPortfolioController) {
        this.vistaPortfolioController = vistaPortfolioController;
    }

    @FXML
    private void onCrearPortfolio() {
        String nombre = txtNombrePortfolio.getText() == null ? "" : txtNombrePortfolio.getText().trim();
        String emoji = cbEmojis.getValue();

        if (nombre.isEmpty()) {
            return;
        }

        if (vistaPortfolioController != null) {
            vistaPortfolioController.agregarPortfolio(emoji, nombre);
        }

        Stage stage = (Stage) btnCrear.getScene().getWindow();
        stage.close();
    }

}
