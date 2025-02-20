public class ProductData {
    SupplierData supplier;
    String product_name;
    float price;

    public ProductData(SupplierData supplier, String product_name, float price) {
        this.supplier = supplier;
        this.product_name = product_name;
        this.price = price;
    }

    @Override
    public String toString() {
        return "ProductData [" + supplier.toString() + ", product_name=" + product_name + ", price=$" + price
                + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((supplier == null) ? 0 : supplier.hashCode());
        result = prime * result + ((product_name == null) ? 0 : product_name.hashCode());
        result = prime * result + Float.floatToIntBits(price);
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
        if (product_name == null) {
            if (other.product_name != null)
                return false;
        } else if (!product_name.equals(other.product_name))
            return false;
        if (Float.floatToIntBits(price) != Float.floatToIntBits(other.price))
            return false;
        return true;
    }
}
