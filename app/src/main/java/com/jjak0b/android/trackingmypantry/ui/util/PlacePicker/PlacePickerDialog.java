package com.jjak0b.android.trackingmypantry.ui.util.PlacePicker;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.PlaceSearchSuggestion;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.db.entities.Place;
import com.jjak0b.android.trackingmypantry.ui.util.ErrorsUtils;
import com.jjak0b.android.trackingmypantry.ui.util.InputUtil;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlacePickerDialog extends DialogFragment implements PlaceResultsAdapter.OnSuggestionListener {

    private static final String TAG = "PlacePickerDialog";

    PlaceResultsAdapter<PlaceSearchSuggestion> suggestionAdapter;
    PlacePickerViewModel viewModel;
    SharedPlaceViewModel sharedViewModel;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PlacePickerViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedPlaceViewModel.class);

        suggestionAdapter = new PlaceResultsAdapter<>(this);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.place_picker_dialog, container);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(R.string.place_picker_title);
        return dialog;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView infoView = view.findViewById(R.id.info);
        infoView.setText(R.string.loading);

        TextInputEditText searchView = view.findViewById(R.id.search);

        searchView.requestFocus();

        RecyclerView listView = view.findViewById(R.id.list);
        /**
         * Warning error:
         * Without this, can cause java.lang.IllegalArgumentException: parameter must be a descendant of this view
         * if Search is triggered, then the focus will auto move to the following item view in the list.
         * But if a search has been already done, then old items will be replaced while focus is on a previous list item.
         * This will cause this exception, so a way to remove the focus is needed before replacing the item view
         * source: https://stackoverflow.com/a/40659632
         */
        listView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        listView.addRecyclerListener(viewHolder -> {
            if( viewHolder.itemView.hasFocus() ) {
                viewHolder.itemView.clearFocus();
            }
        });

        listView.setLayoutManager(new LinearLayoutManager(requireContext()));
        listView.setAdapter(suggestionAdapter);

        searchView.addTextChangedListener(new InputUtil.FieldTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setSearchQuery(s.toString());
            }
        });

        searchView.setOnEditorActionListener((v, actionId, event) -> {
            // Return true if you have consumed the action, else false.
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                viewModel.search();
                return true;
            }
            return false;
        });

        viewModel.getSearchQuery().observe(getViewLifecycleOwner(), query -> {
            InputUtil.setText(searchView, query);
        });

        viewModel.getPlaceSuggestions().observe(getViewLifecycleOwner(), resource -> {

            List<? extends PlaceSearchSuggestion> list = resource.getData();
            boolean shouldSubmitList = list != null;

            switch (resource.getStatus()) {
                case LOADING:
                    infoView.setVisibility(View.VISIBLE);
                    infoView.setText(R.string.loading);

                    break;
                default:
                    infoView.setVisibility(View.VISIBLE);
                    int size = list != null ? list.size() : 0;
                    infoView.setText(getResources().getQuantityString(R.plurals.matches_found, size, size));
                    break;
            }

            if( shouldSubmitList ) {

                List<PlaceSearchSuggestion> dummy = Collections.emptyList();
                dummy = Stream.concat(dummy.stream(), list.stream())
                        .collect(Collectors.toList());

                suggestionAdapter.submitList(dummy);
            }
        });

    }

    @Override
    public void onSuggestionClick(PlaceSearchSuggestion suggestion) {
        Log.d(TAG, "Place at position " + suggestion.getId() + " has been picked");

        LiveData<Resource<Place>> mSource = viewModel.getPlace(suggestion);
        mSource.observe(getViewLifecycleOwner(), resource -> {
            Log.d(TAG, "PLace " + resource );
            switch (resource.getStatus()) {
                case LOADING:
                    break;
                case SUCCESS:
                    if( resource.getData() != null ) {
                        sharedViewModel.setItemSource(mSource);
                        dismiss();
                    }
                    break;
                case ERROR:
                    if( resource.getData() == null ) {
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle(android.R.string.dialog_alert_title)
                                .setMessage(ErrorsUtils.getErrorMessage(requireContext(), resource.getError(), TAG))
                                .setPositiveButton(android.R.string.ok, null)
                                .setCancelable(false)
                                .show();
                    }
                    else {
                        sharedViewModel.setItemSource(new MutableLiveData<>(
                                Resource.success(resource.getData())
                        ));
                        dismiss();
                    }

                    break;
            }
        });
    }
}
