package com.jjak0b.android.trackingmypantry.ui.main;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.common.util.concurrent.ListenableFuture;
import com.hadilq.liveevent.LiveEvent;
import com.jjak0b.android.trackingmypantry.data.PantryRepository;
import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstance;
import com.jjak0b.android.trackingmypantry.data.model.ProductTag;
import com.jjak0b.android.trackingmypantry.data.model.PurchaseInfo;
import com.jjak0b.android.trackingmypantry.data.model.relationships.ProductWithTags;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
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

    private MutableLiveData<ProductInstance> productInstance;

    private MutableLiveData<Integer> productInstancesCount;

    private MutableLiveData<PurchaseInfo> productPurchaseInfo;

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
        productInstancesCount = new MutableLiveData<>(1);
        productInstance = new MutableLiveData<>(null);
        productPurchaseInfo = new MutableLiveData<>(null);
    }

    @Override
    protected void onCleared() {
        resetProductDetails();
        resetProductInstance();
        resetPurchaseInfo();
        super.onCleared();
    }

    public void setBarcode(String barcode) {
        this.barcode.setValue( barcode );
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
        ProductInstance pi = new ProductInstance();
        pi.setExpiryDate( new Date() );
        pi.setPantryId( -1 );
        setArticlesCount( 1 );
        productInstance.setValue( pi );
    }

    public void resetPurchaseInfo(){
        productPurchaseInfo.setValue( new PurchaseInfo(
                0f,
                Calendar.getInstance().getTime(),
                null
        ));
    }

    public LiveData<List<Pantry>> getAvailablePantries(){
        return pantryRepository.getPantries();
    }

    public LiveData<ProductInstance> getProductInstance(){
        return productInstance;
    }

    public LiveData<Integer> getArticlesCount(){
        return productInstancesCount;
    }

    public void setArticlesCount( @NotNull Integer count ){
        if( !productInstancesCount.getValue().equals( count ) )
            productInstancesCount.setValue( count );
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

    public ListenableFuture registerProduct() {
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

        return pantryRepository.addProduct(p, assignedTags.getValue());

    }

    public LiveData<ProductWithTags> getProduct() {
        return this.localProduct;
    }
}