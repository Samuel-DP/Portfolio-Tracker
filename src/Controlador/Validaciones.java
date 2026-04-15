package Controlador;

import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class Validaciones {

    public String normalizarDecimal(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.trim().replace(",", ".");
    }

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
            marcarError(cb_moneda);
            return false;
        }

        if (dp_fecha.getValue() == null) {
            mostrarError(lbl_error, "⚠ Selecciona una fecha");
            marcarError(dp_fecha);
            return false;
        }

        String cTxt = txt_cantidad.getText();
        if (cTxt == null || cTxt.isBlank()) {
            mostrarError(lbl_error, "⚠ La cantidad no puede estar vacía");
            marcarError(txt_cantidad);
            return false;
        }

        String pTxt = txt_precio.getText();
        if (pTxt == null || pTxt.isBlank()) {
            mostrarError(lbl_error, "⚠ El precio no puede estar vacío");
            marcarError(txt_precio);
            return false;
        }

        double unidades;
        double precio;

        try {
            unidades = Double.parseDouble(normalizarDecimal(cTxt));
        } catch (NumberFormatException e) {
            mostrarError(lbl_error, "⚠ Cantidad inválida (ej: 1.5)");
            marcarError(txt_cantidad);
            return false;
        }

        try {
            precio = Double.parseDouble(normalizarDecimal(cTxt));
        } catch (NumberFormatException e) {
            mostrarError(lbl_error, "⚠ Precio inválido (ej: 2500)");
            marcarError(txt_precio);
            return false;
        }

        if (unidades <= 0) {
            mostrarError(lbl_error, "⚠ La cantidad debe ser mayor que 0");
            marcarError(txt_cantidad);
            return false;
        }

        if (precio <= 0) {
            mostrarError(lbl_error, "⚠ El precio debe ser mayor que 0");
            marcarError(txt_precio);
            return false;
        }

        String notas = txt_notas.getText();
        if (notas != null && notas.length() > 200) {
            mostrarError(lbl_error, "⚠ Las notas no pueden superar los 200 caracteres");
            marcarError(txt_notas);
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
            marcarError(cb_moneda);
            return false;
        }

        if (cb_transferencia.getValue() == null || cb_transferencia.getValue().isBlank()) {
            mostrarError(lbl_error, "⚠ Selecciona un tipo de transferencia");
            marcarError(cb_transferencia);
            return false;
        }

        String cTxt = txt_cantidad.getText();
        if (cTxt == null || cTxt.isBlank()) {
            mostrarError(lbl_error, "⚠ La cantidad no puede estar vacía");
            marcarError(txt_cantidad);
            return false;
        }

        double cantidad;

        try {
            cantidad = Double.parseDouble(normalizarDecimal(cTxt));
        } catch (NumberFormatException e) {
            mostrarError(lbl_error, "⚠ Cantidad inválida (ej: 1.5)");
            marcarError(txt_cantidad);
            return false;
        }

        if (cantidad <= 0) {
            mostrarError(lbl_error, "⚠ La cantidad debe ser mayor que 0");
            marcarError(txt_cantidad);
            return false;
        }

        String notas = txt_notas.getText();
        if (notas != null && notas.length() > 200) {
            mostrarError(lbl_error, "⚠ Las notas no pueden superar los 200 caracteres");
            marcarError(txt_notas);
            return false;
        }

        if (dp_fecha.getValue() == null) {
            mostrarError(lbl_error, "⚠ Selecciona una fecha");
            marcarError(dp_fecha);
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
                limpiarBordeError(tf);
            } else if (campo instanceof ComboBox<?> cb) {
                limpiarBordeError(cb);
            } else if (campo instanceof DatePicker dp) {
                limpiarBordeError(dp);
            }
        }
    }

    private void marcarError(javafx.scene.Node nodo) {
        String style = nodo.getStyle();
        style = style == null ? "" : style;

        style = style
                .replaceAll("-fx-border-color\\s*:[^;]*;?", "")
                .replaceAll("-fx-border-width\\s*:[^;]*;?", "")
                .trim();

        if (!style.isEmpty() && !style.endsWith(";")) {
            style += ";";
        }

        style += " -fx-border-color: red; -fx-border-width: 1;";
        nodo.setStyle(style.trim());
    }

    private void limpiarBordeError(javafx.scene.Node nodo) {
        String style = nodo.getStyle();
        if (style == null || style.isBlank()) {
            return;
        }

        String nuevo = style
                .replaceAll("-fx-border-color\\s*:[^;]*;?", "")
                .replaceAll("-fx-border-width\\s*:[^;]*;?", "")
                .replaceAll("\\s{2,}", " ")
                .trim();

        nodo.setStyle(nuevo);
    }

    private void mostrarError(Label lbl_error, String mensaje) {
        lbl_error.setText(mensaje);
        lbl_error.setVisible(true);
    }

}
