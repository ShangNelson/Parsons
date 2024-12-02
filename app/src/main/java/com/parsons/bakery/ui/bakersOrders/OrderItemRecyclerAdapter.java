package com.parsons.bakery.ui.bakersOrders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.parsons.bakery.DBHandler;
import com.parsons.bakery.R;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class OrderItemRecyclerAdapter extends RecyclerView.Adapter<OrderItemRecyclerAdapter.CustomViewHolder> {
    private final List<HashMap<String, String>> itemList;
    private final Context mContext;

    public OrderItemRecyclerAdapter(Context context, List<HashMap<String,String>> feedItemList) {
        this.itemList = feedItemList;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.individual_order_item_recycler_item, null);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        HashMap<String, String> orderItem = itemList.get(i);
        /* ORDER ITEM
         * item
         * type
         * count
         * customizations
         */
        customViewHolder.item.setText(orderItem.get(DBHandler.COLUMN_ORDER_ITEMS_ITEM));
        customViewHolder.count.setText(orderItem.get(DBHandler.COLUMN_ORDER_ITEMS_COUNT));
        if (!Objects.equals(orderItem.get(DBHandler.COLUMN_ORDER_ITEMS_CUSTOMIZATIONS), "null")) {
            customViewHolder.customs.setText(orderItem.get(DBHandler.COLUMN_ORDER_ITEMS_CUSTOMIZATIONS));
        } else {
            customViewHolder.customs.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return (null != itemList ? itemList.size() : 0);
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView item;
        protected TextView count;
        protected TextView customs;

        public CustomViewHolder(View view) {
            super(view);
            item = view.findViewById(R.id.item);
            count = view.findViewById(R.id.count);
            customs = view.findViewById(R.id.customs);
        }
    }
}
