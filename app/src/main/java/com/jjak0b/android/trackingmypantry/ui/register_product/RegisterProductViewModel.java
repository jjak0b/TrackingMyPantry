package com.jjak0b.android.trackingmypantry.ui.register_product;

import android.app.Application;

import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;

import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.hadilq.liveevent.LiveEvent;
import com.jjak0b.android.trackingmypantry.data.model.repositories.PantryRepository;
import com.jjak0b.android.trackingmypantry.data.model.entities.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.entities.Place;
import com.jjak0b.android.trackingmypantry.data.model.entities.Product;
import com.jjak0b.android.trackingmypantry.data.model.entities.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.data.model.entities.ProductTag;
import com.jjak0b.android.trackingmypantry.data.model.entities.PurchaseInfo;
import com.jjak0b.android.trackingmypantry.data.model.relationships.ProductWithTags;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class RegisterProductViewModel extends AndroidViewModel {

    private MutableLiveData<String> barcode;

    private PantryRepository pantryRepository;

    private LiveData<List<Product>> matchingProductsList;

    private LiveEvent<List<Product>> onUpdateMatchingProductsList;

    private LiveData<ProductWithTags> localProduct;

    private Product originalProduct;

    private MutableLiveData<Product.Builder> productBuilder;

    private MutableLiveData<List<ProductTag>> assignedTags;

    private MutableLiveData<Pantry> customPantry;

    private LiveData<Pantry> defaultPantry;

    private MediatorLiveData<Pantry> assignedPantry;

    private MutableLiveData<ProductInstanceGroup> productInstance;

    private MutableLiveData<Integer> productInstancesCount;

    private MutableLiveData<PurchaseInfo> productPurchaseInfo;

    private MutableLiveData<Place> purchasePlace;

    public RegisterProductViewModel(Application application) {
        super(application);
        pantryRepository = PantryRepository.getInstance(application);
        barcode = new MutableLiveData<>();
        productBuilder = new MutableLiveData<>();
        onUpdateMatchingProductsList = new LiveEvent<>();

        matchingProductsList = pantryRepository.getMatchingProducts();
        onUpdateMatchingProductsList.addSource( matchingProductsList, products -> {
            onUpdateMatchingProductsList.setValue( products );
        });

        localProduct = Transformations.switchMap(
                this.productBuilder,
                new Function<Product.Builder, LiveData<ProductWithTags>>() {
                    @Override
                    public LiveData<ProductWithTags> apply(Product.Builder input) {
                        if( input != null )
                            return pantryRepository.getProductWithTags( input.getProductId() );
                        else
                            return new MutableLiveData<>( null );
                    }
                }
        );
        assignedTags = (MutableLiveData<List<ProductTag>>) Transformations.map(
                localProduct,
                new Function<ProductWithTags, List<ProductTag>>() {
                    @Override
                    public List<ProductTag> apply(ProductWithTags input) {
                        if( input != null )
                            return new ArrayList<>(input.tags);
                        else
                            return new ArrayList<>(0);
                    }
                }
        );

        defaultPantry = pantryRepository.getDefaultPantry();
        customPantry = new MutableLiveData<>(null);
        // provide always a correct value for a pantry
        assignedPantry = new MediatorLiveData<>();
        assignedPantry.addSource(customPantry, pantry -> {
            if( pantry == null ){
                defaultPantry.observeForever(new Observer<Pantry>() {
                    @Override
                    public void onChanged(Pantry pantry) {
                        defaultPantry.removeObserver(this::onChanged);
                        assignedPantry.postValue(pantry);
                    }
                });
            }
            else {
                assignedPantry.postValue(pantry);
            }
        });

        productInstancesCount = new MutableLiveData<>(1);
        productInstance = new MutableLiveData<>(null);
        productPurchaseInfo = new MutableLiveData<>(null);
        purchasePlace = (MutableLiveData<Place>)Transformations.switchMap(
                this.productPurchaseInfo,
                new Function<PurchaseInfo, LiveData<Place>>() {
                    @Override
                    public LiveData<Place> apply(PurchaseInfo input) {
                        if( input != null && input.getPlaceId() != null )
                            return pantryRepository.getPlace(input.getPlaceId());
                        else
                            return new MutableLiveData<>(null);
                    }
                }
        );
    }

    @Override
    protected void onCleared() {
        assignedPantry.removeSource(customPantry);
        resetProductDetails();
        resetProductInstance();
        resetPurchaseInfo();
        customPantry.setValue(null);
        customPantry = null;
        defaultPantry = null;
        super.onCleared();
    }

    public void setupNewProduct() {
        resetProductDetails();
        resetProductInstance();
        resetPurchaseInfo();
    }

    public void setEmptyProduct() {
        setProduct( new Product.Builder()
                .setBarcode( getBarcode().getValue() )
                .build());
    }

    public void setBarcode(String barcode) {
        this.barcode.setValue( barcode );
        // TODO: Notify View on fail
        pantryRepository.updateMatchingProducts(barcode);
    }

    public LiveData<Product.Builder> getProductBuilder() {
         return productBuilder;
    }

    public LiveData<String> getBarcode() { return barcode; }

    public void resetProductDetails(){
        setBarcode(null);
        setProduct(null);
    }

    public void resetProductInstance(){
        ProductInstanceGroup pi = new ProductInstanceGroup();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date date = calendar.getTime();
        pi.setExpiryDate( calendar.getTime() );
        pi.setPantryId( 0 );
        pi.setQuantity( 1 );
        productInstance.setValue( pi );
        setPantry(null);
    }

    public void resetPurchaseInfo(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        productPurchaseInfo.setValue( new PurchaseInfo(
                "",
                0f,
                calendar.getTime(),
                null
        ));
        purchasePlace.setValue(null);
    }

    public LiveData<List<Pantry>> getAvailablePantries(){
        return pantryRepository.getPantries();
    }

    public LiveData<ProductInstanceGroup> getProductInstance(){
        return productInstance;
    }

    public MutableLiveData<PurchaseInfo> getProductPurchaseInfo() {
        return productPurchaseInfo;
    }

    public LiveData<List<Product>> onUpdateMatchingProductsList() {
        return onUpdateMatchingProductsList;
    }

    public LiveData<List<Product>> getProducts() {
        return matchingProductsList;
    }

    public void setAssignedTags( List<ProductTag> tags ) {
        assignedTags.setValue( tags );
    }

    public LiveData<List<ProductTag>> getSuggestionTags() {
        return pantryRepository.getAllProductTags();
    }

    public LiveData<List<ProductTag>> getAssignedTags() {
        return assignedTags;
    }

    public void setProduct(Product product) {

        this.originalProduct = product;
        Product.Builder productBuilder = null;

        if( product != null )
            productBuilder = new Product.Builder()
                    .from( product );
        this.productBuilder.setValue( productBuilder );

        if( product != null && product.getId() != null ) {
            for (Product p : Objects.requireNonNull(matchingProductsList.getValue())) {
                if (product.getId().equals( p.getId() ) ) {
                    pantryRepository.voteProduct(p.getId(), 1);
                }
                // Note: API allow only 1 vote per product list
                // else {
                //      pantryRepository.voteProduct(p.getId(), -1);
                //}
            }
        }
    }

    public LiveData<Pantry> getPantry() {
        return assignedPantry;
    }

    public void setPantry( Pantry p ) {
        customPantry.setValue(p);
    }

    public void setPurchasePlace( Place place ) {
        if( !Objects.equals(place, purchasePlace.getValue()) )
            purchasePlace.setValue(place);
    }

    public LiveData<Place> getPurchasePlace() {
        return purchasePlace;
    }

    public ListenableFuture<ProductInstanceGroup> registerProduct() {
        Product p = new Product.Builder()
                .from(
                        productBuilder.getValue()
                                .build()
                ).build();

        // if content of the edited product is different from the original one fetched from matching list
        // then clear the id and so this will consider as new product
        boolean isProductEdited = !p.equals( this.originalProduct );
        if( isProductEdited ) {
            p.setId(null);
        }

        Pantry pantry = getPantry().getValue();
        ProductInstanceGroup group = getProductInstance().getValue();
        PurchaseInfo purchaseInfo = getProductPurchaseInfo().getValue();
        Place purchasePlace = getPurchasePlace().getValue();
        ListenableFuture<Product> futureProduct = pantryRepository.addProduct(p, assignedTags.getValue());

        ListenableFuture<List<Object>> futureProductInstanceGroupParams = Futures.allAsList(
                futureProduct,
                pantryRepository.addPantry( pantry )
        );
        ListenableFuture<Long> futureProductInstanceGroupID = Futures.transformAsync(
                futureProductInstanceGroupParams,
                new AsyncFunction<List<Object>, Long>() {
                    @Override
                    public ListenableFuture<Long> apply(@NullableDecl List<Object> results) {
                        Iterator<Object> it = results.iterator();
                        Product product = (Product) it.next();
                        Pantry pantry = (Pantry) it.next();
                        if( pantry != null && product != null )
                            return pantryRepository.addProductInstanceGroup(group, product, pantry);
                        else {
                            return Futures.immediateFailedFuture(new NullPointerException());
                        }
                    }
                },
                MoreExecutors.directExecutor()
        );
        ListenableFuture<ProductInstanceGroup> futureGroupWithId = Futures.transform(
                futureProductInstanceGroupID,
                new com.google.common.base.Function<Long, ProductInstanceGroup>() {
                    @NullableDecl
                    @Override
                    public ProductInstanceGroup apply(@NullableDecl Long id) {
                        group.setId( id );
                        return group;
                    }
                },
                MoreExecutors.directExecutor()
        );

        ListenableFuture<List<Object>> futurePurchaseInfoParams = Futures.allAsList(
                purchasePlace != null ? pantryRepository.addPlace(purchasePlace) : Futures.immediateFuture(null),
                futureProduct
        );
        ListenableFuture<Long> futureAddPurchaseInfo = Futures.transformAsync(
                futurePurchaseInfoParams,
                new AsyncFunction<List<Object>, Long>() {
                    @Override
                    public ListenableFuture<Long> apply(@NullableDecl List<Object> results) {
                        Iterator<Object> it = results.iterator();
                        Place place = (Place) it.next();
                        Product product = (Product) it.next();

                        if( purchaseInfo != null ) {
                            if( product != null ){
                                purchaseInfo.setProductId(product.getId());
                            }

                            if( place != null ){
                                purchaseInfo.setPlaceId(place.getId());
                            }

                            return pantryRepository.addPurchaseInfo(purchaseInfo);
                        }
                        else {
                            return Futures.immediateFailedFuture(new NullPointerException());
                        }
                    }
                },
                MoreExecutors.directExecutor()
        );

        return Futures.transform( Futures.allAsList(futureGroupWithId, futureAddPurchaseInfo),
                input -> {
                    Iterator<Object> it = input.iterator();
                    ProductInstanceGroup item1 = (ProductInstanceGroup) it.next();
                    return item1;
                },
                MoreExecutors.directExecutor()
        );
    }

    public LiveData<ProductWithTags> getProduct() {
        return this.localProduct;
    }

    public ProductValidator getValidator() {
        return new ProductValidator();
    }

    public class ProductValidator {

        private boolean validityProductBuilder;
        private boolean validityName;
        private boolean validityDescription;
        private boolean validityImage;
        private boolean validityTags;

        private boolean validityProductInstance;
        private boolean validityQuantity;
        private boolean validityExpireDate;
        private boolean validityPantry;

        private boolean validityProductPurchaseInfo;
        private boolean validityCost;
        private boolean validityPurchaseDate;
        private boolean validityPurchaseLocation;

        public void updateProductBuilderValidity() {
            validityProductBuilder = isProductBuilderReady();
            validityName = isNameValid();
            validityDescription = isDescriptionValid();
            validityImage = isImageValid();
            validityTags = areTagsValid();
        }

        public void updateProductInstanceValidity() {
            validityProductPurchaseInfo = isProductPurchaseInfoReady();
            validityQuantity = isQuantityValid();
            validityExpireDate = isExpireDateValid();
        }

        public boolean isProductBuilderReady() {
            return getProductBuilder().getValue() != null;
        }

        public boolean isProductInstanceReady() {
            return getProductInstance().getValue() != null;
        }

        public boolean isProductPurchaseInfoReady() {
            return getProductPurchaseInfo().getValue() != null;
        }

        public boolean isTextFieldValueValid( String value, boolean isRequired ) {
            if( value == null ){
                return !isRequired;
            }
            else {
                value = value.trim();
                if( value.length() < 1) {
                    return !isRequired;
                }

                return true;
            }
        }

        public boolean isNameValid() {

            if( !isProductBuilderReady() ){
                return false;
            }

            String name = productBuilder.getValue().getName();

            return isTextFieldValueValid( name, true );
        }
        public boolean isDescriptionValid() {

            if( !isProductBuilderReady() ){
                return false;
            }

            String description = productBuilder.getValue().getDescription();

            return isTextFieldValueValid( description, false );
        }

        public boolean areTagsValid() {
            return isProductBuilderReady();
        }

        public boolean isImageValid() {
            if( !isProductBuilderReady() ){
                return false;
            }

            String img = productBuilder.getValue().getImg();

            return isTextFieldValueValid( img, false );
        }

        public boolean areProductDetailsValid() {
            return isProductBuilderReady() && isNameValid() && isDescriptionValid() && areTagsValid() && isImageValid();
        }

        public boolean isQuantityValid() {
            return getProductInstance().getValue().getQuantity() > 0;
        }

        public boolean isExpireDateValid() {
            Date d = getProductInstance().getValue().getExpiryDate();
            return d != null;
        }

        public boolean isPantryValid() {
            return getProductInstance().getValue().getPantryId() > 0;
        }

        public boolean areProductInstanceDetailsValid() {
            return isProductInstanceReady() && isQuantityValid() && isExpireDateValid() && isPantryValid();
        }

        public boolean isCostValid() {
            return getProductPurchaseInfo().getValue().getCost() >= 0f;
        }

        public boolean isPurchaseDateValid() {
            Date d = getProductPurchaseInfo().getValue().getPurchaseDate();
            return d != null;
        }

        public boolean isPurchaseLocationValid() {
            // GeoLocation l = getProductPurchaseInfo().getValue().getPurchaseLocation();
            return true;
        }

        public boolean areProductPurchaseInfoValid() {
            return isProductPurchaseInfoReady() && isCostValid() && isPurchaseDateValid() && isPurchaseLocationValid();
        }
    }
}