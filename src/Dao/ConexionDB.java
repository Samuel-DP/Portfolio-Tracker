package Dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    public static Connection conexion = null;

    public static Connection getConexion(String url, String usuario, String password) {

        try {

            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); // Driver de conexion para MSQL Server

            conexion = DriverManager.getConnection(url, usuario, password);

            System.out.println("Conexion establecida");

        } catch (Exception ex) {
            System.out.println("Error de conexion " + ex.getMessage());
            ex.printStackTrace();   
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
   