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
      }
    } catch (Exception e) {
      System.out.println("Exception");
    }
    userMenu();
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
          viewProducts("");
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
      }
    } catch (Exception e) {
      System.out.println("Exception");
    }
    managerMenu();
  }

  private static void getSupplierData() {
    System.out.println("-------------------------");
    System.out.println("[1] Get [A]ll suppliers");
    System.out.println("[2] [F]ilter suppliers");
    System.out.println("[3] Return to previous [M]enu");
    try {
      switch (getChar()) {
        case '1':
        case 'a':
          viewSuppliers(0, null, null);
          break;
        case '2':
        case 'f': {
          System.out.println("Enter supplier id");
          int id = getInt();
          if (id > 0)
            viewSuppliers(id, null, null);
          else {
            String name;
            String location;
            System.out.println("Enter supplier name (or n/a to skip)");
            name = getString();
            if (name.equals("n/a")) {
              name = "";
            }
            System.out.println("Enter supplier location (or n/a to skip)");
            location = getString();
            if (location.equals("n/a")) {
              location = "";
            }
            viewSuppliers(0, name, location);
          }
        }
          break;
        case '3':
        case 'm':
          managerMenu();
          break;
        default:
          System.out.println("Invalid choice");
          break;
      }
    } catch (Exception e) {
      System.out.println("Exception");
    }
    getSupplierData();
  }

  private static char getChar() {
    System.out.print("> ");
    if (scanner.hasNext()) {
      char[] choice = scanner.next().toLowerCase().toCharArray();
      scanner.nextLine();
      return choice[0];
    }
    return '\0';
  }

  /**
   * View shipments by destination, supplier, or product
   * For suppliers, we only get shipments from that supplier
   * 
   * @param supplier_name The name of the supplier
   * @return void
   */
  private static void viewShipments(String supplier_name) {
    System.out.println("-------------------------");
    System.out.println("[1] View shipments by [D]estination");
    System.out.println("[2] View shipments by [S]upplier");
    System.out.println("[3] [C]onfigure Shipments");
    System.out.println("[4] [R]escind a Shipment");
    System.out.println("[5] Return to [M]ain menu");
    ArrayList<ShipmentData> shipments = new ArrayList<>();
    try {
      switch (getChar()) {
        case '1':
        case 'd': {// Get only the shipments by destination from supplier_name
          System.out.println("Enter destination id (-1 to skip):");
          int destination_id = getInt();
          shipments = db.getShipmentsByDest(destination_id, supplier_name);
        }
          break;
        case '2':
        case 's': {
          // If supplier_name is null, we can choose any supplier or skip
          if (supplier_name == null || supplier_name.isEmpty()) {
            System.out.println("Enter supplier name (n/a to skip):");
            supplier_name = getString();
            if (supplier_name.equals("n/a")) {
              supplier_name = "";
            }
          }
          System.out.println("Enter supplier id (-1 to skip):");
          int supplier_id = getInt();
          if (supplier_id == -1) {
            shipments = db.getShipmentsBySupplier(-1, supplier_name);
          } else {
            // Check if the supplier_id matches the supplier name (if it exists)
            SupplierData supplier = db.getSupplierById(supplier_id);
            if (checkSupplier(supplier_name, supplier)) {
              shipments = db.getShipmentsBySupplier(supplier_id, supplier_name);
            } else {
              System.out.println("Supplier id does not match supplier name");
            }
          }
        }
          break;
        case '3':
        case 'c':
          configureShipment(supplier_name);
          break;
        case '4':
        case 'r': {
          // We can only cancel shipments by destination.
          // Ask for shipment id first.
          // so destination id must match supplier if supplier name is not empty
          // else destination id must match store_id
          if (supplier_name == null || supplier_name.isEmpty()) {
            // A shipment to a store

          } else {
            // A shipment to a supplier

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
      System.out.println("Exception");
    }
    if (shipments.size() > 0)
      printShipments(shipments, false);
    viewShipments(supplier_name);
  }

  /**
   * Detailed shipments menu
   * Create a shipment
   * Add a product to a shipment
   * Remove a product from a shipment
   * Update a shipment
   * 
   * @param supplier_name
   */
  private static void configureShipment(String supplier_name) {
    System.out.println("-------------------------");
    System.out.println("[1] Create a new [S]hipment");
    System.out.println("[2] [A]dd a Product to a shipment");
    System.out.println("[3] [R]emove a Product from a shipment");
    System.out.println("[4] [U]pdate a Shipment");
    System.out.println("[5] Return to previous [M]enu");
    try {
      switch (getChar()) {
        case '1':
        case 's': {

          System.out.println("Enter destination id:");
          int destination_id = getInt();
          // Get shipment date
          String ship_date = createDate();
          // Get arrival date
          String arrive_date = createDate();
          System.out.println("Enter supplier id:");
          int supplier_id = getInt();
          SupplierData supplier = db.getSupplierById(supplier_id);
          if (checkSupplier(supplier_name, supplier)) {
            db.addShipmentLog(destination_id, ship_date, arrive_date);
          } else {
            System.out.println("Supplier id does not match supplier name");
          }
        }
          break;
        case '2':
        case 'a': {
          System.out.println("Enter shipment id:");
          int shipment_id = getInt();
          System.out.println("Enter product id:");
          int product_id = getInt();
          System.out.println("Enter quantity:");
          float quantity = getFloat(2);
          System.out.println("Enter supplier id:");
          int supplier_id = getInt();
          SupplierData supplier = db.getSupplierById(supplier_id);
          if (checkSupplier(supplier_name, supplier)) {
            db.addProductToShipment(shipment_id, product_id, supplier_id, quantity);
          } else {
            System.out.println("Supplier id does not match supplier name");
          }
        }
          break;
        case '3':
        case 'r': {
          System.out.println("Enter shipment id:");
          int shipment_id = getInt();
          System.out.println("Enter product id:");
          int product_id = getInt();
          System.out.println("Enter supplier id:");
          int supplier_id = getInt();
          SupplierData supplier = db.getSupplierById(supplier_id);
          if (checkSupplier(supplier_name, supplier)) {
            db.deleteProductFromShipment(shipment_id, product_id, supplier_id);
          } else {
            System.out.println("Supplier id does not match supplier name");
          }
        }
          break;
        case '4':
        case 'u': {
          // update the shipment log given the shipment id.
          // Can update destination, ship_date, arrive_date
          // Supplier name must match the supplier_id
          System.out.println("Enter shipment id:");
          int shipment_id = getInt();
          System.out.println("Enter destination id:");
          int destination_id = getInt();
          System.out.println("Enter ship date:");
          String ship_date = getString();
          System.out.println("Enter arrive date:");
          String arrive_date = getString();
          System.out.println("Enter supplier id:");
          int supplier_id = getInt();
          SupplierData supplier = db.getSupplierById(supplier_id);
          if (checkSupplier(supplier_name, supplier)) {
            db.updateShipmentLog(shipment_id, destination_id, ship_date, arrive_date, supplier_id);
          } else {
            System.out.println("Supplier id does not match supplier name");
          }
        }
          break;
        case '5':
        case 'm':
          viewShipments(supplier_name);
          break;
        default:
          System.out.println("Invalid choice");
          break;
      }
    } catch (Exception e) {
      System.out.println("Exception");
    }
    configureShipment(supplier_name);
  }

  private static String createDate() {
    // Ask for date in format yyyy-mm-dd. If not, use current date
    // Loop until we have a valid date
    boolean valid = false;
    while (!valid) {
      System.out.println("Enter ship date (yyyy-mm-dd) or n/a to skip:");
      String date = getString();
      // check for correct format
      if (date.equals("n/a")) {
        return java.time.LocalDateTime.now().toString();
      } else if (date.matches("\\d{4}-\\d{2}-\\d{2}")) {
        return date;
      }
      System.out.println("Invalid date format. Please enter in yyyy-mm-dd format.");
    }
    return java.time.LocalDateTime.now().toString();
  }

  private static float getFloat(int precision) {
    float x = scanner.nextFloat();
    // round to decimals
    x = Math.round(x * Math.pow(10, precision)) / (float) Math.pow(10, precision);
    scanner.nextLine();
    return x;
  }

  private static void printShipments(ArrayList<ShipmentData> shipments, boolean isSimple) {
    if (!isSimple) {
      String format = "%-5s %-10s %-10s %-10s %-10s %-20s %-10s %-10s";
      System.out
          .println(
              String.format(format, "ID", "Dest. ID", "Ship Date", "Arrive Date", "Supplier ID", "Supplier Name",
                  "Product ID",
                  "Quantity"));
      for (ShipmentData shipment : shipments) {
        System.out.println(String.format(format, shipment.shipment_id, shipment.to_id, shipment.ship_date,
            shipment.supplier.supplier_id, shipment.product_id, shipment.quantity));
      }
    } else {
      String format = "%-5s %-10s %-10s %-10s";
      System.out.println(String.format(format, "ID", "Destination", "Ship Date", "Arrive Date"));
      for (ShipmentData shipment : shipments) {
        System.out.println(
            String.format(format, shipment.shipment_id, shipment.to_id, shipment.ship_date, shipment.arrive_date));
      }
    }
  }

  private static void viewProducts(String supplier_name) {
    System.out.println("-------------------------");
    System.out.println("[1] View all [P]roducts");
    System.out.println("[2] View products by [N]ame");
    System.out.println("[3] View products by Product [I]d");
    System.out.println("[4] View products by [S]upplier Id");
    System.out.println("[5] Return to [M]ain menu");
    ArrayList<ProductData> products = new ArrayList<>();
    try {
      switch (getChar()) {
        case '1':
        case 'p': {
          products = db.getProductByName("", supplier_name);
        }
          break;
        case '2':
        case 'n': {
          System.out.println("Enter product name (n/a to skip):");
          String name = getString();
          if (name.equals("n/a")) {
            name = "";
          }
          products = db.getProductByName(name, supplier_name);
        }
          break;
        case '3':
        case 'i': {
          System.out.println("Enter product id (-1 to skip):");
          int id = getInt();
          if (id == -1) {
            products = db.getProductByName("", supplier_name);
          } else if (supplier_name == null || supplier_name.isEmpty()) {
            // If null, we can get all the products from any supplier
            products = db.getProductByProductId(id, null);
          } else {
            // else, we only get it from the supplier name
            products = db.getProductByProductId(id, supplier_name);
          }
        }
          break;
        case '4':
        case 's':
          System.out.println("Enter supplier id (-1 to skip):");
          int supplier_id = getInt();
          if (supplier_id == -1) {
            products = db.getProductByName("", supplier_name);
          } else if (supplier_name == null) {
            // If null, we can get all the products from that one supplier
            products = db.getProductBySupplierId(supplier_id);
          } else {
            // else, we must verify that the supplier_id matches the supplier_name
            SupplierData supplier = db.getSupplierById(supplier_id);
            if (checkSupplier(supplier_name, supplier))
              products = db.getProductBySupplierId(supplier_id);
            else
              System.out.println("Supplier id does not match supplier name");
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
      System.out.println("Exception");
    }
    if (products.size() > 0)
      printProducts(products, false);
    viewProducts(supplier_name);
  }

  private static void printProducts(ArrayList<ProductData> products, boolean isSimple) {
    if (!isSimple) {
      String format = "%-5s %-20s %-20s %-20s %-10s %-10s";
      System.out.println(String.format(format, "ID", "Name", "Supplier", "Supplier ID", "Price", "Unit Type"));
      for (ProductData product : products) {
        System.out
            .println(String.format(format, product.product_id, product.product_name, product.supplier.supplier_name,
                product.supplier.supplier_id,
                product.price, product.unit_type));
      }
    } else {
      String format = "%-5s %-20s";
      System.out.println(String.format(format, "ID", "Name"));
      for (ProductData product : products) {
        System.out.println(String.format(format, product.product_id, product.product_name));
      }
    }
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
      System.out.println("Exception");
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
    System.out.println("[5] Return to [M]ain Menu");
    try {
      switch (getChar()) {
        case '1':
        case 'l':
          manageSupplier(supplier_name);
          break;
        case '2':
        case 'p':
          productMenu(supplier_name);
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
          break;
      }
    } catch (Exception e) {
      System.out.println("Exception");
    }
    supplierMenu(supplier_name);
  }

  private static void productMenu(String supplier_name) {
    System.out.println("-------------------------");
    System.out.println("[1] View [P]roducts");
    System.out.println("[2] View Product Log");
    System.out.println("[3] [A]dd a new product");
    System.out.println("[4] [R]emove a product");
    System.out.println("[5] [U]pdate a product");
    System.out.println("[6] Return to [M]ain Menu");
    switch (getChar()) {
      case '1':
      case 'p': {
        viewProducts(supplier_name);
      }
        break;
      case '2':
      case 'l': {
        // We are only getting simple products, not the full product information.
        // We don't need supplier information
        ArrayList<ProductData> products = new ArrayList<>();
        System.out.println("Enter product id (-1 to enter product name instead):");
        int id = getInt();
        if (id == -1) { // We choose to use name instead of id
          System.out.println("Enter product name (or n/a to skip):");
          String name = getString();
          if (name.equals("n/a")) { // We don't give a name
            products = db.getProductLog(id, "");
          } else { // We have a product_name
            products = db.getProductLog(id, name);
          }
        } else { // We have a product_id
          products = db.getProductLog(id, "");
        }
        printProducts(products, true);
      }
        break;
      case '3':
      case 'a': {
        // We need to get the supplier id first and check if it matches the name
        System.out.println("Enter supplier id:");
        int supplier_id = getInt();
        SupplierData supplier = db.getSupplierById(supplier_id);
        if (checkSupplier(supplier_name, supplier)) { // Check if the supplier_id matches the name
          System.out.println("Enter product name:");
          String product_name = getString();
          int product_id;
          // Make sure that the product doesn't already exist. If it does, we can allow
          // the supplier to select the id of current products, or -1 to create a new one.
          ArrayList<ProductData> products = db.getProductLog(-1, product_name);
          printProducts(products, true);
          if (products.size() > 0) {
            System.out
                .println("Product already exists. Enter product id to use, or -1 to create a new one.");
            product_id = getInt();
            if (product_id == -1) { // Create a new product
              product_id = db.addProductLog(product_name);
            } else {
              // Change the product name to match the existing product given the id
              // product_id may not match index of products
              for (ProductData product : products) {
                if (product.product_id == product_id) {
                  product_name = product.product_name;
                  break;
                }
              }
            }
          } else {
            product_id = db.addProductLog(product_name);
          }
          System.out.println("Enter price:");
          float price = getFloat(2);
          scanner.nextLine();
          System.out.println("Enter unit type:");
          String unit_type = getString();
          db.addProductFromSupplier(product_id, supplier_id, price, unit_type);
        } else {
          System.out.println("Supplier id does not match supplier name");
        }

      }
        break;
      case '4':
      case 'r': {
        System.out.println("Enter product id:");
        int product_id = getInt();
        System.out.println("Enter supplier id:");
        int supplier_id = getInt();
        SupplierData supplier = db.getSupplierById(supplier_id);
        if (checkSupplier(supplier_name, supplier)) {
          db.deleteProduct(product_id, supplier_id);
        } else {
          System.out.println("Supplier id does not match supplier name");
        }
      }
        break;
      case '5':
      case 'u':
        System.out.println("Enter product id:");
        int product_id = getInt();
        System.out.println("Enter supplier id:");
        int supplier_id = getInt();
        SupplierData supplier = db.getSupplierById(supplier_id);
        if (checkSupplier(supplier_name, supplier)) {
          System.out.println("Enter new price:");
          float price = getFloat(2);
          scanner.nextLine();
          System.out.println("Enter new unit type:");
          String unit_type = getString();
          try {
            db.updateProduct(product_id, supplier_id, price, unit_type);
          } catch (Exception e) {
            System.out.println("Invalid product id. Cannot update product.");
          }
        } else {
          System.out.println("Supplier id does not match supplier name");
        }
        break;
      case '6':
      case 'm':
        userMenu();
        break;
      default:
        System.out.println("Invalid choice");
        break;
    }
    productMenu(supplier_name);
  }

  private static boolean checkSupplier(String supplier_name, SupplierData supplier) {
    return supplier != null && supplier.supplier_name.contains(supplier_name);
  }

  private static void manageSupplier(String supplier_name) {
    System.out.println("-------------------------");
    System.out.println("[1] View my [L]ocations");
    System.out.println("[2] [A]dd a new Location");
    System.out.println("[3] [R]emove a Location");
    System.out.println("[4] [U]pdate a Location");
    System.out.println("[5] Return to Supplier [M]enu");
    try {
      switch (getChar()) {
        case '1':
        case 'l': {
          viewSuppliers(0, supplier_name, "");
        }
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
          SupplierData supplier = db.getSupplierById(id);
          if (checkSupplier(supplier_name, supplier))
            db.removeSupplierById(id);
        }
          break;
        case '4':
        case 'u': {
          System.out.println(String.format("Enter supplier_id to update for supplier[%s]:", supplier_name));
          int id = getInt();
          SupplierData supplier = db.getSupplierById(id);
          if (checkSupplier(supplier_name, supplier)) {
            System.out.println("Current location: " + supplier.location);
            System.out.println("Enter new location: ");
            String location = getString();
            int count = db.updateSupplier(id, supplier.supplier_name, location);
            if (count < 1) {
              System.out.println("Failed to update product location");
            }
          }
        }
          break;
        case '5':
        case 'm':
          supplierMenu(supplier_name);
          break;
        default:
          System.out.println("Invalid choice");
          break;
      }
    } catch (Exception e) {
      System.out.println("Exception");
    }
    manageSupplier(supplier_name);
  }

  private static void viewSuppliers(int supplier_id, String supplier_name, String location) {
    ArrayList<SupplierData> suppliers = new ArrayList<>();
    if (supplier_id > 0) {
      suppliers.add(db.getSupplierById(supplier_id));
    } else {
      if (supplier_name == null)
        supplier_name = "";
      if (location == null)
        location = "";
      suppliers = db.getSupplierByLocationAndName(location, supplier_name);
    }
    if (suppliers.size() > 0) {
      printSuppliers(suppliers);
    }
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
    table.add("building");
    table.add("product_ship");
    return table;
  }
}
