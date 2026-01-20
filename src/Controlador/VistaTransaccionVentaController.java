package Controlador;

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


public class VistaTransaccionVentaController implements Initializable {

    @FXML
    private ChoiceBox<String> cb_moneda;
    @FXML
    private DatePicker dp_fecha;
    @FXML
    private Button btn_agregarVenta;
    @FXML
    private TextField txt_total_gastado;
    @FXML
    private TextField txt_cantidad;
    @FXML
    private TextField txt_precio;
    @FXML
    private TextField txt_notas;

    private VistaAñadirTransaccionController parent;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        cb_moneda.getItems().addAll("BTC", "ETH", "USDT", "SOL");
        
        // Recalculo cuando cambie cantidad o precio
        txt_cantidad.textProperty().addListener((obs,oldV, newV) -> recalcularTotal());
        txt_precio.textProperty().addListener((obs,oldV, newV) -> recalcularTotal());
    }    
    
    public void setParentController(VistaAñadirTransaccionController parent){
        this.parent = parent;
    }

    @FXML
    private void onAgregarVenta(ActionEvent event) {
        
        String activo = (String) cb_moneda.getValue();
        double unidades = Double.parseDouble(txt_cantidad.getText());
        double precioPorMoneda = Double.parseDouble(txt_precio.getText());
        String notas = txt_notas.getText();
        
        LocalTime horaActual = LocalTime.now();
        LocalDate fecha = dp_fecha.getValue();
        
        LocalDateTime fechaHora = LocalDateTime.of(fecha, horaActual);
        
        double importe = unidades * precioPorMoneda;
        
        Transaccion t = new Transaccion( "VENTA", fechaHora, activo, unidades, precioPorMoneda, importe, notas);
        
        parent.setResultadoAndClose(t);   
        
    }
    
    private void recalcularTotal(){
        
        String cTxt = txt_cantidad.getText();
        String pTxt = txt_precio.getText();
        
        if (cTxt == null || cTxt.isBlank() || pTxt == null || pTxt.isBlank()) {
            txt_total_gastado.setText("");
            return;
        }

        try {
            // Si usas coma decimal, esto lo soporta:
            double cantidad = Double.parseDouble(cTxt.replace(",", "."));
            double precio = Double.parseDouble(pTxt.replace(",", "."));

            double total = cantidad * precio;

            txt_total_gastado.setText(String.format("%.2f €", total));
            
        } catch (NumberFormatException e) {
            // Si el usuario está escribiendo y aún no es un número válido
            txt_total_gastado.setText("");
        }
    }
    
}
