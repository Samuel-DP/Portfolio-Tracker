package Modelo;

import Dao.PortfoliosDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PortfolioService {

    private static final ObservableList<PortfolioItem> portfolios = FXCollections.observableArrayList();

    public static ObservableList<PortfolioItem> getPortfolios() {
        return portfolios;
    }

    public static synchronized void loadForCurrentUser() {
        portfolios.clear();

        Integer usuarioId = vGlobales.getUsuarioIdActual();
        if (usuarioId == null) {
            return;
        }

        portfolios.addAll(PortfoliosDAO.obtenerPortfoliosPorUsuario(usuarioId));

        if (portfolios.isEmpty()) {
            PortfolioItem defaultPortfolio = PortfoliosDAO.crearPortfolioSiNoExiste(usuarioId, "📊", "My portfolio", null, true);
            if (defaultPortfolio != null) {
                portfolios.add(defaultPortfolio);
            }
        }
    }

    public static void add(String icono, String nombre) {
        Integer usuarioId = vGlobales.getUsuarioIdActual();
        if (usuarioId == null) {
            return;
        }

        PortfolioItem portfolioCreado = PortfoliosDAO.crearPortfolioSiNoExiste(usuarioId, icono, nombre, null, false);
        if (portfolioCreado != null && portfolios.stream().noneMatch(p -> p.getId() == portfolioCreado.getId())) {
            portfolios.add(portfolioCreado);
        }
    }

    public static boolean remove(PortfolioItem portfolio) {
        if (portfolio == null || portfolio.isEsDefault()) {
            return false;
        }

        Integer usuarioId = vGlobales.getUsuarioIdActual();
        if (usuarioId == null) {
            return false;
        }

        boolean eliminado = PortfoliosDAO.eliminarPortfolio(usuarioId, portfolio.getId());
        if (eliminado) {
            portfolios.removeIf(p -> p.getId() == portfolio.getId());
        }

        return eliminado;
    }

}
