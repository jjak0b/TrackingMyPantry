package com.jjak0b.android.trackingmypantry.data.filters;

import java.util.List;

public class ProductFilterState {
    public String name;
    public String description;
    public String barcode;
    public List<Long> tagsIDs;

    @Override
    public String toString() {
        return "FilterState{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", barcode='" + barcode + '\'' +
                ", tagsIDs=" + tagsIDs +
                '}';
    }
}
