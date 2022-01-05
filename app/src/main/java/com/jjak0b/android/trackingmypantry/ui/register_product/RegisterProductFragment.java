package com.jjak0b.android.trackingmypantry.ui.register_product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.jjak0b.android.trackingmypantry.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterProductFragment extends Fragment {

    private _RegisterProductViewModel mSharedViewModel;
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSharedViewModel = new ViewModelProvider(requireActivity()).get(_RegisterProductViewModel.class);
        mPageViewModel = new ViewModelProvider(this).get(PageViewModel.class);

        ViewPager2 viewPager = view.findViewById(R.id.view_pager);
        Button nextBtn = view.findViewById( R.id.continueBtn );
        TabLayout tabs = view.findViewById( R.id.tabs );

        ProductInfoSectionsPagerAdapter productInfoSectionsPagerAdapter =
                new ProductInfoSectionsPagerAdapter(requireActivity());

        // when product is not ready so allow only tab 0
        mSharedViewModel.onBaseProductSet().observe(getViewLifecycleOwner(), hasBeenSet -> {
            if( !hasBeenSet ){
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
/*
        mSharedViewModel.onCanSubmit().observe(getViewLifecycleOwner(), canSubmit -> {
            if( canSubmit ) {
                registerProduct(view);
            }
        });
*/
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( mPageViewModel.canSelectNextTab() ){
                    int nextIndex = tabs.getSelectedTabPosition() + 1;
                    mPageViewModel.setPageIndex( nextIndex );
                }
                else if( tabs.getSelectedTabPosition() >= productInfoSectionsPagerAdapter.getAbsolutePageCount()-1 ) {
                    // mSharedViewModel.save();
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

        // tabs.setupWithViewPager(viewPager);
        new TabLayoutMediator(tabs, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText( productInfoSectionsPagerAdapter.getTabTitle(position) );
            }
        }).attach();

    }

    // submit product
    void registerProduct( View view) {
/*
        mSharedViewModel.submit().observe(getViewLifecycleOwner(), result -> {
            switch (result.getStatus()) {
                case ERROR:
                    Throwable t = result.getError();
                    if( t instanceof AuthException){
                        Log.e( TAG, "Authentication Error", t );
                        Toast.makeText(getContext(),  "Authentication Error: You need to login first", Toast.LENGTH_SHORT )
                                .show();
                    }
                    else if( t instanceof RemoteException){
                        Log.e( TAG, "Server Error", t );
                        Toast.makeText(getContext(), "Server error: Unable to add to the server", Toast.LENGTH_SHORT )
                                .show();
                    }
                    else if( t instanceof IOException){
                        Log.e( TAG, "Network Error", t );
                        Toast.makeText(getContext(),  "Network error: Unable to connect to server", Toast.LENGTH_SHORT )
                                .show();
                    }
                    else {
                        Log.e( TAG, "Unexpected Error", t );
                        Toast.makeText(getContext(), "Unexpected error: Unable to perform operation", Toast.LENGTH_SHORT )
                                .show();
                    }
                    break;
                case SUCCESS:
                    Toast.makeText(getContext(), "Register product successfully", Toast.LENGTH_LONG ).show();
                    // mSharedViewModel.setupNewProduct();
                    Navigation.findNavController(view)
                            .popBackStack(R.id.registerProductFragment, true);
                    break;
                default:
                    break;
            }
        });
*/
    }
}