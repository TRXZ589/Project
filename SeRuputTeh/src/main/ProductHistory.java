package main;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductHistory {
    MenuBar menuBar;
    Menu homeMenu, cartMenu, accountMenu;
    MenuItem homemenuItem, cartmenuItem, accountmenuItem1, accountmenuItem2;

    Scene historyScene;
    Label labelUserInfo;
    ListView<String> transactionListView;
    ListView<String> productListView;
    Label historyLabel, titleLabel, subTitleLabel;
    Database db;
    private Stage primaryStage;
    VBox vb;
    BorderPane bp;
    String username;
    GridPane gp;
    HBox hb;
    String userID;
    private Label usernameLabel;
    private Label phoneNumberLabel;
    private Label addressLabel;
    private Label totalLabel;

    public ProductHistory(Stage primaryStage, String username) {
        this.primaryStage = primaryStage;
        this.username = username;
        db = new Database();
    }

    public void init() {
        bp = new BorderPane();
        menuBar = new MenuBar();
        homeMenu = new Menu("Home");
        cartMenu = new Menu("Cart");
        accountMenu = new Menu("Account");

        homemenuItem = new MenuItem("Homepage");
        cartmenuItem = new MenuItem("My Cart");
        accountmenuItem1 = new MenuItem("Purchase History");
        accountmenuItem2 = new MenuItem("Log Out");

        initMenu();

       
        historyLabel = new Label(username + "'s Purchase History");
        historyLabel.setFont(new Font(40));
        historyLabel.setStyle("-fx-font-weight: bold");
        historyLabel.setWrapText(true);
        historyLabel.setMaxWidth(Double.MAX_VALUE);

        
        transactionListView = new ListView<>();
        transactionListView.setPrefSize(150, 300);
        transactionListView.setMaxSize(150, Double.MAX_VALUE);
        transactionListView.setPrefSize(180, 300);
        
        
        subTitleLabel = new Label();

        titleLabel = new Label("Select a Transaction to view Details");
        titleLabel.setStyle("-fx-font-weight: bold");

        usernameLabel = new Label("Username: ");
        usernameLabel.setStyle("-fx-font-weight: bold");

        phoneNumberLabel = new Label("Phone Number: ");
        addressLabel = new Label("Address: ");

        totalLabel = new Label("Total: ");
        totalLabel.setStyle("-fx-font-weight: bold");

        
        vb = new VBox();
        vb.setPadding(new Insets(10, 10, 10, 10));
        vb.setMaxWidth(400);
        vb.setSpacing(10);
        
        setUpProductTable();
        displayTransactionDetails("");
        
        hb = new HBox();
        hb.getChildren().addAll(transactionListView, vb);
        hb.setSpacing(10);
        

        gp = new GridPane();
       
        gp.setAlignment(Pos.TOP_LEFT);
        gp.setPadding(new Insets(10, 0, 0, 10));
        layout();


        bp.setTop(menuBar);
        bp.setLeft(gp);

        historyScene = new Scene(bp, 900, 600);
        primaryStage.setScene(historyScene);
        primaryStage.setTitle("History");
        primaryStage.show();
    }

    private void setUpProductTable() {
        db.connect();
        try {
            String query = "SELECT transactionID FROM transaction_header WHERE userID = ?";

            try (PreparedStatement preparedStatement = db.getConnection().prepareStatement(query)) {
                preparedStatement.setString(1, getUserId(username));

                try (ResultSet rs = preparedStatement.executeQuery()) {
                    transactionListView.getItems().clear();
                    while (rs.next()) {
                        String transactionID = rs.getString("transactionID");
                        transactionListView.getItems().add(transactionID);
                    }
                }
            }

            transactionListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    displayTransactionDetails(newValue);
                }
            });

            productListView = new ListView<>();
            productListView.setPrefSize(300, 300);
            productListView.setMaxSize(500, Double.MAX_VALUE);

            vb.getChildren().addAll(titleLabel, subTitleLabel, usernameLabel, phoneNumberLabel, addressLabel, totalLabel, productListView);
            
            productListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    updateProductListView(newValue);
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getUserId(String username) {
        db.connect();
        try {
            String query = "SELECT userID FROM user WHERE username = ?";

            try (PreparedStatement preparedStatement = db.getConnection().prepareStatement(query)) {
                preparedStatement.setString(1, username);

                try (ResultSet rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("userID");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void displayTransactionDetails(String transactionID) {
        db.connect();
        try {
            String query = "SELECT td.quantity, p.product_name, p.product_price, u.phone_num, u.address " +
                    "FROM transaction_detail td " +
                    "JOIN product p ON td.productID = p.productID " +
                    "JOIN transaction_header th ON td.transactionID = th.transactionID " +
                    "JOIN user u ON th.userID = u.userID " +
                    "WHERE td.transactionID = ?";

            try (PreparedStatement preparedStatement = db.getConnection().prepareStatement(query)) {
                preparedStatement.setString(1, transactionID);
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    long total = 0;
                    subTitleLabel.setVisible(false);

                    if (!rs.isBeforeFirst()) {
                    	subTitleLabel.setVisible(true);
                        subTitleLabel.setText("No details available for this transaction.");
                        productListView.getItems().clear();
                    } else {
                        subTitleLabel.setVisible(false);
                        titleLabel.setText("Transaction ID: " + transactionID);

                        if (rs.next()) {
                            usernameLabel.setText("Username: " + username);
                            phoneNumberLabel.setText("Phone Number: " + rs.getString("phone_num"));
                            addressLabel.setText("Address: " + rs.getString("address"));

                            do {
                                int quantity = rs.getInt("quantity");
                                long productPrice = rs.getLong("product_price");

                                total += quantity * productPrice;
                            } while (rs.next());

                            totalLabel.setText("Total: Rp." + total);
                            updateProductListView(transactionID);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void updateProductListView(String transactionID) {
        db.connect();
        try {
            String query = "SELECT td.quantity, p.product_name, p.product_price FROM transaction_detail td JOIN product p ON td.productID = p.productID WHERE td.transactionID = ?";
            try (PreparedStatement preparedStatement = db.getConnection().prepareStatement(query)) {
                preparedStatement.setString(1, transactionID);
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    List<String> productDetails = new ArrayList<>();

                    if (!rs.isBeforeFirst()) {
                        productDetails.add("No products available for this transaction.");
                    } else {
                        while (rs.next()) {
                            int quantity = rs.getInt("quantity");
                            String productName = rs.getString("product_name");
                            long productPrice = rs.getLong("product_price");
                            long totalHarga = quantity * productPrice;

                            String productDetail = quantity + "x " + productName + " (RP." + totalHarga + ")";
                            productDetails.add(productDetail);
                        }
                    }

                    productListView.getSelectionModel().clearSelection();
                    productListView.setMouseTransparent(true);
                    productListView.setFocusTraversable(false);
                    productListView.getItems().setAll(productDetails);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private void initMenu() {
        menuBar.getMenus().add(homeMenu);
        homeMenu.getItems().add(homemenuItem);
        
        homemenuItem.setOnAction(e -> {
            Home home = new Home(primaryStage, username, userID);
            home.init();
        });

        menuBar.getMenus().add(cartMenu);
        cartMenu.getItems().add(cartmenuItem);
        
        cartmenuItem.setOnAction(e ->{
 			Cart cart = new Cart(primaryStage, username);
 			cart.createMainScene();
 			cart.updateTotalLabel();
 			
 		});

        menuBar.getMenus().add(accountMenu);
        accountMenu.getItems().addAll(accountmenuItem1, accountmenuItem2);
        
        accountmenuItem2.setOnAction(e -> {
 		    Login login = new Login(primaryStage);
 		    login.login();
 		});
    }

    private void layout() {
    	gp.add(historyLabel, 0, 0);
    	gp.add(hb, 0, 1);    

    }
}