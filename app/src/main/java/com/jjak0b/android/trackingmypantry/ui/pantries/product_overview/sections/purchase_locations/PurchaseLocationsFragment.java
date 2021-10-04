package com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.sections.purchase_locations;

import androidx.annotation.DrawableRes;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Bitmap;
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
import com.jjak0b.android.trackingmypantry.data.model.relationships.PlaceWithPurchases;
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


public class PurchaseLocationsFragment extends Fragment implements OnMapLoadedListener {

    private final static String TAG = "PurchaseLocationsFragment";
    private PurchaseLocationsViewModel mViewModel;
    private ProductOverviewViewModel mProductViewModel;
    private PurchasesInPlaceViewModel mPurchasesInPlaceViewModel;
    private MapView mapView;
    private static final Point DEFAULT_CAMERA_POINT = Point.fromLngLat(12.483333, 41.9 ); // Rome
    private static final double DEFAULT_CAMERA_ZOOM = 7.0;
    @DrawableRes
    private static final int DEFAULT_MARKER_ICON = R.drawable.mapbox_marker_icon_default;
    private Bitmap DEFAULT_MARKER_BITMAP;
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
        if( DEFAULT_MARKER_BITMAP == null ){
            DEFAULT_MARKER_BITMAP = BitmapFactory.decodeResource(getResources(), DEFAULT_MARKER_ICON);
        }
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

        mViewModel.getPurchaseInfoList().observe(getViewLifecycleOwner(), purchaseInfos -> {
            if( purchaseInfos == null ){
                pointAnnotationManager.getClickListeners().clear();
                pointAnnotationManager.deleteAll();
            }
            else {
                pointAnnotationManager.deleteAll();

                double north = -90.0;
                double south = 90.0;
                double west = 180.0;
                double east = -180.0;

                for ( PlaceWithPurchases placeWithPurchases : purchaseInfos ) {
                    Place place = placeWithPurchases.place;
                    if( place == null) continue;

                    Point placeCenter = GeoUtils.getCenter(place.getFeature());
                    north = Math.max(north, placeCenter.latitude());
                    south = Math.min(south, placeCenter.latitude());
                    west = Math.min(west, placeCenter.longitude());
                    east = Math.max(east, placeCenter.longitude());

                    Log.e("Place", place.toJson() );
                    Feature feature = place.getFeature();

                    // create waypoint on view
                    PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
                            .withPoint(GeoUtils.getCenter(feature))
                            .withIconImage(DEFAULT_MARKER_BITMAP)
                            .withTextField(place.getName());

                    PointAnnotation pointAnnotation = pointAnnotationManager.create(pointAnnotationOptions);
                    pointAnnotationManager.addClickListener(new OnPointAnnotationClickListener(pointAnnotation.getId(), placeWithPurchases){
                        @Override
                        public boolean onAnnotationClick(@NonNull PointAnnotation pointAnnotation) {
                            if( pointAnnotation.getId() == getAnnotationID()) {
                                mapView.getMapboxMap()
                                        .setCamera( new CameraOptions.Builder()
                                                .center(GeoUtils.getCenter(getPlaceWithPurchases().place.getFeature()))
                                                .build()
                                        );
                                mPurchasesInPlaceViewModel.setPurchases(getPlaceWithPurchases().purchases);
                                Navigation.findNavController(requireView())
                                        .navigate(PurchaseLocationsFragmentDirections.actionShowPurchasesInPlace());
                                return true;
                            }
                            else {
                                return false;
                            }
                        }
                    });
                }

                BoundingBox bbox = BoundingBox.fromLngLats(west, south, east, north);

                mapView.getMapboxMap()
                        .setCamera( new CameraOptions.Builder()
                                .zoom(DEFAULT_CAMERA_ZOOM)
                                .center(GeoUtils.getCenter(bbox))
                                .build()
                        );

            }
        });
    }

    private abstract class OnPointAnnotationClickListener implements com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener {
        private PlaceWithPurchases placeWithPurchases;
        private long annotationID;
        public OnPointAnnotationClickListener(long annotationID, @NonNull PlaceWithPurchases placeWithPurchases) {
            this.placeWithPurchases = placeWithPurchases;
            this.annotationID = annotationID;
        }

        public PlaceWithPurchases getPlaceWithPurchases() {
            return placeWithPurchases;
        }

        public long getAnnotationID() {
            return annotationID;
        }

        @Override
        public abstract boolean onAnnotationClick(@NonNull PointAnnotation pointAnnotation);
    }
}