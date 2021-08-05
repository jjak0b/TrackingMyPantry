package com.jjak0b.android.trackingmypantry.ui.pantries.product_instance_group_table;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.evrencoskun.tableview.TableView;
import com.evrencoskun.tableview.adapter.AbstractTableAdapter;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.ui.pantries.product_instance_group_table.holder.*;
import com.jjak0b.android.trackingmypantry.ui.pantries.product_instance_group_table.model.*;

import java.util.List;

public class ProductInstanceGroupTableViewAdapter extends AbstractTableAdapter<ColumnHeader, RowHeader, Cell> {

    private ProductInstanceGroupTableViewModel mViewModel;
    private int rowWidth;

    public ProductInstanceGroupTableViewAdapter(){
        mViewModel = new ProductInstanceGroupTableViewModel();
        rowWidth = 0;
    }

    public void setRowWidth( int width ){
        this.rowWidth = width;
    }

    public int getHeadersCount() {
        return super.mColumnHeaderItems.size();
    }

    public void setItems(List<ProductInstanceGroup> list) {
        // Generate the lists that are used to TableViewAdapter
        mViewModel.generateListForTableView(list);

        // Now we got what we need to show on TableView.
        setAllItems(
                mViewModel.getColumHeaderModeList(),
                mViewModel.getRowHeaderModelList(),
                mViewModel.getCellModelList()
        );
    }

