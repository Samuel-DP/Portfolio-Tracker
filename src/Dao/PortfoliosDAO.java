package Dao;

import Modelo.ConfigDB;
import Modelo.PortfolioItem;
import Modelo.vGlobales;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PortfoliosDAO {

    public static List<PortfolioItem> obtenerPortfoliosPorUsuario(int usuarioId) {
        List<PortfolioItem> portfolios = new ArrayList<>();

        String sql = "SELECT ID, NOMBRE, ICONO, DESCRIPCION, ES_DEFAULT "
                + "FROM PORTFOLIOS "
                + "WHERE USUARIO_ID = ? AND IS_ACTIVE = 1 "
                + "ORDER BY ES_DEFAULT DESC, CREATED_AT ASC";

        try (Connection conn = ConexionDB.getConexion(vGlobales.getCadena(), ConfigDB.getUser(), ConfigDB.getPassword()); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    portfolios.add(mapearPortfolio(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return portfolios;
    }

    public static PortfolioItem crearPortfolioSiNoExiste(int usuarioId, String icono, String nombre, String descripcion, boolean esDefault) {
        String sql = "IF NOT EXISTS (SELECT 1 FROM PORTFOLIOS WHERE USUARIO_ID = ? AND NOMBRE = ?) "
                + "BEGIN "
                + "INSERT INTO PORTFOLIOS (USUARIO_ID, NOMBRE, ICONO, DESCRIPCION, ES_DEFAULT) VALUES (?, ?, ?, ?, ?) "
                + "END";

        try (Connection conn = ConexionDB.getConexion(vGlobales.getCadena(), ConfigDB.getUser(), ConfigDB.getPassword()); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);
            ps.setString(2, nombre);
            ps.setInt(3, usuarioId);
            ps.setString(4, nombre);
            ps.setString(5, icono);
            ps.setString(6, descripcion);
            ps.setBoolean(7, esDefault);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return obtenerPortfolioPorNombre(usuarioId, nombre);
    }

    private static PortfolioItem obtenerPortfolioPorNombre(int usuarioId, String nombre) {
        String sql = "SELECT TOP 1 ID, NOMBRE, ICONO, DESCRIPCION, ES_DEFAULT "
                + "FROM PORTFOLIOS "
                + "WHERE USUARIO_ID = ? AND NOMBRE = ? AND IS_ACTIVE = 1 "
                + "ORDER BY ID DESC";

        try (Connection conn = ConexionDB.getConexion(vGlobales.getCadena(), ConfigDB.getUser(), ConfigDB.getPassword()); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);
            ps.setString(2, nombre);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearPortfolio(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static PortfolioItem mapearPortfolio(ResultSet rs) throws SQLException {
        return new PortfolioItem(
                rs.getInt("ID"),
                rs.getString("ICONO"),
                rs.getString("NOMBRE"),
                rs.getString("DESCRIPCION"),
                rs.getBoolean("ES_DEFAULT")
        );
    }
}

// Me gustaria implemetar el poder eliminar portfolios tambien de la DB y de mi menu vertical, para mañana
