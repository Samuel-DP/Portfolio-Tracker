package Dao;

import Modelo.ConfigDB;
import Modelo.Usuario;
import Modelo.vGlobales;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

public class UsuarioDAO {

    public static boolean insertarUsuario(Usuario usuario) {

        String sql = "INSERT INTO USUARIOS (EMAIL, USERNAME, PASSWORD_HASH) VALUES (?, ?, ?)";

        try (Connection conn = ConexionDB.getConexion(vGlobales.getCadena(), ConfigDB.getUser(), ConfigDB.getPassword()); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, usuario.getEmail());
            ps.setString(2, usuario.getUsername());
            ps.setString(3, usuario.getPasswordHash());

            ps.executeUpdate();

            return true;

        } catch (SQLException e) {

            System.out.println("Error al insertar usuario: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static Usuario obtenerUsuarioPorEmailOUsername(String datoLogin) {
        String sql = "SELECT ID, EMAIL, USERNAME, PASSWORD_HASH, IS_ACTIVE "
                + "FROM USUARIOS WHERE EMAIL = ? OR USERNAME = ?";

        try (Connection conn = ConexionDB.getConexion(vGlobales.getCadena(), ConfigDB.getUser(), ConfigDB.getPassword()); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, datoLogin);
            ps.setString(2, datoLogin);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Usuario(
                        rs.getInt("ID"),
                        rs.getString("EMAIL"),
                        rs.getString("USERNAME"),
                        rs.getString("PASSWORD_HASH"),
                        rs.getBoolean("IS_ACTIVE")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

}
