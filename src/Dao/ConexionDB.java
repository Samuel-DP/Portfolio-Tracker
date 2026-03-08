package Dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    Connection conexion = null;

    public Connection getConexion(String url, String usuario, String password) {

        try {

            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); // Driver de conexion para MSQL Server

            conexion = DriverManager.getConnection(url, usuario, password);

            System.out.println("Conexion establecida");

        } catch (Exception ex) {
            System.out.println("Error de conexion " + ex.getMessage());
        }

        return conexion;

    }

    public void cerrarConexion() throws SQLException {
        if (conexion != null) {
            if (!conexion.isClosed()) {
                conexion.close();
            }
        }
    }

}
   
// ME HE QUEDAO HACIENDO LA CONEXION A LA DB 