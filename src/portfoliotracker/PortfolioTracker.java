
package portfoliotracker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PortfolioTracker extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/Vista/Login.fxml"));
        
        primaryStage.setTitle("Portfolio Tracker");
        primaryStage.setScene(new Scene(root));
        // primaryStage.setResizable(false); // elimino el boton de maximizar
        primaryStage.show();  
                
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    
    
}
