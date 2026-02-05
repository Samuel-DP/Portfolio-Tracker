package Controlador;

import Modelo.Favoritos;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;

public class VistaFavoritosController implements Initializable {

    @FXML
    private TableView<Favoritos> tablaFavoritos;
    @FXML
    private TableColumn<Favoritos, String> colFav;
    @FXML
    private TableColumn<Favoritos, String> colTipo;
    @FXML
    private TableColumn<Favoritos, String> colSimbolTicker;
    @FXML
    private TableColumn<Favoritos, String> colNombre;
    @FXML
    private TableColumn<Favoritos, Double> colPrecio;
    @FXML
    private TableColumn<Favoritos, Double> col24h;
    @FXML
    private TableColumn<Favoritos, Double> colMarketCap;

    private final ObservableList<Favoritos> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colSimbolTicker.setCellValueFactory(new PropertyValueFactory<>("simboloTicker"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreEmpresa"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        col24h.setCellValueFactory(new PropertyValueFactory<>("porcentaje24h"));
        colMarketCap.setCellValueFactory(new PropertyValueFactory<>("marketCap"));
        
        colFav.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(""));
        
        tablaFavoritos.setItems(Modelo.FavoritesService.getFavoritos());

        // Dentro de la colFav metemos el toogle de favoritos
        colFav.setCellFactory(tc -> new TableCell<>() {
            private final ToggleButton btn = new ToggleButton();

            {
                btn.getStyleClass().add("fav-toggle");
                btn.setFocusTraversable(false);
                btn.setOnAction(e -> {
                    Favoritos row = getTableView().getItems().get(getIndex());
                    
                    boolean selected = btn.isSelected();
                    row.setFavorito(btn.isSelected());
                    btn.setText(btn.isSelected() ? "★" : "☆"); // 

                    // si lo desmarco de favoritos, lo quito de la lista
                    if (!selected){
                        Modelo.FavoritesService.remove(row.getTipo(), row.getSimboloTicker());
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Favoritos row = getTableView().getItems().get(getIndex());
                btn.setSelected(row.isFavorito());
                btn.setText(row.isFavorito() ? "★" : "☆");
                setGraphic(btn);
            }
        });
        
        // Inicio favoritos , me he quedado metienndole el CSS al toogle

    }
}
