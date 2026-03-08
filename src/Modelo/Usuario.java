
package Modelo;

import java.util.Objects;


public class Usuario {
    
    private int id;
    private String email;
    private String username;
    private String passwordHash;
    private boolean isActive;
    
    // Constructor para crear usuario nuevo de registro
    public Usuario(String email, String username, String passwordHash){
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    
    public Usuario(int id, String email, String username, String passwordHash, boolean isActive){
        this.id = id;
        this.email = email;
        this.username = username;
        this.passwordHash  = passwordHash;
        this.isActive = isActive;
        
    }
    
    public int getId(){ return id; }
    public String getEmail(){ return email; }
    public String getUsername(){ return username; }
    public String getPasswordHash(){ return passwordHash; }
    public boolean IsActive(){ return isActive; }
    
    
}
