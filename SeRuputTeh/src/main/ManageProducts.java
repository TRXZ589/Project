package main;

import java.sql.ResultSet;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.Database;
import model.Product;

public class ManageProducts{
 
	Scene manageScene;
	BorderPane bp;
	GridPane gp;
 
    MenuBar menuBar;
    Menu homeMenu, manageMenu, accountMenu;
    MenuItem homemenuItem, managemenuItem, accountmenuItem;
    
    Label homeLabel, titleLabel, subTitleLabel, priceLabel, quantityLabel, totalLabel, newProductName, newProductPrice, newProductDes, updateProductPrice;
    
    Button addProduct, updateProduct, removeProduct;
    
    Spinner<Integer> quantitySpinner;
    
    VBox vb;
    HBox hb;
    
    ListView<Product> productListView;
    
    private Stage primaryStage;
    
    ArrayList<String> listOfItems = new ArrayList<String>();
    
    private String username;
    
    private Database db;
    
    public ManageProducts(Stage primaryStage, String username) {
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
     
     homeLabel = new Label("Manage Products");
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
     
     addProduct = new Button("Add Product");
     addProduct.setOnAction(e -> showOtherAddProductDialog());
     
     updateProduct = new Button("Update Product");
     removeProduct = new Button("Remove Product");
     updateProduct.setVisible(false);
     removeProduct.setVisible(false);
     
     hb = new HBox();
     hb.getChildren().addAll(updateProduct, removeProduct);
     hb.setSpacing(10);

     vb.getChildren().addAll(titleLabel, subTitleLabel, priceLabel, addProduct, hb);
     vb.setPadding(new Insets (10, 10, 10, 10)); 
     vb.setMaxWidth(400);
     vb.setSpacing(10);
   
     gp = new GridPane();
     layoutComps();
     gp.setAlignment(Pos.TOP_LEFT);
     gp.setPadding(new Insets(10, 0, 0, 10));
     
     bp.setTop(menuBar);
     bp.setLeft(gp);
     
     manageScene = new Scene(bp, 900, 700);
     
     primaryStage.setTitle("ManageProducts");
     primaryStage.setScene(manageScene);
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
    	        showButtons();
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
 	private void showButtons() {
	 	priceLabel.setVisible(true);
	 	updateProduct.setVisible(true);
	 	removeProduct.setVisible(true);
		addProduct.setOnAction(e -> showAddProductDialog());
	    updateProduct.setOnAction(e -> showUpdateProductDialog());
	    removeProduct.setOnAction(e -> showRemoveProductDialog());
	}
 	
 	private void showAddProductDialog() {
 		addProduct.setVisible(false);
 	    updateProduct.setVisible(false);
 	    removeProduct.setVisible(false);

 	    TextField productNameField = new TextField();
 	    TextField productPriceField = new TextField();
 	    TextArea productDescriptionArea = new TextArea();

 	    newProductName = new Label("Input Product Name");
 	    newProductPrice = new Label("Input Product Price");
 	    newProductDes = new Label("Input Product Description");
 	    
 	    newProductName.setStyle("-fx-font-weight: bold");
 	    newProductPrice.setStyle("-fx-font-weight: bold");
 	    newProductDes.setStyle("-fx-font-weight: bold");

 	    productNameField.setPromptText("Input product name...");
 	    productPriceField.setPromptText("Input product price...");
 	    productDescriptionArea.setPromptText("Input product description...");

 	    VBox dialogContent = new VBox(newProductName, productNameField, newProductPrice, productPriceField, newProductDes, productDescriptionArea);
 	    dialogContent.setSpacing(10);

 	    vb.getChildren().add(dialogContent);

 	    Button confirmButton = new Button("Add Product");
 	    confirmButton.setOnAction(e -> {
 	        String productID = generateID();
 	        String productName = productNameField.getText();
 	        String productPriceText = productPriceField.getText();
 	        String productDescription = productDescriptionArea.getText();

 	        try {
 	            long productPrice = Long.parseLong(productPriceText);
 	            db.addProduct(new Product(productID, productName, productPrice, productDescription));
 	            productListView.getItems().clear();
 	            productListView.getItems().addAll(db.getAllProducts());
 	        } catch (NumberFormatException ex) {
 	            showErrorAlert("Invalid product price. Please enter a valid number.");
 	        }

 	        vb.getChildren().remove(dialogContent);

 	        addProduct.setVisible(true);
 	        updateProduct.setVisible(true);
 	        removeProduct.setVisible(true);
 	    });

 	    Button cancelButton = new Button("Back");
 	    cancelButton.setOnAction(e -> {
 	    	
 	        vb.getChildren().remove(dialogContent);

 	        addProduct.setVisible(true);
 	        updateProduct.setVisible(true);
 	        removeProduct.setVisible(true);
 	    });

 	    HBox buttonBox = new HBox(confirmButton, cancelButton);
 	    buttonBox.setSpacing(10);
 	    dialogContent.getChildren().add(buttonBox);
 	}
 	private void showOtherAddProductDialog() {
 		addProduct.setVisible(false);
 	    updateProduct.setVisible(false);
 	    removeProduct.setVisible(false);

 	    TextField productNameField = new TextField();
 	    TextField productPriceField = new TextField();
 	    TextArea productDescriptionArea = new TextArea();

 	    newProductName = new Label("Input Product Name");
 	    newProductPrice = new Label("Input Product Price");
 	    newProductDes = new Label("Input Product Description");
 	    
 	    newProductName.setStyle("-fx-font-weight: bold");
 	    newProductPrice.setStyle("-fx-font-weight: bold");
 	    newProductDes.setStyle("-fx-font-weight: bold");

 	    productNameField.setPromptText("Input product name...");
 	    productPriceField.setPromptText("Input product price...");
 	    productDescriptionArea.setPromptText("Input product description...");

 	    VBox dialogContent = new VBox(newProductName, productNameField, newProductPrice, productPriceField, newProductDes, productDescriptionArea);
 	    dialogContent.setSpacing(10);

 	    vb.getChildren().add(dialogContent);

 	    Button confirmButton = new Button("Add Product");
 	    confirmButton.setOnAction(e -> {
 	        String productID = generateID();
 	        String productName = productNameField.getText();
 	        String productPriceText = productPriceField.getText();
 	        String productDescription = productDescriptionArea.getText();

 	        try {
 	            long productPrice = Long.parseLong(productPriceText);
 	            db.addProduct(new Product(productID, productName, productPrice, productDescription));
 	            productListView.getItems().clear();
 	            productListView.getItems().addAll(db.getAllProducts());
 	        } catch (NumberFormatException ex) {
 	            showErrorAlert("Invalid product price. Please enter a valid number.");
 	        }

 	        vb.getChildren().remove(dialogContent);

 	        addProduct.setVisible(true);
 	    });

 	    Button cancelButton = new Button("Back");
 	    cancelButton.setOnAction(e -> {
 	    	
 	        vb.getChildren().remove(dialogContent);
 	        addProduct.setVisible(true);
 	    });

 	    HBox buttonBox = new HBox(confirmButton, cancelButton);
 	    buttonBox.setSpacing(10);
 	    dialogContent.getChildren().add(buttonBox);
 	}

    private void showUpdateProductDialog() {
    	Product selectedProduct = productListView.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showErrorAlert("Please select a product to update.");
            return;
        }
        addProduct.setVisible(false);
        updateProduct.setVisible(false);
        removeProduct.setVisible(false);
        
        updateProductPrice = new Label("Update Product");
        updateProductPrice.setStyle("-fx-font-weight: bold");

        TextField newPriceField = new TextField();
        newPriceField.setPromptText("Input new price...");

        vb.getChildren().addAll(updateProductPrice, newPriceField);

        Button confirmButton = new Button("Update Product");
        Button cancelButton = new Button("Back");
        HBox buttonBox = new HBox(confirmButton, cancelButton);
        buttonBox.setSpacing(10);
        vb.getChildren().add(buttonBox);

        confirmButton.setOnAction(e -> {
            String newPriceText = newPriceField.getText();

            try {
                long newPrice = Long.parseLong(newPriceText);
                db.updateProductPrice(selectedProduct.getProductId(), newPrice);
                productListView.getItems().clear();
                productListView.getItems().addAll(db.getAllProducts());
            } catch (NumberFormatException ex) {
                showErrorAlert("Invalid new product price. Please enter a valid number.");
            }

            vb.getChildren().removeAll(updateProductPrice, newPriceField, buttonBox);

            addProduct.setVisible(true);
            updateProduct.setVisible(true);
            removeProduct.setVisible(true);
        });

        cancelButton.setOnAction(e -> {
            vb.getChildren().removeAll(updateProductPrice, newPriceField, buttonBox);

            addProduct.setVisible(true);
            updateProduct.setVisible(true);
            removeProduct.setVisible(true);
        });
    }

