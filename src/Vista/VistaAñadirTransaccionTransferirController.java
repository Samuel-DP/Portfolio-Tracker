package Vista;

import Controlador.Validaciones;
import Controlador.VistaAñadirTransaccionController;
import Modelo.Transaccion;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class VistaAñadirTransaccionTransferirController implements Initializable {

    @FXML
    private Button btn_transferir;
    @FXML
    private ChoiceBox<String> cb_moneda;
    @FXML
    private ChoiceBox<String> cb_transferencia;
    @FXML
    private TextField txt_cantidad;
    @FXML
    private DatePicker dp_fecha;
    @FXML
    private TextField txt_notas;
    

    private VistaAñadirTransaccionController parent;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cb_moneda.getItems().addAll("BTC", "ETH", "USDT", "SOL");
        cb_transferencia.getItems().addAll("Transferencia entrante", "Transferencia saliente");
    }    
    
    public void setParentController(VistaAñadirTransaccionController parent){
        this.parent = parent;
    }

    @FXML
    private void onTransferir(ActionEvent event) {
        
        // ME HE QUEDADO VALIDANDO LAS TRANSFERENCIAS
        
        String activo = cb_moneda.getValue();
        String tipo = cb_transferencia.getValue();
        double cantidad = Double.parseDouble(txt_cantidad.getText());
        String notas = txt_notas.getText();
        
        LocalTime horaActual = LocalTime.now();
        LocalDate fecha = dp_fecha.getValue();
        
        LocalDateTime fechaHora = LocalDateTime.of(fecha, horaActual);
        
        Transaccion t = new Transaccion(tipo, fechaHora , activo, cantidad, notas );
        
        parent.setResultadoAndClose(t);
        
    }
    
    
    
}
