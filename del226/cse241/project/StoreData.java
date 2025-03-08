package del226.cse241.project;

/**
 * StoreData.java lists the fields of the store table
 */
public class StoreData {
    /**
     * The store id
     */
    int store_id;

    /**
     * The store location
     */
    String location;

    /**
     * Construct a RowData object by providing values for its fields
     * 
     * @param store_id The store id
     * @param location The store location
     */
    public StoreData(int store_id, String location) {
        this.store_id = store_id;
        this.location = location;
    }

    @Override
    public int hashCode() {
        int result = 17; // Initial value, typically a prime number
        result = 31 * result + store_id; // Multiply by a prime number and add the field
        result = 31 * result + (location != null ? location.hashCode() : 0); // Handle null
        return result;
    }

    @Override
    public String toString() {
        return String.format("StoreData [store_id=%s, location=%s]", store_id, location);
    }
}
