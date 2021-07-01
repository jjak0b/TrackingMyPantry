package com.jjak0b.android.trackingmypantry.ui.util;

import com.hootsuite.nachos.chip.ChipInfo;
import com.jjak0b.android.trackingmypantry.data.model.ProductTag;

import java.util.ArrayList;
import java.util.List;

public class ChipTagUtil {


    public static ChipInfo newInstanceFrom(ProductTag tag) {
        return new ChipInfo( tag.getName(), tag );
    }

    public static List<ChipInfo> newInstanceFrom(ProductTag[] tags) {
        List<ChipInfo> chips = new ArrayList<>(tags.length);
        for (int i = 0; i < tags.length; i++) {
            chips.add( newInstanceFrom( tags[i] ) );
        }
        return chips;
    }
}
