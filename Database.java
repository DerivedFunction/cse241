import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
public class Database {
  private Connection dbConnection;
  private PreparedStatement selectAll;
  private Database() {}

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
      e.printStackTrace();
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
    try {
      db.selectAll = db.dbConnection.prepareStatement("");
    } catch (SQLException e) {
      Log.error("Error creating prepared statement");
      e.printStackTrace();
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
      e.printStackTrace();
      dbConnection = null;
      return false;
    }
    dbConnection = null;
    return true;
  }
}
