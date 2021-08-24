package com.jjak0b.android.trackingmypantry.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.viewpager.widget.ViewPager;

import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.auth.AuthException;
import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.ui.util.ChipTagUtil;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Calendar;

import retrofit2.HttpException;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterProductFragment extends Fragment {

    private RegisterProductViewModel mProductViewModel;
    private PageViewModel mPageViewModel;
    private static final String TAG = RegisterProductFragment.class.getName();

    public RegisterProductFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProductViewModel = new ViewModelProvider(requireActivity()).get(RegisterProductViewModel.class);
        mPageViewModel = new ViewModelProvider(requireActivity()).get(PageViewModel.class);

        ViewPager viewPager = view.findViewById(R.id.view_pager);
        Button nextBtn = view.findViewById( R.id.continueBtn );
        TabLayout tabs = view.findViewById( R.id.tabs );

        ProductInfoSectionsPagerAdapter productInfoSectionsPagerAdapter =
                new ProductInfoSectionsPagerAdapter(getActivity(), getActivity().getSupportFragmentManager(), mProductViewModel);

        // when product is not ready so allow only tab 0
        mProductViewModel.getProductBuilder().observe( getViewLifecycleOwner(), builder -> {
            if( builder == null ){
                mPageViewModel.setPageIndex( 0 );
                mPageViewModel.setMaxNavigableTabCount( 1 );
            }
            else {
                mPageViewModel.setMaxNavigableTabCount( productInfoSectionsPagerAdapter.getAbsolutePageCount()  );
            }
        });

        mPageViewModel.getPageIndex().observe( getViewLifecycleOwner(), index -> {
            tabs.selectTab( tabs.getTabAt( index ) );

            boolean shouldEnableBtn = mPageViewModel.canSelectNextTab();

            if( index >= productInfoSectionsPagerAdapter.getAbsolutePageCount()-1 ){
                shouldEnableBtn = true;
                nextBtn.setText( R.string.action_register_product );
            }
            else{
                nextBtn.setText( R.string.action_next_product_section);
            }

            nextBtn.setEnabled( shouldEnableBtn );
        });

        mPageViewModel.getMaxNavigableTabCount().observe( getViewLifecycleOwner(), count -> {
            nextBtn.setEnabled( mPageViewModel.canSelectNextTab() );
            productInfoSectionsPagerAdapter.setMaxEnabledTabs( count );
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( mPageViewModel.canSelectNextTab() ){
                    int nextIndex = tabs.getSelectedTabPosition() + 1;
                    mPageViewModel.setPageIndex( nextIndex );
                }
                else if( tabs.getSelectedTabPosition() >= productInfoSectionsPagerAdapter.getAbsolutePageCount()-1 ) {
                    registerProduct( view );
                }
            }
        });

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mPageViewModel.setPageIndex( tab.getPosition() );
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.setAdapter(productInfoSectionsPagerAdapter);
        tabs.setupWithViewPager(viewPager);

    }

    // submit product
    void registerProduct( View view) {
        Futures.addCallback(
                mProductViewModel.registerProduct(),
                new FutureCallback<ProductInstanceGroup>() {
                    @Override
                    public void onSuccess(@NullableDecl ProductInstanceGroup result) {
                        Toast.makeText(getContext(), "Register product successfully", Toast.LENGTH_LONG ).show();
                        mProductViewModel.setupNewProduct();
                        Navigation.findNavController(view)
                                .popBackStack(R.id.registerProductFragment, true);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        if( t instanceof AuthException){
                            Log.e( TAG, "Authentication Error", t );
                            Toast.makeText(getContext(), "Authentication Error: You need to login first", Toast.LENGTH_SHORT )
                                    .show();
                        }
                        else if( t instanceof HttpException){
                            Log.e( TAG, "Server Error", t );
                            Toast.makeText(getContext(), "Server error: Unable to add to the server", Toast.LENGTH_SHORT )
                                    .show();
                        }
                        else if( t instanceof IOException){
                            Log.e( TAG, "Network Error", t );
                            Toast.makeText(getContext(), "Network error: Unable to connect to server", Toast.LENGTH_SHORT )
                                    .show();
                        }
                        else {
                            Log.e( TAG, "Unexpected Error", t );
                            Toast.makeText(getContext(), "Unexpected error: Unable to perform operation", Toast.LENGTH_SHORT )
                                    .show();
                        }
                    }
                },
                ContextCompat.getMainExecutor( getContext() )
        );
    }
}