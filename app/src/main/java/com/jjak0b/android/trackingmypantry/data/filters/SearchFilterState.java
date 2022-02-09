package com.jjak0b.android.trackingmypantry.data.filters;

import java.util.List;

public class SearchFilterState {
    public String query;
    public List<Long> searchTags;

    @Override
    public String toString() {
        return "SearchFilterState{" +
                "query='" + query + '\'' +
                ", searchTags=" + searchTags +
                '}';
    }
}
