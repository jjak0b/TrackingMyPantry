package com.jjak0b.android.trackingmypantry.ui.maps;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonObject;
import com.jjak0b.android.trackingmypantry.R;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.plugins.places.common.PlaceConstants;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

/**
 * Use the places plugin to take advantage of Mapbox's location search ("geocoding") capabilities. The plugin
 * automatically makes geocoding requests, has built-in saved locations, includes location picker functionality,
 * and adds beautiful UI into your Android project.
 */
public class PlacesPluginActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int COUNT_MAX_SEARCH_RESULTS = 10;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private CarmenFeature previous;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private String symbolIconId = "symbolIconId";

    private PlacesPluginViewModel mViewModel;
    private ActivityResultLauncher<Intent> searchIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_places_plugin);

        mViewModel = new ViewModelProvider(this).get(PlacesPluginViewModel.class);

        searchIntent = registerForActivityResult( new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {

                // Retrieve selected location's CarmenFeature
                CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(result.getData());
                mViewModel.setPlace(selectedCarmenFeature);
            }
        });

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                initSearchFab();
                initConfirmFab();

                // addUserLocations();

                // Add the symbol layer icon to map for future use
                style.addImage(symbolIconId, BitmapFactory.decodeResource(
                        PlacesPluginActivity.this.getResources(), R.drawable.mapbox_marker_icon_default));

                // Create an empty GeoJSON source using the empty feature collection
                setUpSource(style);

                // Set up a new symbol layer for displaying the searched location's feature coordinates
                setupLayer(style);

                // Create a new FeatureCollection and add a new Feature to it using selectedCarmenFeature above.
                // Then retrieve and update the source designated for showing a selected location's symbol layer icon
                mViewModel.getPlace().observe(PlacesPluginActivity.this, selectedCarmenFeature -> {
                    if (selectedCarmenFeature != null ) {
                        GeoJsonSource source = style.getSourceAs(geojsonSourceLayerId);
                        if (source != null) {
                            source.setGeoJson(FeatureCollection.fromFeatures(new Feature[] {
                                    Feature.fromJson(selectedCarmenFeature.toJson())
                            }));
                        }

                        // Move map camera to the selected location
                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(new LatLng(
                                                ((Point)selectedCarmenFeature.geometry()).latitude(),
                                                ((Point)selectedCarmenFeature.geometry()).longitude()
                                        ))
                                        .zoom(14)
                                        .build()), 4000);
                    }
                });
            }
        });
    }

    private void initSearchFab() {

        FloatingActionButton fab = findViewById(R.id.fab_location_search);

        fab.setOnClickListener(view -> {
            Intent intent = new PlaceAutocomplete.IntentBuilder()
                    .accessToken(Mapbox.getAccessToken())
                    .placeOptions(PlaceOptions.builder()
                            .limit(COUNT_MAX_SEARCH_RESULTS)
                            .build(PlaceOptions.MODE_CARDS))
                    .build(PlacesPluginActivity.this);
            searchIntent.launch(intent);
        });
    }

    private void initConfirmFab() {

        FloatingActionButton fab = findViewById(R.id.fab_location_confirm);
        mViewModel.getPlace().observe( this, carmenFeature -> {
            if( carmenFeature == null ){
                fab.setEnabled(false);
            }

            else {
                fab.setEnabled(true);
            }

            fab.setOnClickListener(v -> {
                if( carmenFeature != null ) {
                    setResult(
                            RESULT_OK,
                            new Intent().putExtra(
                                    PlaceConstants.RETURNING_CARMEN_FEATURE,
                                    carmenFeature.toJson()
                            )
                    );
                    finish();
                }
            });
        });
    }

    private void addUserLocations() {

        // maybe if called provide a GeoJSON i could provide a "previous" value
        previous = CarmenFeature.builder().text("Previous")
                .geometry(Point.fromLngLat(-122.3964485, 37.7912561))
                .placeName("Mistery Place")
                .id("mapbox-mistery")
                .properties(new JsonObject())
                .build();
    }

    private void setUpSource(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource(geojsonSourceLayerId));
    }

    private void setupLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addLayer(new SymbolLayer("SYMBOL_LAYER_ID", geojsonSourceLayerId).withProperties(
                iconImage(symbolIconId),
                iconOffset(new Float[] {0f, -8f})
        ));
    }

    public static CarmenFeature getPlace(Intent result) {
        return CarmenFeature.fromJson( result.getStringExtra(PlaceConstants.RETURNING_CARMEN_FEATURE) );
    }

    // Add the mapView lifecycle to the activity's lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
