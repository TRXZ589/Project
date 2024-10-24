package main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.Database;

public class Login {
	private Label loginLabel, usernameLabel, passLabel, registerLabel;
	
	private Hyperlink hyperLink;
	
	private TextField usernameField;
	private PasswordField passField;
	
	private Button loginButton;
	
	private GridPane gp;

	private Stage primaryStage;
	
	Database db;

	public Login(Stage primaryStage) {
		super();
		this.primaryStage = primaryStage;
	}

	public void login() {
	  primaryStage.setTitle("Login");
		
      loginLabel = new Label("Login");
      usernameLabel = new Label("Username:");
      passLabel = new Label("Password:");
      registerLabel = new Label("Don't have an account yet?");
      
      hyperLink = new Hyperlink("Register here");
      
      usernameField = new TextField();
      passField = new PasswordField();
      
      loginButton = new Button("Login");
        
      usernameField.setMaxWidth(200);
      passField.setMaxWidth(200);
      
      loginLabel.setFont(new Font(20));
      loginLabel.setStyle("-fx-font-weight: bold");
      
      db = new Database();


      loginButton.setOnAction(e -> {
      String username = usernameField.getText();
      String password = passField.getText();
      
      handleLogin(username, password);
      });
         
      hyperLink.setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent event) {
            Register register1 = new Register(primaryStage, primaryStage);
            register1.register();
            
          }
      });
      gp = new GridPane();
      gp.setAlignment(Pos.CENTER);
      gp.setPadding(new Insets(20, 20, 20, 20));
      gp.setVgap(10);
      gp.setHgap(10);
      loginForm();
      
      Scene loginScene = new Scene(gp, 600, 600);
      primaryStage.setScene(loginScene);
      primaryStage.show();
	}
	
	private void loginForm() {
		gp.add(loginLabel, 1, 0);
		gp.add(usernameLabel, 0, 1);
		gp.add(usernameField, 1, 1);
		gp.add(passLabel, 0, 2);
		gp.add(passField, 1, 2);
		gp.add(registerLabel, 1, 3);
		gp.add(hyperLink, 2, 3);
		gp.add(loginButton, 1, 4);
	}

	private String handleLogin(String username, String password) {
	    db.connect();
	    try (Connection connection = db.getConnection()) {
	        String query = "SELECT * FROM user WHERE username = ? AND password = ?";
	        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
	            preparedStatement.setString(1, username);
	            preparedStatement.setString(2, password);

	            try (ResultSet resultSet = preparedStatement.executeQuery()) {
	                if (resultSet.next()) {
	                    String role = resultSet.getString("role");
	                    String userID = resultSet.getString("userID");
	                    System.out.println("Login berhasil!");

	                    if ("Admin".equals(role)) {
	                        // Redirect ke halaman home admin
	                        HomeAdmin homeAdmin = new HomeAdmin(primaryStage, username);
	                        homeAdmin.init();
	                    } else {
	                        // Redirect ke halaman home customer
	                        Home home = new Home(primaryStage, username, userID);
	                        home.init();
	                    }

	                    return userID;
	                } else {
	                    showErrorAlert();
	                    return null;
	                }
	            }
	        }
	    } catch (SQLException ex) {
	        ex.printStackTrace();
	        return null;
	    }
	}    

    private void showErrorAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Invalid username or password.");
        alert.showAndWait();
    }
}
