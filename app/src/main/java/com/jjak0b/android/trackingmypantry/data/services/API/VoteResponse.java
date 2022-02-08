package com.jjak0b.android.trackingmypantry.data.services.API;

import com.google.gson.annotations.Expose;

public class VoteResponse {
    @Expose
    String id;

    @Expose
    int rating;

    @Expose
    String productId;

    @Expose
    String userId;

}
