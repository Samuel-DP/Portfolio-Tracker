
package Controlador;

import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;


public class Validaciones {
    
    public boolean validarCampos(
            ChoiceBox<String> cb_moneda, 
            DatePicker dp_fecha, 
            TextField txt_cantidad, 
            TextField txt_precio, 
            TextField txt_notas
    ) {

        if (cb_moneda.getValue() == null) {
            mostrarError("Moneda", "Selecciona una moneda");
            return false;
        }

        if (dp_fecha.getValue() == null) {
            mostrarError("Fecha", "Selecciona una fecha");
            return false;
        }
        
        String cTxt = txt_cantidad.getText();
        if(cTxt == null || cTxt.isBlank() ){
            mostrarError("Cantidad", "La cantidad no puede esta vacia");
            return false;
        }
        
        String pTxt = txt_precio.getText();
        if(pTxt == null || pTxt.isBlank()){
            mostrarError("Precio", "El precio no puede estar vacio");
            return false;
        }
        
        double unidades;
        double precio;
        
        try {
            unidades = Double.parseDouble(cTxt.trim().replace(",", "."));        
        }catch (NumberFormatException e){
            mostrarError("Cantidad", "Introduce un nº valido (ej: 1.5).");
            return false;
        }
        
        try {
            precio = Double.parseDouble(pTxt.trim().replace(",", "."));        
        }catch (NumberFormatException e){
            mostrarError("Precio", "Introduce un nº valido (ej: 2500).");
            return false;
        }
        
        if(unidades <= 0){
            mostrarError("Cantidad", "La cantidad debe ser mayor que 0");
            return false;
        }
        
        if(precio <= 0){
            mostrarError("Precio", "El precio debe ser mayor que 0");
            return false;
        }
        
        String notas = txt_notas.getText();
        if(notas != null && notas.length() > 200){
            mostrarError("Notas", "Las notas no pueden superar los 200 caracteres");
            return false;
        }
        

        return true;

    }

    private void mostrarError(String campo, String mensaje) {

        Alert alert = new Alert(Alert.AlertType.ERROR); // Esto es para sacar una ventana emergente
        alert.setTitle("Error de validación");
        alert.setHeaderText("Problema en: " + campo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
}
