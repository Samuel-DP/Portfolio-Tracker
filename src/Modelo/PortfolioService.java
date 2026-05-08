package Modelo;

import Dao.PortfoliosDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PortfolioService {

    private static final ObservableList<PortfolioItem> portfolios = FXCollections.observableArrayList();
    private static Integer portfolioActualId;

    public static ObservableList<PortfolioItem> getPortfolios() {
        return portfolios;
    }

    public static synchronized void loadForCurrentUser() {
        portfolios.clear();
        portfolioActualId = null;

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
        if (!portfolios.isEmpty()) {
            PortfolioItem porDefecto = portfolios.stream().filter(PortfolioItem::isEsDefault).findFirst().orElse(portfolios.get(0));
            portfolioActualId = porDefecto.getId();
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

        if (portfolioActualId == null && portfolioCreado != null) {
            portfolioActualId = portfolioCreado.getId();
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

            if (portfolioActualId != null && portfolioActualId == portfolio.getId()) {
                PortfolioItem fallback = portfolios.stream().filter(PortfolioItem::isEsDefault).findFirst().orElse(portfolios.isEmpty() ? null : portfolios.get(0));
                portfolioActualId = fallback != null ? fallback.getId() : null;
            }
        }

        return eliminado;
    }

    public static Integer getPortfolioActualId() {
        return portfolioActualId;
    }

    public static PortfolioItem getPortfolioActual() {
        if (portfolioActualId == null) {
            return null;
        }

        return portfolios.stream().filter(p -> p.getId() == portfolioActualId).findFirst().orElse(null);
    }

    public static boolean setPortfolioActual(int portfolioId) {
        boolean existe = portfolios.stream().anyMatch(p -> p.getId() == portfolioId);
        if (!existe) {
            return false;
        }

        portfolioActualId = portfolioId;
        return true;
    }

}
