import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.spi.DirStateFactory.Result;

public class Database {
  private Connection dbConnection;

  private PreparedStatement addBuilding;
  private PreparedStatement updateBuilding;
  private PreparedStatement deleteBuilding;

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

  private PreparedStatement selectAllShipmentByDest;
  private PreparedStatement selectAllShipmentFromSupplierId;
  private PreparedStatement selectAllShipmentFromSupplierName;
  private PreparedStatement selectShipmentIdsToStore;
  private PreparedStatement selectShipmentIdsToSupplier;
  private PreparedStatement addShipmentLogId;
  private PreparedStatement updateShipmentLogDest;
  private PreparedStatement deleteShipmentLogId;

  private PreparedStatement selectProductsFromShipmentId;
  private PreparedStatement addProducttoShipmentId;
  private PreparedStatement deleteProductFromShipmentId;

  private PreparedStatement selectAllManufacturingFromSupplierName;
  private PreparedStatement selectAllManufacturingFromSupplierId;
  private PreparedStatement selectAllManufacturingFromProductId;
  private PreparedStatement selectAllManufacturingFromProductName;
  private PreparedStatement selectAllManufacturingFromComponent;
  private PreparedStatement selectOneManufacturing;

  private PreparedStatement addManufacturing;
  private PreparedStatement updateManufacturing;
  private PreparedStatement deleteManufacturingByComponent;
  private PreparedStatement deleteManufacturingById;

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
    if (isNull(path) || "".equals(path)) {
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
    String product_ship = dbTable.get(6);
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
          .prepareStatement(String.format("SELECT * FROM %1$s ORDER BY %1$s_name", product));
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
          .prepareStatement(String.format("SELECT * FROM %1$slog ORDER BY %1$s_name", product));
      db.selectProductLogByName = db.dbConnection
          .prepareStatement(String.format("SELECT * FROM %1$slog WHERE %1$s_name LIKE ? ORDER BY %1$s_name", product));
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
      db.selectAllShipmentByDest = db.dbConnection
          .prepareStatement(
              String.format("SELECT * FROM %1$slog WHERE to_id = ? AND %1$s_id IN (" +
                  "SELECT %1$s_id FROM %1$s WHERE %2$s_name LIKE ?) ORDER BY %1$s_id",
                  shipment, supplier));
      db.selectAllShipmentFromSupplierName = db.dbConnection
          .prepareStatement(
              String.format("SELECT * FROM %1$slog WHERE %1$s_id IN (" +
                  "SELECT %1$s_id FROM %1$s WHERE %2$s_name LIKE ?) ORDER BY %1$s_id",
                  shipment, supplier));
      db.selectAllShipmentFromSupplierId = db.dbConnection
          .prepareStatement(
              String.format("SELECT * FROM %1$slog WHERE %1$s_id IN (" +
                  "SELECT %1$s_id FROM %1$s WHERE %2$s_id = ?) ORDER BY %1$s_id",
                  shipment, supplier));
      // select shipment ids only from shipment to suppliers
      db.selectShipmentIdsToSupplier = db.dbConnection
          .prepareStatement(String.format(
              "SELECT * FROM %1$slog WHERE to_id IN (" +
                  "SELECT %2$s_id FROM %2$s WHERE %2$s_name LIKE ?) ORDER BY %1$s_id",
              shipment, supplier));
      // select shipment ids only from shipment to stores
      db.selectShipmentIdsToStore = db.dbConnection
          .prepareStatement(String.format(
              "SELECT * FROM %1$slog WHERE to_id IN (SELECT %2$s_id FROM %2$s) ORDER BY %1$s_id",
              shipment, store));
      db.addShipmentLogId = db.dbConnection
          .prepareStatement(
              String.format(
                  "INSERT INTO %1$slog (to_id, ship_date, arrive_date) VALUES (?,TO_DATE(?,'YYYY-MM-DD HH24:MI'),TO_DATE(?,'YYYY-MM-DD HH24:MI'))",
                  shipment),
              new String[] { "shipment_id" });
      db.updateShipmentLogDest = db.dbConnection
          .prepareStatement(String.format(
              "UPDATE %1$slog SET to_id = ?, ship_date = ?, arrive_date = ? WHERE %1$s_id = ?" +
                  "AND %1$s_id IN (SELECT %1$s_id FROM %1$s WHERE %2$s_id = ?)",
              shipment, supplier));
      db.deleteShipmentLogId = db.dbConnection
          .prepareStatement(String.format("DELETE FROM %1$slog WHERE %1$s_id = ?", shipment));
      // CRUD operations for product in shipment
      db.selectProductsFromShipmentId = db.dbConnection
          .prepareStatement(String.format("SELECT * FROM %1$s WHERE %1$s_id = ? ORDER BY %1$s_id", shipment));
      db.addProducttoShipmentId = db.dbConnection
          .prepareStatement(String.format("INSERT INTO %1$s (%2$s_id, %3$s_id, %4$s_id, qty) VALUES (?,?,?,?)",
              product_ship, shipment, product, supplier));
      db.deleteProductFromShipmentId = db.dbConnection
          .prepareStatement(String.format("DELETE FROM %1$s WHERE %2$s_id = ? AND %3$s_id = ? AND %4$s_id = ?",
              product_ship, shipment, product, supplier));

      // CRUD operations for manufacturing
      db.selectAllManufacturingFromSupplierName = db.dbConnection
          .prepareStatement(
              String.format("SELECT * FROM %1$s WHERE %2$s_id IN (" +
                  "SELECT %2$s_id FROM %2$s WHERE %2$s_name LIKE ?) ORDER BY %2$s_name",
                  manufacturing, supplier));
      db.selectAllManufacturingFromSupplierId = db.dbConnection
          .prepareStatement(
              String.format("SELECT * FROM %1$s WHERE %2$s_id = ? ORDER BY %2$s_id", manufacturing, supplier));
      db.selectAllManufacturingFromProductId = db.dbConnection
          .prepareStatement(
              String.format("SELECT * FROM %1$s WHERE %2$s_id = ? ORDER BY %2$s_id", manufacturing, product));
      db.selectAllManufacturingFromProductName = db.dbConnection
          .prepareStatement(
              String.format("SELECT * FROM %1$s WHERE %2$s_id IN (" +
                  "SELECT %2$s_id FROM %2$s WHERE %2$s_name LIKE ?) ORDER BY %2$s_name",
                  manufacturing, product));
      db.selectAllManufacturingFromComponent = db.dbConnection
          .prepareStatement(
              String.format("SELECT * FROM %1$s WHERE component LIKE ? ORDER BY component", manufacturing));
      db.selectOneManufacturing = db.dbConnection
          .prepareStatement(
              String.format("SELECT * FROM %1$s WHERE %1$s_id = ?", manufacturing));
      db.addManufacturing = db.dbConnection
          .prepareStatement(String.format("INSERT INTO %1$sb (%2$s_id, %3$s_id, component) VALUES (?,?,?)",
              manufacturing, product, supplier), new String[] { "manufacturing_id" });
      db.updateManufacturing = db.dbConnection
          .prepareStatement(
              String.format("UPDATE %1$sb SET component = ? WHERE %1$s_id = ?",
                  manufacturing));
      db.deleteManufacturingByComponent = db.dbConnection
          .prepareStatement(String.format("DELETE FROM %1$sb WHERE %2$s_id = ? AND %3$s_id = ? AND component LIKE ?",
              manufacturing, product, supplier));

      db.deleteManufacturingById = db.dbConnection
          .prepareStatement(String.format("DELETE FROM %1$sb WHERE %1$s_id = ?", manufacturing));

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

  /**
   * Get storeData from result set
   * 
   * @param rs result set
   * @return StoreData
   */
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
   * @param location location
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
   * @param supplier_id id
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
   * Get suppliers by location and/or name
   * 
   * @param location      location
   * @param supplier_name name
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

  /**
   * Adjusts the wildcards for SQL queries
   * 
   * @param string string to adjust
   * @return adjusted string
   */
  private String adjustWildcards(String string) {
    if (isNull(string) || string.isEmpty()) {
      return "%";
    }
    return "%" + string + "%";
  }

  /**
   * Get supplierData from result set
   * 
   * @param rs result set
   * @return SupplierData
   */
  private SupplierData getSupplier(ResultSet rs) {
    Integer supplier_id;
    try {
      supplier_id = rs.getInt("supplier_id");
      String supplier_name = rs.getString("supplier_name");
      String loc = rs.getString("location");
      Log.info(new SupplierData(supplier_id, supplier_name, loc).toString());
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

  /**
   * Get all products
   * 
   * @return An ArrayList of all products
   */
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

  /**
   * Get all products from log
   * 
   * @return An ArrayList of all products
   */
  ArrayList<ProductData> getAllProductsSimple() {
    ArrayList<ProductData> products = new ArrayList<>();
    try {
      ResultSet rs = selectAllProductsLog.executeQuery();
      while (rs.next()) {
        products.add(getProductFromLog(rs));
      }
    } catch (SQLException e) {
      Log.error("Cannot get all products");
    }
    return products;
  }

  /**
   * Get all products from log
   * 
   * @param product_id   product id
   * @param product_name product name
   * @return An ArrayList of all products
   */
  ArrayList<ProductData> getProductLog(int product_id, String product_name) {
    ArrayList<ProductData> products = new ArrayList<>();
    try {
      ResultSet rs;
      // Only product_id is given
      if (product_id > 0 && isNull(product_name)) {
        selectOneSimpleProduct.setInt(1, product_id);
        rs = selectOneSimpleProduct.executeQuery();
      } else if (product_id < 0 && !isNull(product_name)) { // Only product_name is given
        selectProductLogByName.setString(1, adjustWildcards(product_name));
        rs = selectProductLogByName.executeQuery();
      } else { // Get the entire log
        rs = selectAllProductsLog.executeQuery();
      }
      while (rs.next()) {
        products.add(getProductFromLog(rs));
      }
    } catch (SQLException e) {
      Log.error("Cannot get all products");
    }
    return products;
  }

  /**
   * Get productData from result set
   * 
   * @param rs result set
   * @return ProductData
   */
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

  /**
   * Get productData from ProductLog from result set
   * 
   * @param rs result set
   * @return ProductData
   */
  private ProductData getProductFromLog(ResultSet rs) {
    try {
      String product_name = rs.getString("product_name");
      int product_id = rs.getInt("product_id");
      return new ProductData(null, product_id, product_name, 0, null);
    } catch (SQLException e) {
      Log.error("Cannot get product");
    }
    return null;
  }

  /**
   * Get all products by name
   * 
   * @param product_name  product name
   * @param supplier_name supplier name
   * @return An ArrayList of all products
   */
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

  /**
   * Get all products by product id
   * 
   * @param product_id    product id
   * @param supplier_name supplier name
   * @return An ArrayList of all products
   */
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

  /**
   * Get all products by supplier id
   * 
   * @param supplier_id supplier id
   * @return An ArrayList of all products
   */
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

  /**
   * Get one product by product id and supplier id
   * 
   * @param product_id  product id
   * @param supplier_id supplier id
   * @return ProductData object
   */
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

  /**
   * Adds a new product by name
   * 
   * @param product_name product name
   * @return product_id
   */
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
      Log.error(product_id + " " + supplier_id + " " + price + " " + unit_type);
    }
    return count;
  }

  /**
   * Adds a new product by name
   * 
   * @param product_name product name
   * @return product_id
   */
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
    Log.info("Got product id: " + product_id);
    return product_id;
  }

