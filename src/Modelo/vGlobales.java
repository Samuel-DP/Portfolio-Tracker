
package Modelo;


public class vGlobales {
    
    private static final String FINNHUB_KEY = "d5s8lnhr01qoo9r3aongd5s8lnhr01qoo9r3aoo0";
    
    public static String USER =""; 
    public static String PASSWORD =""; 
    
    public static String host = "localhost";
    public static String puerto = "1433"; // Puerto por defecto de SQL Server
    public static String db = "PortfolioTracker"; // Nombre de la DB que me quiera conectar 
    
    public static String getCadena(){
        
        String cadena_con = "jdbc:sqlserver://"+ host + ":" + puerto + ";databaseName=" + db + " ;encrypt=true;trustServerCertificate=true" ;
        
        return cadena_con;
        
    }
    
    public static String getApiKey(){
        return FINNHUB_KEY;
    }
    
}
