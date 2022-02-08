package com.jjak0b.android.trackingmypantry.ui.util.PlacePicker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jjak0b.android.trackingmypantry.data.PlaceSearchSuggestion;

public class PlaceResultViewHolder extends RecyclerView.ViewHolder {

    private TextView nameView;
    private TextView descriptionView;

    public PlaceResultViewHolder(@NonNull View itemView) {
        super(itemView);
        nameView = itemView.findViewById(android.R.id.text1);
        descriptionView = itemView.findViewById(android.R.id.text2);
    }


    public static PlaceResultViewHolder create(@NonNull ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.two_line_list_item, parent, false);
        return new PlaceResultViewHolder(view);
    }

    public void bind(PlaceSearchSuggestion suggestion) {
        String title = suggestion.getName();
        String subtitle = suggestion.getAddressDescription();
        nameView.setText(title);
        descriptionView.setText(subtitle);
    }
}
