package com.parsons.bakery.baker.ui.orders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.parsons.bakery.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class OrderRecyclerAdapter extends RecyclerView.Adapter<OrderRecyclerAdapter.CustomViewHolder> {
    private final List<OrderItem> itemList;
    private final Context mContext;

    public OrderRecyclerAdapter(Context context, List<OrderItem> feedItemList) {
        this.itemList = feedItemList;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.order_item_display, null);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        OrderItem feedItem = itemList.get(i);
        /* ORDER ITEM
         * item
         * type
         * count
         * customizations
         * time
         */
        customViewHolder.item.setText(feedItem.getItem());
        customViewHolder.count.setText(String.valueOf(feedItem.getCount()));
        customViewHolder.customs.setText(feedItem.getCustomizations());
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
