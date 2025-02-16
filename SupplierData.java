/**
 * SupplierData.java lists the fields of the supplier table
 */
public class SupplierData {
    /**
     * The supplier id
     */
    int supplier_id;

    /**
     * The supplier's name
     */
    String supplier_name;

    /**
     * The supplier location
     */
    String location;

    /**
     * Construct a RowData object by providing values for its fields
     * 
     * @param supplier_id   The supplier id
     * @param supplier_name The supplier's name
     * @param location      The supplier location
     */
    public SupplierData(int supplier_id, String supplier_name, String location) {
        this.supplier_id = supplier_id;
        this.supplier_name = supplier_name;
        this.location = location;
    }

    @Override
    public int hashCode() {
        int result = 17; // Initial value, typically a prime number
        result = 31 * result + supplier_id; // Multiply by a prime number and add the field
        result = 31 * result + (location != null ? location.hashCode() : 0); // Handle null
        result = 31 * result + (supplier_name != null ? supplier_name.hashCode() : 0); // Handle null
        return result;
    }

    @Override
    public String toString() {
        return String.format("supplierData [supplier_id=%s, supplier_name=%s, location=%s]", supplier_id, supplier_name,
                location);
    }
}
