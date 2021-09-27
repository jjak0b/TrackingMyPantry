package com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.sections.purchase_locations;

import androidx.annotation.DrawableRes;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.Place;
import com.jjak0b.android.trackingmypantry.data.model.PurchaseInfo;
import com.jjak0b.android.trackingmypantry.data.model.relationships.PurchaseInfoWithPlace;
import com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.ProductOverviewViewModel;
import com.jjak0b.android.trackingmypantry.ui.util.PlaceAdapter;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;

import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.CoordinateContainer;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.utils.GeoJsonUtils;
import com.mapbox.maps.CameraBounds;
import com.mapbox.maps.CameraBoundsOptions;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.CoordinateBounds;
import com.mapbox.maps.MapInitOptions;
import com.mapbox.maps.MapboxMap;

import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationPluginImpl;
import com.mapbox.maps.plugin.annotation.AnnotationType;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.annotation.generated.*;
import com.mapbox.maps.plugin.delegates.MapDelegateProvider;
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadedListener;

import java.util.ArrayList;
import java.util.List;

public class PurchaseLocationsFragment extends Fragment implements OnMapLoadedListener {


    private PurchaseLocationsViewModel mViewModel;
    private ProductOverviewViewModel mProductViewModel;
    private MapView mapView;
    private static final Point DEFAULT_CAMERA_POINT = Point.fromLngLat(-52.6885, -70.1395);
    private static final double DEFAULT_CAMERA_ZOOM = 9.0;
    @DrawableRes
    private static final int DEFAULT_MARKER_ICON = R.drawable.mapbox_marker_icon_default;
    private PointAnnotationManager pointAnnotationManager;

    public static PurchaseLocationsFragment newInstance() {
        return new PurchaseLocationsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(PurchaseLocationsViewModel.class);
        mProductViewModel = new ViewModelProvider(requireParentFragment()).get(ProductOverviewViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.purchase_locations_fragment, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProductViewModel.getProduct().observe( getViewLifecycleOwner(), productWithTags -> {
            if( productWithTags == null ){
                mViewModel.setProduct(null);
            }
            else {
                mViewModel.setProduct(productWithTags.product);
            }
        });

        mapView = view.findViewById(R.id.mapView);
        MapboxMap mapboxMap = mapView.getMapboxMap();

        mapboxMap.setCamera(new CameraOptions.Builder()
                .center(DEFAULT_CAMERA_POINT)
                .zoom(DEFAULT_CAMERA_ZOOM)
                .build()
        );
        mapboxMap.addOnMapLoadedListener(this);
        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS, null, null);

        AnnotationPlugin annotationApi = (AnnotationPlugin) new Plugin.Mapbox(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID)
                .getInstance();
        Log.e("BLA", annotationApi.toString() );
        pointAnnotationManager = (PointAnnotationManager) annotationApi
                .createAnnotationManager(mapView, AnnotationType.PointAnnotation, null);
    }

    // Invoked when the Map's style has been fully loaded, and the Map has rendered all visible tiles.
    @Override
    public void onMapLoaded() {

        mViewModel.getPurchaseInfoList().observe(getViewLifecycleOwner(), purchaseInfos -> {
            if( purchaseInfos == null ){
                pointAnnotationManager.deleteAll();
            }
            else {
                pointAnnotationManager.deleteAll();

                double north = -90.0;
                double south = 90.0;
                double west = 180.0;
                double east = -180.0;

                // if( markerViewManager == null ){
                //     markerViewManager = new MarkerViewManager( (MapView) mapFragment.getView(), mapboxMap);
                // }

                // LatLngBounds.Builder cameraBoundsBuilder = new LatLngBounds.Builder();
                ArrayList<PointAnnotationOptions> annotationsOptions = new ArrayList<>(purchaseInfos.size());
                for ( PurchaseInfoWithPlace purchaseInfo : purchaseInfos ) {
                    CarmenFeature place = PlaceAdapter.from(purchaseInfo.place);

                    // LatLng placeCenter = new LatLng(
                    //        place.center().latitude(),
                    //        place.center().longitude()
                    // );

                    // cameraBoundsBuilder.include(placeCenter);

                    // MarkerView markerView = createMarker(placeCenter, purchaseInfo.info, purchaseInfo.place);
                    // markerViewManager.addMarker(markerView);

                    PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
                            .withPoint(place.center())
                            .withIconImage(getResources().getResourceName(DEFAULT_MARKER_ICON))
                            .withTextField(purchaseInfo.place.getName());
                    annotationsOptions.add(pointAnnotationOptions);

                    north = Math.max(north, place.bbox().north());
                    south = Math.min(south, place.bbox().south());
                    west = Math.min(west, place.bbox().west());
                    east = Math.max(east, place.bbox().east());

                }
                pointAnnotationManager.create(annotationsOptions);
                // LatLngBounds cameraBounds = cameraBoundsBuilder.build();
                // mapboxMap.moveCamera(CameraUpdateFactory.newLatLng(cameraBounds.getCenter()));
                // mapboxMap.setLatLngBoundsForCameraTarget(cameraBounds);

                mapView.getMapboxMap()
                        .setBounds(new CameraBoundsOptions.Builder()
                                .bounds(new CoordinateBounds(Point.fromLngLat(west, south), Point.fromLngLat(east, north)))
                                .build()
                        );
                mapView.getMapboxMap()
                        .setCamera( new CameraOptions.Builder()
                                .center(Point.fromLngLat( (west+east)/2.0, (north+south)/2.0 ))
                                .build()
                        );

            }
        });
    }
}