    private void showRemoveProductDialog() {
    	Product selectedProduct = productListView.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showErrorAlert("Please select a product to remove.");
            return;
        }

        addProduct.setVisible(false);
        updateProduct.setVisible(false);
        removeProduct.setVisible(false);

        Label confirmationLabel = new Label("Are you sure, you want to remove this product?");
        confirmationLabel.setStyle("-fx-font-weight: bold");
        
        Button yesButton = new Button("Remove Product");
        Button noButton = new Button("Back");
        HBox buttonBox = new HBox(yesButton, noButton);
        buttonBox.setSpacing(10);
        vb.getChildren().addAll(confirmationLabel, buttonBox);

        yesButton.setOnAction(e -> {
            db.removeProduct(selectedProduct.getProductId());
            productListView.getItems().clear();
            productListView.getItems().addAll(db.getAllProducts());

            vb.getChildren().removeAll(confirmationLabel, buttonBox);

            addProduct.setVisible(true);
            updateProduct.setVisible(true);
            removeProduct.setVisible(true);
        });

        noButton.setOnAction(e -> {
            vb.getChildren().removeAll(confirmationLabel, buttonBox);
            
            addProduct.setVisible(true);
            updateProduct.setVisible(true);
            removeProduct.setVisible(true);
        });
       
    }
    
    private String generateID() {
    	db.connect();
        ResultSet rs = db.executeQuery("SELECT productID FROM product WHERE productID LIKE 'TE%'");
        String uid = "TE001";

        try {
            if (rs.last()) {
            	
                int temp = Integer.valueOf(rs.getString("productID").substring(2, 5));
                uid = String.format("TE%03d", temp + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return uid;
    }
    
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
 
 	public void layoutComps() {
 		gp.add(homeLabel, 0, 0);
 		gp.add(productListView, 0, 1);
 		gp.add(vb, 1, 1);
 }
 }