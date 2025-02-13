import java.io.*;
import java.sql.*;
import java.util.ArrayList;
public class Main {

  public static void main(String[] args) {
    String dbid = System.getenv("DBID");
    String port = System.getenv("PORT");
    String userid = System.getenv("USERID");
    String passwd = System.getenv("PASSWD");
    String path = System.getenv("PATH");
    ArrayList<String> table = null;
    Database db = Database.getDatabase(dbid, port, path, userid, passwd, table);
  }
}
