package main;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.Database;
import model.Product;

public class Home{
 
	Scene HomeScene;
	BorderPane bp;
	GridPane gp;
 
    MenuBar menuBar;
    Menu homeMenu, cartMenu, accountMenu;
    MenuItem homemenuItem, cartmenuItem, accountmenuItem1, accountmenuItem2;
    
    Label homeLabel, titleLabel, subTitleLabel, priceLabel, quantityLabel, totalLabel, totalCartLabel;
    
    Spinner<Integer> quantitySpinner;
    
    VBox vb;
    HBox hb;
    
    Button addButton;
    
    ListView<Product> productListView;
    private Stage primaryStage;
    private ProductHistory history;
    
    ArrayList<String> listOfItems = new ArrayList<String>();
    
    private String username;
    
    private Database db;
   

    public Home(Stage primaryStage, String username, String userID) {
        super();
        this.primaryStage = primaryStage;
        this.username = username;
        db = new Database();
        setUpProductTable();
    }
    
    public void init() {
        db.connect();
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

        homeLabel = new Label("SeRuput Teh");
        homeLabel.setFont(new Font(40));
        homeLabel.setStyle("-fx-font-weight: bold");

        vb = new VBox();
        titleLabel = new Label("Welcome, " + username + "!");
        titleLabel.setStyle("-fx-font-weight: bold");

        subTitleLabel = new Label("Select a product to view");
        subTitleLabel.setWrapText(true);

        priceLabel = new Label("Price: ");
        priceLabel.setVisible(false);

        quantitySpinner = new Spinner<>(1, 100, 1);
        quantityLabel = new Label("Quantity:");
        totalLabel = new Label("Price: ");
        quantityLabel.setVisible(false);
        quantitySpinner.setVisible(false);
        totalLabel.setVisible(false);

        hb = new HBox();
        hb.getChildren().addAll(quantityLabel, quantitySpinner, totalLabel);
        hb.setSpacing(10);

        addButton = new Button("Add To Cart");
        addButton.setVisible(false);
        addButton.setOnAction(e -> {
            Product selectedProduct = productListView.getSelectionModel().getSelectedItem();
            int quantity = quantitySpinner.getValue();

            if (selectedProduct != null && quantity > 0) {
                addToCart(username, selectedProduct, quantity);
            }
        });

        vb.getChildren().addAll(titleLabel, subTitleLabel, priceLabel, hb, addButton);
        vb.setPadding(new Insets(10, 10, 10, 10));
        vb.setMaxWidth(400);
        vb.setSpacing(10);

        gp = new GridPane();
        layoutComps();
        gp.setAlignment(Pos.TOP_LEFT);
        gp.setPadding(new Insets(10, 0, 0, 10));

        bp.setTop(menuBar);
        bp.setLeft(gp);

        HomeScene = new Scene(bp, 900, 600);

        primaryStage.setTitle("Home");
        primaryStage.setScene(HomeScene);
        primaryStage.show();
    }
    
    private void setUpProductTable() {
        productListView = new ListView<>();

        productListView.setCellFactory(param -> new ListCell<Product>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getProductName());
                }
            }
        });

        List<Product> productList = db.getAllProducts();
        productListView.getItems().addAll(productList);

        productListView.setOnMouseClicked(event -> {
            Product selectedProduct = productListView.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                String productDescription = selectedProduct.getProductDescription();
                long productPrice = selectedProduct.getProductPrice();
                titleLabel.setText(selectedProduct.getProductName());
                showProductDescription(productDescription, productPrice);
                showQuantitySpinner();
                quantitySpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                    if (newValue != null) {
                        int quantity = newValue;
                        long totalPrice = quantity * productPrice;
                        totalLabel.setText("Total: " + String.valueOf(totalPrice));
                    }
                });

            }
        });
    }

 	private void showProductDescription(String productDescription, long productPrice) {
	    subTitleLabel.setText("");
	    subTitleLabel.setText(productDescription);
	    priceLabel.setText("Price: " + productPrice);
	    quantitySpinner.getValueFactory().setValue(1);
	    totalLabel.setText("Total: " + productPrice);
	}
 
 	private void initMenu() {
  
 		menuBar.getMenus().add(homeMenu);
 		homeMenu.getItems().add(homemenuItem);
  
 		menuBar.getMenus().add(cartMenu);
 		cartMenu.getItems().add(cartmenuItem);
 		
 		cartmenuItem.setOnAction(e ->{
 			Cart cart = new Cart(primaryStage, username);
 			cart.createMainScene();
 			cart.updateTotalLabel();
 			
 		});

 		menuBar.getMenus().add(accountMenu);
 		accountMenu.getItems().addAll(accountmenuItem1, accountmenuItem2);
 		
 		accountmenuItem1.setOnAction(e -> {
 			history = new ProductHistory(primaryStage, username);
 			history.init();
 		});
 		
 		accountmenuItem2.setOnAction(e -> {
 		    Login login = new Login(primaryStage);
 		    login.login();
 		});
 		
 }
 	public void addToCart(String username, Product selectedProduct, int quantity) {
 	    db.connect();

 	    try (Connection connection = db.getConnection()) {
 	        if (db.userExists(username)) {
 	            String productName = selectedProduct.getProductName();
 	            String productID = db.getProductIDByName(productName);

 	            if (productID != null) {
 	                if (db.productExistsInCart(username, productID)) {
 	                    db.updateCartItem(username, productID, quantity);
 	                    System.out.println("Product quantity updated in the cart successfully.");
 	                } else {
 	                    db.connect();
 	                    db.addToCart(username, productName, quantity);
 	                    System.out.println("Product added to cart successfully.");

 	                    showAlert("Message", "Added to Cart");
 	                }
 	            } else {
 	                System.out.println("Product not found.");
 	            }
 	        } else {
 	            System.out.println("User not found.");
 	        }
 	    } catch (SQLException e) {
 	        e.printStackTrace();

 	        showAlert("Error", "Failed to add product to cart");
 	    }
 	}

 	 private void showAlert(String title, String content) {
         Alert alert = new Alert(Alert.AlertType.INFORMATION);
         alert.setTitle(title);
         alert.setHeaderText(null);
         alert.setContentText(content);
         alert.showAndWait();
     }
 	
 
 	private void showQuantitySpinner() {
	 	priceLabel.setVisible(true);
	 	quantityLabel.setVisible(true);
	    quantitySpinner.setVisible(true);
	    totalLabel.setVisible(true);
	    addButton.setVisible(true);
	}
 
 	public void layoutComps() {
 		gp.add(homeLabel, 0, 0);
 		gp.add(productListView, 0, 1);
 		gp.add(vb, 1, 1);
 }
 }