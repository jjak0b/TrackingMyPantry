package com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.purchase_locations;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.db.entities.Place;
import com.jjak0b.android.trackingmypantry.data.db.entities.PurchaseInfo;
import com.jjak0b.android.trackingmypantry.ui.register_product.SharedProductViewModel;
import com.jjak0b.android.trackingmypantry.ui.util.ErrorsUtils;
import com.jjak0b.android.trackingmypantry.ui.util.GeoUtils;
import com.jjak0b.android.trackingmypantry.ui.util.ImageUtil;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.observable.eventdata.MapLoadedEventData;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationType;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadedListener;

import java.util.List;
import java.util.Map;


public class PurchaseLocationsFragment extends Fragment implements OnMapLoadedListener {

    private final static String TAG = "PurchaseLocationsFragment";
    private PurchaseLocationsViewModel mViewModel;
    private SharedProductViewModel mProductViewModel;
    private PurchasesInPlaceViewModel mPurchasesInPlaceViewModel;
    private MapView mapView;
    private static final Point DEFAULT_CAMERA_POINT = Point.fromLngLat(12.483333, 41.9 ); // Rome
    private static final double DEFAULT_CAMERA_ZOOM = 7.0;
    @DrawableRes
    private static final int DEFAULT_MARKER_ICON_RES = R.drawable.ic_baseline_location_on;
    private Bitmap DEFAULT_MARKER_ICON_VALUE;
    private PointAnnotationManager pointAnnotationManager;

    public static PurchaseLocationsFragment newInstance() {
        return new PurchaseLocationsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(PurchaseLocationsViewModel.class);
        mPurchasesInPlaceViewModel = new ViewModelProvider(requireParentFragment()).get(PurchasesInPlaceViewModel.class);
        mProductViewModel = new ViewModelProvider(requireParentFragment()).get(SharedProductViewModel.class);
        updateMarkerIcon();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.purchase_locations_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mProductViewModel.getItem().observe( getViewLifecycleOwner(), mViewModel::setProduct);

        mapView = view.findViewById(R.id.mapView);
        MapboxMap mapboxMap = mapView.getMapboxMap();

        mapboxMap.setCamera(new CameraOptions.Builder()
                .center(DEFAULT_CAMERA_POINT)
                .zoom(DEFAULT_CAMERA_ZOOM)
                .build()
        );
        mapboxMap.addOnMapLoadedListener(this);
        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS, null, null);
        AnnotationPlugin annotationApi = mapView.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID);
        pointAnnotationManager = (PointAnnotationManager) annotationApi
                .createAnnotationManager(AnnotationType.PointAnnotation, null);

    }

    /**
     * Invoked when the Map's style has been fully loaded, and the Map has rendered all visible tiles.
     */
    @Override
    public void onMapLoaded(@NonNull MapLoadedEventData mapLoadedEventData) {

        mViewModel.getPurchaseInfoList().removeObservers(getViewLifecycleOwner());
        mViewModel.getPurchaseInfoList().observe(getViewLifecycleOwner(), resource-> {
            switch (resource.getStatus()) {
                case LOADING:
                    pointAnnotationManager.getClickListeners().clear();
                    pointAnnotationManager.deleteAll();
                    break;
                case ERROR:
                    new AlertDialog.Builder(requireContext())
                            .setTitle(android.R.string.dialog_alert_title)
                            .setMessage(ErrorsUtils.getErrorMessage(requireContext(), resource.getError(), TAG))
                            .show();
                    break;
                case SUCCESS:
                    if( resource.getData() == null || resource.getData().size() < 1 ) {
                        new AlertDialog.Builder(requireContext())
                                .setMessage(R.string.place_no_purchases_available)
                                .show();
                    }
                    else {
                        double north = -90.0;
                        double south = 90.0;
                        double west = 180.0;
                        double east = -180.0;

                        for (Map.Entry<Place, List<PurchaseInfo>> entry : resource.getData().entrySet() ) {
                            Place place = entry.getKey();
                            if( place == null ) continue;
                            Point placeCenter = place.getFeature();
                            if( placeCenter == null ) continue;

                            north = Math.max(north, placeCenter.latitude());
                            south = Math.min(south, placeCenter.latitude());
                            west = Math.min(west, placeCenter.longitude());
                            east = Math.max(east, placeCenter.longitude());

                            addMarker(place, entry);
                        }

                        BoundingBox bbox = BoundingBox.fromLngLats(west, south, east, north);

                        mapView.getMapboxMap()
                                .setCamera( new CameraOptions.Builder()
                                        .zoom(DEFAULT_CAMERA_ZOOM)
                                        .center(GeoUtils.getCenter(bbox))
                                        .build()
                                );

                    }
                    break;
            }
        });
    }

    private void updateMarkerIcon() {
        DEFAULT_MARKER_ICON_VALUE = ImageUtil.getBitmap(AppCompatResources.getDrawable(
                requireContext(), DEFAULT_MARKER_ICON_RES));
    }

    private void addMarker(Place place, Map.Entry<Place, List<PurchaseInfo>> entry) {
        // create waypoint on view
        PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
                .withPoint(place.getFeature())
                .withIconImage(DEFAULT_MARKER_ICON_VALUE)
                .withTextField(place.getName());

        PointAnnotation pointAnnotation = pointAnnotationManager.create(pointAnnotationOptions);
        pointAnnotationManager.addClickListener(pointAnnotationClicked -> {
            if( pointAnnotationClicked.getId() == pointAnnotation.getId()) {
                mapView.getMapboxMap()
                        .setCamera( new CameraOptions.Builder()
                                .center(place.getFeature())
                                .build()
                        );
                mPurchasesInPlaceViewModel.setPurchases(entry.getValue());
                Navigation.findNavController(requireView())
                        .navigate(PurchaseLocationsFragmentDirections.actionShowPurchasesInPlace());
                return true;
            }
            else {
                return false;
            }
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Update if orientation changes
        updateMarkerIcon();
    }

    @Override
    public void onStart() {
        super.onStart();
        if( mapView != null) mapView.onStart();
    }

    @Override
    public void onStop() {
        if( mapView != null) mapView.onStop();
        super.onStop();
    }

    @Override
    public void onLowMemory() {
        if( mapView != null) mapView.onLowMemory();
        super.onLowMemory();
    }

    @Override
    public void onDestroy() {
        if( mapView != null) mapView.onDestroy();
        super.onDestroy();
    }
}