
package Modelo;


public class vGlobales {
    
    private static Integer usuarioIdActual;
    
    public static String getCadena(){
        
        String cadena_con = "jdbc:sqlserver://"+ ConfigDB.getHost() + ":" + ConfigDB.getPort() + ";databaseName=" + ConfigDB.getDatabase() + " ;encrypt=true;trustServerCertificate=true" ;
        
        return cadena_con;
        
    }
    
    public static String getApiKey(){
        String apiKey = System.getenv("FINNHUB_KEY");
        
        if(apiKey == null || apiKey.isBlank()){
            throw new IllegalStateException("La variable de entorno FINNHUB_KEY no esta configurada.");
        }
        
        return apiKey;
    }
    
     public static void setUsuarioIdActual(Integer usuarioId){
        usuarioIdActual = usuarioId;
    }

    public static Integer getUsuarioIdActual(){
        return usuarioIdActual;
    }
    
}
