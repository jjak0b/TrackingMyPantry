package com.jjak0b.android.trackingmypantry.ui.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.jjak0b.android.trackingmypantry.R;

import org.jetbrains.annotations.NotNull;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterProductFragment extends Fragment {

    private RegisterProductViewModel mProductViewModel;
    private PageViewModel mPageViewModel;
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
        TabLayout tabs = view.findViewById( R.id.tabs );

        ProductInfoSectionsPagerAdapter productInfoSectionsPagerAdapter =
                new ProductInfoSectionsPagerAdapter(getActivity(), getActivity().getSupportFragmentManager(), mProductViewModel);

        mProductViewModel.getProductBuilder().observe( getViewLifecycleOwner(), builder -> {
            productInfoSectionsPagerAdapter.enableTabs( builder != null && builder.getBarcode() != null );
        });

        viewPager.setAdapter(productInfoSectionsPagerAdapter);
        tabs.setupWithViewPager(viewPager);

    }
}