
package Dao;

import Modelo.Usuario;
import Modelo.vGlobales;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class UsuarioDAO {
    
    public static boolean insertarUsuario(Usuario usuario) {

    String sql = "INSERT INTO USUARIOS (EMAIL, USERNAME, PASSWORD_HASH) VALUES (?, ?, ?)";

    try (Connection conn = ConexionDB.getConexion(vGlobales.getCadena(), vGlobales.USER, vGlobales.PASSWORD);
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, usuario.getEmail());
        ps.setString(2, usuario.getUsername());
        ps.setString(3, usuario.getPasswordHash());

        ps.executeUpdate();

        return true;

    } catch (SQLException e) {

        System.out.println("Error al insertar usuario: " + e.getMessage());
        return false;
    }
}
    
}
