package com.jjak0b.android.trackingmypantry;

public interface ItemViewModel<I> {
    I getItem();
    void setItem( I item );
}