  /**
   * Deletes a product by product_id and supplier_id
   * 
   * @param product_id  product id
   * @param supplier_id supplier id
   * @return 1 if successful, -1 if not
   */
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

  /**
   * Update product based on product_id and supplier_id
   * 
   * @param product_id  product id
   * @param supplier_id supplier id
   * @param price       price
   * @param unit_type   unit type
   * @return number of rows updated (1 on success)
   */
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

  /**
   * Get all shipments
   * 
   * @param to_id         destination id
   * @param supplier_name supplier name
   * @return An ArrayList of all shipments
   */
  ArrayList<ShipmentData> getShipmentsByDest(int to_id, String supplier_name) {
    ArrayList<ShipmentData> shipments = new ArrayList<>();
    try {
      ResultSet rs;
      // Only destination id is given
      if (to_id > 0 && isNull(supplier_name)) {
        selectAllShipmentByDest.setInt(1, to_id);
        rs = selectAllShipmentByDest.executeQuery();
      } else if (to_id < 0 && !isNull(supplier_name)) { // Only supplier name is given
        selectAllShipmentFromSupplierName.setString(1, adjustWildcards(supplier_name));
        rs = selectAllShipmentFromSupplierName.executeQuery();
      } else { // Since both are not given (-1, null), get the log from store
        Log.info("Getting all shipments to store");
        rs = selectShipmentIdsToStore.executeQuery();
      }
      while (rs.next()) {
        shipments.add(getShipmentLog(rs));
      }
    } catch (SQLException e) {
      Log.error("Cannot get all shipments with destination: " + to_id);
    }
    return shipments;
  }

