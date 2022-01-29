package com.jjak0b.android.trackingmypantry.ui.register_product;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.ui.register_product.tabs.SectionProductDetailsFragment;
import com.jjak0b.android.trackingmypantry.ui.register_product.tabs.SectionProductInstanceDetailsFragment;
import com.jjak0b.android.trackingmypantry.ui.register_product.tabs.SectionProductPurchaseDetailsFragment;
import com.jjak0b.android.trackingmypantry.ui.util.ErrorsUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterProductFragment extends Fragment {

    private SharedProductViewModel mProductPickerViewModel;
    private RegisterProductViewModel mSharedViewModel;
    private PageViewModel mPageViewModel;
    private static final String TAG = "RegisterProductFragment";

    public RegisterProductFragment() {
        super();
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProductPickerViewModel = new ViewModelProvider(requireActivity()).get(SharedProductViewModel.class);
        mSharedViewModel = new ViewModelProvider(requireActivity()).get(RegisterProductViewModel.class);
        mPageViewModel = new ViewModelProvider(this).get(PageViewModel.class);
        mSharedViewModel.setupNew();
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

        String barcode = RegisterProductFragmentArgs.fromBundle(getArguments()).getProductID();
        if( barcode != null ) {
            mProductPickerViewModel.setItemSource(mSharedViewModel.getMyProduct(barcode));
        }

        ViewPager2 viewPager = view.findViewById(R.id.view_pager);
        Button nextBtn = view.findViewById( R.id.continueBtn );
        TabLayout tabs = view.findViewById( R.id.tabs );

        ProductInfoSectionsPagerAdapter productInfoSectionsPagerAdapter =
            new ProductInfoSectionsPagerAdapter(requireActivity()) {
                @NonNull
                @Override
                public Fragment createFragment(int position) {
                    return createPageFragment(position);
                }
            };

        // when product is not ready so allow only tab 0
        mSharedViewModel.onBaseProductSet().observe(getViewLifecycleOwner(), hasBeenSet -> {
            // Log.d(TAG, "IsBaseProductSet " + hasBeenSet );
            if( !hasBeenSet ){
                mPageViewModel.setPageIndex( 0 );
                mPageViewModel.setMaxNavigableTabCount( 1 );
            }
            else {
                mPageViewModel.setMaxNavigableTabCount( productInfoSectionsPagerAdapter.getAbsolutePageCount()  );
            }
        });

        mPageViewModel.getPageIndex().observe( getViewLifecycleOwner(), pageIndex -> {
            if( pageIndex == null ) return;

            int index = pageIndex.first;
            int prevIndex = pageIndex.second;

            Log.d(TAG, "going from page " + prevIndex + " to " + index );

            if( index != prevIndex ) {
                // trigger saving on previous page
                saveOnChangePage(prevIndex);
            }

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
                    mSharedViewModel.save();
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



        mSharedViewModel.canSave().observe(getViewLifecycleOwner(), canSave -> {
            // Log.d(TAG, "canSave="+canSave);
        });

        mSharedViewModel.onSave().observe(getViewLifecycleOwner(), isSaving ->  {
            if( !isSaving ) {
                // Log.d(TAG, "not saving");
                return;
            }

            mSharedViewModel.saveProductDetails();
            mSharedViewModel.saveProductInfoDetails();
            mSharedViewModel.saveProductPurchaseDetails();

            mSharedViewModel.saveComplete();
        });

        mSharedViewModel.onSaved().observe(getViewLifecycleOwner(), resource -> {
            Log.d(TAG, "saved result " + resource );
            switch (resource.getStatus()) {
                case LOADING:
                    break;
                case SUCCESS:
                    Snackbar.make(requireView(), R.string.product_register_complete, BaseTransientBottomBar.LENGTH_SHORT)
                            .show();

                    mSharedViewModel.setupNew();
                    mSharedViewModel.onReset().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                        @Override
                        public void onChanged(Boolean isResetting) {
                            if( !isResetting ) {
                                mSharedViewModel.onReset().removeObserver(this::onChanged);

                                Navigation.findNavController(view)
                                        .navigate(RegisterProductFragmentDirections.onRegisterCompleted());
                            }
                        }
                    });

                    break;
                case ERROR:
                    Throwable error = resource.getError();
                    String errorMsg = ErrorsUtils.getErrorMessage(requireContext(), error, TAG);

                    new AlertDialog.Builder(requireContext())
                            .setTitle(android.R.string.dialog_alert_title)
                            .setMessage(errorMsg)
                            .setPositiveButton(android.R.string.ok, null)
                            .setNeutralButton(R.string.action_retry, (dialog, which) -> {
                                mSharedViewModel.save();
                            })
                            .show();
                    break;
            }
        });
    }

    @NonNull
    public Fragment createPageFragment( int position ) {
        switch ( position ){
            case 0:
                return new SectionProductDetailsFragment();
            case 1:
                return new SectionProductInstanceDetailsFragment();
            case 2:
                return new SectionProductPurchaseDetailsFragment();
            default:
                throw new IllegalArgumentException("Undefined page at index " + position);
        }
    }

    public void saveOnChangePage( int previousPage ) {
        Log.d(TAG, "Trigger saving page " + previousPage);
        switch (previousPage) {
            case 0:
                mSharedViewModel.saveProductDetails();
                break;
            case 1:
                mSharedViewModel.saveProductInfoDetails();
                break;
            case 2:
                mSharedViewModel.saveProductPurchaseDetails();
                break;
            default:
                break;
        }
    }

}