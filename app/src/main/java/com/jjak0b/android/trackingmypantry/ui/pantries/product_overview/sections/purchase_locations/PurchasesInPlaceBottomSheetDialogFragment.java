package com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.sections.purchase_locations;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.PurchaseInfo;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class PurchasesInPlaceBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private PurchasesInPlaceViewModel mViewModel;
    private final static String TAG = "PurchasesHistoryFragment";
    public static PurchasesInPlaceBottomSheetDialogFragment newInstance() {
        return new PurchasesInPlaceBottomSheetDialogFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.purchases_bottom_sheet_dialog_fragment, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireParentFragment()).get(PurchasesInPlaceViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LineChart viewChart = view.findViewById(R.id.chart);
        ProgressBar loadingBar = view.findViewById(R.id.purchasesLoadingBar);

        mViewModel.getPurchases().observe(getViewLifecycleOwner(), purchaseInfos -> {
            if( purchaseInfos == null) return;
            setupChart(purchaseInfos, viewChart);
        });

        mViewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if( isLoading ){
                loadingBar.setVisibility(View.VISIBLE);
            }
            else {
                loadingBar.setVisibility(View.GONE);
            }
        });
    }

    void setupChart(List<PurchaseInfo> unsortedPurchases, LineChart viewChart) {
        final DateFormat dateFormat = android.text.format.DateFormat
                .getDateFormat(requireContext());

        ArrayList<PurchaseInfo> purchases = new ArrayList<>(unsortedPurchases);
        ArrayList<Entry> entries = new ArrayList<>(purchases.size());

        int i = 0;
        for ( PurchaseInfo purchaseInfo : purchases ) {
            entries.add( new Entry(i, purchaseInfo.getCost(), purchaseInfo) );
            i++;
        }

        LineDataSet dataSet = new LineDataSet(entries, getString(R.string.product_cost) );
        dataSet.setLineWidth(3f);
        dataSet.setHighlightEnabled(true);
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        LineData lineData = new LineData(dataSet);

        ValueFormatter xAxisFormatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int index = (int) value;
                if( value < 0 || value >= purchases.size() ) return "";

                PurchaseInfo info = purchases.get(index);
                if( info != null )
                    return dateFormat.format(info.getPurchaseDate());
                return "N/A";
            }
        };
        XAxis xAxis = viewChart.getXAxis();
        xAxis.setValueFormatter(xAxisFormatter);
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        viewChart.setData(lineData);
        viewChart.notifyDataSetChanged();
    }
}