public class ManufacturingData {
    int manufacturing_id;
    ProductData product;
    SupplierData supplier;
    String component;

    public ManufacturingData(ProductData product, SupplierData supplier, String component, int manufacturing_id) {
        this.product = product;
        this.supplier = supplier;
        this.component = component;
        this.manufacturing_id = manufacturing_id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + manufacturing_id;
        result = prime * result + ((product == null) ? 0 : product.hashCode());
        result = prime * result + ((supplier == null) ? 0 : supplier.hashCode());
        result = prime * result + ((component == null) ? 0 : component.hashCode());
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
        ManufacturingData other = (ManufacturingData) obj;
        if (manufacturing_id != other.manufacturing_id)
            return false;
        if (product == null) {
            if (other.product != null)
                return false;
        } else if (!product.equals(other.product))
            return false;
        if (supplier == null) {
            if (other.supplier != null)
                return false;
        } else if (!supplier.equals(other.supplier))
            return false;
        if (component == null) {
            if (other.component != null)
                return false;
        } else if (!component.equals(other.component))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ManufacturingData [manufacturing_id=" + manufacturing_id + ", product=" + product.toString()
                + ", supplier=" + supplier.toString()
                + ", component=" + component + "]";
    }

}
