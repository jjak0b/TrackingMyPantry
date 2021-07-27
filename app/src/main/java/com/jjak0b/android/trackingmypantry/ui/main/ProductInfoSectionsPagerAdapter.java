package com.jjak0b.android.trackingmypantry.ui.main;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.jjak0b.android.trackingmypantry.R;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class ProductInfoSectionsPagerAdapter extends FragmentStatePagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{
            R.string.tab_product_details,
            R.string.tab_product_instance_details,
            R.string.tab_product_purchase_details
    };
    private final Context mContext;
    private RegisterProductViewModel vm;
    private int pageCount;

    public ProductInfoSectionsPagerAdapter(Context context, FragmentManager fm, RegisterProductViewModel vm ) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT );
        mContext = context;
        this.vm = vm;
        this.pageCount = 1;
    }

    @Override
    public Fragment getItem(int position) {
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

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }


    public void enableTabs( boolean shouldEnable ){
        if( !shouldEnable ){
            pageCount = 1;
        }
        else{
            pageCount = TAB_TITLES.length;
        }
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return pageCount;
    }
}