    /**
     * This is where you create your custom Cell ViewHolder. This method is called when Cell
     * RecyclerView of the TableView needs a new RecyclerView.ViewHolder of the given type to
     * represent an item.
     *
     * @param viewType : This value comes from #getCellItemViewType method to support different type
     *                 of viewHolder as a Cell item.
     *
     * @see #getCellItemViewType(int);
     */
    @Override
    public AbstractViewHolder onCreateCellViewHolder(ViewGroup parent, int viewType) {
        // Get cell xml layout
        View layout;
        switch ( viewType ){
            case ProductInstanceGroupTableViewModel.TYPES.STATUS:
                layout = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.product_instance_group_table_cell_status_layout, parent, false);
                return new ProductInstanceGroupCellStatusViewHolder(layout);
            case ProductInstanceGroupTableViewModel.TYPES.EXPIRE_DATE:
                layout = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.product_instance_group_table_cell_expire_date_layout, parent, false);
                return new ProductInstanceGroupCellExpireDateViewHolder(layout);
            case ProductInstanceGroupTableViewModel.TYPES.QUANTITY:
                layout = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.product_instance_group_table_cell_quantity_layout, parent, false);
                return new ProductInstanceGroupCellQuantityViewHolder(layout);
            default:
                layout = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.product_instance_group_table_cell_layout, parent, false);
                return new ProductInstanceGroupCellViewHolder(layout);
        }
    }

    /**
     * That is where you set Cell View Model data to your custom Cell ViewHolder. This method is
     * Called by Cell RecyclerView of the TableView to display the data at the specified position.
     * This method gives you everything you need about a cell item.
     *
     * @param holder       : This is one of your cell ViewHolders that was created on
     *                     ```onCreateCellViewHolder``` method. In this example, we have created
     *                     "MyCellViewHolder" holder.
     * @param cellItemModel     : This is the cell view model located on this X and Y position. In this
     *                     example, the model class is "Cell".
     * @param columnPosition : This is the X (Column) position of the cell item.
     * @param rowPosition : This is the Y (Row) position of the cell item.
     *
     * @see #onCreateCellViewHolder(ViewGroup, int);
     */
    @Override
    public void onBindCellViewHolder(@NonNull AbstractViewHolder holder, @Nullable Cell cellItemModel, int columnPosition, int rowPosition) {
        // Get the holder to update cell content
        if( holder instanceof ProductInstanceGroupCellStatusViewHolder){
            ((ProductInstanceGroupCellStatusViewHolder) holder).bind( cellItemModel );
        }
        else if( holder instanceof ProductInstanceGroupCellExpireDateViewHolder){
            ((ProductInstanceGroupCellExpireDateViewHolder) holder).bind( cellItemModel );
        }
        else if( holder instanceof ProductInstanceGroupCellQuantityViewHolder){
            ((ProductInstanceGroupCellQuantityViewHolder) holder).bind( cellItemModel );
        }
        else {
            ((ProductInstanceGroupCellViewHolder) holder).bind( cellItemModel );
        }
    }


    /**
     * This is where you create your custom Column Header ViewHolder. This method is called when
     * Column Header RecyclerView of the TableView needs a new RecyclerView.ViewHolder of the given
     * type to represent an item.
     *
     * @param viewType : This value comes from "getColumnHeaderItemViewType" method to support
     *                 different type of viewHolder as a Column Header item.
     *
     * @see #getColumnHeaderItemViewType(int);
     */
    @Override
    public AbstractViewHolder onCreateColumnHeaderViewHolder(ViewGroup parent, int viewType) {

        // Get Column Header xml Layout
        View layout = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_instance_group_table_column_header, parent, false);

        // Create a ColumnHeader ViewHolder
        return new ProductInstanceGroupColumnHeaderViewHolder(layout);
    }

    /**
     * That is where you set Column Header View Model data to your custom Column Header ViewHolder.
     * This method is Called by ColumnHeader RecyclerView of the TableView to display the data at
     * the specified position. This method gives you everything you need about a column header
     * item.
     *
     * @param holder   : This is one of your column header ViewHolders that was created on
     *                 ```onCreateColumnHeaderViewHolder``` method. In this example we have created
     *                 "MyColumnHeaderViewHolder" holder.
     * @param columnHeaderItemModel : This is the column header view model located on this X position. In this
     *                 example, the model class is "ColumnHeader".
     * @param columnPosition : This is the X (Column) position of the column header item.
     *
     * @see #onCreateColumnHeaderViewHolder(ViewGroup, int) ;
     */
    @Override
    public void onBindColumnHeaderViewHolder(@NonNull AbstractViewHolder holder, @Nullable ColumnHeader columnHeaderItemModel, int columnPosition) {
        ProductInstanceGroupColumnHeaderViewHolder columnHeaderHolder = (ProductInstanceGroupColumnHeaderViewHolder) holder;
        columnHeaderHolder.bind( columnHeaderItemModel );

        TableView view = (TableView) getTableView();
        view.setColumnWidth(
                columnPosition,
                Math.max(rowWidth / getHeadersCount(), columnHeaderHolder.getContainer().getWidth())
        );
    }

    /**
     * This is where you create your custom Row Header ViewHolder. This method is called when
     * Row Header RecyclerView of the TableView needs a new RecyclerView.ViewHolder of the given
     * type to represent an item.
     *
     * @param viewType : This value comes from "getRowHeaderItemViewType" method to support
     *                 different type of viewHolder as a row Header item.
     *
     * @see #getRowHeaderItemViewType(int);
     */
    @Override
    public AbstractViewHolder onCreateRowHeaderViewHolder(ViewGroup parent, int viewType) {

        // Get Row Header xml Layout
        View layout = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_instance_group_table_row_header, parent, false);

        // Create a Row Header ViewHolder
        return new ProductInstanceGroupRowHeaderViewHolder(layout);
    }

    /**
     * That is where you set Row Header View Model data to your custom Row Header ViewHolder. This
     * method is Called by RowHeader RecyclerView of the TableView to display the data at the
     * specified position. This method gives you everything you need about a row header item.
     *
     * @param holder   : This is one of your row header ViewHolders that was created on
     *                 ```onCreateRowHeaderViewHolder``` method. In this example, we have created
     *                 "MyRowHeaderViewHolder" holder.
     * @param rowHeaderItemModel : This is the row header view model located on this Y position. In this
     *                 example, the model class is "RowHeader".
     * @param rowPosition : This is the Y (row) position of the row header item.
     *
     * @see #onCreateRowHeaderViewHolder(ViewGroup, int) ;
     */
    @Override
    public void onBindRowHeaderViewHolder(@NonNull AbstractViewHolder holder, @Nullable RowHeader rowHeaderItemModel, int rowPosition) {
        // Get the holder to update row header item text
        ((ProductInstanceGroupRowHeaderViewHolder) holder).bind( rowHeaderItemModel, rowPosition );
    }

    @Override
    public View onCreateCornerView(ViewGroup parent) {
        // Get Corner xml layout
        return LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_instance_group_table_corner, parent, false);
    }

    @Override
    public int getColumnHeaderItemViewType(int columnPosition) {
        // The unique ID for this type of column header item
        // If you have different items for Cell View by X (Column) position,
        // then you should fill this method to be able create different
        // type of ColumnViewHolder on "onCreateColumnViewHolder"
        return mViewModel.getColumnTypeByIndex( columnPosition );
    }

    @Override
    public int getRowHeaderItemViewType(int rowPosition) {
        // The unique ID for this type of row header item
        // If you have different items for Row Header View by Y (Row) position,
        // then you should fill this method to be able create different
        // type of RowHeaderViewHolder on "onCreateRowHeaderViewHolder"
        return 0;
    }

    @Override
    public int getCellItemViewType(int columnPosition) {


        // The unique ID for this type of cell item
        // If you have different items for Cell View by X (Column) position,
        // then you should fill this method to be able create different
        // type of CellViewHolder on "onCreateCellViewHolder"
        return mViewModel.getColumnTypeByIndex( columnPosition );
    }
}
