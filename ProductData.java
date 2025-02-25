public class ProductData {
    SupplierData supplier;
    int product_id;
    String product_name;
    float price;
    String unit_type;

    public ProductData(SupplierData supplier, int product_id, String product_name, float price, String unit_type) {
        this.supplier = supplier;
        this.product_id = product_id;
        this.product_name = product_name;
        this.price = price;
        this.unit_type = unit_type;
    }

    @Override
    public String toString() {
        if (supplier == null) {
            return "ProductData [product_id=" + product_id + ", product_name=" + product_name + "]";
        }
        return "ProductData [" + supplier.toString() + ", product_id=" + product_id + ", product_name="
                + product_name + ", price=$" + price + "unit=" + unit_type + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((supplier == null) ? 0 : supplier.hashCode());
        result = prime * result + product_id;
        result = prime * result + ((product_name == null) ? 0 : product_name.hashCode());
        result = prime * result + Float.floatToIntBits(price);
        result = prime * result + ((unit_type == null) ? 0 : unit_type.hashCode());
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
        ProductData other = (ProductData) obj;
        if (supplier == null) {
            if (other.supplier != null)
                return false;
        } else if (!supplier.equals(other.supplier))
            return false;
        if (product_id != other.product_id)
            return false;
        if (product_name == null) {
            if (other.product_name != null)
                return false;
        } else if (!product_name.equals(other.product_name))
            return false;
        if (Float.floatToIntBits(price) != Float.floatToIntBits(other.price))
            return false;
        if (unit_type == null) {
            if (other.unit_type != null)
                return false;
        } else if (!unit_type.equals(other.unit_type))
            return false;
        return true;
    }
}
