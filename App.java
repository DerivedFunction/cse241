import java.io.Console;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;

/**
 * Main App function for user interface
 */
public class App {
  static Scanner scanner = new Scanner(System.in);
  static Database db;

  public static void main(String[] args) {
    Log.info("Logging enabled");
    String dbid = "edgar1.cse.lehigh.edu";
    String port = "1521";
    String path = "cse241";
    System.out.print("Enter your username: ");
    String userid = scanner.next();
    Console console = System.console();
    String passwd = new String(console.readPassword("Enter your password: "));
    scanner.nextLine();

    ArrayList<String> table = InitalizeTable();
    int attempts = 0;
    while (attempts < 3) {
      db = Database.getDatabase(dbid, port, path, userid, passwd, table);
      if (db != null) {
        break;
      }
      System.out.println("Failed to connect to database. Please try again.");
      attempts++;
      System.out.print("Enter your username: ");
      userid = scanner.next();
      passwd = new String(console.readPassword("Enter your password: "));
      scanner.nextLine();
    }

    if (db == null) {
      System.out.println("Failed to connect to database after 3 attempts");
      scanner.close();
      return;
    }
    userMenu();
  }

  /**
   * Initial Menu after logging in.
   */
  static void userMenu() {
    System.out.println("-------------------------");
    System.out.println("[1] I am a Store [M]anager");
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
      Log.printStackTrace(e);
    }
    userMenu();
  }

  /**
   * Disconnects the database and gracefully exits
   */
  private static void exitMenu() {
    System.out.println("Database disconnected.");
    System.exit(0);
  }

  /**
   * Generates random data and populates the SQL tables.
   */
  private static void generateData() {
    System.out.println("Confirm data genration with (!random): ");
    String name = getString();
    String location;
    if (name.equals("!random")) {
      ArrayList<StoreData> stores = new ArrayList<>();
      ArrayList<SupplierData> suppliers = new ArrayList<>();
      ArrayList<ProductData> products = new ArrayList<>();
      for (int i = 0; i < 5; i++) {
        Log.info("Generating random data...");
        // Generate a random store, supplier location
        // Generate a random length for string
        java.security.SecureRandom secureRandom = new java.security.SecureRandom();
        Integer randomLength = secureRandom.nextInt(10) + 10;
        location = UUID.randomUUID().toString().substring(0, randomLength);
        String supplier_name = UUID.randomUUID().toString().substring(0, randomLength);
        stores.add(db.getStoreById(db.addStore(location)));
        stores.add(db.getStoreById(db.addStore(location)));
        suppliers.add(db.getSupplierById(db.addSupplier(supplier_name, location)));
        suppliers.add(db.getSupplierById(db.addSupplier(supplier_name, location)));
      }

      for (int i = 0; i < 5; i++) {
        // Generate a random product name
        java.security.SecureRandom secureRandom = new java.security.SecureRandom();
        Integer randomLength = secureRandom.nextInt(10) + 10;
        String product_name = UUID.randomUUID().toString().substring(0, randomLength);
        int product_id = db.addProductLog(product_name);
        ProductData product = db.getProductLog(product_id, null).get(0);
        Log.info(product.toString());
        products.add(product);
      }
      // Now create a shipment
      int shipment_id = db.addShipmentLog(stores.get(0).store_id, "2021-01-01 00:00", "2025-01-01 00:00");
      Log.info("Shipment id: " + shipment_id);
      for (ProductData product : products) {
        Log.info("Adding product to shipment..." + product.toString());
        // add product from supplier
        db.addProductFromSupplier(product.product_id, suppliers.get(0).supplier_id, 10.0f, "kg");
        Log.info("Product id: " + product.product_id + " Supplier id: " + suppliers.get(0).supplier_id);
        db.addProductFromSupplier(product.product_id, suppliers.get(1).supplier_id, 10.0f, "lb");
        db.addProductToShipment(shipment_id, product.product_id, suppliers.get(0).supplier_id, 10.0f);
        db.addProductToShipment(shipment_id, product.product_id, suppliers.get(1).supplier_id, 5.0f);
      }
      // Now add some random components to a product
      for (ProductData product : products) {
        for (int i = 0; i < 5; i++) {
          java.security.SecureRandom secureRandom = new java.security.SecureRandom();
          Integer randomLength = secureRandom.nextInt(10) + 10;
          String component_name = UUID.randomUUID().toString().substring(0, randomLength);
          int component_id = db.addManufacturing(product.product_id, suppliers.get(0).supplier_id, component_name);
          Log.info("Component id: " + component_id);
        }
      }
    }
    userMenu();
  }

  /**
   * Menu for store managers
   */
  static void managerMenu() {
    System.out.println("-------------------------");
    System.out.println("[1] View/Manage my Store [L]ocations");
    System.out.println("[2] View [S]uppliers");
    System.out.println("[3] View [P]roducts");
    System.out.println("[4] View/Manage my S[h]ipments");
    System.out.println("[5] View [C]omponents");
    System.out.println("[6] Return to [M]ain menu");
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
          viewProducts(null);
          break;
        case '4':
        case 'h':
          viewShipments(null);
          break;
        case '5':
        case 'c':
          viewComponents(null);
          break;
        case '6':
        case 'm':
          userMenu();
          break;
        default:
          System.out.println("Invalid choice");
      }
    } catch (Exception e) {
      Log.printStackTrace(e);
    }
    managerMenu();
  }

  /**
   * Viewing suppliers as a store manager
   */
  private static void getSupplierData() {
    System.out.println("-------------------------");
    System.out.println("[1] Get [A]ll Suppliers");
    System.out.println("[2] [F]ilter Suppliers");
    System.out.println("[3] Return to Previous [M]enu");
    try {
      switch (getChar()) {
        case '1':
        case 'a':
          viewSuppliers(0, null, null);
          break;
        case '2':
        case 'f': {
          System.out.println("Enter supplier id (-1 to skip)");
          int id = getInt();
          if (id > 0)
            viewSuppliers(id, null, null);
          else {
            String name;
            String location;
            System.out.println("Enter supplier name");
            name = checkForNA();
            System.out.println("Enter supplier location");
            location = checkForNA();
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
      Log.printStackTrace(e);
    }
    getSupplierData();
  }

  private static String checkForNA() {
    System.out.println("(or n/a to skip)");
    String name;
    name = getString();
    if (name.equals("n/a")) {
      name = "";
    }
    return name;
  }

  /**
   * Gets one char (lowercase format)
   * 
   * @return the first character from user input
   */
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
   */
  private static void viewShipments(String supplier_name) {
    System.out.println("-------------------------");
    System.out.println("[0] View Shipment's products using Shipment [I]d");
    System.out.println("[1] View Shipments by [D]estination");
    System.out.println("[2] View Shipments by [S]upplier");
    System.out.println("[3] [C]onfigure Shipments");
    System.out.println("[4] [R]escind a Shipment");
    System.out.println("[5] Return to Previous [M]enu");
    Log.info(supplier_name);
    ArrayList<ShipmentData> shipments = new ArrayList<>();
    try {
      switch (getChar()) {
        case '0': {
          // We can only get shipments if the destination is a store when supplier_name is
          // null.
          // If the destination is a supplier, we can get the products from the shipment
          // from the supplier name
          System.out.println("Enter shipment id:");
          int shipment_id = getInt();
          if (shipment_id < 0) {
            System.out.println("Invalid shipment id");
            break;
          }
          // Lets get all the shipments
          shipments = db.getShipmentsByDest(-1, supplier_name);
          ArrayList<ShipmentData> data = new ArrayList<>();
          // Lets check if the shipment_id is in the list
          // If it is, we can get the products from that shipment
          for (ShipmentData shipment : shipments) {
            if (shipment.shipment_id == shipment_id) {
              data = db.getProductsFromShipmentId(shipment_id);
              break;
            }
          }
          printShipments(data, false);
        }
          break;
        case '1':
        case 'd': {// Get only the shipments by destination from supplier_name
          System.out.println("Enter destination id (-1 to skip):");
          int destination_id = getInt();
          shipments = db.getShipmentsByDest(destination_id, supplier_name);
          printShipments(shipments, true);
        }
          break;
        case '2':
        case 's': {
          String temp_supplier_name = supplier_name;
          // If supplier_name is null, we can choose any supplier or skip
          if (isStore(supplier_name)) {
            System.out.println("Enter supplier name");
            temp_supplier_name = checkForNA();
          }
          int supplier_id = checkSupplier(supplier_name, true);
          if (supplier_id == 0) {
            shipments = db.getShipmentsBySupplier(-1, temp_supplier_name);
          } else if (supplier_id > 0) {
            shipments = db.getShipmentsBySupplier(supplier_id, temp_supplier_name);
            printShipments(shipments, true);
          }
        }
          break;
        case '3':
        case 'c':
          if (isStore(supplier_name)) {
            System.out.println("Not a supplier. Cannot configure shipments");
          } else {
            configureShipment(supplier_name);
          }
          break;
        case '4':
        case 'r': {
          // We can only cancel shipments by destination in the shipment log
          // Ask for shipment id first. Then we can cancel the shipment
          ArrayList<ShipmentData> shipmentsToCancel = new ArrayList<>();
          if (isStore(supplier_name)) {
            // A shipment to a store
            shipmentsToCancel = db.getShipmentToStore();
          } else {
            // A shipment to a supplier
            shipmentsToCancel = db.getShipmentToSupplier(supplier_name);
          }
          // Now check if we have any shipments to cancel, and enter the shipment id that
          // is in the list
          if (shipmentsToCancel.size() > 0) {
            printShipments(shipmentsToCancel, true);
            System.out.println("Enter shipment id to cancel:");
            int shipment_id = getInt();
            db.deleteShipmentLog(shipment_id);
          } else {
            System.out.println("No shipments to cancel");
          }
        }
          break;
        case '5':
        case 'm':
          goToMenu(supplier_name);
          break;
        default:
          System.out.println("Invalid choice");
          break;
      }
    } catch (

    Exception e) {
      Log.printStackTrace(e);
    }

    viewShipments(supplier_name);
  }

  /**
   * Detailed shipments menu
   * Create a shipment
   * Add a product to a shipment
   * Remove a product from a shipment
   * Update a shipment
   * 
   * @param supplier_name The name of the supplier
   */
  private static void configureShipment(String supplier_name) {
    System.out.println("-------------------------");
    System.out.println("[1] Create a New [S]hipment");
    System.out.println("[2] [A]dd a Product to a Shipment");
    System.out.println("[3] [R]emove a Product from a Shipment");
    System.out.println("[4] [U]pdate a Shipment");
    System.out.println("[5] Return to Previous [M]enu");
    try {
      switch (getChar()) {
        case '1':
        case 's': {
          int supplier_id = checkSupplier(supplier_name, false);
          if (supplier_id > 0) {
            System.out.println("Enter destination id:");
            int destination_id = getInt();
            // Get shipment date
            System.out.println("Enter ship date:");
            String ship_date = createDate();
            // Get arrival date
            System.out.println("Enter arrival date:");
            String arrive_date = createDate();
            int shipment_id = db.addShipmentLog(destination_id, ship_date, arrive_date);
            addProductShipment(supplier_id, shipment_id, supplier_name);
          }
        }
          break;
        case '2':
        case 'a': {
          int supplier_id = checkSupplier(supplier_name, false);
          if (supplier_id > 0) {
            addProductShipment(supplier_id, -1, supplier_name);
          }
        }
          break;
        case '3':
        case 'r': {

          int supplier_id = checkSupplier(supplier_name, false);
          if (supplier_id > 0) {
            System.out.println("Enter shipment id:");
            int shipment_id = getInt();
            System.out.println("Enter product id:");
            int product_id = getInt();
            db.deleteProductFromShipment(shipment_id, product_id, supplier_id);
          }
        }
          break;
        case '4':
        case 'u': {
          // update the shipment log given the shipment id.
          // Can update destination, ship_date, arrive_date
          // Supplier name must match the supplier_id

          int supplier_id = checkSupplier(supplier_name, false);
          if (supplier_id > 0) {
            System.out.println("Enter shipment id:");
            int shipment_id = getInt();
            System.out.println("Enter destination id:");
            int destination_id = getInt();
            System.out.println("Enter ship date:");
            String ship_date = createDate();
            System.out.println("Enter arrive date:");
            String arrive_date = createDate();
            db.updateShipmentLog(shipment_id, destination_id, ship_date, arrive_date, supplier_id);
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
      Log.printStackTrace(e);
      e.printStackTrace();
    }
    configureShipment(supplier_name);
  }

  /**
   * Bulk adding products to a certain shipment_id
   * 
   * @param supplier_id   The supplier id (loops until -1)
   * @param shipment_id   The specfic shipment
   * @param supplier_name The supplier name (checks for matching supplier_id)
   */
  private static void addProductShipment(int supplier_id, int shipment_id, String supplier_name) {
    System.out.println("Bulk adding products to shipment.");
    if (shipment_id < 0) {
      System.out.println("Enter shipment id:");
      shipment_id = getInt();
    }
    int count = 0;
    while (supplier_id > 0) {
      System.out.println("Enter product id:");
      int product_id = getInt();
      System.out.println("Enter quantity:");
      float quantity = getFloat(4);
      if (db.addProductToShipment(shipment_id, product_id, supplier_id, quantity) > 0)
        count++;
      supplier_id = checkSupplier(supplier_name, true);
    }
    System.out.println(String.format("%d products added", count));
  }

  /**
   * Ask for date in valid sql timestamp. If not, use current timestamp
   * Loop until we have a valid timestamp (in 12hr format)
   * yyyy-MM-dd HH:MI
   * 
   * @return timestamp String
   */
  private static String createDate() {

    boolean valid = false;
    String date = "2000-01-01 00:00";
    while (!valid) {
      System.out.println("Enter date (YYYY-MM-DD HH:MI) or n/a to skip:");
      date = getString();
      // check for correct format
      if (date.equals("n/a")) {
        date = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        valid = true;
      } else if (date.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")) {
        valid = true;
      } else if (date.matches("\\d{4}-\\d{2}-\\d{2}")) {
        date = date + " 00:00";
        valid = true;
      } else {
        System.out.println("Invalid date format. Please enter in yyyy-MM-dd HH:mm format.");
        valid = false;
      }
    }
    Log.info(date);
    return date;

  }

  private static float getFloat(int precision) {
    while (!scanner.hasNextFloat()) {
      System.out.println("Invalid input. Please enter a float:");
      scanner.nextLine();
    }
    float x = Float.parseFloat(scanner.nextLine());
    return Math.round(x * Math.pow(10, precision)) / (float) Math.pow(10, precision);
  }

  private static void printShipments(ArrayList<ShipmentData> shipments, boolean isSimple) {
    if (shipments.size() == 0) {
      if (!isSimple) {
        System.out.println("No products in shipment");
      } else {
        System.out.println("No shipments found");
      }
      return;
    }
    try {
      float total = 0;
      if (!isSimple) {
        String format = "%-5s %-10s %-20s %-20s %20s %-5s %20s %-5s %10s %-10s %-10s";
        System.out.println(
            String.format(format, "ID", "Dest. ID", "Ship Date", "Arrive Date",
                "Supplier Name", "ID",
                "Product Name", "ID", "Quantity", "Price", "UI"));
        for (ShipmentData shipment : shipments) {
          System.out.println(
              String.format(format, shipment.shipment_id, shipment.to_id, shipment.ship_date, shipment.arrive_date,
                  shipment.supplier.supplier_name, shipment.supplier.supplier_id,
                  shipment.product.product_name, shipment.product.product_id, shipment.quantity,
                  String.format("$%.2f", shipment.product.price),
                  shipment.product.unit_type));
          total += shipment.quantity * shipment.product.price;
        }
        System.out.println(String.format("Total: $%.2f", total));
      } else {
        String format = "%-5s %-10s %-20s %-20s";
        System.out.println(String.format(format, "ID", "Dest. ID", "Ship Date", "Arrive Date"));
        for (ShipmentData shipment : shipments) {
          System.out.println(
              String.format(format, shipment.shipment_id, shipment.to_id, shipment.ship_date, shipment.arrive_date));
        }
      }
    } catch (Exception e) {
      Log.printStackTrace(e);
      System.out.println("Error with results");
    }
  }

  /**
   * Menu for viewing products
   * Options to filter by id, name and supplier
   * 
   * @param supplier_name supplier name if it is a supplier
   */
  private static void viewProducts(String supplier_name) {
    System.out.println("-------------------------");
    System.out.println("[1] View All [P]roducts");
    System.out.println("[2] View Products by [N]ame");
    System.out.println("[3] View Products by Product [I]d");
    System.out.println("[4] View Products by [S]upplier Id");
    System.out.println("[5] Return to Previous [m]enu");
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
          System.out.println("Enter product name");
          String name = checkForNA();
          products = db.getProductByName(name, supplier_name);
        }
          break;
        case '3':
        case 'i': {
          System.out.println("Enter product id (-1 to skip):");
          int id = getInt();
          if (id < 0) {
            products = db.getProductByName("", supplier_name);
          } else if (isStore(supplier_name)) {
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
          int supplier_id = checkSupplier(supplier_name, true);
          if (supplier_id == 0) {
            products = db.getProductByName("", supplier_name);
          } else if (isStore(supplier_name)) {
            // If it is a store, we can get all the products from that one supplier
            // For any supplier id
            products = db.getProductBySupplierId(supplier_id);
          } else if (supplier_id > 0) {
            // Cannot view products from another supplier if they don't match
            products = db.getProductBySupplierId(supplier_id);
          }
          break;
        case '5':
        case 'm':
          goToMenu(supplier_name);
          break;
        default:
          System.out.println("Invalid choice");
          break;
      }
    } catch (Exception e) {
      Log.printStackTrace(e);
    }
    printProducts(products, false);
    viewProducts(supplier_name);
  }

  /**
   * Pretty prints the data from products
   * 
   * @param products ArrayList of ProductData
   * @param isSimple Prints only id and name if true
   */
  private static void printProducts(ArrayList<ProductData> products, boolean isSimple) {
    if (products.size() == 0) {
      System.out.println("No Products found");
      return;
    }
    if (!isSimple) {
      String format = "%-5s %-20s %20s %-5s %-10s %-10s";
      System.out.println(String.format(format, "ID", "Name", "Supplier Name", "ID", "Price", "UI"));
      for (ProductData product : products) {
        System.out.println(String.format(format,
            product.product_id, product.product_name,
            product.supplier.supplier_name, product.supplier.supplier_id,
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

  /**
   * Store Manager Menu for managing locations
   */
  private static void manageStoreLocations() {
    System.out.println("-------------------------");
    System.out.println("[1] View Store [L]ocations");
    System.out.println("[2] [A]dd Store location");
    System.out.println("[3] [R]emove Store location");
    System.out.println("[4] [U]pdate Store location");
    System.out.println("[5] Return to Previous [M]enu");
    try {

      switch (getChar()) {
        case '1':
        case 'l': {
          ArrayList<StoreData> stores = new ArrayList<>();
          System.out.println("Select store id/location to view. Enter -1 to view all stores.");
          // Check if input is an int or string
          if (scanner.hasNextInt()) {
            int storeId = getInt();
            if (storeId < 0) {
              stores = db.getAllStores();
              printStores(stores);
            } else {
              StoreData store = db.getStoreById(storeId);
              if (store != null) {
                stores.add(store);
              }
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
          managerMenu();
          break;
        default:
          System.out.println("Invalid choice");
          break;
      }
    } catch (Exception e) {
      Log.printStackTrace(e);
    } finally {
      manageStoreLocations();
    }

  }

  /**
   * Gets string from user (lowercase only)
   * 
   * @return string
   */
  private static String getString() {
    String input = scanner.nextLine();
    while (input == null || input.trim().isEmpty()) {
      input = scanner.nextLine();
    }
    return input.toLowerCase();
  }

  /**
   * Gets integer from user input
   * 
   * @return integer
   */
  private static int getInt() {
    while (!scanner.hasNextInt()) {
      System.out.println("Invalid input. Please enter an integer:");
      scanner.nextLine();
    }
    int x = Integer.parseInt(scanner.nextLine());
    return x;
  }

  /**
   * Pretty Prints the stores by id and location
   * 
   * @param stores ArrayList of StoreData
   */
  private static void printStores(ArrayList<StoreData> stores) {
    if (stores.size() == 0) {
      System.out.println("No stores found");
      return;
    }
    String format = "%-5s %-20s";
    System.out.println(String.format(format, "ID", "Location"));
    for (StoreData store : stores) {
      System.out.println(String.format(format, store.store_id, store.location));
    }
  }

  /**
   * Pretty Prints the stores by id, name, and location
   * 
   * @param suppliers ArrayList of SupplierData
   */
  private static void printSuppliers(ArrayList<SupplierData> suppliers) {
    if (suppliers.size() == 0) {
      System.out.println("No suppliers found");
      return;
    }
    String format = "%-5s %-20s %-20s";
    System.out.println(String.format(format, "ID", "Name", "Location"));
    for (SupplierData supplier : suppliers) {
      System.out.println(String.format(format, supplier.supplier_id, supplier.supplier_name, supplier.location));
    }
  }

  /**
   * Supplier Menu. Once entered, all other menus are filtered
   * for this name of supplier only.
   * 
   * @param supplier_name user inputted name for supplier
   */
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
          viewShipments(supplier_name);
          break;
        case '4':
        case 'c':
          viewComponents(supplier_name);
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
      Log.printStackTrace(e);
    }
    supplierMenu(supplier_name);
  }

  /**
   * Allows suppliers to add, remove, update, or delete products
   * 
   * @param supplier_name supplier name
   */
  private static void productMenu(String supplier_name) {
    System.out.println("-------------------------");
    System.out.println("[1] View [P]roducts");
    System.out.println("[2] View Product Log");
    System.out.println("[3] [A]dd a new Product");
    System.out.println("[4] [R]emove a Product");
    System.out.println("[5] [U]pdate a Product");
    System.out.println("[6] Return to Previous [M]enu");
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
        if (id < 0) { // We choose to use name instead of id
          System.out.println("Enter product name");
          String name = checkForNA();
          products = db.getProductLog(id, name);
        } else { // We have a product_id
          products = db.getProductLog(id, "");
        }
        printProducts(products, true);
      }
        break;
      case '3':
      case 'a': {
        // We need to get the supplier id first and check if it matches the name
        int supplier_id = checkSupplier(supplier_name, false);
        if (supplier_id > 0) { // Check if the supplier_id matches the name
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
            if (product_id < 0) { // Create a new product
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
          float price = getFloat(4);
          scanner.nextLine();
          System.out.println("Enter unit type:");
          String unit_type = getString();
          db.addProductFromSupplier(product_id, supplier_id, price, unit_type);
        }
      }
        break;
      case '4':
      case 'r': {

        int supplier_id = checkSupplier(supplier_name, false);
        if (supplier_id > 0) {
          System.out.println("Enter product id:");
          int product_id = getInt();
          db.deleteProduct(product_id, supplier_id);
        }
      }
        break;
      case '5':
      case 'u':
        int supplier_id = checkSupplier(supplier_name, false);
        if (supplier_id > 0) {
          System.out.println("Enter product id:");
          int product_id = getInt();
          System.out.println("Enter new price:");
          float price = getFloat(4);
          scanner.nextLine();
          System.out.println("Enter new unit type:");
          String unit_type = getString();
          try {
            db.updateProduct(product_id, supplier_id, price, unit_type);
          } catch (Exception e) {
            System.out.println("Invalid product id. Cannot update product.");
          }
        }
        break;
      case '6':
      case 'm':
        goToMenu(supplier_name);
        break;
      default:
        System.out.println("Invalid choice");
        break;
    }
    productMenu(supplier_name);
  }

  /**
   * Checks if the user input of supplier id matches the given supplier name
   * A single supplier may have multiple locations, hence ids.
   * 
   * @param supplier_name name of supplier to reference
   * 
   * @return -1 if not found, 0 if skips, supplier_id if matches
   */
  private static int checkSupplier(String supplier_name, boolean skip) {
    int supplier_id = -1;
    if (skip) {
      System.out.println("Enter supplier id (-1 to skip): ");
      supplier_id = getInt();
      if (supplier_id < 0) {
        return 0;
      }
    } else {
      System.out.println("Enter supplier id: ");
      supplier_id = getInt();
      if (supplier_id < 0) {
        return -1;
      }
    }
    SupplierData supplier = db.getSupplierById(supplier_id);
    if (supplier == null) {
      System.out.println("Supplier not found");
      return -1;
    }
    if (supplier.supplier_name.contains(supplier_name)) {
      return supplier_id;
    } else {
      System.out.println("Supplier id does not match supplier name");
      return -1;
    }
  }

  /**
   * Menus use supplier_name to track suppliers
   * Store managers will have this argument either null or empty
   * 
   * @param name the string to check
   * @return true of null or empty
   */
  private static boolean isStore(String name) {
    return name == null || name.isEmpty();
  }

  /**
   * Supplier menu to add, remove, or update locations
   * 
   * @param supplier_name supplier name
   */
  private static void manageSupplier(String supplier_name) {
    System.out.println("-------------------------");
    System.out.println("[1] View my [L]ocations");
    System.out.println("[2] [A]dd a New Location");
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
          System.out.println(String.format("Supplier[%s], Removing location", supplier_name));
          int supplier_id = checkSupplier(supplier_name, false);
          if (supplier_id > 0)
            db.removeSupplierById(supplier_id);
        }
          break;
        case '4':
        case 'u': {
          System.out.println(String.format("Enter supplier_id to update for supplier[%s]:", supplier_name));
          int supplier_id = checkSupplier(supplier_name, false);
          if (supplier_id > 0) {
            SupplierData supplier = db.getSupplierById(supplier_id);
            System.out.println("Current location: " + supplier.location);
            System.out.println("Enter new location: ");
            String location = getString();
            int count = db.updateSupplier(supplier_id, supplier.supplier_name, location);
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
      Log.printStackTrace(e);
    }
    manageSupplier(supplier_name);
  }

  /**
   * Queries suppliers based on filters
   * Queries by id if supplier_id > 0, will locate the specific supplier
   * Else use the name and location
   * 
   * @param supplier_id   id
   * @param supplier_name name. Can be null
   * @param location      lcocation. Can be null
   */
  private static void viewSuppliers(int supplier_id, String supplier_name, String location) {
    ArrayList<SupplierData> suppliers = new ArrayList<>();
    if (supplier_id > 0) {
      SupplierData supplier = db.getSupplierById(supplier_id);
      if (supplier != null) {
        suppliers.add(supplier);
      } else {
        System.out.println("Supplier not found");
      }
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

  /**
   * manage or view the components in each product
   * 
   * @param supplier_name supplier name
   */
  private static void viewComponents(String supplier_name) {
    System.out.println("-------------------------");
    System.out.println("[0] View Components by [N]ame");
    System.out.println("[1] View Components by [S]upplier");
    System.out.println("[2] View Components by [P]roduct");
    System.out.println("[3] [A]dd a New Component");
    System.out.println("[4] [R]emove a Component");
    System.out.println("[5] [U]pdate a Component");
    System.out.println("[6] Return to Previous [M]enu");
    try {
      switch (getChar()) {
        case '0':
        case 'n': {
          System.out.println("Enter component name");
          String component = checkForNA();
          ArrayList<ManufacturingData> components = db.getManufacturingByComponent(component);
          printComponents(components);
        }
          break;
        case '1':
        case 's': {
          String tempSupplierName = supplier_name;
          if (isStore(supplier_name)) {
            System.out.println("Enter supplier name");
            tempSupplierName = checkForNA();
          }
          int supplier_id = checkSupplier(tempSupplierName, true);
          if (supplier_id == 0) {
            ArrayList<ManufacturingData> components = db.getManufacturingBySupplierName(tempSupplierName);
            printComponents(components);
          } else if (supplier_id > 0) {
            ArrayList<ManufacturingData> components = db.getManufacturingBySupplierId(supplier_id);
            printComponents(components);
          }
        }

          break;
        case '2':
        case 'p': {
          System.out.println("Enter product id:");
          int product_id = getInt();
          ArrayList<ManufacturingData> components = db.getManufacturingByProductId(product_id);
          printComponents(components);
        }
          break;
        case '3':
        case 'a': {
          if (isStore(supplier_name)) {
            System.out.println("Not a supplier. Cannot add component.");
            break;
          }
          int supplier_id = checkSupplier(supplier_name, false);
          if (supplier_id > 0) {
            System.out.println("Enter product id:");
            int product_id = getInt();
            System.out.println("Enter Component:");
            String component = getString();
            db.addManufacturing(product_id, supplier_id, component);
          }
        }
          break;
        case '4':
        case 'r': {
          if (isStore(supplier_name)) {
            System.out.println("Not a supplier. Cannot add component.");
            break;
          }
          System.out.println("Enter manufacturer id (-1 to skip):");
          int m_id = getInt();
          if (m_id < 0) { // We skip the manufacturer id and use the other fields
            int supplier_id = checkSupplier(supplier_name, false);
            if (supplier_id > 0) {
              System.out.println("Enter product id:");
              int product_id = getInt();
              System.out.println("Enter component");
              String component = checkForNA();
              db.deleteManufacturing(product_id, supplier_id, component);
            }
          } else {
            db.deleteManufacturing(m_id);
          }
        }
          break;
        case '5':
        case 'u': {
          if (isStore(supplier_name)) {
            System.out.println("Not a supplier. Cannot add component.");
            break;
          }
          int supplier_id = checkSupplier(supplier_name, false);
          if (supplier_id > 0) {
            System.out.println("Enter component id:");
            int m_id = getInt();
            ManufacturingData old = db.getOneManufacturing(m_id);
            if (old == null) {
              System.out.println("Component not found");
              break;
            }
            System.out.println("Old component: " + old.component);
            System.out.println("Enter new component:");
            String component = getString();
            db.updateManufacturing(m_id, component);
          }
        }
          break;
        case '6':
        case 'm':
          goToMenu(supplier_name);
          break;
        default:
          System.out.println("Invalid choice");
          break;
      }
    } catch (

    Exception e) {
      Log.printStackTrace(e);
    }

    viewComponents(supplier_name);
  }

  /**
   * Returns to proper menu given context
   * 
   * @param supplier_name if null, it is a store
   */
  private static void goToMenu(String supplier_name) {
    if (isStore(supplier_name)) {
      managerMenu();
    } else {
      supplierMenu(supplier_name);
    }
  }

  /**
   * Pretty prints components by id, product, name, and supplier
   * 
   * @param components ArrayList of ManufacturingData
   */
  private static void printComponents(ArrayList<ManufacturingData> components) {
    if (components.size() == 0) {
      System.out.println("No components found");
      return;
    }
    String format = "%-5s %20s %-5s %20s %-5s %-20s";
    System.out.println(String.format(format, "ID", "Product Name", "ID", "Supplier Name", "ID", "Component"));
    for (ManufacturingData component : components) {
      System.out.println(String.format(format,
          component.manufacturing_id, component.product.product_name, component.product.product_id,
          component.supplier.supplier_name, component.supplier.supplier_id,
          component.component));
    }

  }

  /**
   * Initializes the table/view names
   * 
   * @return ArrayList of entity names
   */
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