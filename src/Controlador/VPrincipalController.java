
package Controlador;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;


public class VPrincipalController implements Initializable {

    @FXML
    private AnchorPane contentArea;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Al iniciar, mostramos Mercados por defecto
        try {
            loadMercados();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }    

    @FXML
    private void onMercados(ActionEvent event) throws IOException {
        loadMercados();
    }

    @FXML
    private void onPortfolio(ActionEvent event) throws IOException {
        Parent portfolioView = FXMLLoader.load(getClass().getResource("/Vista/VistaPortfolio.fxml"));
        setContent(portfolioView);
    }
    
    private void loadMercados() throws IOException {
        Parent mercadosView = FXMLLoader.load(getClass().getResource("/Vista/vistaMercados.fxml"));
        setContent(mercadosView);
    }

    private void setContent(Parent view) {
        contentArea.getChildren().setAll(view);

        // Para que el contenido ocupe el AnchorPane completo
        AnchorPane.setTopAnchor(view, 0.0);
        AnchorPane.setBottomAnchor(view, 0.0);
        AnchorPane.setLeftAnchor(view, 0.0);
        AnchorPane.setRightAnchor(view, 0.0);
    }
    
   
    
}
