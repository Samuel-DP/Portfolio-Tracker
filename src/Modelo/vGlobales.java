
package Modelo;


public class vGlobales {
    
    private static final String FINNHUB_KEY = "d5s8lnhr01qoo9r3aongd5s8lnhr01qoo9r3aoo0";// gratuita por eso esta aquí y no en varibales del sistema como los datos en getCadena
    private static Integer usuarioIdActual;
    
    public static String getCadena(){
        
        String cadena_con = "jdbc:sqlserver://"+ ConfigDB.getHost() + ":" + ConfigDB.getPort() + ";databaseName=" + ConfigDB.getDatabase() + " ;encrypt=true;trustServerCertificate=true" ;
        
        return cadena_con;
        
    }
    
    public static String getApiKey(){
        return FINNHUB_KEY;
    }
    
     public static void setUsuarioIdActual(Integer usuarioId){
        usuarioIdActual = usuarioId;
    }

    public static Integer getUsuarioIdActual(){
        return usuarioIdActual;
    }
    
}