  /**
   * Get all shipments by supplier
   * 
   * @param supplier_id   supplier id
   * @param supplier_name supplier name
   * @return An ArrayList of all shipments
   */
  ArrayList<ShipmentData> getShipmentsBySupplier(int supplier_id, String supplier_name) {
    ArrayList<ShipmentData> shipments = new ArrayList<>();
    try {
      ResultSet rs;
      // Only supplier name is given
      if (supplier_id < 0 && !isNull(supplier_name)) {
        selectAllShipmentFromSupplierName.setString(1, adjustWildcards(supplier_name));
        rs = selectAllShipmentFromSupplierName.executeQuery();
      } else if (supplier_id > 0 && isNull(supplier_name)) {// Only supplier name is given
        selectAllShipmentFromSupplierId.setInt(1, supplier_id);
        rs = selectAllShipmentFromSupplierId.executeQuery();
      } else { // Get the entire log
        selectShipmentIdsToStore.setInt(1, supplier_id);
        rs = selectShipmentIdsToStore.executeQuery();
      }
      while (rs.next()) {
        shipments.add(getShipmentLog(rs));
      }
    } catch (SQLException e) {
      Log.error("Cannot get all shipments with supplier: " + supplier_id);
    }
    return shipments;
  }

  /**
   * Check if a string is null or empty
   * 
   * @param string string to check
   * @return true if null or empty
   */
  private static boolean isNull(String string) {
    return string == null || string.isEmpty();
  }

