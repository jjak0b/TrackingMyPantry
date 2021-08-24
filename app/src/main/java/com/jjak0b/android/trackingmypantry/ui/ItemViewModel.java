package com.jjak0b.android.trackingmypantry.ui;

public interface ItemViewModel<I> {
    I getItem();
    void setItem( I item );
}
