
package Modelo;

import java.util.HashSet;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class FavoritesService { 
    
    private static final ObservableList<Favoritos> favoritos = FXCollections.observableArrayList();
    private static final Set<String> keys = new HashSet<>(); // evita ducplicados
    
    private static String key(String tipo, String simbolo){
        return tipo + ":" + simbolo;
    }
    
    public static ObservableList<Favoritos> getFavoritos() {
        return favoritos;
    }

    public static boolean isFavorite(String tipo, String simbolo) {
        return keys.contains(key(tipo, simbolo));
    }

    public static void add(Favoritos fav) {
        String k = key(fav.getTipo(), fav.getSimboloTicker());
        if (keys.add(k)) {
            favoritos.add(fav);
        }
    }

    public static void remove(String tipo, String simbolo) {
        String k = key(tipo, simbolo);
        if (keys.remove(k)) {
            favoritos.removeIf(f -> f.getTipo().equals(tipo) && f.getSimboloTicker().equals(simbolo));
        }
    }

    public static void toggle(Favoritos fav) {
        if (isFavorite(fav.getTipo(), fav.getSimboloTicker())) {
            remove(fav.getTipo(), fav.getSimboloTicker());
        } else {
            add(fav);
        }
    }
    
}
