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
  private PreparedStatement removeStorebyId;
  private PreparedStatement updateStorebyId;

  private PreparedStatement selectAllSuppliers;
  private PreparedStatement selectSupplierById;
  private PreparedStatement selectSupplierByLocation;
  private PreparedStatement selectSupplierbyName;
  private PreparedStatement addSupplier;
  private PreparedStatement removeSupplierById;
  private PreparedStatement updateSupplierbyId;

  private PreparedStatement selectAllProducts;
  private PreparedStatement selectAllProductsById;
  private PreparedStatement selectAllProductsByStore;
  private PreparedStatement selectAllProductsBySupplierName;
  private PreparedStatement selectAllProductsByName;
  private PreparedStatement selectOneProduct;

  private PreparedStatement addProduct;
  private PreparedStatement updateProduct;
  private PreparedStatement removeProduct;

  private PreparedStatement addBuilding;
  private PreparedStatement updateBuilding;
  private PreparedStatement removeBuilding;

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
          .prepareStatement(String.format("INSERT INTO %1$s (location) VALUES (?)", building));
      db.removeBuilding = db.dbConnection
          .prepareStatement(String.format("DELETE FROM %1$s WHERE id = ?", building));
      db.updateBuilding = db.dbConnection
          .prepareStatement(String.format("UPDATE %1$s SET location = ? WHERE id = ?", building));
      // CRUD operations for store
      db.selectAllStores = db.dbConnection
          .prepareStatement(String.format("SELECT * FROM %1$s ORDER BY %1$s_id", store));
      db.selectStoreById = db.dbConnection
          .prepareStatement(String.format("SELECT * FROM %1$s WHERE %1$s_id = ?", store));
      db.selectStorebyLocation = db.dbConnection
          .prepareStatement(String.format("SELECT * FROM %1$s WHERE location LIKE ? ORDER BY %1$s_id", store));
      db.addStore = db.dbConnection.prepareStatement(String.format("INSERT INTO %1$s (location) VALUES (?)", store));
      db.removeStorebyId = db.removeBuilding; // We can just remove the id in building to cascade and remove it in store
      db.updateStorebyId = db.updateBuilding;// We can just update it in building

      // CRUD operations for supplier
      db.selectAllSuppliers = db.dbConnection
          .prepareStatement(String.format("SELECT * FROM %1$s ORDER BY %1$s_id", supplier));
      db.selectSupplierById = db.dbConnection
          .prepareStatement(String.format("SELECT * FROM %1$s WHERE %1$s_id = ?", supplier));
      db.selectSupplierByLocation = db.dbConnection
          .prepareStatement(String.format("SELECT * FROM %1$s WHERE location LIKE ? ORDER BY %1$s_id", supplier));
      db.selectSupplierbyName = db.dbConnection
          .prepareStatement(String.format("SELECT * FROM %1$s WHERE %1$s_name LIKE ? ORDER BY %1$s_id", supplier));
      db.addSupplier = db.dbConnection
          .prepareStatement(String.format("INSERT INTO %1$s (%1$s_name, location) VALUES (?, ?)", supplier));
      db.removeSupplierById = db.dbConnection
          .prepareStatement(String.format("DELETE FROM %1$s WHERE %1$s_id = ?", supplier));
      db.updateSupplierbyId = db.dbConnection.prepareStatement(
          String.format("UPDATE %1$s SET %1$s_name = ?, location = ? WHERE %1$s_id = ?", supplier));

      // CRUD operations for product
      db.selectAllProducts = db.dbConnection
          .prepareStatement(String.format("SELECT * FROM %1$sview", product));
      db.selectAllProductsById = db.dbConnection
          .prepareStatement(String.format("SELECT * FROM %1$sview WHERE %1$s_id = ?", product));
      db.selectAllProductsByStore = db.dbConnection
          .prepareStatement(String.format("SELECT * FROM %1$sview WHERE %2$s_id = ?", product, store));
      db.selectAllProductsBySupplierName = db.dbConnection
          .prepareStatement(String.format("SELECT * FROM %1$sview WHERE %2$s_name LIKE ?", product, supplier));
      db.selectAllProductsByName = db.dbConnection
          .prepareStatement(String.format("SELECT * FROM %1$sview WHERE %1$s_name LIKE ?", product));
      db.selectOneProduct = db.dbConnection
          .prepareStatement(
              String.format("SELECT * FROM %1$sview WHERE %1$s_id = ? AND %2$s_id = ?", product, supplier));
      db.addProduct = db.dbConnection
          .prepareStatement(String.format("INSERT INTO %1$s (%1$s_id, %1$s_name, price, %2$s_id) VALUES (?,?,?,?)",
              product, supplier));
      db.updateProduct = db.dbConnection
          .prepareStatement(String.format("UPDATE %1$s SET %1$s_name = ?, price = ? WHERE %1$s_id = ? AND %2$s_id = ?",
              product, supplier));
      db.removeProduct = db.dbConnection
          .prepareStatement(String.format("DELETE FROM %1$s WHERE %1$s_id = ? AND %2$s_id = ?", product, supplier));
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
        Integer store_id = rs.getInt("store_id");
        String location = rs.getString("location");
        result.add(new StoreData(store_id, location));
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
        Integer store_id = rs.getInt(1);
        String location = rs.getString(2);
        result = new StoreData(store_id, location);
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
      selectStorebyLocation.setString(1, "%" + location + "%");
      ResultSet rs = selectStorebyLocation.executeQuery();
      while (rs.next()) {
        Integer store_id = rs.getInt("store_id");
        String loc = rs.getString("location");
        result.add(new StoreData(store_id, loc));
      }
    } catch (SQLException e) {
      Log.error("SQL Exception: Cannot get all stores: " + location);

    }
    return result;
  }

  /**
   * Adds a new store by location
   * 
   * @return 1 if successful, -1 if not
   */
  int addStore(String location) {
    int count = -1;
    try {
      addStore.setString(1, location);
      count = addStore.executeUpdate();
    } catch (SQLException e) {
      Log.error("Invalid SQL Exception: Cannot add store");

    }
    return count;
  }

  /**
   * Deletes a store by store-id
   * 
   * @return 1 if successful, -1 if not
   */
  int deleteStoreById(int id) {
    int count = -1;
    try {
      removeStorebyId.setInt(1, id);
      count = removeStorebyId.executeUpdate();
    } catch (SQLException e) {
      Log.error("Invalid SQL Exception: Cannot remove store: " + id);

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
   * Get all suppliers
   * 
   * @return An ArrayList of all suppliers
   */
  ArrayList<SupplierData> getAllSuppliers() {
    ArrayList<SupplierData> result = new ArrayList<>();
    try {
      ResultSet rs = selectAllSuppliers.executeQuery();
      while (rs.next()) {
        Integer id = rs.getInt("supplier_id");
        String name = rs.getString("supplier_name");
        String location = rs.getString("location");
        result.add(new SupplierData(id, name, location));
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
  SupplierData getSupplierById(int id) {
    SupplierData result = null;
    if (id < 0) {
      return null;
    }
    try {
      selectSupplierById.setInt(1, id);
      ResultSet rs = selectSupplierById.executeQuery();
      if (rs.next()) {
        Integer supplier_id = rs.getInt(1);
        String supplier_name = rs.getString(2);
        String location = rs.getString(3);
        result = new SupplierData(supplier_id, supplier_name, location);
      }
    } catch (SQLException e) {
      Log.error("SQL Exception: supplier not found: " + id);

    }
    return result;
  }

  /**
   * Get suppliers by location
   * 
   * @return An ArrayList of all suppliers
   */
  ArrayList<SupplierData> getSupplierByLocation(String location) {
    ArrayList<SupplierData> result = new ArrayList<>();
    try {
      selectSupplierByLocation.setString(1, "%" + location + "%");
      ResultSet rs = selectSupplierByLocation.executeQuery();
      while (rs.next()) {
        Integer supplier_id = rs.getInt("supplier_id");
        String supplier_name = rs.getString("supplier_name");
        String loc = rs.getString("location");
        result.add(new SupplierData(supplier_id, supplier_name, loc));
      }
    } catch (SQLException e) {
      Log.error("SQL Exception: Cannot get all suppliers: " + location);

    }
    return result;
  }

  /**
   * Get suppliers by name
   * 
   * @return An ArrayList of all suppliers
   */
  ArrayList<SupplierData> getSupplierbyName(String name) {
    ArrayList<SupplierData> result = new ArrayList<>();
    try {
      selectSupplierbyName.setString(1, "%" + name + "%");
      ResultSet rs = selectSupplierbyName.executeQuery();
      while (rs.next()) {
        Integer supplier_id = rs.getInt("supplier_id");
        String supplier_name = rs.getString("supplier_name");
        String loc = rs.getString("location");
        result.add(new SupplierData(supplier_id, supplier_name, loc));
      }
    } catch (SQLException e) {
      Log.error("SQL Exception: Cannot get all suppliers: " + name);

    }
    return result;
  }

  /**
   * Adds a new supplier by name and location
   * 
   * @return 1 if successful, -1 if not
   */
  int addSupplier(String name, String location) {
    int count = -1;
    try {
      addSupplier.setString(1, name);
      addSupplier.setString(2, location);
      count = addSupplier.executeUpdate();
    } catch (SQLException e) {
      Log.error("Invalid SQL Exception: Cannot add supplier");

    }
    return count;
  }

  /**
   * Removes a supplier by id
   * 
   * @return 1 if successful, -1 if not
   */
  int removeSupplierById(int id) {
    int count = -1;
    try {
      removeSupplierById.setInt(1, id);
      count = removeSupplierById.executeUpdate();
    } catch (SQLException e) {
      Log.error("Invalid SQL Exception: Cannot add supplier");

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
      updateSupplierbyId.setInt(3, supplier_id);
      updateSupplierbyId.setString(1, supplier_name);
      updateSupplierbyId.setString(2, location);

      count = updateSupplierbyId.executeUpdate();
    } catch (SQLException e) {
      Log.error("Invalid SQL Exception: Cannot update supplier " + supplier_id + ": " + location);

    }
    return count;
  }
}
