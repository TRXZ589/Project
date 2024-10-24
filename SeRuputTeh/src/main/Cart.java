package main;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.Database;
import model.Product;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class Cart {

    private Scene scene;
    private Label titleLabel;
    private Label welcomeLabel;
    private Label selectLabel;
    private Label productPriceLabel;
    private Label quantityLabel;
    private Label totalLabel;
    private Label orderLabel;
    private Label usernameLabel;
    private Label phoneLabel;
    private Label addressLabel;
    private Spinner<Integer> quantitySpinner;
    private Button updateCartButton;
    private Button removeFromCartButton;
    private Button makePurchaseButton;
    private ListView<Product> cartListView;
    private Stage primaryStage;
    private BorderPane borderPane;
    private String username;
    private String userID;
    MenuBar menuBar;
    Menu homeMenu, cartMenu, accountMenu;
    MenuItem homemenuItem, cartmenuItem, accountmenuItem1, accountmenuItem2;
    private ProductHistory history;
    private long currentProductPrice;   
    private Label totalLabelSpec;

    private Database db;

    public Cart(Stage primaryStage, String username) {
        super();
        this.primaryStage = primaryStage;
        this.username = username;
        db = new Database();
    }

    public void createMainScene() {

        borderPane = new BorderPane();

        welcomeLabel = new Label("Welcome, " + username);
        selectLabel = new Label("Select a product to add and remove");

        productPriceLabel = new Label("Price: Rp.");
        quantityLabel = new Label("Quantity: ");
        totalLabel = new Label("Total: Rp.");
        orderLabel = new Label("Order Information");
        orderLabel.setStyle("-fx-font-weight: bold;");
        usernameLabel = new Label("Username: ");
        phoneLabel = new Label("Phone Number: ");
        addressLabel = new Label("Address: ");

        menuBar = new MenuBar();
        homeMenu = new Menu("Home");
        cartMenu = new Menu("Cart");
        accountMenu = new Menu("Account");

        homemenuItem = new MenuItem("Homepage");
        cartmenuItem = new MenuItem("My Cart");
        accountmenuItem1 = new MenuItem("Purchase History");
        accountmenuItem2 = new MenuItem("Log Out");

        quantitySpinner = new Spinner<>(-100, 100, 1);
        totalLabelSpec = new Label("Total : Rp."); 

        updateCartButton = new Button("Update Cart");
        removeFromCartButton = new Button("Remove From Cart");
        makePurchaseButton = new Button("Make Purchase");

        initMenu();

        cartListView = new ListView<>();
        updateCartButton.setOnAction(e -> handleUpdateCart());
        removeFromCartButton.setOnAction(e -> handleRemoveFromCart());
        makePurchaseButton.setOnAction(e -> handleMakePurchase());

        cartListView.setPrefHeight(200);

        titleLabel = new Label(username + "'s Cart");
        titleLabel.setFont(new Font(40));
        titleLabel.setStyle("-fx-font-weight: bold;");
        welcomeLabel.setAlignment(Pos.CENTER_LEFT);
        selectLabel.setAlignment(Pos.CENTER_LEFT);

        welcomeLabel.setFont(new Font(14));
        welcomeLabel.setStyle("-fx-font-weight: bold;");
        selectLabel.setFont(new Font(14));

        productPriceLabel.setVisible(false);
        quantitySpinner.setVisible(false);

        setUpProductTable();

        if (isCartEmpty()) {
            welcomeLabel.setText("No item in cart");
            selectLabel.setText("Consider adding one!");
        } else {
            welcomeLabel.setText("Welcome, " + username);
            selectLabel.setText("Select a product to add and remove");
        }

        totalLabelSpec.setVisible(false);
        updateCartButton.setVisible(false);
        removeFromCartButton.setVisible(false);

        HBox hb = new HBox();
        hb.getChildren().addAll(quantitySpinner, totalLabelSpec);
        hb.setSpacing(10);

        HBox hb1 = new HBox();
        hb1.getChildren().addAll(updateCartButton, removeFromCartButton);
        hb1.setSpacing(10);

        VBox rightContainer = new VBox();
        rightContainer.setSpacing(10);
        rightContainer.getChildren().addAll(
                welcomeLabel,
                selectLabel,
                productPriceLabel,
                hb, 
                hb1
        );
        rightContainer.setPadding(new Insets(10, 10 , 10, 10));

        GridPane leftGrid = new GridPane();
        leftGrid.setAlignment(Pos.TOP_LEFT);
        leftGrid.setHgap(10);
        leftGrid.setVgap(10);
        leftGrid.setPadding(new Insets(10));

        leftGrid.add(titleLabel, 0, 0);
        leftGrid.add(cartListView, 0, 1);
        leftGrid.add(rightContainer, 1, 1);
        leftGrid.add(totalLabel, 0, 2);
        leftGrid.add(orderLabel, 0, 3);
        leftGrid.add(usernameLabel, 0, 4);
        leftGrid.add(phoneLabel, 0, 5);
        leftGrid.add(addressLabel, 0, 6);
        leftGrid.add(makePurchaseButton, 0, 7);

        borderPane.setLeft(leftGrid);
        borderPane.setTop(menuBar);

        updateUserInfo(); 

        scene = new Scene(borderPane, 800, 600);
        primaryStage.setTitle("Cart");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleMakePurchase() {
        if (isCartEmpty()) {
            showAlert("Error", "Failed to Make Transaction");
            return;
        }

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Order Confirmation");
        confirmationAlert.setHeaderText(null);
        confirmationAlert.setContentText("Are you sure you want to make the purchase?");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String transactionID = generateTransactionID();

                db.insertTransactionHeader(transactionID, username);

                for (Product product : cartListView.getItems()) {
                    String productID = product.getProductId();
                    int quantity = getQuantityForProduct(product);

                    db.insertTransactionDetail(transactionID, productID, quantity);
                    db.removeProductFromCart(username, productID);
                }
                updateUserInfo();
                cartListView.getItems().clear();

                System.out.println("Purchase completed successfully!");
                showAlert("Order Confirmation", "Purchase completed successfully!");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("An error occurred while making the purchase.");
                showAlert("Error", "An error occurred while making the purchase.");
            }
        } else {
            System.out.println("Purchase canceled.");
        }

        welcomeLabel.setText("Welcome, " + username);
        selectLabel.setText("Select a product to add and remove");

        productPriceLabel.setVisible(false);
        quantitySpinner.setVisible(false);
        totalLabel.setVisible(false);
        updateCartButton.setVisible(false);
        removeFromCartButton.setVisible(false);
        makePurchaseButton.setVisible(true);
        totalLabelSpec.setVisible(false);
    }

    private boolean isCartEmpty() {
        return cartListView.getItems().isEmpty();
    }

    private void handleUpdateCart() {
        db.connect();
        Product selectedProduct = cartListView.getSelectionModel().getSelectedItem();

        if (selectedProduct != null) {
            int newQuantity = quantitySpinner.getValue();

            if (newQuantity < 0) {
                decreaseCartItemQuantity(selectedProduct.getProductId());
            } else {
                db.updateCartItem(username, selectedProduct.getProductId(), newQuantity);
            }
            List<Product> cartItemList = db.getCartItems(username);
            cartListView.getItems().setAll(cartItemList);

            long totalCartPrice = calculateTotalPriceForCart();
            totalLabel.setText("Total: Rp." + totalCartPrice);
            System.out.println("Updated from Cart");
            showAlert("Message", "Updated from Cart");
        }
    }

    private void handleRemoveFromCart() {
        Product selectedProduct = cartListView.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            String productId = selectedProduct.getProductId();

            db.removeProductFromCart(username, productId);

            cartListView.getItems().remove(selectedProduct);
            clearProductDetails();
            updateUserInfo();

            System.out.println("Deleted from Cart");
            showAlert("Message", "Deleted from Cart");
        }
    }

    private void clearProductDetails() {
        productPriceLabel.setText("Price: Rp.");
        quantityLabel.setVisible(false);
        quantitySpinner.setVisible(false);
        totalLabel.setVisible(false);
        updateCartButton.setVisible(false);
        removeFromCartButton.setVisible(false);
        makePurchaseButton.setVisible(false);
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

    public void addProductToCart(String username, Product selectedProduct, int quantity) {
        db.connect();
        try (Database db = new Database()) {
            if (selectedProduct == null || quantity <= 0) {
                System.out.println("Invalid input. Please check the product and quantity.");
                return;
            }

            if (isProductInCart(username, selectedProduct.getProductId())) {
                updateCartItemQuantity(username, selectedProduct.getProductId(), quantity);
            } else {
                addProductToCart(username, selectedProduct, quantity);
            }

            List<Product> cartItemList = db.getCartItems(username);
            cartListView.getItems().setAll(cartItemList);
            long totalCartPrice = calculateTotalPriceForCart();
            totalLabel.setText("Total: Rp." + totalCartPrice);

            System.out.println("Product added to the cart successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("An error occurred while adding the product to the cart.");
        }
    }

    private boolean isProductInCart(String username, String productId) {
        db.connect();
        try (Database db = new Database()) {
            List<Product> cartItems = db.getCartItems(username);
            return cartItems.stream().anyMatch(item -> item.getProductId().equals(productId));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String generateTransactionID() {
        db.connect();
        ResultSet rs = db.executeQuery("SELECT transactionID FROM transaction_header WHERE transactionID LIKE 'TR%'");
        String uid = "TR001";

        try {
            if (rs.last()) {
                int temp = Integer.valueOf(rs.getString("transactionID").substring(2, 5));
                uid = String.format("TR%03d", temp + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return uid;
    }

    private void updateCartItemQuantity(String username, String productId, int newQuantity) {
        db.connect();
        try (Database db = new Database()) {
            db.updateCartItem(username, productId, newQuantity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addProductToCartListView(Product product) {
        db.connect();
        if (cartListView != null) {
            List<Product> cartItemList = db.getCartItems(username);
            cartListView.getItems().setAll(cartItemList);
        } else {
            System.out.println("Error: cartListView is not initialized.");
        }
    }

    private void setUpProductTable() {
        try {
            db.connect();
            List<Product> cartItemList = db.getCartItems(username);

            cartListView.setCellFactory(param -> new ListCell<Product>() {
                @Override
                protected void updateItem(Product item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(getFormattedCartItem(item));
                    }
                }
            });
            cartListView.getItems().clear();
            cartListView.getItems().addAll(cartItemList);

            cartListView.setOnMouseClicked(event -> {
                Product selectedProduct = cartListView.getSelectionModel().getSelectedItem();
                if (selectedProduct != null) {
                    String productDescription = selectedProduct.getProductDescription();
                    long productPrice = selectedProduct.getProductPrice();
                    welcomeLabel.setText(selectedProduct.getProductName());
                    showProductDescription(productDescription, productPrice);
                    showQuantitySpinner(productPrice);

                    updateTotalLabelSpec(quantitySpinner.getValue(), productPrice);
                }
            });
            long totalCartPrice = calculateTotalPriceForCart();
            totalLabel.setText("Total: Rp." + totalCartPrice);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showProductDescription(String productDescription, long productPrice) {
        selectLabel.setText("");
        selectLabel.setText(productDescription);
        productPriceLabel.setText("Price: " + productPrice);
        quantitySpinner.getValueFactory().setValue(1);
    }

    private void showQuantitySpinner(long productPrice) {
        productPriceLabel.setVisible(true);
        quantityLabel.setVisible(true);
        quantitySpinner.setVisible(true);
        updateCartButton.setVisible(true);
        removeFromCartButton.setVisible(true);
        currentProductPrice = productPrice;
        quantitySpinner.getValueFactory().setValue(1);

        quantitySpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                int quantity = newValue;
                long totalPrice = quantity * currentProductPrice;
                totalLabelSpec.setText("Total : Rp." + totalPrice);
            }
        });
    }

    private String getFormattedCartItem(Product product) {
        int quantity = getQuantityForProduct(product);
        long productPrice = product.getProductPrice();
        long totalHarga = quantity * productPrice;
        return quantity + "x " + product.getProductName() + " (RP." + totalHarga + ")";
    }

    public void updateUserInfo() {
        db.connect();
        try {
            String username = this.username;
            long totalCartPrice = calculateTotalCartPrice(username);
            totalLabel.setText("Total: Rp." + totalCartPrice);

            String[] userInfo = getUserInfo(username);
            if (userInfo != null) {
                usernameLabel.setText("Username: " + userInfo[0]);
                phoneLabel.setText("Phone Number: " + userInfo[1]);
                addressLabel.setText("Address: " + userInfo[2]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long calculateTotalCartPrice(String username) {
        db.connect();
        try {
            List<Product> cartItemList = db.getCartItems(username);
            return cartItemList.stream()
                    .mapToLong(Product::getProductPrice)
                    .sum();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private String[] getUserInfo(String username) {
        db.connect();
        try {
            String query = "SELECT username, phone_num, address FROM user WHERE username = ?";
            try (PreparedStatement preparedStatement = db.getConnection().prepareStatement(query)) {
                preparedStatement.setString(1, username);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String[] userInfo = new String[3];
                        userInfo[0] = resultSet.getString("username");
                        userInfo[1] = resultSet.getString("phone_num");
                        userInfo[2] = resultSet.getString("address");
                        return userInfo;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    private long calculateTotalPriceForCart() {
        long totalCartPrice = 0;
        List<Product> cartItemList = db.getCartItems(username);

        for (Product product : cartItemList) {
            int quantity = getQuantityForProduct(product);
            totalCartPrice += quantity * product.getProductPrice();
        }

        return totalCartPrice;
    }
    
    private void decreaseCartItemQuantity(String productId) {
        int currentQuantity = db.getQuantityForProduct(username, productId);
        int spinnerValue = quantitySpinner.getValue();
        int newQuantity = Math.max(0, currentQuantity + spinnerValue);

        db.updateCartItem(username, productId, newQuantity);
        quantitySpinner.getValueFactory().setValue(1); 
    }
    
    private int getQuantityForProduct(Product product) {
    	db.connect();
        try {
            String productID = product.getProductId();
            return db.getQuantityForProduct(username, productID);
        } catch (Exception e) {
            e.printStackTrace();
            return 0; 
        }
    }
    public void updateTotalLabel() {
        long totalCartPrice = calculateTotalPriceForCart();
        totalLabel.setText("Total: Rp." + totalCartPrice);
    }
    
    private void updateTotalLabelSpec(int quantity, long productPrice) {
        long totalPrice = quantity * productPrice;
        totalLabelSpec.setText("Total : Rp." + totalPrice);
        totalLabelSpec.setVisible(true);
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

