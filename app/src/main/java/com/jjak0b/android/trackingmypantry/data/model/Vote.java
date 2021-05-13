package com.jjak0b.android.trackingmypantry.data.model;

import com.google.gson.annotations.Expose;
import com.jjak0b.android.trackingmypantry.data.model.TokenizedItem;

public class Vote extends TokenizedItem {
    @Expose
    int rating;

    @Expose
    String productId;
}
