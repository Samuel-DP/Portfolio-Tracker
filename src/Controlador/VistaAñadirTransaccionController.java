
package Controlador;

import Modelo.Transaccion;
import Vista.VistaAñadirTransaccionTransferirController;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class VistaAñadirTransaccionController implements Initializable {

    @FXML
    private Button btn_transferencia;
    @FXML
    private Pane contenidoTransacciones;
    @FXML
    private Button btn_comprar;
    @FXML
    private Button btn_vender;
    
    private Transaccion resultado;
    
    public Transaccion getResultado(){ return resultado; }

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            compras();
        } catch (IOException ex) {
            Logger.getLogger(VistaAñadirTransaccionController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }    
    
    public void setResultadoAndClose(Transaccion t){
        this.resultado = t;
        Stage stage = (Stage) contenidoTransacciones.getScene().getWindow();
        stage.close();
    }

    
    @FXML
    private void onComprar(ActionEvent event) throws IOException {
        
        compras();
        
    }

    @FXML
    private void onVender(ActionEvent event) throws IOException {
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vista/vistaTransaccionVenta.fxml")); 
        Parent vista = loader.load();
        
        VistaTransaccionVentaController ctrlVenta = loader.getController();
        ctrlVenta.setParentController(this); // Aqui conecto hijo con Padre
        
        contenidoTransacciones.getChildren().setAll(vista);
    }
    
    @FXML
    private void onTransferir(ActionEvent event) throws IOException {
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vista/vistaAñadirTransaccionTransferir.fxml"));
        Parent vista = loader.load();
        
        VistaAñadirTransaccionTransferirController ctrlTransferir = loader.getController();
        ctrlTransferir.setParentController(this); // Aqui conecto hijo con Padre
        
        // Meto la vista de vistaTransaccionTransferir dentro de un panel de vistaAñadirTransaccion ya que no quiero cambiar la vista entera, solo un trozo de panel
        contenidoTransacciones.getChildren().setAll(vista);
        
    }
    
    public void compras() throws IOException{
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vista/vistaTransaccionCompra.fxml"));
        Parent vista = loader.load();
        
        VistaTransaccionCompraController ctrlCompra = loader.getController();
        ctrlCompra.setParentController(this); // Aqui conecto hijo con Padre
        
        contenidoTransacciones.getChildren().setAll(vista);
        
    }

    
}
