package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Database implements AutoCloseable {
	private Connection connection;
	private Statement st;
	private static final String JDBC_URL = "jdbc:mysql://localhost:3306/seruputteh";
	private static final String DB_USER = "root";
	private static final String DB_PASSWORD = "";

	public Database() {
		connect();
	}

	public void connect() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
			st = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			System.out.println("Connected to the database.");
		} catch (ClassNotFoundException e) {
			System.err.println("Error: MySQL JDBC driver not found.");
			e.printStackTrace();
		} catch (SQLException e) {
			System.err.println("Error: Failed to connect to the database.");
			e.printStackTrace();
		}
	}

	public Connection getConnection() {
		return connection;
	}

	public void executeUpdate(String userID, String userName, String passWord, String role, String address,
	        String phone_num, String gender, String action) {
		connect();
	    String query = "";

	    if ("create".equals(action)) {
	        query = String.format("INSERT INTO user VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s')", userID,
	                userName, passWord, role, address, phone_num, gender);
	    }

	    try {
	        st.executeUpdate(query);
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	public void closeConnection() {
	    try {
	        if (connection != null) {
	            connection.close();
	            System.out.println("Database connection closed.");
	        }
	    } catch (SQLException e) {
	        System.err.println("Error: Failed to close the database connection.");
	        e.printStackTrace();
	    }
	}

	public ResultSet executeQuery(String query) {
		connect();
	    try {
	        return st.executeQuery(query);
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;
	}

	public List<Product> getAllProducts() {
		connect();
		List<Product> productList = new ArrayList<>();
		try (Connection connection = getConnection()) {
			String query = "SELECT * FROM product";
			
			try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					while (resultSet.next()) {
						String productId = resultSet.getString("productID");
						String productName = resultSet.getString("product_name");
						long productPrice = resultSet.getLong("product_price");
						String productDescription = resultSet.getString("product_des");

						Product product = new Product(productId, productName, productPrice, productDescription);
						productList.add(product);
					}
				}
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		return productList;
	}

	public void addProduct(Product product) {
		connect();
		String query = "INSERT INTO product (productID, product_name, product_price, product_des) VALUES (?, ?, ?, ?)";
		try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
			preparedStatement.setString(1, product.getProductId());
			preparedStatement.setString(2, product.getProductName());
			preparedStatement.setLong(3, product.getProductPrice());
			preparedStatement.setString(4, product.getProductDescription());

			preparedStatement.executeUpdate();
			System.out.println("Product added successfully.");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateProductPrice(String productId, long newPrice) {
		connect();
		try (Connection connection = getConnection()) {
			String query = "UPDATE product SET product_price = ? WHERE productID = ?";
			try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
				preparedStatement.setLong(1, newPrice);
				preparedStatement.setString(2, productId);

				preparedStatement.executeUpdate();
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public void removeProduct(String productId) {
		connect();
		try (Connection connection = getConnection()) {
			String query = "DELETE FROM product WHERE productID = ?";
			try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
				preparedStatement.setString(1, productId);

				preparedStatement.executeUpdate();
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
	
	public void addProductToCart(String username, Product selectedProduct, int quantity) {
	    connect();
	    try (Connection connection = getConnection()) {
	        if (userExists(username)) {
	            String productID = selectedProduct.getProductId();
	            if (productExistsInCart(username, productID)) {
	                updateCartItem(username, productID, quantity);
	                System.out.println("Product quantity updated in the cart successfully.");
	            } else {
	                String insertQuery = "INSERT INTO cart (productID, userID, quantity) VALUES (?, ?, ?)";
	                try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
	                    insertStatement.setString(1, productID);
	                    insertStatement.setString(2, getUserIDByUsername(username));
	                    insertStatement.setInt(3, quantity);
	                    insertStatement.executeUpdate();
	                    System.out.println("Product added to cart successfully.");
	                }
	            }
	        } else {
	            System.out.println("User not found.");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	
	public void close() {
	    try {
	        if (st != null) {
	            st.close();
	        }
	        if (connection != null) {
	            connection.close();
	            System.out.println("Database connection closed.");
	        }
	    } catch (SQLException e) {
	        System.err.println("Error: Failed to close the database connection.");
	        e.printStackTrace();
	    }
	}

	public List<Product> getCartItems(String username) {
	    connect();
	    List<Product> cartItems = new ArrayList<>();

	    try (Connection connection = getConnection()) {
	        String query = "SELECT p.* FROM product p INNER JOIN cart c ON p.productID = c.productID WHERE c.userID = ?";

	        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
	            preparedStatement.setString(1, getUserIDByUsername(username));

	            try (ResultSet resultSet = preparedStatement.executeQuery()) {
	                while (resultSet.next()) {
	                    String productId = resultSet.getString("productID");
	                    String productName = resultSet.getString("product_name");
	                    long productPrice = resultSet.getLong("product_price");
	                    String productDescription = resultSet.getString("product_des");

	                    Product product = new Product(productId, productName, productPrice, productDescription);
	                    cartItems.add(product);
	                }
	            }
	        }
	    } catch (SQLException ex) {
	        ex.printStackTrace();
	    }

	    return cartItems;
	}

	
	public void addToCart(String username, String product, int quantity) {
		connect();
        try  {
            if (userExists(username)) {
                String productID = getProductIDByName(product);
                if (productExistsInCart(username, productID)) {
                    updateCartItem(username, productID, quantity);
                    System.out.println("Product quantity updated in the cart successfully.");
                } else {
                	connect();
                    String insertQuery = "INSERT INTO cart (productID, userID, quantity) VALUES (?, ?, ?)";
                    try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                        insertStatement.setString(1, productID);
                        insertStatement.setString(2, getUserIDByUsername(username));
                        insertStatement.setInt(3, quantity);
                        insertStatement.executeUpdate();
                        System.out.println("Product added to cart successfully.");
                    }
                }
            } else {
                System.out.println("User not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
	
	public int getQuantityForProduct(String username, String productId) {
	    connect();
	    try (Connection connection = getConnection()) {
	        String query = "SELECT quantity FROM cart WHERE userID = ? AND productID = ?";
	        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
	            preparedStatement.setString(1, getUserIDByUsername(username));
	            preparedStatement.setString(2, productId);
	            try (ResultSet resultSet = preparedStatement.executeQuery()) {
	                if (resultSet.next()) {
	                    return resultSet.getInt("quantity");
	                }
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return 0;
	}
	
	public void removeProductFromCart(String username, String productId) {
	    connect();
	    try (Connection connection = getConnection()) {
	        String query = "DELETE FROM cart WHERE userID = ? AND productID = ?";
	        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
	            preparedStatement.setString(1, getUserIDByUsername(username));
	            preparedStatement.setString(2, productId);

	            preparedStatement.executeUpdate();
	        }
	    } catch (SQLException ex) {
	        ex.printStackTrace();
	    }
	}
	
	public String getProductIDByName(String productName) {
		connect();
        String productID = null;

        try (Connection connection = getConnection()) {
            String query = "SELECT productID FROM product WHERE product_name = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, productName);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        productID = resultSet.getString("productID");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return productID;
    }
	
	 public boolean productExistsInCart(String username, String productID) {
		 connect();
	        try (Connection connection = getConnection()) {
	            String query = "SELECT * FROM cart WHERE userID = ? AND productID = ?";
	            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
	                preparedStatement.setString(1, getUserIDByUsername(username));
	                preparedStatement.setString(2, productID);
	                try (ResultSet resultSet = preparedStatement.executeQuery()) {
	                    return resultSet.next();
	                }
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	            return false;
	        }
	    }

    public boolean userExists(String username) {
    	connect();
    	try (Connection connection = getConnection()) {
            String query = "SELECT * FROM user WHERE username = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public void updateCartItem(String username, String productId, int additionalQuantity) {
        connect();
        try (Connection connection = getConnection()) {
            String updateQuery = "UPDATE cart SET quantity = quantity + ? WHERE userID = ? AND productID = ?";
            try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                updateStatement.setInt(1, additionalQuantity);
                updateStatement.setString(2, getUserIDByUsername(username));
                updateStatement.setString(3, productId);
                updateStatement.executeUpdate();
                System.out.println("Cart item quantity updated successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public String getUserIDByUsername(String username) {
    	connect();
    	try (Connection connection = getConnection()) {
            String query = "SELECT userID FROM user WHERE username = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString("userID");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void insertTransactionHeader(String transactionID, String userID) {
        connect();
        try (Connection connection = getConnection()) {
            String query = "INSERT INTO transaction_header (transactionID, userID) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, transactionID);
                preparedStatement.setString(2, getUserIDByUsername(userID));

                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void insertTransactionDetail(String transactionID, String productID, int quantity) {
        connect();
        try (Connection connection = getConnection()) {
            String query = "INSERT INTO transaction_detail (transactionID, productID, quantity) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, transactionID);
                preparedStatement.setString(2, productID);
                preparedStatement.setInt(3, quantity);

                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    

    public void close1() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("Database connection closed.");
        }
    }
}
    
	
