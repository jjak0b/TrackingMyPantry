package com.jjak0b.android.trackingmypantry.ui.pantries.product_instance_group_table;

import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.ui.pantries.product_instance_group_table.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ProductInstanceGroupTableViewModel {

    private List<ColumnHeader> mColumnHeaderModelList;
    private List<RowHeader> mRowHeaderModelList;
    private List<List<Cell>> mCellModelList;
    private List<ProductInstanceGroup> modelList;

    public static class TYPES {
        public static final int
                EXPIRE_DATE = 0,
                STATUS = 1,
                QUANTITY = 2;
    }

    public int getColumnTypeByIndex( int column ) {

        switch ( column ){
            case 0:
                return TYPES.EXPIRE_DATE;
            case 1:
                return TYPES.STATUS;
            case 2:
                return TYPES.QUANTITY;
            default:
                return -1;
        }
    }

    public List<ColumnHeader> getColumHeaderModeList() {
        return mColumnHeaderModelList;
    }

    public List<RowHeader> getRowHeaderModelList() {
        return mRowHeaderModelList;
    }

    public List<List<Cell>> getCellModelList() {
        return mCellModelList;
    }

    public void generateListForTableView(List<ProductInstanceGroup> productInstanceGroups) {
        modelList = new ArrayList<>(productInstanceGroups);
        mColumnHeaderModelList = createColumnHeaderModelList();
        mCellModelList = createCellModelList(productInstanceGroups);
        mRowHeaderModelList = createRowHeaderList(productInstanceGroups.size());
    }

    private List<ColumnHeader> createColumnHeaderModelList() {
        List<ColumnHeader> mColumnHeaderList = Arrays.asList(
                new ColumnHeader(R.string.product_expire_date),
                new ColumnHeader(R.string.product_unconsumed_amount),
                new ColumnHeader(R.string.product_quantity)
        );

        return  mColumnHeaderList;
    }

    protected List<List<Cell>> createCellModelList(List<ProductInstanceGroup> productInstanceGroups) {
        List<List<Cell>> mCellList = new ArrayList<>(productInstanceGroups.size());
        Iterator<ProductInstanceGroup> it = productInstanceGroups.iterator();

        while( it.hasNext() ){
            ProductInstanceGroup item = it.next();
            List<Cell> row = createRowModel(item);
            mCellList.add( row );
        }

        return  mCellList;
    }

    protected List<Cell> createRowModel(ProductInstanceGroup item){
        List<Cell> row = Arrays.asList(
                new Cell(item.getExpiryDate()),
                new Cell(item.getCurrentAmountPercent()),
                new Cell(item.getQuantity())
        );
        return row;
    }

    protected List<RowHeader> createRowHeaderList(int size) {
        // The library require a list, even null.
        // but if this list is null or not of the same size of real rows
        // an indexOutOfBoundExeption may happen due to library bug

        List<RowHeader> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            // Row headers just shows the index of the TableView List.
            list.add(new RowHeader(String.valueOf(i + 1)));
        }
        return list;
    }

    public ProductInstanceGroup getItem( int position ){
        return modelList.get( position );
    }

    public ProductInstanceGroup removeRowItem( int position ){
        return modelList.remove(position);
    }

    public void addItem( int position, ProductInstanceGroup item ){
        modelList.add( position, item );
    }
}
