import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;

public class App {
  static Scanner scanner = new Scanner(System.in);
  static Database db;

  public static void main(String[] args) {

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
      return;
    }
    userMenu();
  }

  static void userMenu() {
    System.out.println("Welcome to the database!");
    System.out.println("[1] I am a store [M]anager");
    System.out.println("[2] I am a [S]upplier");
    System.out.println("[3] [E]xit");
    System.out.println("[4] Generate random data");
    System.out.print("> ");
    try {
      char[] choice = getChoice();
      switch (choice[0]) {
        case '1':
        case 'm':
          managerMenu();
          break;
        case '2':
        case 's':
          supplierMenu();
          break;
        case '3':
        case 'e':
          System.exit(0);
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
    }
    userMenu();
  }

  private static void generateData() {
    System.out.println("Generating random data...");
    for (int i = 0; i < 20; i++) {
      // Generate a random store, supplier location
      // Generate a random length for string
      java.security.SecureRandom secureRandom = new java.security.SecureRandom();
      Integer randomLength = secureRandom.nextInt(10) + 10;
      String location = UUID.randomUUID().toString().substring(0, randomLength);
      String supplier_name = UUID.randomUUID().toString().substring(0, randomLength);
      db.addStore(location);
      db.addStore(location);
      db.addSupplier(supplier_name, location);
      db.addSupplier(supplier_name, location);
    }
  }

  static void managerMenu() {
    System.out.println("Welcome to the store manager menu!");
    System.out.println("[1] View/Manage my store [L]ocations");
    System.out.println("[2] View [S]uppliers");
    System.out.println("[3] View [P]roducts");
    System.out.println("[4] View/Manage my S[h]ipments");
    System.out.println("[5] [R]eturn to main menu");
    System.out.print("> ");
    try {
      char[] choice = getChoice();
      switch (choice[0]) {
        case '1':
        case 'l':
          manageStoreLocations();
          break;
        case '2':
        case 's':
          viewSuppliers(null);
          break;
        case '3':
        case 'p':
          viewProducts(null);
          break;
        case '4':
        case 'h':
          viewShipments(null);
          break;
        case '5':
        case 'r':
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

  private static char[] getChoice() {
    char[] choice = scanner.nextLine().toLowerCase().toCharArray();
    return choice;
  }

  private static void viewShipments(String input) {
    // TODO Auto-generated method stub
    System.out.println("Unimplemented method 'viewShipments'");
  }

  private static void viewProducts(String input) {
    // TODO Auto-generated method stub
    System.out.println("Unimplemented method 'viewProducts'");
  }

  private static void viewSuppliers(String input) {
    if (input == null) {
      System.out.println("Viewing all suppliers");
      ArrayList<SupplierData> suppliers = new ArrayList<>();
      suppliers = db.getAllSuppliers();
      for (SupplierData supplier : suppliers) {
        System.out.println(supplier);
      }
    } else {
      // TODO Auto-generated method stub
      System.out.println("Unimplemented method 'viewSuppliers'");
    }
  }

  private static void manageStoreLocations() {
    System.out.println("-------------------------");
    System.out.println("[1] View store locations");
    System.out.println("[2] Add store location");
    System.out.println("[3] Remove store location");
    System.out.println("[4] Update store location");
    System.out.println("[5] Return to main menu");
    System.out.print("> ");
    try {
      char[] choice = getChoice();
      switch (choice[0]) {
        case '1': {
          ArrayList<StoreData> stores = new ArrayList<>();
          System.out.println("Select store id/location to view. Enter -1 to view all stores: ");
          // Check if input is an int or string
          if (scanner.hasNextInt()) {
            int storeId = scanner.nextInt();
            if (storeId == -1) {
              stores = db.getAllStores();
              printStores(stores);
            } else {
              System.out.println(db.getStoreById(storeId));
            }
          } else {
            String location = scanner.nextLine();
            stores = db.getStoreByLocation(location);
            printStores(stores);
          }
        }
          break;
        case '2':
          db.addStore(null);
          break;
        case '3': {
          System.out.print("Enter store id: ");
          char[] store_id = getChoice();
          db.deleteStoreById(Integer.parseInt(store_id.toString()));
        }
          break;
        case '4': {
          System.out.print("Enter store id: ");
          int store_id = Integer.parseInt(getChoice().toString());
          System.out.print("Enter location: ");
          String location = getChoice().toString();
          db.updateStoreLocation(store_id, location);
        }
          break;
        case '5':
          userMenu();
          break;
        default:
          System.out.println("Invalid choice");
          manageStoreLocations();
      }
    } catch (Exception e) {
      System.out.println("Exception: Invalid choice");
    }
    manageStoreLocations();
  }

  private static void printStores(ArrayList<StoreData> stores) {
    String format = "%-5s %-20s";
    System.out.println(String.format(format, "ID", "Location"));
    for (StoreData store : stores) {
      System.out.println(String.format(format, store.store_id, store.location));
    }
  }

  static void supplierMenu() {
    System.out.println("Welcome to the supplier menu!");
    System.out.println("[1] View/Manage my [L]ocations");
    System.out.println("[2] View/Manage my [P]roduct");
    System.out.println("[3] View/Manage my [S]hipments");
    System.out.println("[4] View/Manage/Recall my [M]anufacturing components");
    System.out.println("[5] [R]eturn to main menu");
    System.out.print("> ");
    try {
      char[] choice = getChoice();
      switch (choice[0]) {
        case '1':
        case 'l':
          viewSuppliers(null);
          break;
        case '2':
        case 'p':
          viewProducts(null);
          break;
        case '3':
        case 's':
          viewShipments(null);
          break;
        case '4':
        case 'm':
          viewComponents(null);
          break;
        case '5':
        case 'r':
          userMenu();
          break;
        default:
          System.out.println("Invalid choice");
          supplierMenu();
      }
    } catch (Exception e) {
      System.out.println("Exeption: Invalid choice");
    }
    supplierMenu();
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
