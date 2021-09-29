package com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.sections.purchase_locations;

import androidx.annotation.DrawableRes;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.Place;
import com.jjak0b.android.trackingmypantry.data.model.PurchaseInfo;
import com.jjak0b.android.trackingmypantry.data.model.relationships.PurchaseInfoWithPlace;
import com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.ProductOverviewViewModel;
import com.jjak0b.android.trackingmypantry.ui.util.GeoUtils;

import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapboxMap;

import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationType;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.annotation.generated.*;
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadedListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PurchaseLocationsFragment extends Fragment implements OnMapLoadedListener {


    private PurchaseLocationsViewModel mViewModel;
    private ProductOverviewViewModel mProductViewModel;
    private PurchasesInPlaceViewModel mPurchasesInPlaceViewModel;
    private MapView mapView;
    private static final Point DEFAULT_CAMERA_POINT = Point.fromLngLat(12.483333, 41.9 ); // Rome
    private static final double DEFAULT_CAMERA_ZOOM = 7.0;
    @DrawableRes
    private static final int DEFAULT_MARKER_ICON = R.drawable.ic_red_marker;
    private PointAnnotationManager pointAnnotationManager;

    public static PurchaseLocationsFragment newInstance() {
        return new PurchaseLocationsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(PurchaseLocationsViewModel.class);
        mPurchasesInPlaceViewModel = new ViewModelProvider(requireParentFragment()).get(PurchasesInPlaceViewModel.class);
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

        AnnotationPlugin annotationApi = mapView.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID);
        pointAnnotationManager = (PointAnnotationManager) annotationApi
                .createAnnotationManager(mapView, AnnotationType.PointAnnotation, null);

    }

    /**
     * Invoked when the Map's style has been fully loaded, and the Map has rendered all visible tiles.
     */
    @Override
    public void onMapLoaded() {
        ViewGroup container = requireView().findViewById(R.id.layout_container);

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

                ArrayList<PointAnnotationOptions> annotationsOptions = new ArrayList<>(purchaseInfos.size());
                HashMap<String, Place> placeHashMap = new HashMap<>( (int)Math.floor(purchaseInfos.size()*0.75)+1 );
                HashMap<String, ArrayList<PurchaseInfo>>  purchasesMap = new HashMap<>((int)Math.floor(purchaseInfos.size()*0.75)+1);
                for ( PurchaseInfoWithPlace purchaseInfo : purchaseInfos ) {
                    if( purchaseInfo.place == null) continue;

                    String placeID = purchaseInfo.place.getId();

                    ArrayList<PurchaseInfo> purchasesInLocation = purchasesMap.get(placeID);

                    if( purchasesInLocation == null ){
                        purchasesInLocation = new ArrayList<>();
                        purchasesMap.put(placeID, purchasesInLocation);
                    }
                    purchasesInLocation.add(purchaseInfo.info);


                    Place place = placeHashMap.get(placeID);
                    if( place == null ){
                        place = purchaseInfo.place;
                        placeHashMap.put(placeID, place);
                    }

                    Point placeCenter = GeoUtils.getCenter(place.getFeature());
                    north = Math.max(north, placeCenter.latitude());
                    south = Math.min(south, placeCenter.latitude());
                    west = Math.min(west, placeCenter.longitude());
                    east = Math.max(east, placeCenter.longitude());
                }

                BoundingBox bbox = BoundingBox.fromLngLats(west, south, east, north);

                for ( Place place : placeHashMap.values() ) {
                    Log.e("Place", place.toJson() );
                    Feature feature = place.getFeature();


                    PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
                            .withPoint(GeoUtils.getCenter(feature))
                            .withIconImage(BitmapFactory
                                    .decodeResource(getResources(), DEFAULT_MARKER_ICON))
                            .withTextField(place.getName());
                    annotationsOptions.add(pointAnnotationOptions);

                    pointAnnotationManager.addClickListener(new OnPointAnnotationClickListener() {
                        @Override
                        public boolean onAnnotationClick(@NonNull PointAnnotation pointAnnotation) {
                            // TODO: for this use case should be better a "PlaceWithPurchases" POJO class

                            mPurchasesInPlaceViewModel.setPurchases(purchasesMap.get(place.getId()));

                            Navigation.findNavController(requireView())
                                    .navigate(PurchaseLocationsFragmentDirections.actionShowPurchasesInPlace());

                            return true;
                        }
                    });
                }

                pointAnnotationManager.create(annotationsOptions);
                mapView.getMapboxMap()
                        .setCamera( new CameraOptions.Builder()
                                .zoom(DEFAULT_CAMERA_ZOOM)
                                .center(GeoUtils.getCenter(bbox))
                                .build()
                        );

            }
        });
    }
}