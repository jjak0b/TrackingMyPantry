package com.jjak0b.android.trackingmypantry.ui.main;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.ui.main.tabs.SectionProductDetailsFragment;
import com.jjak0b.android.trackingmypantry.ui.main.tabs.SectionProductInstanceDetailsFragment;
import com.jjak0b.android.trackingmypantry.ui.main.tabs.SectionProductPurchaseDetailsFragment;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class ProductInfoSectionsPagerAdapter extends FragmentStateAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{
            R.string.tab_product_details,
            R.string.tab_product_instance_details,
            R.string.tab_product_purchase_details
    };
    private final Context mContext;
    private RegisterProductViewModel vm;
    private int pageCount;

    public ProductInfoSectionsPagerAdapter(FragmentActivity fragmentActivity, RegisterProductViewModel vm ) {
        super(fragmentActivity);
        mContext = fragmentActivity;
        this.vm = vm;
        this.pageCount = 1;
    }


    @StringRes
    public int getTabTitle(int index) {
        return TAB_TITLES[ index % getItemCount() ];
    }

    private Fragment getItem(int position) {
        switch ( position ){
            case 0:
                return new SectionProductDetailsFragment();
            case 1:
                return new SectionProductInstanceDetailsFragment();
            case 2:
                return new SectionProductPurchaseDetailsFragment();
            default:
                Log.e( this.getClass().getName(), "Unable to get tab index " + position );
                break;
        }
        return null;
    }

    public void setMaxEnabledTabs( int pageCount ){
        this.pageCount = pageCount;
        this.notifyDataSetChanged();
    }

    public int getAbsolutePageCount() {
        return TAB_TITLES.length;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return getItem(position);
    }

    @Override
    public int getItemCount() {
        return this.pageCount;
    }
}