  /**
   * Get all shipments by supplier
   * 
   * @param supplier_name supplier name
   * @return An ArrayList of all shipments
   */
  ArrayList<ShipmentData> getShipmentToSupplier(String supplier_name) {
    ArrayList<ShipmentData> shipments = new ArrayList<>();
    try {
      selectShipmentIdsToSupplier.setString(1, adjustWildcards(supplier_name));
      ResultSet rs = selectShipmentIdsToSupplier.executeQuery();
      while (rs.next()) {
        shipments.add(getShipmentLog(rs));
      }
    } catch (SQLException e) {
      Log.error("Cannot get all shipments with destination: store");
    }
    return shipments;
  }

  /**
   * Get all shipments to the store
   * 
   * @return An ArrayList of all shipments
   */
  ArrayList<ShipmentData> getShipmentToStore() {
    ArrayList<ShipmentData> shipments = new ArrayList<>();
    try {
      ResultSet rs = selectShipmentIdsToStore.executeQuery();
      while (rs.next()) {
        shipments.add(getShipmentLog(rs));
      }
    } catch (SQLException e) {
      Log.error("Cannot get all shipments with destination: store");
    }
    return shipments;
  }

  /**
   * Add a new shipment log
   * 
   * @param to_id       destination id
   * @param ship_date   ship date
   * @param arrive_date arrive date
   * @return shipment id
   */
  int addShipmentLog(int to_id, String ship_date, String arrive_date) {
    int shipment_id = -1;
    try {
      addShipmentLogId.setInt(1, to_id);
      addShipmentLogId.setString(2, ship_date);
      addShipmentLogId.setString(3, arrive_date);
      int affectedRows = addShipmentLogId.executeUpdate();
      if (affectedRows > 0) {
        ResultSet rs = addShipmentLogId.getGeneratedKeys();
        if (rs.next()) {
          shipment_id = rs.getInt(1);
        }
      }
    } catch (SQLException e) {
      Log.error("Cannot add shipment log");
      e.printStackTrace();
    }
    return shipment_id;
  }

