package dngsoftware.spoolid;

import androidx.annotation.NonNull;

public class MaterialItem {
    private final String materialBrand;
    private final String materialID;

    public MaterialItem(String brand, String id) {
        this.materialBrand = brand;
        this.materialID = id;
    }

    public String getMaterialBrand() {
        return materialBrand;
    }

    public String getMaterialID() {
        return materialID;
    }

    @NonNull
    @Override
    public String toString() {
        return materialBrand;
    }
}