package main;

import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.Database;

public class Register {

 private Label usernameLabel, emailLabel, passLabel, confirmPassLabel, phoneNumLabel, addressLabel, genderLabel,
   hyperlinkLabel, registration;

 private FlowPane genderFlow;
 private ToggleGroup genderGroup;
 private RadioButton maleRadio, femaleRadio;

 private TextField usernameTF, emailTF, phoneNumTF;
 private PasswordField passwordTF, confirmPassTF;
 private TextArea addressTF;
 private Button registerButton;

 private Scene scene;
 private CheckBox agree;
 private GridPane gp;
 private BorderPane bp;
 private Hyperlink loginLink;

 private HBox hb;
 private Stage login;
 private Stage primaryStage;
 Database db;

 public Register(Stage login, Stage primaryStage) {
  super();
  db = new Database();
  this.login = login;
  this.primaryStage = primaryStage;

 }

 public void register() {
  registration = new Label("Register");
  usernameLabel = new Label("Username");
  passLabel = new Label("Password :");
  emailLabel = new Label("Email: ");
  confirmPassLabel = new Label("Confirm Password : ");
  phoneNumLabel = new Label("Phone Number : ");
  addressLabel = new Label("Address : ");
  genderLabel = new Label("Gender : ");
  hyperlinkLabel = new Label("Have an account? ");
  bp = new BorderPane();

  usernameTF = new TextField();
  emailTF = new TextField();
  phoneNumTF = new TextField();
  passwordTF = new PasswordField();
  confirmPassTF = new PasswordField();
  addressTF = new TextArea();

  maleRadio = new RadioButton("Male");
  femaleRadio = new RadioButton("Female");
  genderGroup = new ToggleGroup();
  maleRadio.setToggleGroup(genderGroup);
  femaleRadio.setToggleGroup(genderGroup);
  genderFlow = new FlowPane();
  genderFlow.getChildren().addAll(maleRadio, femaleRadio);

  agree = new CheckBox("I agree to all terms and conditions");

  gp = new GridPane();

  hb = new HBox();
  loginLink = new Hyperlink("Login here");
  hb.getChildren().addAll(hyperlinkLabel, loginLink);

  loginLink.setOnAction(e -> handleLoginLink());

  registerButton = new Button("Register");
  registerButton.setOnAction(e -> handleRegistration());
  registration.setFont(new Font(40));
  registration.setStyle("-fx-font-weight: bold");

  bp.setCenter(gp);
  gp.setPadding(new Insets(20, 20, 20, 20));
  gp.setVgap(10);
  gp.setHgap(10);
  isiForm();

  scene = new Scene(bp, 800, 800);
  primaryStage.setScene(scene);
  primaryStage.show();

 }

 public void isiForm() {
  gp.add(registration, 1, 0);

  gp.add(usernameLabel, 0, 2);
  gp.add(usernameTF, 1, 2);

  gp.add(emailLabel, 0, 3);
  gp.add(emailTF, 1, 3);

  gp.add(passLabel, 0, 4);
  gp.add(passwordTF, 1, 4); // 1 itu spasi ke samping, 2 itu spasi kebawah

  gp.add(confirmPassLabel, 0, 5);
  gp.add(confirmPassTF, 1, 5);

  gp.add(phoneNumLabel, 0, 6);
  gp.add(phoneNumTF, 1, 6);

  gp.add(addressLabel, 0, 7);
  gp.add(addressTF, 1, 7);

  gp.add(genderLabel, 0, 8);
  gp.add(genderFlow, 1, 8);

  gp.add(agree, 1, 9);
  gp.add(hb, 1, 10);

  gp.add(registerButton, 1, 11);
 }

 private String generateID() {
     ResultSet rs = db.executeQuery("SELECT userID FROM user WHERE userID LIKE 'CU%'");
     String uid = "CU001";
        try {
         int temp = Integer.MIN_VALUE;
         
   while (rs.next()) {
    int id = Integer.valueOf(rs.getString("userID").substring(2, 5));
    
    if (temp < id) 
     temp = id;
   
   }
    uid = String.format("CU%03d", temp + 1);
        } catch (SQLException e) {
         e.printStackTrace();
        }
   
   return uid;
        }

 private void handleRegistration() {
  
  if (validateInput()) {
   // Insert user to the database with assigned role and User ID
   String uid = generateID();
   String name = usernameTF.getText();
   String role = "Customer";
   String password = passwordTF.getText();
   String address = addressTF.getText();
   String phone_num = phoneNumTF.getText();
   RadioButton rb = (RadioButton) genderGroup.getSelectedToggle();
   String gender = rb.getText();
   
   db.executeUpdate(uid, name, password, role, address,  phone_num, gender, "create");
   
   showInfoAlert("Registration Successful", "You have successfully registered!");
   handleLoginLink();
  }
 }

 private boolean validateInput() {
  StringBuilder errorMessage = new StringBuilder();
  if(!(usernameTF.getText().length() >= 5 && usernameTF.getText().length() <= 20))
   errorMessage.append("Username must be 5-20 characters\n");
  for (int i = 0; i < passwordTF.getText().length(); i++) {
   if (!Character.isLetterOrDigit(passwordTF.getText().charAt(i))) {
    errorMessage.append("Password must be alphanumeric\n");
    break;
   }  
  }
  if(!(passwordTF.getText().length() >= 5))
   errorMessage.append("Password must be at least 5 characters\n");
  
  if(!(passwordTF.getText().equals(confirmPassTF.getText())))
   errorMessage.append("Confirm Password must equals to password\n");
  
  for (int i = 1; i < phoneNumTF.getText().length(); i++) {
   if(!Character.isDigit(phoneNumTF.getText().charAt(i))){
    errorMessage.append("Phone number must be numeric\n");
   }
  }
  if(!(phoneNumTF.getText().startsWith("+62")))
   errorMessage.append("Phone number must start with '+62'\n");
  
  if (addressTF.getText().isEmpty() || !agree.isSelected() || (genderGroup.getSelectedToggle() == null))
   errorMessage.append("All fields must be filled\n");
   
  if (errorMessage.length() > 0) {
   showErrorAlert("Registration Error", errorMessage.toString());
   return false;
  }

  return true;
 }

 private void showErrorAlert(String title, String message) {
  Alert alert = new Alert(AlertType.ERROR);
  alert.setTitle(title);
  alert.setHeaderText(null);
  alert.setContentText(message);
  alert.showAndWait();
 }

 private void showInfoAlert(String title, String message) {
  Alert alert = new Alert(AlertType.INFORMATION);
  alert.setTitle(title);
  alert.setHeaderText(null);
  alert.setContentText(message);
  alert.showAndWait();
 }

 private void handleLoginLink() {
  Login login1 = new Login(login);
  login1.login();
 }
}