  /**
   * Update a shipment log
   * 
   * @param shipment_id shipment id
   * @param to_id       destination id
   * @param ship_date   ship date
   * @param arrive_date arrive date
   * @return number of rows updated (1 on success)
   */
  int updateShipmentLog(int shipment_id, int to_id, String ship_date, String arrive_date, int supplier_id) {
    int count = -1;
    try {
      updateShipmentLogDest.setInt(1, to_id);

      updateShipmentLogDest.setString(2, ship_date);
      updateShipmentLogDest.setString(3, arrive_date);
      updateShipmentLogDest.setInt(4, shipment_id);
      updateShipmentLogDest.setInt(5, supplier_id);
      count = updateShipmentLogDest.executeUpdate();
    } catch (SQLException e) {
      Log.error("Cannot update shipment log");
    }
    return count;
  }

  /**
   * Delete a shipment log
   * 
   * @param shipment_id shipment id
   * @return number of rows deleted (1 on success)
   */
  int deleteShipmentLog(int shipment_id) {
    int count = -1;
    try {
      deleteShipmentLogId.setInt(1, shipment_id);
      count = deleteShipmentLogId.executeUpdate();
    } catch (SQLException e) {
      Log.error("Cannot delete shipment log");
    }
    return count;
  }

  /**
   * Get all products from shipment id
   * 
   * @param shipment_id shipment id
   * @return An ArrayList of all shipments
   */
  ArrayList<ShipmentData> getProductsFromShipmentId(int shipment_id) {
    ArrayList<ShipmentData> shipments = new ArrayList<>();
    try {
      selectProductsFromShipmentId.setInt(1, shipment_id);
      ResultSet rs = selectProductsFromShipmentId.executeQuery();
      while (rs.next()) {
        shipments.add(getShipment(rs));
      }
    } catch (SQLException e) {
      Log.error("Cannot get all shipments with destination: " + shipment_id);
    }
    return shipments;
  }

  /**
   * Add a product to a shipment
   * 
   * @param shipment_id shipment id
   * @param product_id  product id
   * @param supplier_id supplier id
   * @param quantity    quantity
   * @return number of rows added (1 on success)
   */
  int addProductToShipment(int shipment_id, int product_id, int supplier_id, float quantity) {
    int count = 0;
    quantity = Math.round(quantity * 100) / 100;
    try {
      addProducttoShipmentId.setInt(1, shipment_id);
      addProducttoShipmentId.setInt(2, product_id);
      addProducttoShipmentId.setInt(3, supplier_id);
      addProducttoShipmentId.setFloat(4, quantity);
      count = addProducttoShipmentId.executeUpdate();
    } catch (SQLException e) {
      Log.error("Cannot add product to shipment");
      e.printStackTrace();
    }
    return count;
  }

  /**
   * Update a product in a shipment
   * 
   * @param shipment_id shipment id
   * @param product_id  product id
   * @param supplier_id supplier id
   * @param quantity    quantity
   * @return number of rows updated (1 on success)
   */
  int deleteProductFromShipment(int shipment_id, int product_id, int supplier_id) {
    int count = 0;
    try {
      deleteProductFromShipmentId.setInt(1, shipment_id);
      deleteProductFromShipmentId.setInt(2, product_id);
      deleteProductFromShipmentId.setInt(3, supplier_id);
      count = deleteProductFromShipmentId.executeUpdate();
    } catch (SQLException e) {
      Log.error("Cannot delete product from shipment");
    }
    return count;
  }

  /**
   * Get shipmentData from result set
   * All the products from that shipment
   * 
   * @param rs result set
   * @return ShipmentData
   */
  private ShipmentData getShipment(ResultSet rs) {
    try {
      int shipment_id = rs.getInt("shipment_id");
      int to_id = rs.getInt("to_id");
      String ship_date = rs.getString("ship_date");
      String arrive_date = rs.getString("arrive_date");
      int supplier_id = rs.getInt("supplier_id");
      String supplier_name = rs.getString("supplier_name");
      SupplierData supplier = new SupplierData(supplier_id, supplier_name, "");
      int product_id = rs.getInt("product_id");
      String product_name = rs.getString("product_name");
      String unit_type = rs.getString("unit_type");
      float price = rs.getFloat("price");
      ProductData product = new ProductData(supplier, product_id, product_name, price, unit_type);
      int quantity = rs.getInt("qty");
      return new ShipmentData(shipment_id, to_id, ship_date, arrive_date, supplier, product, quantity);
    } catch (SQLException e) {
      Log.error("Cannot get shipment");
    }
    return null;
  }

