/**
 * ShipmentData.java lists the fields of the shipment table
 */
public class ShipmentData {
    /**
     * The shipment id
     */
    int shipment_id;
    /**
     * The store id
     */
    int store_id;

    /**
     * The supplier id
     */
    int supplier_id;
    /**
     * The product id
     */
    int product_id;

    /**
     * The supplier's name
     */
    String supplier_name;

    /**
     * The supplier location
     */
    String location;

    /**
     * The quantity of the product in the shipment
     */
    int quantity;

    /**
     * The unit price of the product in the shipment
     */
    double unit_price;

    /**
     * The date when the shipment was shipped
     */
    String ship_date;

    public ShipmentData(int shipment_id, int store_id, int supplier_id, int product_id, String supplier_name,
            String location, int quantity, double unit_price, String ship_date) {
        this.shipment_id = shipment_id;
        this.store_id = store_id;
        this.supplier_id = supplier_id;
        this.product_id = product_id;
        this.supplier_name = supplier_name;
        this.location = location;
        this.quantity = quantity;
        this.unit_price = unit_price;
        this.ship_date = ship_date;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + shipment_id;
        result = prime * result + store_id;
        result = prime * result + supplier_id;
        result = prime * result + product_id;
        result = prime * result + ((supplier_name == null) ? 0 : supplier_name.hashCode());
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        result = prime * result + quantity;
        long temp;
        temp = Double.doubleToLongBits(unit_price);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((ship_date == null) ? 0 : ship_date.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return String.format(
                "ShipmentData [shipment_id=%d, store_id=%d, supplier_id=%d, product_id=%d, supplier_name='%s', location='%s', quantity=%d, unit_price=%.2f, ship_date='%s']",
                shipment_id, store_id, supplier_id, product_id, supplier_name, location, quantity, unit_price,
                ship_date);
    }

}
