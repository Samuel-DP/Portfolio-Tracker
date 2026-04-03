package Controlador;

import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class Validaciones {

    public boolean validarCampos(
            ComboBox<String> cb_moneda,
            DatePicker dp_fecha,
            TextField txt_cantidad,
            TextField txt_precio,
            TextField txt_notas,
            Label lbl_error
    ) {

        limpiarError(lbl_error, cb_moneda, txt_cantidad, txt_precio, txt_notas);

        if (cb_moneda.getValue() == null || cb_moneda.getValue().isBlank()) {
            mostrarError(lbl_error, "⚠ Selecciona una moneda");
            cb_moneda.setStyle("-fx-border-color: red;");
            return false;
        }

        if (dp_fecha.getValue() == null) {
            mostrarError(lbl_error, "⚠ Selecciona una fecha");
            dp_fecha.setStyle("-fx-border-color: red;");
            return false;
        }

        String cTxt = txt_cantidad.getText();
        if (cTxt == null || cTxt.isBlank()) {
            mostrarError(lbl_error, "⚠ La cantidad no puede estar vacía");
            txt_cantidad.setStyle("-fx-border-color: red;");
            return false;
        }

        String pTxt = txt_precio.getText();
        if (pTxt == null || pTxt.isBlank()) {
            mostrarError(lbl_error, "⚠ El precio no puede estar vacío");
            txt_precio.setStyle("-fx-border-color: red;");
            return false;
        }

        double unidades;
        double precio;

        try {
            unidades = Double.parseDouble(cTxt.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            mostrarError(lbl_error, "⚠ Cantidad inválida (ej: 1.5)");
            txt_cantidad.setStyle("-fx-border-color: red;");
            return false;
        }

        try {
            precio = Double.parseDouble(pTxt.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            mostrarError(lbl_error, "⚠ Precio inválido (ej: 2500)");
            txt_precio.setStyle("-fx-border-color: red;");
            return false;
        }

        if (unidades <= 0) {
            mostrarError(lbl_error, "⚠ La cantidad debe ser mayor que 0");
            txt_cantidad.setStyle("-fx-border-color: red;");
            return false;
        }

        if (precio <= 0) {
            mostrarError(lbl_error, "⚠ El precio debe ser mayor que 0");
            txt_precio.setStyle("-fx-border-color: red;");
            return false;
        }

        String notas = txt_notas.getText();
        if (notas != null && notas.length() > 200) {
            mostrarError(lbl_error, "⚠ Las notas no pueden superar los 200 caracteres");
            txt_notas.setStyle("-fx-border-color: red;");
            return false;
        }

        return true;

    }

    public boolean validarTransferir(
            ComboBox<String> cb_moneda,
            ComboBox<String> cb_transferencia,
            TextField txt_cantidad,
            TextField txt_notas,
            DatePicker dp_fecha,
            Label lbl_error
    ) {

        limpiarError(lbl_error, cb_moneda, cb_transferencia, txt_cantidad, txt_notas);

        if (cb_moneda.getValue() == null || cb_moneda.getValue().isBlank()) {
            mostrarError(lbl_error, "⚠ Selecciona una moneda");
            cb_moneda.setStyle("-fx-border-color: red;");
            return false;
        }

        if (cb_transferencia.getValue() == null || cb_transferencia.getValue().isBlank()) {
            mostrarError(lbl_error, "⚠ Selecciona un tipo de transferencia");
            cb_transferencia.setStyle("-fx-border-color: red;");
            return false;
        }

        String cTxt = txt_cantidad.getText();
        if (cTxt == null || cTxt.isBlank()) {
            mostrarError(lbl_error, "⚠ La cantidad no puede estar vacía");
            txt_cantidad.setStyle("-fx-border-color: red;");
            return false;
        }

        double cantidad;

        try {
            cantidad = Double.parseDouble(cTxt.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            mostrarError(lbl_error, "⚠ Cantidad inválida (ej: 1.5)");
            txt_cantidad.setStyle("-fx-border-color: red;");
            return false;
        }

        if (cantidad <= 0) {
            mostrarError(lbl_error, "⚠ La cantidad debe ser mayor que 0");
            txt_cantidad.setStyle("-fx-border-color: red;");
            return false;
        }

        String notas = txt_notas.getText();
        if (notas != null && notas.length() > 200) {
            mostrarError(lbl_error, "⚠ Las notas no pueden superar los 200 caracteres");
            txt_notas.setStyle("-fx-border-color: red;");
            return false;
        }

        if (dp_fecha.getValue() == null) {
            mostrarError(lbl_error, "⚠ Selecciona una fecha");
            dp_fecha.setStyle("-fx-border-color: red;");
            return false;
        }

        return true;
    }

    public void limpiarError(Label lbl_error, Object... campos) {
        if (lbl_error != null) {
            lbl_error.setVisible(false);
            lbl_error.setText("");
        }

        for (Object campo : campos) {
            if (campo instanceof TextField tf) {
                tf.setStyle(null);
            } else if (campo instanceof ComboBox<?> cb) {
                cb.setStyle(null);
            } else if (campo instanceof DatePicker dp) {
                dp.setStyle(null);
            }
        }
    }

    private void mostrarError(Label lbl_error, String mensaje) {
        lbl_error.setText(mensaje);
        lbl_error.setVisible(true);
    }

}
