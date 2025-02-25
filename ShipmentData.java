/**
 * ShipmentData.java lists the fields of the shipment table
 */
public class ShipmentData {
    /**
     * The shipment id
     */
    int shipment_id;
    /**
     * The destination id
     */
    int to_id;

    /**
     * The date when the shipment was shipped
     */
    String ship_date;

    /**
     * The date when the shipment will arrive
     */
    String arrive_date;

    /**
     * The supplier
     */
    SupplierData supplier;

    /**
     * The product
     */
    ProductData product;

    /**
     * The quantity of the product in the shipment
     */
    float quantity;

    public ShipmentData(int shipment_id, int to_id, String ship_date, String arrive_date) {
        this.shipment_id = shipment_id;
        this.to_id = to_id;
        this.ship_date = ship_date;
        this.arrive_date = arrive_date;
    }

    public ShipmentData(int shipment_id, int to_id, String ship_date, String arrive_date, SupplierData supplier,
            ProductData product, float quantity) {
        this.shipment_id = shipment_id;
        this.to_id = to_id;
        this.ship_date = ship_date;
        this.arrive_date = arrive_date;
        this.supplier = supplier;
        this.product = product;
        this.quantity = quantity;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + shipment_id;
        result = prime * result + to_id;
        result = prime * result + ((ship_date == null) ? 0 : ship_date.hashCode());
        result = prime * result + ((arrive_date == null) ? 0 : arrive_date.hashCode());
        result = prime * result + ((supplier == null) ? 0 : supplier.hashCode());
        result = prime * result + ((product == null) ? 0 : product.hashCode());
        result = prime * result + Float.floatToIntBits(quantity);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ShipmentData other = (ShipmentData) obj;
        if (shipment_id != other.shipment_id)
            return false;
        if (to_id != other.to_id)
            return false;
        if (ship_date == null) {
            if (other.ship_date != null)
                return false;
        } else if (!ship_date.equals(other.ship_date))
            return false;
        if (arrive_date == null) {
            if (other.arrive_date != null)
                return false;
        } else if (!arrive_date.equals(other.arrive_date))
            return false;
        if (supplier == null) {
            if (other.supplier != null)
                return false;
        } else if (!supplier.equals(other.supplier))
            return false;
        if (product == null) {
            if (other.product != null)
                return false;
        } else if (!product.equals(other.product))
            return false;
        if (Float.floatToIntBits(quantity) != Float.floatToIntBits(other.quantity))
            return false;
        return true;
    }

    @Override
    public String toString() {
        if (supplier == null) {
            return "ShipmentData [shipment_id=" + shipment_id + ", to_id=" + to_id + ", ship_date=" + ship_date
                    + ", arrive_date=" + arrive_date + "]";
        }
        return "ShipmentData [shipment_id=" + shipment_id + ", to_id=" + to_id + ", ship_date=" + ship_date
                + ", arrive_date=" + arrive_date + ", supplier=" + supplier.toString() + ", product="
                + product.toString() + ", quantity="
                + quantity + "]";
    }

}
