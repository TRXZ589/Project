package main;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
	
	public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SeRuput Teh");
        
        Login login1 = new Login(primaryStage);
        
        
        login1.login();
    }
}
	