  /**
   * Get shipmentData from shipmentLog from result set
   * 
   * @param rs result set
   * @return ShipmentData
   */
  private ShipmentData getShipmentLog(ResultSet rs) {
    try {
      int shipment_id = rs.getInt("shipment_id");
      int to_id = rs.getInt("to_id");
      String ship_date = rs.getString("ship_date");
      String arrive_date = rs.getString("arrive_date");
      Log.info(new ShipmentData(shipment_id, to_id, ship_date, arrive_date).toString());
      return new ShipmentData(shipment_id, to_id, ship_date, arrive_date);
    } catch (SQLException e) {
      Log.error("Cannot get shipment");
    }
    return null;
  }

  /**
   * Get all manufacturing
   * 
   * @param supplier_name supplier name
   * @return An ArrayList of all manufacturing
   */
  ArrayList<ManufacturingData> getManufacturingBySupplierName(String supplier_name) {
    ArrayList<ManufacturingData> manufacturing = new ArrayList<>();
    try {
      selectAllManufacturingFromSupplierName.setString(1, adjustWildcards(supplier_name));
      ResultSet rs = selectAllManufacturingFromSupplierName.executeQuery();
      while (rs.next()) {
        manufacturing.add(getManufacturing(rs));
      }
    } catch (SQLException e) {
      Log.error("Cannot get all manufacturing with supplier: " + supplier_name);
    }
    return manufacturing;
  }

  /**
   * Get all manufacturing
   * 
   * @param supplier_id supplier id
   * @return An ArrayList of all manufacturing
   */
  ArrayList<ManufacturingData> getManufacturingBySupplierId(int supplier_id) {
    ArrayList<ManufacturingData> manufacturing = new ArrayList<>();
    try {
      selectAllManufacturingFromSupplierId.setInt(1, supplier_id);
      ResultSet rs = selectAllManufacturingFromSupplierId.executeQuery();
      while (rs.next()) {
        manufacturing.add(getManufacturing(rs));
      }
    } catch (SQLException e) {
      Log.error("Cannot get all manufacturing with supplier: " + supplier_id);
    }
    return manufacturing;
  }

  /**
   * Get all manufacturing
   * 
   * @param product_id product id
   * @return An ArrayList of all manufacturing
   */
  ArrayList<ManufacturingData> getManufacturingByProductId(int product_id) {
    ArrayList<ManufacturingData> manufacturing = new ArrayList<>();
    try {
      selectAllManufacturingFromProductId.setInt(1, product_id);
      ResultSet rs = selectAllManufacturingFromProductId.executeQuery();
      while (rs.next()) {
        manufacturing.add(getManufacturing(rs));
      }
    } catch (SQLException e) {
      Log.error("Cannot get all manufacturing with product: " + product_id);
    }
    return manufacturing;
  }

  /**
   * Get all manufacturing
   * 
   * @param product_name product name
   * @return An ArrayList of all manufacturing
   */
  ArrayList<ManufacturingData> getManufacturingByProductName(String product_name) {
    ArrayList<ManufacturingData> manufacturing = new ArrayList<>();
    try {
      selectAllManufacturingFromProductName.setString(1, adjustWildcards(product_name));
      ResultSet rs = selectAllManufacturingFromProductName.executeQuery();
      while (rs.next()) {
        manufacturing.add(getManufacturing(rs));
      }
    } catch (SQLException e) {
      Log.error("Cannot get all manufacturing with product: " + product_name);
    }
    return manufacturing;
  }

