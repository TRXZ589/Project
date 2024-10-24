package main;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.Database;
import model.Product;

public class HomeAdmin{
 
	Scene HomeScene;
	BorderPane bp;
	GridPane gp;
 
    MenuBar menuBar;
    Menu homeMenu, manageMenu, accountMenu;
    MenuItem homemenuItem, managemenuItem, accountmenuItem;
    
    Label homeLabel, titleLabel, subTitleLabel, priceLabel, quantityLabel, totalLabel;
    
    Spinner<Integer> quantitySpinner;
    
    VBox vb;
    
    ListView<Product> productListView;
    private Stage primaryStage;
    
    ArrayList<String> listOfItems = new ArrayList<String>();
    
    private String username;
    
    private Database db;
   
    public HomeAdmin(Stage primaryStage, String username) {
    	super();
    	this.primaryStage = primaryStage;
    	this.username = username;
    	db = new Database();
 }
    public void init() {
    
     bp = new BorderPane();
     
     menuBar = new MenuBar();
     homeMenu = new Menu("Home");
     manageMenu = new Menu("Manage Products");
     accountMenu = new Menu("Account");
      
     homemenuItem = new MenuItem("Homepage");
     managemenuItem = new MenuItem("Manage Products");
     accountmenuItem = new MenuItem("Log out");
     
     initMenu();
     
     homeLabel = new Label("SeRuput Teh");
     homeLabel.setFont(new Font(40));
     homeLabel.setStyle("-fx-font-weight: bold");
     
     setUpProductTable();
     
     vb = new VBox();
     titleLabel = new Label("Welcome, " + username + "!");
     titleLabel.setStyle("-fx-font-weight: bold");
     
     subTitleLabel = new Label("Select a product to view");
     subTitleLabel.setWrapText(true);
     
     priceLabel = new Label("Price: ");
     priceLabel.setVisible(false);

     vb.getChildren().addAll(titleLabel, subTitleLabel, priceLabel);
     vb.setPadding(new Insets (10, 10, 10, 10)); 
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
		            setText(item.getProductName()); // Sesuaikan dengan metode yang sesuai untuk mendapatkan nama produk
		        }
		    }
		});

     db = new Database();
     List<Product> productList =db.getAllProducts();
     productListView.getItems().addAll(productList);
     
     productListView.setOnMouseClicked(event -> {
    	    Product selectedProduct = productListView.getSelectionModel().getSelectedItem();
    	    if (selectedProduct != null) {
    	        String productDescription = selectedProduct.getProductDescription();
    	        long productPrice = selectedProduct.getProductPrice();
    	        titleLabel.setText(selectedProduct.getProductName());
    	        showProductDescription(productDescription, productPrice);
    	        showQuantitySpinner();
    	    }
    	});
     
     
 }

 	private void showProductDescription(String productDescription, long productPrice) {
	    subTitleLabel.setText("");
	    subTitleLabel.setText(productDescription);
	    priceLabel.setText("Price: " + productPrice);
	}
 
 	private void initMenu() {
  
 		menuBar.getMenus().add(homeMenu);
 		homeMenu.getItems().add(homemenuItem);
  
 		menuBar.getMenus().add(manageMenu);
 		manageMenu.getItems().add(managemenuItem);

 		menuBar.getMenus().add(accountMenu);
 		accountMenu.getItems().addAll(accountmenuItem);
 		
 		homemenuItem.setOnAction(e -> {
 			HomeAdmin homeAdmin = new HomeAdmin(primaryStage, username);
 			homeAdmin.init();
 		});
 		
 		managemenuItem.setOnAction(e -> {
 			ManageProducts manageProducts = new ManageProducts(primaryStage, username);
 			manageProducts.init();
 		});
 		
 		accountmenuItem.setOnAction(e -> {
 		    Login login = new Login(primaryStage);
 		    login.login();
 		});
 }
 
 
 	private void showQuantitySpinner() {
	 	priceLabel.setVisible(true);
	}
 
 	public void layoutComps() {
 		gp.add(homeLabel, 0, 0);
 		gp.add(productListView, 0, 1);
 		gp.add(vb, 1, 1);
 }
 }