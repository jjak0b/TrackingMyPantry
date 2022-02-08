package com.jjak0b.android.trackingmypantry.ui.util;

import androidx.annotation.NonNull;

import com.hootsuite.nachos.chip.Chip;
import com.hootsuite.nachos.chip.ChipInfo;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductTag;

import java.util.ArrayList;
import java.util.List;

public class ChipTagUtil {


    public static ChipInfo newInstanceFrom(ProductTag tag) {
        return new ChipInfo( tag.getName(), tag );
    }

    public static List<ChipInfo> newChipsInstanceFromTags(ProductTag... tags) {
        List<ChipInfo> chips = new ArrayList<>(tags.length);
        for (int i = 0; i < tags.length; i++) {
            if( tags[i] != null )
                chips.add( newInstanceFrom( tags[i] ) );
        }
        return chips;
    }

    public static List<ChipInfo> newChipsInstanceFromTags( @NonNull List<ProductTag> tags) {
        int size = tags.size();
        List<ChipInfo> chips = new ArrayList<>(size);
        for (ProductTag tag : tags) {
            if( tag != null )
                chips.add( newInstanceFrom( tag ) );
        }
        return chips;
    }

    public static List<ProductTag> newTagsInstanceFromChips( @NonNull List<Chip> chips) {
        int size = chips.size();
        List<ProductTag> tags = new ArrayList<>(size);
        for (Chip chip : chips) {
            if( chip != null ){
                ProductTag data = (ProductTag) chip.getData();
                if( data == null ) data = ProductTag.creteDummy(chip.getText().toString() );
                tags.add( data );
            }
        }
        return tags;
    }
}
