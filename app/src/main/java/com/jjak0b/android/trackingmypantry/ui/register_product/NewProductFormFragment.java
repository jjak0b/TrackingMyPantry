package com.jjak0b.android.trackingmypantry.ui.register_product;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.ui.products.details.ProductInfoFragment;
import com.jjak0b.android.trackingmypantry.ui.util.ErrorsUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewProductFormFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewProductFormFragment extends ProductInfoFragment {
    private static final String TAG = "NewProductForm";
    private SharedProductViewModel sharedViewModel;
    private String mParamBarcode;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected NewProductFormViewModel initViewModel() {
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedProductViewModel.class);
        return new ViewModelProvider(this).get(NewProductFormViewModel.class);
    }

    private NewProductFormViewModel getViewModel() {
        return (NewProductFormViewModel) mViewModel;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_product_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mParamBarcode = NewProductFormFragmentArgs
                .fromBundle(getArguments()).getBarcode();

        getViewModel().setBarcode(mParamBarcode);

        final FloatingActionButton fabSave = view.findViewById(R.id.fab_action_save);
        final TextInputLayout barcodeInputLayout = view.findViewById(R.id.barcodeInputLayout);

        barcodeInputLayout.setEnabled(false);
        barcodeInputLayout.setStartIconVisible(false);
        barcodeInputLayout.setEndIconVisible(false);

        fabSave.setOnClickListener( v -> getViewModel().save());

        getViewModel().canSave().observe(getViewLifecycleOwner(), fabSave::setEnabled );

        getViewModel().onSave().observe(getViewLifecycleOwner(), isSaving-> {
            if( isSaving ){
                Log.d(TAG, "Saving" );
                getViewModel().saveComplete();
                return;
            }
            // here there should be soem code if some data need to be extracted from view at this time
        });

        getViewModel().onSaved().observe(getViewLifecycleOwner(), saveDataResult -> {
            switch (saveDataResult.getStatus()) {
                case LOADING:

                    break;
                case SUCCESS:
                    notifyResult(saveDataResult);
                    break;
                case ERROR:
                    Log.e(TAG, "Error while saving", saveDataResult.getError());
                    Toast.makeText(
                            requireContext(),
                            saveDataResult.getError().getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                    break;
            }
        });
    }


    private void notifyResult(Resource<Product> result) {

        Log.d(TAG, "submitting: " + result);

        LiveData<Resource<Product>> operation = getViewModel().submit(result.getData());
        operation.observe(getViewLifecycleOwner(), new Observer<Resource<Product>>() {
            @Override
            public void onChanged(Resource<Product> resource) {
                boolean shouldReturnProduct = false;

                switch (resource.getStatus()) {
                    case ERROR:
                        operation.removeObserver(this);
                        shouldReturnProduct = resource.getData() != null;

                        Throwable error = resource.getError();
                        String errorMsg = ErrorsUtils.getErrorMessage(requireContext(), error, TAG);

                        new AlertDialog.Builder(requireContext())
                                .setMessage(errorMsg)
                                .show();

                        // Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG ).show();
                        break;
                    case SUCCESS:
                        operation.removeObserver(this);
                        shouldReturnProduct = true;
                        break;
                    default:
                        sharedViewModel.setProduct(resource);
                        break;
                }

                if( shouldReturnProduct ) {
                    sharedViewModel.setProduct(resource);
                    NewProductFormFragmentDirections.onProductCreated();
                }
            }
        });
    }
}