package model;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Product {
    private final SimpleStringProperty productIDProperty;
    private final SimpleStringProperty productNameProperty;
    private final SimpleLongProperty productPriceProperty;
    private final SimpleStringProperty productDescriptionProperty;
    

    public Product(String productID, String productName, long productPrice, String productDescription) {
        this.productIDProperty = new SimpleStringProperty(productID);
        this.productNameProperty = new SimpleStringProperty(productName);
        this.productPriceProperty = new SimpleLongProperty(productPrice);
        this.productDescriptionProperty = new SimpleStringProperty(productDescription);
    }

    // Getter methods for properties
    public StringProperty productIDProperty() {
        return productIDProperty;
    }

    public StringProperty productNameProperty() {
        return productNameProperty;
    }

    public LongProperty productPriceProperty() {
        return productPriceProperty;
    }

    public StringProperty productDescriptionProperty() {
        return productDescriptionProperty;
    }

    // Getter methods for non-property attributes
    public String getProductId() {
        return productIDProperty.get();
    }

    public String getProductName() {
        return productNameProperty.get();
    }

    public long getProductPrice() {
        return productPriceProperty.get();
    }

    public String getProductDescription() {
        return productDescriptionProperty.get();
    }

    // Setter methods (if needed)
    public void setProductId(String productID) {
        this.productIDProperty.set(productID);
    }

    public void setProductName(String productName) {
        this.productNameProperty.set(productName);
    }

    public void setProductPrice(long productPrice) {
        this.productPriceProperty.set(productPrice);
    }

    public void setProductDescription(String productDescription) {
        this.productDescriptionProperty.set(productDescription);
    }
}
