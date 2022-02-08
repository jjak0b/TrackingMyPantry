package com.jjak0b.android.trackingmypantry.ui.util.PlacePicker;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.jjak0b.android.trackingmypantry.data.PlaceSearchSuggestion;

import java.util.Objects;

public class PlaceResultsAdapter<T extends PlaceSearchSuggestion> extends ListAdapter<T, PlaceResultViewHolder> {

    private OnSuggestionListener listener;

    public PlaceResultsAdapter(OnSuggestionListener listener) {
        super(new PLaceDiff<>());
        this.listener = listener;
    }

    @Override
    public long getItemId(int position) {
        return Objects.hashCode(getItem(position).getId());
    }

    @NonNull
    @Override
    public PlaceResultViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return PlaceResultViewHolder.create(viewGroup);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceResultViewHolder viewHolder, int i) {
        PlaceSearchSuggestion suggestion = getItem(i);
        viewHolder.bind(suggestion);
        if( listener != null )
            viewHolder.itemView.setOnClickListener(v -> listener.onSuggestionClick(suggestion) );
        else
            viewHolder.itemView.setOnClickListener(null);
    }

    static class PLaceDiff<T extends PlaceSearchSuggestion> extends DiffUtil.ItemCallback<T> {
        @Override
        public boolean areItemsTheSame(@NonNull T oldItem, @NonNull T newItem) {
            return Objects.equals( oldItem.getId(), newItem.getId() );
        }

        @Override
        public boolean areContentsTheSame(@NonNull T oldItem, @NonNull T newItem) {
            return Objects.equals( oldItem, newItem );
        }
    }

    public interface OnSuggestionListener {
        void onSuggestionClick (PlaceSearchSuggestion suggestion );
    }


}