  /**
   * Get all manufacturing
   * 
   * @param component component
   * @return An ArrayList of all manufacturing
   */
  ArrayList<ManufacturingData> getManufacturingByComponent(String component) {
    ArrayList<ManufacturingData> manufacturing = new ArrayList<>();
    try {
      selectAllManufacturingFromComponent.setString(1, adjustWildcards(component));
      ResultSet rs = selectAllManufacturingFromComponent.executeQuery();
      while (rs.next()) {
        manufacturing.add(getManufacturing(rs));
      }
    } catch (SQLException e) {
      Log.error("Cannot get all manufacturing with component: " + component);
    }
    return manufacturing;
  }

  /**
   * Get one manufacturing by id
   * 
   * @param manufacturing_id id
   * @return ManufacturingData object
   */
  ManufacturingData getOneManufacturing(int manufacturing_id) {
    try {
      selectOneManufacturing.setInt(1, manufacturing_id);
      ResultSet rs = selectOneManufacturing.executeQuery();
      if (rs.next()) {
        return getManufacturing(rs);
      }
    } catch (SQLException e) {
      Log.error("Cannot get all manufacturing with id =" + manufacturing_id);
    }
    return null;
  }

  /**
   * Get manufacturingData from result set
   * 
   * @param rs result set
   * @return ManufacturingData
   */
  int addManufacturing(int product_id, int supplier_id, String component) {
    int count = 0;
    try {
      addManufacturing.setInt(1, product_id);
      addManufacturing.setInt(2, supplier_id);
      addManufacturing.setString(3, component);
      int affectedRows = addManufacturing.executeUpdate();
      if (affectedRows > 0) {
        ResultSet rs = addManufacturing.getGeneratedKeys();
        if (rs.next()) {
          count = rs.getInt(1);
        }
      }
    } catch (SQLException e) {
      Log.error("Cannot add manufacturing");
    }
    return count;
  }

  /**
   * Update manufacturingData from result set
   * 
   * @param manufacturing_id manufacturing id
   * @param component        new component name
   * @return
   */
  int updateManufacturing(int manufacturing_id, String component) {
    int count = 0;
    try {
      updateManufacturing.setString(1, component);
      updateManufacturing.setInt(2, manufacturing_id);
      count = updateManufacturing.executeUpdate();
    } catch (SQLException e) {
      Log.error("Cannot update manufacturing");
    }
    return count;
  }

  /**
   * Delete manufacturing
   * 
   * @param product_id  product id
   * @param supplier_id supplier id
   * @param component   component name
   * @return
   */
  int deleteManufacturing(int product_id, int supplier_id, String component) {
    int count = 0;
    try {
      deleteManufacturingByComponent.setInt(1, product_id);
      deleteManufacturingByComponent.setInt(2, supplier_id);
      deleteManufacturingByComponent.setString(3, adjustWildcards(component));
      count = deleteManufacturingByComponent.executeUpdate();
    } catch (SQLException e) {
      Log.error("Cannot delete manufacturing");
    }
    return count;
  }

  /**
   * Delete manufacturing
   * 
   * @param manufacturing_id manufacturing id
   * @return
   */
  int deleteManufacturing(int manufacturing_id) {
    int count = 0;
    try {
      deleteManufacturingById.setInt(1, manufacturing_id);
      count = deleteManufacturingById.executeUpdate();
    } catch (SQLException e) {
      Log.error("Cannot delete manufacturing");
    }
    return count;
  }

  /**
   * Get manufacturingData from result set
   * 
   * @param rs result set
   * @return ManufacturingData
   */
  private ManufacturingData getManufacturing(ResultSet rs) {
    try {
      int manufacturing_id = rs.getInt("manufacturing_id");
      int product_id = rs.getInt("product_id");
      String product_name = rs.getString("product_name");
      float price = rs.getFloat("price");
      String unit_type = rs.getString("unit_type");
      int supplier_id = rs.getInt("supplier_id");
      String supplier_name = rs.getString("supplier_name");
      String loc = rs.getString("location");
      SupplierData supplier = new SupplierData(supplier_id, supplier_name, loc);
      ProductData product = new ProductData(supplier, product_id, product_name, price, unit_type);
      String component = rs.getString("component");
      return new ManufacturingData(product, supplier, component, manufacturing_id);
    } catch (SQLException e) {
      Log.error("Cannot get manufacturing");
    }
    return null;
  }
}
