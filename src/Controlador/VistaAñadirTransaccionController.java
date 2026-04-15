package Controlador;

import Modelo.Transaccion;
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

    private Transaccion transaccionEditar;

    public Transaccion getResultado() {
        return resultado;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            compras();
        } catch (IOException ex) {
            Logger.getLogger(VistaAñadirTransaccionController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void setResultadoAndClose(Transaccion t) {
        this.resultado = t;
        Stage stage = (Stage) contenidoTransacciones.getScene().getWindow();
        stage.close();
    }

    public void setTransaccionParaEditar(Transaccion transaccion) throws IOException {
        this.transaccionEditar = transaccion;
        cargarVistaSegunTipo(transaccion);
    }

    @FXML
    private void onComprar(ActionEvent event) throws IOException {

        compras(transaccionEditar != null ? transaccionEditar : null);

    }

    @FXML
    private void onVender(ActionEvent event) throws IOException {

        venta(transaccionEditar != null ? transaccionEditar : null);
    }

    @FXML
    private void onTransferir(ActionEvent event) throws IOException {

        transferencia(transaccionEditar != null ? transaccionEditar : null);

    }

    public void compras() throws IOException {
        compras(null);
    }

    public void compras(Transaccion transaccion) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vista/vistaTransaccionCompra.fxml"));
        Parent vista = loader.load();

        VistaTransaccionCompraController ctrlCompra = loader.getController();
        ctrlCompra.setParentController(this); // Aqui conecto hijo con Padre

        if (transaccion != null) {
            ctrlCompra.cargarTransaccion(transaccion);
        }

        contenidoTransacciones.getChildren().setAll(vista);

    }

    private void venta(Transaccion transaccion) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vista/vistaTransaccionVenta.fxml"));
        Parent vista = loader.load();

        VistaTransaccionVentaController ctrlVenta = loader.getController();
        ctrlVenta.setParentController(this); // Aqui conecto hijo con Padre
        if (transaccion != null) {
            ctrlVenta.cargarTransaccion(transaccion);
        }

        contenidoTransacciones.getChildren().setAll(vista);
    }

    private void transferencia(Transaccion transaccion) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vista/vistaAñadirTransaccionTransferir.fxml"));
        Parent vista = loader.load();

        VistaAñadirTransaccionTransferirController ctrlTransferir = loader.getController();
        ctrlTransferir.setParentController(this); // Aqui conecto hijo con Padre
        if (transaccion != null) {
            ctrlTransferir.cargarTransaccion(transaccion);
        }

        // Meto la vista de vistaTransaccionTransferir dentro de un panel de vistaAñadirTransaccion ya que no quiero cambiar la vista entera, solo un trozo de panel
        contenidoTransacciones.getChildren().setAll(vista);
    }

    private void cargarVistaSegunTipo(Transaccion transaccion) throws IOException {
        if (transaccion == null) {
            compras();
            return;
        }

        String tipo = transaccion.getTipo();
        if ("VENTA".equalsIgnoreCase(tipo)) {
            venta(transaccion);
        } else if ("Transferencia entrante".equalsIgnoreCase(tipo) || "Transferencia saliente".equalsIgnoreCase(tipo)) {
            transferencia(transaccion);
        } else {
            compras(transaccion);
        }
    }

}
