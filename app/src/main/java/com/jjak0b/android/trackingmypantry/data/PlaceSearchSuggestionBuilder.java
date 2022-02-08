package com.jjak0b.android.trackingmypantry.data;

import androidx.annotation.NonNull;

import com.jjak0b.android.trackingmypantry.data.db.Converters;
import com.jjak0b.android.trackingmypantry.data.db.entities.Address;
import com.jjak0b.android.trackingmypantry.data.db.entities.Place;
import com.mapbox.search.result.SearchAddress;
import com.mapbox.search.result.SearchSuggestion;

import java.util.Objects;

public class PlaceSearchSuggestionBuilder {

    public static APISearchSuggestion create(@NonNull SearchSuggestion result) {
        return new APISearchSuggestion(result);
    }

    public static FallbackSearchSuggestion create(@NonNull Place result) {
        return new FallbackSearchSuggestion(result);
    }

    private static String getAddressDescription(@NonNull SearchAddress address ) {
        return address.formattedAddress(SearchAddress.FormatStyle.Medium.INSTANCE);
    }

    public static class APISearchSuggestion implements PlaceSearchSuggestion {

        @NonNull
        private SearchSuggestion result;

        public APISearchSuggestion(@NonNull SearchSuggestion result) {
            super();
            this.result = result;
        }

        @Override
        public String getId() {
            return result.getId();
        }

        @Override
        public String getName() {
            return result.getName();
        }

        @Override
        public String getAddressDescription() {
            if( result.getAddress() != null ) {
                return PlaceSearchSuggestionBuilder.getAddressDescription(result.getAddress());
            }
            else {
                return result.getDescriptionText();
            }
        }

        @NonNull
        public SearchSuggestion getResult() {
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof APISearchSuggestion)) return false;
            APISearchSuggestion that = (APISearchSuggestion) o;
            return Objects.equals(result, that.result);
        }

        @Override
        public int hashCode() {
            return Objects.hash(result);
        }
    }


    public static class FallbackSearchSuggestion implements PlaceSearchSuggestion {

        @NonNull
        private Place result;

        public FallbackSearchSuggestion(@NonNull Place result) {
            super();
            this.result = result;
        }

        @Override
        public String getId() {
            return result.getId();
        }

        @Override
        public String getName() {
            return result.getName();
        }

        @Override
        public String getAddressDescription() {
            Address address = result.getAddress();
            if( address != null ) {

                return PlaceSearchSuggestionBuilder.getAddressDescription(
                        Converters.fromRAddress(address)
                );
            }
            else {
                return getName();
            }
        }

        @NonNull
        public Place getResult() {
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FallbackSearchSuggestion)) return false;
            FallbackSearchSuggestion that = (FallbackSearchSuggestion) o;
            return Objects.equals(result, that.result);
        }

        @Override
        public int hashCode() {
            return Objects.hash(result);
        }
    }

}
