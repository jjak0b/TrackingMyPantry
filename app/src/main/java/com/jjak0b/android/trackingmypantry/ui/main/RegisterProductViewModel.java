package com.jjak0b.android.trackingmypantry.ui.main;

import android.app.Application;
import android.util.Log;

import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.common.util.concurrent.ListenableFuture;
import com.jjak0b.android.trackingmypantry.data.PantryRepository;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.ProductTag;
import com.jjak0b.android.trackingmypantry.data.model.relationships.ProductWithTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RegisterProductViewModel extends AndroidViewModel {

    private MutableLiveData<String> barcode;

    private PantryRepository pantryRepository;

    private LiveData<List<Product>> matchingProductsList;

    private LiveData<ProductWithTags> localProduct;

    private Product originalProduct;

    private MutableLiveData<Product.Builder> productBuilder;

    private MutableLiveData<List<ProductTag>> assignedTags;

    public RegisterProductViewModel(Application application) {
        super(application);
        pantryRepository = PantryRepository.getInstance(application);
        barcode = new MutableLiveData<>();
        productBuilder = new MutableLiveData<>();
        matchingProductsList = pantryRepository.getMatchingProducts();
        localProduct = Transformations.switchMap(
                this.productBuilder,
                new Function<Product.Builder, LiveData<ProductWithTags>>() {
                    @Override
                    public LiveData<ProductWithTags> apply(Product.Builder input) {
                        return pantryRepository.getProductWithTags( input.getProductId());
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
    }

    public void setBarcode(String barcode) {
        this.barcode.setValue( barcode );
        pantryRepository.updateMatchingProducts(barcode);
    }

    public LiveData<Product.Builder> getProductBuilder() {
         return productBuilder;
    }

    public LiveData<String> getBarcode() { return barcode; }


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
        Product.Builder productBuilder = new Product.Builder()
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