package com.jjak0b.android.trackingmypantry.ui.register_product;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.jjak0b.android.trackingmypantry.R;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public abstract class ProductInfoSectionsPagerAdapter extends FragmentStateAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{
            R.string.tab_product_details,
            R.string.tab_product_instance_details,
            R.string.tab_product_purchase_details
    };
    private int pageCount;


    public ProductInfoSectionsPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
        setMaxEnabledTabs(1);
    }

    public ProductInfoSectionsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        setMaxEnabledTabs(1);
    }

    public ProductInfoSectionsPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
        setMaxEnabledTabs(1);
    }

    @StringRes
    public int getTabTitle(int index) {
        return TAB_TITLES[ index % getItemCount() ];
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setMaxEnabledTabs(int pageCount ){
        this.pageCount = pageCount;
        this.notifyDataSetChanged();
    }

    public int getAbsolutePageCount() {
        return TAB_TITLES.length;
    }



    @Override
    public int getItemCount() {
        return this.pageCount;
    }
}