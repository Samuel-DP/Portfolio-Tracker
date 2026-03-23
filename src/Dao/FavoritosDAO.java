package Dao;

import Modelo.ConfigDB;
import Modelo.Favoritos;
import Modelo.vGlobales;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FavoritosDAO {

    public static List<Favoritos> obtenerFavoritosPorUsuario(int usuarioId) {
        List<Favoritos> favoritos = new ArrayList<>();

        String sql = "SELECT A.TIPO_ACTIVO, A.SIMBOLO, A.NOMBRE "
                + "FROM FAVORITOS F "
                + "INNER JOIN ACTIVOS A ON A.ID = F.ACTIVO_ID "
                + "WHERE F.USUARIO_ID = ? AND A.IS_ACTIVE = 1";

        try (Connection conn = ConexionDB.getConexion(vGlobales.getCadena(), ConfigDB.getUser(), ConfigDB.getPassword()); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    favoritos.add(new Favoritos(
                            rs.getString("TIPO_ACTIVO"),
                            rs.getString("SIMBOLO"),
                            rs.getString("NOMBRE"),
                            0,
                            0,
                            0,
                            true
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return favoritos;
    }

    public static void guardarFavorito(int usuarioId, Favoritos favorito) {
        Integer activoId = obtenerOCrearActivoId(favorito);

        if (activoId == null) {
            return;
        }

        String sql = "IF NOT EXISTS (SELECT 1 FROM FAVORITOS WHERE USUARIO_ID = ? AND ACTIVO_ID = ?) "
                + "BEGIN "
                + "INSERT INTO FAVORITOS (USUARIO_ID, ACTIVO_ID) VALUES (?, ?) "
                + "END";

        try (Connection conn = ConexionDB.getConexion(vGlobales.getCadena(), ConfigDB.getUser(), ConfigDB.getPassword()); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);
            ps.setInt(2, activoId);
            ps.setInt(3, usuarioId);
            ps.setInt(4, activoId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void eliminarFavorito(int usuarioId, String tipo, String simbolo) {
        Integer activoId = obtenerActivoId(tipo, simbolo);

        if (activoId == null) {
            return;
        }

        String sql = "DELETE FROM FAVORITOS WHERE USUARIO_ID = ? AND ACTIVO_ID = ?";

        try (Connection conn = ConexionDB.getConexion(vGlobales.getCadena(), ConfigDB.getUser(), ConfigDB.getPassword()); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);
            ps.setInt(2, activoId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Integer obtenerOCrearActivoId(Favoritos favorito) {
        Integer activoId = obtenerActivoId(favorito.getTipo(), favorito.getSimboloTicker());

        if (activoId != null) {
            actualizarActivo(activoId, favorito);
            return activoId;
        }

        return crearActivo(favorito);
    }

    private static Integer obtenerActivoId(String tipo, String simbolo) {
        String sql = "SELECT ID FROM ACTIVOS WHERE TIPO_ACTIVO = ? AND SIMBOLO = ?";

        try (Connection conn = ConexionDB.getConexion(vGlobales.getCadena(), ConfigDB.getUser(), ConfigDB.getPassword()); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tipo);
            ps.setString(2, simbolo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ID");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Integer crearActivo(Favoritos favorito) {
        String sql = "INSERT INTO ACTIVOS (TIPO_ACTIVO, SIMBOLO, NOMBRE, API_ID, FUENTE) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionDB.getConexion(vGlobales.getCadena(), ConfigDB.getUser(), ConfigDB.getPassword()); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, favorito.getTipo());
            ps.setString(2, favorito.getSimboloTicker());
            ps.setString(3, favorito.getNombreEmpresa());
            ps.setString(4, favorito.getSimboloTicker());
            ps.setString(5, obtenerFuente(favorito.getTipo()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return obtenerActivoId(favorito.getTipo(), favorito.getSimboloTicker());
    }

    private static void actualizarActivo(int activoId, Favoritos favorito) {
        String sql = "UPDATE ACTIVOS "
                + "SET NOMBRE = ?, API_ID = ?, FUENTE = ?, IS_ACTIVE = 1, UPDATED_AT = GETDATE() "
                + "WHERE ID = ?";

        try (Connection conn = ConexionDB.getConexion(vGlobales.getCadena(), ConfigDB.getUser(), ConfigDB.getPassword()); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, favorito.getNombreEmpresa());
            ps.setString(2, favorito.getSimboloTicker());
            ps.setString(3, obtenerFuente(favorito.getTipo()));
            ps.setInt(4, activoId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String obtenerFuente(String tipo) {
        if ("CRYPTO".equalsIgnoreCase(tipo)) {
            return "COINGECKO";
        }

        if ("STOCK".equalsIgnoreCase(tipo)) {
            return "FINNHUB";
        }

        return null;
    }
}
