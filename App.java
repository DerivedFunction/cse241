import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;

public class App {
  static Scanner scanner = new Scanner(System.in);
  static Database db;

  public static void main(String[] args) {
    Log.info("Logging enabled");
    System.out.print("Enter your username: ");
    String userid = scanner.next();
    System.out.print("Enter your password: ");
    String passwd = scanner.next();
    scanner.nextLine();
    String dbid = "edgar1.cse.lehigh.edu";
    String port = "1521";
    String path = "cse241";
    ArrayList<String> table = InitalizeTable();
    db = Database.getDatabase(dbid, port, path, userid, passwd, table);
    if (db == null) {
      System.out.println("Failed to connect to database");
      scanner.close();
      return;
    }
    userMenu();
  }

  static void userMenu() {
    System.out.println("-------------------------");
    System.out.println("[1] I am a store [M]anager");
    System.out.println("[2] I am a [S]upplier");
    System.out.println("[3] [E]xit");
    try {

      switch (getChar()) {
        case '1':
        case 'm':
          managerMenu();
          break;
        case '2':
        case 's':
          System.out.println("Enter supplier name:");
          String supplier_name = getString();
          supplierMenu(supplier_name);
          break;
        case '3':
        case 'e':
          scanner.close();
          db.disconnect();
          exitMenu();
          break;
        case '4':
          generateData();
          break;
        default:
          System.out.println("Invalid choice");
          userMenu();
      }
    } catch (Exception e) {
      System.out.println("Exception: Invalid choice");
      userMenu();
    }
  }

  private static void exitMenu() {
    System.out.println("Database disconnected.");
    System.out.println("Press Ctrl+C to close");
    // A workaround to some issues with System.exit(0) repeating main() for no
    // reason
    while (true) {
      ;
    }
  }

  private static void generateData() {
    System.out.println("Create a new supplier. Enter supplier name (or !random): ");
    String name = getString();
    String location;
    if (name.equals("!random")) {
      for (int i = 0; i < 20; i++) {
        Log.info("Generating random data...");
        // Generate a random store, supplier location
        // Generate a random length for string
        java.security.SecureRandom secureRandom = new java.security.SecureRandom();
        Integer randomLength = secureRandom.nextInt(10) + 10;
        location = UUID.randomUUID().toString().substring(0, randomLength);
        String supplier_name = UUID.randomUUID().toString().substring(0, randomLength);
        db.addStore(location);
        db.addStore(location);
        db.addSupplier(supplier_name, location);
        db.addSupplier(supplier_name, location);
      }
    } else {
      System.out.println("Enter location:");
      location = getString();
      db.addSupplier(name, location);
    }
    userMenu();
  }

  static void managerMenu() {
    System.out.println("-------------------------");
    System.out.println("[1] View/Manage my store [L]ocations");
    System.out.println("[2] View [S]uppliers");
    System.out.println("[3] View [P]roducts");
    System.out.println("[4] View/Manage my S[h]ipments");
    System.out.println("[5] Return to [M]ain menu");
    try {
      switch (getChar()) {
        case '1':
        case 'l':
          manageStoreLocations();
          break;
        case '2':
        case 's':
          getSupplierData();
          break;
        case '3':
        case 'p':
          viewProducts();
          break;
        case '4':
        case 'h':
          viewShipments(null);
          break;
        case '5':
        case 'm':
          userMenu();
          break;
        default:
          System.out.println("Invalid choice");
          managerMenu();
      }
    } catch (Exception e) {
      System.out.println("Exception: Invalid choice");
    }
    managerMenu();
  }

  private static void getSupplierData() {
    System.out.println("-------------------------");
    System.out.println("[1] Get [A]ll suppliers");
    System.out.println("[2] Get supplier by [I]d");
    System.out.println("[3] Get suppliers by [N]ame");
    System.out.println("[4] Get suppliers by [L]ocation");

    switch (getChar()) {
      case '1':
      case 'a':
        viewSuppliers(0, null, null);
        break;
      case '2':
      case 'i': {
        System.out.println("Enter supplier id");
        int id = getInt();
        viewSuppliers(id, null, null);
      }
        break;
      case '3':
      case 'n': {
        System.out.println("Enter supplier name");
        String name = getString();
        viewSuppliers(0, name, null);
      }
        break;
      case '4':
      case 'l':
        System.out.println("Enter supplier location");
        String location = getString();
        viewSuppliers(0, null, location);
        break;
      default:
        System.out.println("Invalid choice");
        getSupplierData();
        break;
    }
  }

  private static char getChar() {
    System.out.print("> ");
    if (scanner.hasNext()) {
      char[] choice = scanner.nextLine().toLowerCase().toCharArray();
      return choice[0];
    }
    return '\0';
  }

  private static void viewShipments(String input) {

  }

  private static void viewProducts() {

  }

