import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Database {
  private Connection dbConnection;
  private PreparedStatement selectAllStores;
  private PreparedStatement selectStoreById;
  private PreparedStatement selectStorebyLocation;
  private PreparedStatement addStore;
  private PreparedStatement deleteStorebyId;
  private PreparedStatement updateStorebyId;

  private PreparedStatement selectAllSuppliers;
  private PreparedStatement selectSupplierById;
  private PreparedStatement selectSupplierByLocationName;
  private PreparedStatement addSupplier;
  private PreparedStatement deleteSupplierById;
  private PreparedStatement updateSupplierbyId;

  private PreparedStatement selectAllProducts;
  private PreparedStatement selectAllProductsLog;
  private PreparedStatement selectProductLogByName;
  private PreparedStatement selectOneSimpleProduct;

  private PreparedStatement selectAllProductsByNames;
  private PreparedStatement selectAllProductsByProductId;
  private PreparedStatement selectAllProductsBySupplierId;

  private PreparedStatement selectOneProduct;

  private PreparedStatement addProductLog;
  private PreparedStatement addProductFromSupplier;
  private PreparedStatement updateProduct;
  private PreparedStatement deleteProduct;

  private PreparedStatement addBuilding;
  private PreparedStatement updateBuilding;
  private PreparedStatement deleteBuilding;

  private Database() {
  }

  /**
   * Get a fully-configured connection to the database
   *
   * @param dbUrl       The url to the database
   * @param portDefault port to use if absent in db_url
   * @param dbTable     ArrayList of all SQL tables to use
   *
   * @return A Database object, or null if we cannot connect properly
   */
  static Database getDatabase(String dbUrl, String portDefault,
      ArrayList<String> dbTable) {
    try {
      URI dbUri = new URI(dbUrl);
      String username = dbUri.getUserInfo().split(":")[0];
      String password = dbUri.getUserInfo().split(":")[1];
      String host = dbUri.getHost();
      String path = dbUri.getPath();
      String port = dbUri.getPort() == -1 ? portDefault
          : Integer.toString(dbUri.getPort());

      return getDatabase(host, port, path, username, password, dbTable);
    } catch (URISyntaxException s) {
      Log.error("URI syntax Error");
      return null;
    }
  }

  /**
   * Get a fully configured connected to the database
   *
   * @param ip    The IP address of server
   * @param port  The port on the server
   * @param path  The path
   * @param user  The user ID to use
   * @param pass  The password to use
   * @param table The ArrayList of all SQL tables
   * @return Connected Database
   */
  static Database getDatabase(String ip, String port, String path, String user,
      String pass, ArrayList<String> table) {
    if (path == null || "".equals(path)) {
      path = "/";
    }
    // Create an unconfigured Database obj
    Database db = new Database();

    // Give the Database obj a connection, or else fail
    try {
      Connection conn = DriverManager.getConnection(
          "jdbc:oracle:thin:@" + ip + ":" + port + ":" + path, user, pass);
      if (conn == null) {
        Log.error("Error: DriverManager.getConnection() returns null");
        return null;
      }
      db.dbConnection = conn;
    } catch (SQLException e) {
      Log.error("Error: DriverManager.getConnection() threw a SQLException");

      return null;
    }
    return createPreparedStatements(db, table);
  }

  /**
   * creates prepared SQL statments
   *
   * @param db      The connected database
   * @param dbTable The list of db tables we want to connect
   * @return The database with SQL statements
   */
  private static Database createPreparedStatements(Database db,
      ArrayList<String> dbTable) {
    if (dbTable == null || dbTable.size() == 0) {
      Log.error("Error: No tables to connect to");
      db.disconnect();
      return null;
    }
    String store = dbTable.get(0);
    String supplier = dbTable.get(1);
    String product = dbTable.get(2);
    String shipment = dbTable.get(3);
    String manufacturing = dbTable.get(4);
    String building = dbTable.get(5);
    try {
      // CRUD operations for building
      db.addBuilding = db.dbConnection
          .prepareStatement(String.format("INSERT INTO %1$s (location) VALUES (?)", building),
              new String[] { "id" });
      db.deleteBuilding = db.dbConnection
          .prepareStatement(String.format("DELETE FROM %1$s WHERE id = ?", building));
      db.updateBuilding = db.dbConnection
          .prepareStatement(String.format("UPDATE %1$s SET location = ? WHERE id = ?", building));
      // CRUD operations for store
      db.selectAllStores = db.dbConnection
          .prepareStatement(String.format("SELECT * FROM %1$s ORDER BY %1$s_id", store));
      db.selectStoreById = db.dbConnection
          .prepareStatement(String.format("SELECT * FROM %1$s WHERE %1$s_id = ?", store));
      db.selectStorebyLocation = db.dbConnection
          .prepareStatement(String.format("SELECT * FROM %1$s WHERE location LIKE ? ORDER BY location", store));
      db.addStore = db.dbConnection.prepareStatement(String.format("INSERT INTO %1$sb (%1$s_id) VALUES (?)", store));
      db.deleteStorebyId = db.deleteBuilding; // We can just remove the id in building to cascade and remove it in store
      db.updateStorebyId = db.updateBuilding; // We can just update it in building

      // CRUD operations for supplier
      db.selectAllSuppliers = db.dbConnection
          .prepareStatement(String.format("SELECT * FROM %1$s ORDER BY %1$s_id", supplier));
      db.selectSupplierById = db.dbConnection
          .prepareStatement(String.format("SELECT * FROM %1$s WHERE %1$s_id = ?", supplier));
      db.selectSupplierByLocationName = db.dbConnection
          .prepareStatement(String
              .format("SELECT * FROM %1$s WHERE location LIKE ? AND %1$s_name LIKE ? ORDER BY %1$s_name, location",
                  supplier));
      db.addSupplier = db.dbConnection
          .prepareStatement(
              String.format("INSERT INTO %1$sb (%1$s_id, %1$s_name) VALUES (?, ?)", supplier));
      // We can just remove the id in building to cascade and remove it in supplier
      db.deleteSupplierById = db.deleteBuilding;
      db.updateSupplierbyId = db.dbConnection
          .prepareStatement(String.format("UPDATE %1$s SET %1$s_name = ? WHERE %1$s_id = ?", supplier));
      // CRUD operations for product
      db.selectAllProducts = db.dbConnection
          .prepareStatement(String.format("SELECT * FROM %1$s", product));
      db.selectAllProductsByNames = db.dbConnection
          .prepareStatement(
              String.format(
                  "SELECT * FROM %1$s WHERE %1$s_name LIKE ? AND %2$s_name LIKE ? ORDER BY %1$s_name, %2$s_name",
                  product, supplier));
      db.selectAllProductsByProductId = db.dbConnection
          .prepareStatement(
              String.format("SELECT * FROM %1$s WHERE %1$s_id = ? AND %2$s_name LIKE ? ORDER BY %1$s_id, %2$s_id",
                  product, supplier));
      db.selectAllProductsBySupplierId = db.dbConnection
          .prepareStatement(
              String.format("SELECT * FROM %1$s WHERE %2$s_id = ? ORDER BY %1$s_id, %2$s_id",
                  product, supplier));

      db.selectAllProductsLog = db.dbConnection
          .prepareStatement(String.format("SELECT * FROM %1$slog", product));
      db.selectProductLogByName = db.dbConnection
          .prepareStatement(String.format("SELECT * FROM %1$slog WHERE %1$s_name LIKE ?", product));
      db.selectOneSimpleProduct = db.dbConnection
          .prepareStatement(String.format("SELECT * FROM %1$slog WHERE %1$s_id = ?", product));

      db.selectOneProduct = db.dbConnection
          .prepareStatement(
              String.format("SELECT * FROM %1$s WHERE %1$s_id = ? AND %2$s_id = ?", product, supplier));
      db.addProductLog = db.dbConnection
          .prepareStatement(String.format("INSERT INTO %1$slog (%1$s_name) VALUES (?)",
              product), new String[] { "product_id" });
      db.addProductFromSupplier = db.dbConnection
          .prepareStatement(String.format("INSERT INTO %1$sb (%1$s_id, %2$s_id, price, unit_type) VALUES (?,?,?,?)",
              product, supplier));
      db.updateProduct = db.dbConnection
          .prepareStatement(String.format("UPDATE %1$s SET price = ?, unit_type = ? WHERE %1$s_id = ? AND %2$s_id = ?",
              product, supplier));
      db.deleteProduct = db.dbConnection
          .prepareStatement(
              String.format("DELETE FROM %1$s WHERE %1$s_id = ? AND %2$s_id = ?", product, supplier));
      // CRUD operations for shipment

      // CRUD operations for manufacturing

    } catch (SQLException e) {
      Log.error("Error creating prepared statement");

      db.disconnect();
      return null;
    }
    return db;
  }

  /**
   * Close the current connection to data database, if it exist
   *
   * @return true if connection closes as expected
   */
  boolean disconnect() {
    if (dbConnection == null) {
      Log.error("Unable to close connection: Connection was null");
      return false;
    }
    try {
      dbConnection.close();
    } catch (SQLException e) {
      Log.error("Error: Connection.close() threw a SQLException");

      dbConnection = null;
      return false;
    }
    dbConnection = null;
    return true;
  }

  /**
   * Get all stores
   * 
   * @return An ArrayList of all stores
   */
  ArrayList<StoreData> getAllStores() {
    ArrayList<StoreData> result = new ArrayList<>();
    try {
      ResultSet rs = selectAllStores.executeQuery();
      while (rs.next()) {
        result.add(getStore(rs));
      }
    } catch (SQLException e) {
      Log.error("SQL Exception: Cannot get all stores");

    }
    return result;
  }

  /**
   * Get one store by id
   * 
   * @return StoreData object
   */
  StoreData getStoreById(int id) {
    StoreData result = null;
    if (id < 0) {
      return null;
    }
    try {
      selectStoreById.setInt(1, id);
      ResultSet rs = selectStoreById.executeQuery();
      while (rs.next()) {
        result = getStore(rs);
      }
    } catch (SQLException e) {
      Log.error("SQL Exception: store not found: " + id);

    }
    return result;
  }

  /**
   * Get stores by location
   * 
   * @return An ArrayList of all stores
   */
  ArrayList<StoreData> getStoreByLocation(String location) {
    ArrayList<StoreData> result = new ArrayList<>();
    try {
      selectStorebyLocation.setString(1, adjustWildcards(location));
      ResultSet rs = selectStorebyLocation.executeQuery();
      while (rs.next()) {
        result.add(getStore(rs));
      }
    } catch (SQLException e) {
      Log.error("SQL Exception: Cannot get all stores: " + location);

    }
    return result;
  }

  private StoreData getStore(ResultSet rs) {
    try {
      Integer store_id = rs.getInt("store_id");
      String loc = rs.getString("location");
      return new StoreData(store_id, loc);
    } catch (SQLException e) {
      Log.error("Cannot get store");
    }
    return null;
  }

  /**
   * Adds a new store by location
   * 
   * @return store id
   */
  int addStore(String location) {
    int store_id = -1;
    try {
      store_id = addBuilding(location);
      addStore.setInt(1, store_id);
      addStore.executeUpdate();
    } catch (SQLException e) {
      Log.error("Invalid SQL Exception: Cannot add store");

    }
    Log.info("Got store id: " + store_id);
    return store_id;
  }

  /**
   * Deletes a store by store-id
   * 
   * @return 1 if successful, -1 if not
   */
  int deleteStoreById(int store_id) {
    int count = -1;
    try {
      deleteStorebyId.setInt(1, store_id);
      count = deleteStorebyId.executeUpdate();
    } catch (SQLException e) {
      Log.error("Invalid SQL Exception: Cannot remove store: " + store_id);

    }
    return count;
  }

  /**
   * Update store's location based on store_id
   * 
   * @param store_id store id
   * @param location new location
   * @return number of rows updated (1 on success)
   */
  int updateStoreLocation(int store_id, String location) {
    int count = -1;
    try {
      updateStorebyId.setString(1, location);
      updateStorebyId.setInt(2, store_id);
      count = updateStorebyId.executeUpdate();
    } catch (SQLException e) {
      Log.error("Invalid SQL Exception: Cannot update store " + store_id + ": " + location);

    }
    return count;
  }

  /**
   * Adds a new building by location
   * 
   * @return the building id.
   */
  int addBuilding(String location) {
    int id = -1;
    try {
      addBuilding.setString(1, location);
      int affectedRows = addBuilding.executeUpdate();
      if (affectedRows > 0) {
        ResultSet rs = addBuilding.getGeneratedKeys();
        if (rs.next()) {
          id = rs.getInt(1);
        }
      }
    } catch (SQLException e) {
      Log.error("Invalid SQL Exception: Cannot add building");
    }
    return id;
  }

  /**
   * Deletes a building by id
   * 
   * @return 1 if successful, -1 if not
   */
  int deleteBuilding(int id) {
    int count = -1;
    try {
      deleteBuilding.setInt(1, id);
      count = deleteBuilding.executeUpdate();
    } catch (SQLException e) {
      Log.error("Invalid SQL Exception: Cannot remove building: " + id);
    }
    return count;
  }

  /**
   * Update building's location based on id
   * 
   * @param id       store id
   * @param location new location
   * @return number of rows updated (1 on success)
   */
  int updateBuilding(int id, String location) {
    int count = -1;
    try {
      updateBuilding.setString(1, location);
      updateBuilding.setInt(2, id);
      count = updateBuilding.executeUpdate();
    } catch (SQLException e) {
      Log.error("Invalid SQL Exception: Cannot update building " + id + ": " + location);

    }
    return count;
  }

  /**
   * Get all suppliers
   * 
   * @return An ArrayList of all suppliers
   */
  ArrayList<SupplierData> getAllSuppliers() {
    ArrayList<SupplierData> result = new ArrayList<>();
    try {
      ResultSet rs = selectAllSuppliers.executeQuery();
      while (rs.next()) {

        result.add(getSupplier(rs));
      }
    } catch (SQLException e) {
      Log.error("SQL Exception: Cannot get all suppliers");

    }
    return result;
  }

  /**
   * Get one supplier by id
   * 
   * @return SupplierData object
   */
  SupplierData getSupplierById(int supplier_id) {
    SupplierData result = null;
    if (supplier_id < 0) {
      return null;
    }
    try {
      selectSupplierById.setInt(1, supplier_id);
      ResultSet rs = selectSupplierById.executeQuery();
      if (rs.next()) {
        result = getSupplier(rs);
      }
    } catch (SQLException e) {
      Log.error("SQL Exception: supplier not found: " + supplier_id);

    }
    return result;
  }

  /**
   * Get suppliers by location
   * 
   * @return An ArrayList of all suppliers
   */
  ArrayList<SupplierData> getSupplierByLocationAndName(String location, String supplier_name) {
    ArrayList<SupplierData> result = new ArrayList<>();
    try {
      Log.info(adjustWildcards(supplier_name));
      selectSupplierByLocationName.setString(1, adjustWildcards(location));
      selectSupplierByLocationName.setString(2, adjustWildcards(supplier_name));
      ResultSet rs = selectSupplierByLocationName.executeQuery();
      while (rs.next()) {
        result.add(getSupplier(rs));
      }
    } catch (SQLException e) {
      Log.error("SQL Exception: Cannot get all suppliers: " + location);

    }
    return result;
  }

  private String adjustWildcards(String string) {
    if (string == null || string.isEmpty()) {
      return "%";
    }
    return "%" + string + "%";
  }

  private SupplierData getSupplier(ResultSet rs) {
    Integer supplier_id;
    try {
      supplier_id = rs.getInt("supplier_id");
      String supplier_name = rs.getString("supplier_name");
      String loc = rs.getString("location");
      return new SupplierData(supplier_id, supplier_name, loc);
    } catch (SQLException e) {
      Log.error("Cannot get supplier");
    }
    return null;
  }

  /**
   * Adds a new supplier by name and location
   * 
   * @param supplier_name name
   * @param location      location
   * @return supplier_id
   */
  int addSupplier(String supplier_name, String location) {
    int supplier_id = -1;
    try {
      supplier_id = addBuilding(location);
      addSupplier.setInt(1, supplier_id);
      addSupplier.setString(2, supplier_name);
      addSupplier.executeUpdate();
    } catch (SQLException e) {
      Log.error("Invalid SQL Exception: Cannot add supplier");
    }
    Log.info("Adding supplier: " + supplier_id);
    return supplier_id;
  }

  /**
   * Removes a supplier by id
   * 
   * @param supplier_id supplier id
   * @return 1 if successful, -1 if not
   */
  int removeSupplierById(int supplier_id) {
    int count = -1;
    try {
      deleteSupplierById.setInt(1, supplier_id);
      count = deleteSupplierById.executeUpdate();
    } catch (SQLException e) {
      Log.error("Invalid SQL Exception: Cannot remove supplier");

    }
    return count;
  }

  /**
   * Update supplier based on supplier_id
   * 
   * @param supplier_id   store id
   * @param supplier_name name
   * @param location      new location
   * @return number of rows updated (1 on success)
   */
  int updateSupplier(int supplier_id, String supplier_name, String location) {
    int count = -1;
    try {
      updateBuilding(supplier_id, location);
      updateSupplierbyId.setInt(2, supplier_id);
      updateSupplierbyId.setString(1, supplier_name);
      count = updateSupplierbyId.executeUpdate();
    } catch (SQLException e) {
      Log.error("Invalid SQL Exception: Cannot update supplier " + supplier_id + ": " + location);

    }
    return count;
  }

  ArrayList<ProductData> getAllProducts() {
    ArrayList<ProductData> products = new ArrayList<>();
    try {
      ResultSet rs = selectAllProducts.executeQuery();
      while (rs.next()) {
        products.add(getProduct(rs));
      }
    } catch (SQLException e) {
      Log.error("Cannot get all products");
    }
    return products;
  }

  ArrayList<ProductData> getAllProductsSimple() {
    ArrayList<ProductData> products = new ArrayList<>();
    try {
      ResultSet rs = selectAllProductsLog.executeQuery();
      while (rs.next()) {
        products.add(getProductSimple(rs));
      }
    } catch (SQLException e) {
      Log.error("Cannot get all products");
    }
    return products;
  }

  ArrayList<ProductData> getProductLog(int product_id, String product_name) {
    ArrayList<ProductData> products = new ArrayList<>();
    try {
      ResultSet rs;
      // Only product_id is given
      if (product_id > 0 && product_name == null) {
        selectOneSimpleProduct.setInt(1, product_id);
        rs = selectOneSimpleProduct.executeQuery();
      } else if (product_id < 0 && product_name != null) { // Only product_name is given
        selectProductLogByName.setString(1, adjustWildcards(product_name));
        rs = selectProductLogByName.executeQuery();
      } else { // Get the entire log
        rs = selectAllProductsLog.executeQuery();
      }
      while (rs.next()) {
        products.add(getProductSimple(rs));
      }
    } catch (SQLException e) {
      Log.error("Cannot get all products");
    }
    return products;
  }

  private ProductData getProduct(ResultSet rs) {
    try {
      int supplier_id = rs.getInt("supplier_id");
      String supplier_name = rs.getString("supplier_name");
      SupplierData supplier = new SupplierData(supplier_id, supplier_name, "");
      String product_name = rs.getString("product_name");
      int product_id = rs.getInt("product_id");
      float price = rs.getFloat("price");
      String unit = rs.getString("unit_type");
      Log.info(new ProductData(supplier, product_id, product_name, price, unit).toString());
      return new ProductData(supplier, product_id, product_name, price, unit);
    } catch (SQLException e) {
      Log.error("Cannot get product");
    }
    return null;
  }

  private ProductData getProductSimple(ResultSet rs) {
    try {
      String product_name = rs.getString("product_name");
      int product_id = rs.getInt("product_id");
      return new ProductData(null, product_id, product_name, 0, null);
    } catch (SQLException e) {
      Log.error("Cannot get product");
    }
    return null;
  }

  ArrayList<ProductData> getProductByName(String product_name, String supplier_name) {
    ArrayList<ProductData> products = new ArrayList<>();
    try {
      selectAllProductsByNames.setString(1, adjustWildcards(product_name));
      selectAllProductsByNames.setString(2, adjustWildcards(supplier_name));
      ResultSet rs = selectAllProductsByNames.executeQuery();
      while (rs.next()) {
        products.add(getProduct(rs));
      }
    } catch (SQLException e) {
      Log.error("Cannot get all products with name: " + product_name);
    }
    return products;
  }

  ArrayList<ProductData> getProductByProductId(int product_id, String supplier_name) {
    ArrayList<ProductData> products = new ArrayList<>();
    try {
      selectAllProductsByProductId.setInt(1, product_id);
      selectAllProductsByProductId.setString(2, adjustWildcards(supplier_name));
      ResultSet rs = selectAllProductsByProductId.executeQuery();
      while (rs.next()) {
        products.add(getProduct(rs));
      }
    } catch (SQLException e) {
      Log.error("Cannot get all products with id: " + product_id);
    }
    return products;
  }

  ArrayList<ProductData> getProductBySupplierId(int supplier_id) {
    ArrayList<ProductData> products = new ArrayList<>();
    try {
      selectAllProductsBySupplierId.setInt(1, supplier_id);
      ResultSet rs = selectAllProductsBySupplierId.executeQuery();
      while (rs.next()) {
        products.add(getProduct(rs));
      }
    } catch (SQLException e) {
      Log.error("Cannot get all products with id: " + supplier_id);
    }
    return products;
  }

  ProductData getOneProduct(int product_id, int supplier_id) {
    try {
      selectOneProduct.setInt(1, product_id);
      selectOneProduct.setInt(2, supplier_id);
      ResultSet rs = selectOneProduct.executeQuery();
      if (rs.next()) {
        return getProduct(rs);
      }
    } catch (SQLException e) {
      Log.error("Cannot get all products with id =" + product_id + "supplier = " + supplier_id);
    }
    return null;
  }

  int addProductFromSupplier(int product_id, int supplier_id, float price, String unit_type) {
    int count = 0;
    price = Math.round(price * 100) / 100;
    try {
      addProductFromSupplier.setInt(1, product_id);
      addProductFromSupplier.setInt(2, supplier_id);
      addProductFromSupplier.setFloat(3, price);
      addProductFromSupplier.setString(4, unit_type);
      count = addProductFromSupplier.executeUpdate();
    } catch (SQLException e) {
      Log.error("cannot add product.");
    }
    return count;
  }

  int addProductLog(String product_name) {
    int product_id = -1;
    try {
      addProductLog.setString(1, product_name);
      int affectedRows = addProductLog.executeUpdate();
      if (affectedRows > 0) {
        ResultSet rs = addProductLog.getGeneratedKeys();
        if (rs.next()) {
          product_id = rs.getInt(1);
        }
      }

    } catch (SQLException e) {
      Log.error("cannot add product to log");
    }
    return product_id;
  }

  int deleteProduct(int product_id, int supplier_id) {
    int count = -1;
    try {
      deleteProduct.setInt(1, product_id);
      deleteProduct.setInt(1, supplier_id);
      count = deleteProduct.executeUpdate();
    } catch (SQLException e) {
      Log.error("Invalid SQL Exception: Cannot remove product: " + product_id + "from supplier: " + supplier_id);

    }
    return count;
  }

  int updateProduct(int product_id, int supplier_id, float price, String unit_type) {
    int count = -1;
    try {
      updateProduct.setFloat(1, price);
      updateProduct.setString(2, unit_type);
      updateProduct.setInt(3, product_id);
      updateProduct.setInt(4, supplier_id);
      count = updateProduct.executeUpdate();
    } catch (SQLException e) {
      Log.error("Cannot update product");
    }
    return count;
  }
}
