package Modelo;

// Esta clase se encarga de leer las variables de entorno de mi sistema
public class ConfigDB {

    public static String getUser() {
        return System.getenv("DB_USER");
    }

    public static String getPassword() {
        return System.getenv("DB_PASSWORD");
    }

    public static String getHost() {
        return System.getenv("DB_HOST");
    }

    public static String getPort() {
        return System.getenv("DB_PORT");
    }

    public static String getDatabase() {
        return System.getenv("DB_NAME");
    }

}
