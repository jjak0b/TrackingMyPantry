package com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.pantries.products_groups;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Status;
import com.jjak0b.android.trackingmypantry.data.db.entities.Pantry;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.pantries.SharedPantryViewModel;
import com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.pantries.products_groups.model.ProductInstanceGroupInteractionsListener;
import com.jjak0b.android.trackingmypantry.ui.register_product.SharedProductViewModel;
import com.jjak0b.android.trackingmypantry.ui.util.ErrorsUtils;
import com.jjak0b.android.trackingmypantry.ui.util.InputUtil;
import com.jjak0b.android.trackingmypantry.ui.util.QuantityPickerBuilder;
import com.jjak0b.android.trackingmypantry.ui.util.SelectItemDialogBuilder;
import com.jjak0b.android.trackingmypantry.util.Callback;

import java.util.List;

public class ProductsGroupsBrowserBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private final static String TAG = "ProductsGroupsBrowserBottomSheetDialogFragment";
    private ProductsGroupsBrowserViewModel mViewModel;
    private SharedPantryViewModel mSharedPantryViewModel;
    private SharedProductViewModel mSharedProductViewModel;
    private ProductInstanceGroupListAdapter listAdapter;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    public static ProductsGroupsBrowserBottomSheetDialogFragment newInstance() {
        return new ProductsGroupsBrowserBottomSheetDialogFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_products_groups_browser, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPantryViewModel = new ViewModelProvider(requireParentFragment()).get(SharedPantryViewModel.class);
        mSharedProductViewModel = new ViewModelProvider(requireParentFragment()).get(SharedProductViewModel.class);
        mViewModel = new ViewModelProvider(this).get(ProductsGroupsBrowserViewModel.class);

        mViewModel.setGroupsOf(
                mSharedProductViewModel.getItem(),
                mSharedPantryViewModel.getItem()
        );

        listAdapter = new ProductInstanceGroupListAdapter(new ProductInstanceGroupListAdapter.ProductDiff(), interactionsListener) {
            @NonNull
            @Override
            public ViewModelStore getViewModelStore() {
                return requireParentFragment().getViewModelStore();
            }
        };
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.list);
        progressBar = view.findViewById(R.id.pantryLoadingBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(listAdapter);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        TextView groupsInfo = view.findViewById(R.id.noGroupsInfo);
        groupsInfo.setVisibility(View.GONE);

        mSharedPantryViewModel.getItem().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case SUCCESS:
                    Pantry pantry = resource.getData();
                    toolbar.setLogo(null);
                    if( pantry != null ) {
                        toolbar.setSubtitle(pantry.getName());
                    }
                    break;
                default:
                    toolbar.setLogo(R.drawable.loading_spinner);
                    toolbar.setSubtitle(null);
                    break;
            }
        });

        mViewModel.getItem().observe(getViewLifecycleOwner(), resource -> {
            Log.d(TAG, "Submitted new Pantry content items "  +resource );
            switch (resource.getStatus()) {
                case LOADING:
                    groupsInfo.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    List<ProductInstanceGroup> list = resource.getData();
                    if( list == null || list.isEmpty() ){
                        groupsInfo.setVisibility(View.VISIBLE);
                    }
                    else {
                        groupsInfo.setVisibility(View.GONE);
                    }
                    listAdapter.submitList(list);
                    break;
                case ERROR:
                    progressBar.setVisibility(View.GONE);
                    break;
            }
        });

        setupToolbar(toolbar);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        mViewModel.setItemSource(null);
    }

    final ProductInstanceGroupInteractionsListener interactionsListener = new ProductInstanceGroupInteractionsListener() {
        @Override
        public void onItemClicked(int pantryPosition, View pantryView, Pantry item, List<ProductInstanceGroup> content) {

        }

        @Override
        public void onItemLongClicked(int pantryPosition, View pantryView, Pantry item, List<ProductInstanceGroup> content) {

        }

        @Override
        public void onConsume(int groupPosition, ProductInstanceGroup group, int amount) {
            LiveData<Resource<Void>> result = mViewModel.consume(group, amount);
            result.observe(getViewLifecycleOwner(), resource -> {
                Log.d(TAG, "Consuming " + amount + " from group\n" + resource);
                if( resource.getStatus() != Status.LOADING ) {
                    result.removeObservers(getViewLifecycleOwner());
                }

                if( resource.getStatus() == Status.ERROR) {
                    Toast.makeText(requireContext(),
                            getString(R.string.error_generic_failed_unknown, getString(R.string.option_consume)),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        }

        @Override
        public void onRemove(int groupPosition, ProductInstanceGroup group, int quantity) {
            LiveData<Resource<Void>> result = mViewModel.delete(group, quantity);
            result.observe(getViewLifecycleOwner(), resource -> {
                Log.d(TAG, "Removing " + quantity + " from group\n" + resource);
                if( resource.getStatus() != Status.LOADING ) {
                    result.removeObservers(getViewLifecycleOwner());
                }

                if( resource.getStatus() == Status.ERROR) {
                    Toast.makeText(requireContext(),
                            getString(R.string.error_generic_failed_unknown, getString(R.string.option_remove_entry)),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        }

        @Override
        public void onMove(int groupPosition, ProductInstanceGroup group, Pantry destination, int quantity) {
            LiveData<Resource<Long>> result = mViewModel.moveToPantry(
                    group, destination, quantity
            );
            result.observe(getViewLifecycleOwner(), resource -> {
                Log.d(TAG, "Moving " + quantity + " to " + destination + " from group\n" + resource);
                if( resource.getStatus() != Status.LOADING ) {
                    result.removeObservers(getViewLifecycleOwner());
                }

                if( resource.getStatus() == Status.ERROR) {
                    Toast.makeText(requireContext(),
                            getString(R.string.error_generic_failed_unknown, getString(R.string.option_move_to_pantry)),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        }

        @Override
        public void onMore(int groupPosition, ProductInstanceGroup group, PopupMenu popup) {
            popup.getMenu().clear();
            popup.getMenuInflater()
                    .inflate( R.menu.popup_menu_product_instance_group_operations, popup.getMenu() );

            popup.setOnMenuItemClickListener( item -> {
                switch (item.getItemId()) {
                    case R.id.option_move_to:
                        new SelectItemDialogBuilder<Pantry>(requireContext())
                                .loadOn( mViewModel.getAvailablePantries(), getViewLifecycleOwner(), pantry -> {
                                    showQuantityPicker(1, group.getQuantity(), quantity -> {
                                        interactionsListener.onMove(groupPosition, group, pantry, quantity);
                                    });
                                })
                                .setCancelable(true)
                                .setNegativeButton(android.R.string.cancel, null)
                                .create()
                                .show();
                        return true;
                    default:
                        return false;
                }
            });
            popup.show();

        }
    };

    public void showQuantityPicker(int min, int max, @NonNull Callback<Integer> onOk ) {
        new QuantityPickerBuilder(requireContext())
                .setMin(min)
                .setMax(max)
                .setPositiveButton(android.R.string.ok, onOk)
                .setNegativeButton(android.R.string.cancel , null )
                .setCancelable(true)
                .setTitle(R.string.product_quantity)
                .show();
    }

    void setupToolbar(Toolbar toolbar) {
        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_rename:
                    onActionClickRename();
                    return true;
                case R.id.action_remove:
                    onActionClickRemove();
                    return true;
                default:
                    // If we got here, the user's action was not recognized.
                    // Invoke the superclass to handle it.
                    return super.onOptionsItemSelected(item);
            }
        });
    }

    /**
     * Shows up a dialog to prompt user to rename the current pantry's name
     */
    public void onActionClickRename() {

        PantryActionsDialogViewModel viewModel = new ViewModelProvider(this).get(PantryActionsDialogViewModel.class);
        LiveData<Resource<Pantry>> mCurrentPantry = mSharedPantryViewModel.getItem();

        // update the dialog viewmodel with current pantry
        Observer<Resource<Pantry>> mCurrentPantryObserver = viewModel::setPantry;
        mCurrentPantry.observe(getViewLifecycleOwner(), mCurrentPantryObserver );

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.option_rename)
                .setMessage(R.string.pantry_action_rename_description)
                .setView(R.layout.rename_pantry_layout)
                // Submit rename
                .setPositiveButton(R.string.option_rename, (dialogInterface, i) -> {

                    LiveData<Resource<Integer>> onRename = viewModel.submitRename();
                    onRename.observe(getViewLifecycleOwner(), resource -> {
                        if( resource.getStatus() != Status.LOADING ) {
                            onRename.removeObservers(getViewLifecycleOwner());

                            // close dialog
                            dialogInterface.dismiss();

                            // notify error to user
                            if( resource.getStatus() == Status.ERROR) {
                                new MaterialAlertDialogBuilder(requireContext())
                                        .setMessage(ErrorsUtils.getErrorMessage(requireContext(), resource.getError(), TAG) )
                                        .setPositiveButton(android.R.string.ok, null)
                                        .show();
                            }
                        }
                    });
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setOnDismissListener(dialogInterface -> {
                    // remove observers used for this dialog
                    mCurrentPantry.removeObserver(mCurrentPantryObserver);
                    viewModel.getName().removeObservers(getViewLifecycleOwner());
                })
                .create();
        dialog.show();

        TextInputEditText editText = dialog.findViewById(R.id.editText);
        TextInputLayout inputLayout = dialog.findViewById(R.id.inputLayout);
        Button okBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        editText.addTextChangedListener(new InputUtil.FieldTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setName(s.toString());
            }
        });

        viewModel.getName().observe(getViewLifecycleOwner(), resource -> {
            okBtn.setEnabled(resource.getStatus() == Status.SUCCESS );

            editText.setText(resource.getData());
            editText.setSelection(editText.length());

            switch (resource.getStatus()) {
                case LOADING:
                    inputLayout.setError(null);
                    break;
                case ERROR:
                    String errorMsg = ErrorsUtils.getErrorMessage(dialog.getContext(), resource.getError(), TAG );
                    inputLayout.setError(errorMsg);

                    break;
            }
        });
    }

    public void onActionClickRemove() {
        PantryActionsDialogViewModel viewModel = new ViewModelProvider(this).get(PantryActionsDialogViewModel.class);
        LiveData<Resource<Pantry>> mCurrentPantry = mSharedPantryViewModel.getItem();

        // update the dialog viewmodel with current pantry
        Observer<Resource<Pantry>> mCurrentPantryObserver = viewModel::setPantry;
        mCurrentPantry.observe(getViewLifecycleOwner(), mCurrentPantryObserver );

        new MaterialAlertDialogBuilder(requireContext())
                .setPositiveButton(android.R.string.yes, (dialogInterface, i) -> {
                    LiveData<Resource<Void>> onRemove = viewModel.submitRemove();
                    onRemove.observe(getViewLifecycleOwner(), resource -> {
                        if( resource.getStatus() != Status.LOADING ) {
                            onRemove.removeObservers(getViewLifecycleOwner());

                            // close dialog
                            dialogInterface.dismiss();

                            // notify error to user
                            if( resource.getStatus() == Status.ERROR) {
                                new MaterialAlertDialogBuilder(requireContext())
                                        .setMessage(ErrorsUtils.getErrorMessage(requireContext(), resource.getError(), TAG) )
                                        .setPositiveButton(android.R.string.ok, null)
                                        .show();
                            }
                        }
                    });
                })
                .setNegativeButton(android.R.string.no, null )
                .setTitle(R.string.option_remove_entry)
                .setMessage(R.string.pantry_action_remove_description)
                .setOnDismissListener(dialogInterface -> {
                    // remove observers used for this dialog
                    mCurrentPantry.removeObserver(mCurrentPantryObserver);
                    viewModel.getName().removeObservers(getViewLifecycleOwner());
                })
                .show();
    }
}