  private static void manageStoreLocations() {
    System.out.println("-------------------------");
    System.out.println("[1] View store [L]ocations");
    System.out.println("[2] [A]dd store location");
    System.out.println("[3] [R]emove store location");
    System.out.println("[4] [U]pdate store location");
    System.out.println("[5] Return to [m]ain menu");
    try {

      switch (getChar()) {
        case '1':
        case 'l': {
          ArrayList<StoreData> stores = new ArrayList<>();
          System.out.println("Select store id/location to view. Enter -1 to view all stores.");
          System.out.print("> ");
          // Check if input is an int or string
          if (scanner.hasNextInt()) {
            int storeId = getInt();
            if (storeId == -1) {
              stores = db.getAllStores();
              printStores(stores);
            } else {
              stores.add(db.getStoreById(storeId));
              printStores(stores);
            }
          } else {
            String location = getString();
            stores = db.getStoreByLocation(location);
            printStores(stores);
          }
        }
          break;
        case '2':
        case 'a': {
          System.out.println("Enter store location: ");
          String location = getString();
          db.addStore(location);
        }
          break;
        case '3':
        case 'r': {
          System.out.println("Enter store id: ");
          int store_id = getInt();
          db.deleteStoreById(store_id);
        }
          break;
        case '4':
        case 'u': {
          System.out.println("Enter store id:");
          int store_id = getInt();
          System.out.println("Enter location:");
          String location = getString();
          db.updateStoreLocation(store_id, location);
        }
          break;
        case '5':
        case 'm':
          userMenu();
          break;
        default:
          System.out.println("Invalid choice");
          break;
      }
    } catch (Exception e) {
      System.out.println("Exception: Invalid choice");
    } finally {
      manageStoreLocations();
    }

  }

  private static String getString() {
    String input = scanner.nextLine();
    while (input == null || input.trim().isEmpty()) {
      input = scanner.nextLine();
    }
    return input;
  }

  private static int getInt() {
    if (scanner.hasNextInt()) {
      int x = scanner.nextInt();
      scanner.nextLine();
      return x;
    }
    return -1;
  }

  private static void printStores(ArrayList<StoreData> stores) {
    String format = "%-5s %-20s";
    System.out.println(String.format(format, "ID", "Location"));
    for (StoreData store : stores) {
      System.out.println(String.format(format, store.store_id, store.location));
    }
  }

  private static void printSuppliers(ArrayList<SupplierData> suppliers) {
    String format = "%-5s %-20s %-20s";
    System.out.println(String.format(format, "ID", "Name", "Location"));
    for (SupplierData supplier : suppliers) {
      System.out.println(String.format(format, supplier.supplier_id, supplier.supplier_name, supplier.location));
    }
  }

  static void supplierMenu(String supplier_name) {
    System.out.println("-------------------------");
    System.out.println("[1] View/Manage my [L]ocations");
    System.out.println("[2] View/Manage my [P]roducts");
    System.out.println("[3] View/Manage my [S]hipments");
    System.out.println("[4] View/Manage/Recall my Manufacturing [C]omponents");
    System.out.println("[5] Return to [M]ain menu");
    try {

      switch (getChar()) {
        case '1':
        case 'l':
          manageSupplier(supplier_name);
          break;
        case '2':
        case 'p':
          viewProducts();
          break;
        case '3':
        case 's':
          viewShipments(null);
          break;
        case '4':
        case 'c':
          viewComponents(null);
          break;
        case '5':
        case 'm':
          userMenu();
          break;
        default:
          System.out.println("Invalid choice");
          supplierMenu(supplier_name);
      }
    } catch (Exception e) {
      System.out.println("Exeption: Invalid choice");
    }
    supplierMenu(supplier_name);
  }

  private static void manageSupplier(String supplier_name) {
    System.out.println("-------------------------");
    System.out.println("[1] View my [L]ocations");
    System.out.println("[2] [A]dd a new Location");
    System.out.println("[3] [R]emove a Location");
    System.out.println("[4] [U]pdate a Location");
    System.out.println("[5] Return to [M]ain Menu");
    try {
      switch (getChar()) {
        case '1':
        case 'l':
          viewSuppliers(0, supplier_name, null);
          break;
        case '2':
        case 'a': {
          System.out.println(String.format("Enter a new location for supplier[%s]:", supplier_name));
          String location = getString();
          db.addSupplier(supplier_name, location);
        }
          break;
        case '3':
        case 'r': {
          System.out.println(String.format("Enter supplier_id to remove for supplier[%s]:", supplier_name));
          int id = getInt();
          db.removeSupplierById(id);
        }
          break;
        case '4':
        case 'u': {
          System.out.println(String.format("Enter supplier_id to update for supplier[%s]:", supplier_name));
          int id = getInt();
          SupplierData supplier = db.getSupplierById(id);
          if (supplier != null && supplier.supplier_name.contains(supplier_name)) {
            System.out.println("Current location: " + supplier.location);
            System.out.println("Enter new location: ");
            String location = getString();
            db.updateSupplier(id, supplier.supplier_name, location);
          }
        }
          break;
        case '5':
        case 'm':
          userMenu();
          break;
        default:
          System.out.println("Invalid choice");
          break;
      }
    } catch (Exception e) {
      System.out.println("Exception: Invalid choice");
    }
  }

  private static void viewSuppliers(int supplier_id, String supplier_name, String location) {
    ArrayList<SupplierData> suppliers = new ArrayList<>();
    if (supplier_name != null)
      suppliers = db.getSupplierbyName(supplier_name);
    else if (location != null)
      suppliers = db.getSupplierByLocation(location);
    else if (supplier_id > 0)
      suppliers.add(db.getSupplierById(supplier_id));
    else if (location == null && supplier_name == null)
      suppliers = db.getAllSuppliers();
    if (suppliers.size() > 0)
      printSuppliers(suppliers);
  }

  private static void viewComponents(String object) {
    // TODO Auto-generated method stub
    System.out.println("Unimplemented method 'viewComponents'");
  }

  private static ArrayList<String> InitalizeTable() {
    ArrayList<String> table = new ArrayList<>();
    table.add("store");
    table.add("supplier");
    table.add("product");
    table.add("shipment");
    table.add("manufacturing");
    return table;
  }
}
