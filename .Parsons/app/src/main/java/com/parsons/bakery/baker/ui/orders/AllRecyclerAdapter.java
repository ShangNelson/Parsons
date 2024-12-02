package com.parsons.bakery.baker.ui.orders;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.annotations.JsonAdapter;
import com.parsons.bakery.R;
import com.parsons.bakery.client.Category;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AllRecyclerAdapter extends RecyclerView.Adapter<AllRecyclerAdapter.CustomViewHolder> {
    private final List<JSONObject> itemList;
    private final Context mContext;

    public AllRecyclerAdapter(Context context, List<JSONObject> feedItemList) {
        this.itemList = feedItemList;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.orders_all, null);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        JSONObject feedItem = itemList.get(i);
        if (feedItem.length() == 0) {
            customViewHolder.name.setText("No Orders");
            return;
        }
        /*
        * Category - current loop section
            * item
            * type
            * count
            * customizations
            * time
        */
        Iterator<String> keys = feedItem.keys();
        String category = keys.next();
        List<OrderItem> allItems = new ArrayList<>();
        try {
            List<JSONObject> items = (List<JSONObject>) feedItem.get(category);
            for (JSONObject value : items) {
                OrderItem orderItem = new OrderItem();
                orderItem.setTime(value.getString("time"));
                orderItem.setType(value.getString("type"));
                orderItem.setItem(value.getString("item"));
                orderItem.setCustomizations(value.getString("customizations"));
                orderItem.setCount(Integer.parseInt(value.getString("count")));
                allItems.add(orderItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        customViewHolder.name.setText(category);
        LinearLayoutManager ln = new LinearLayoutManager(mContext);
        customViewHolder.content.setLayoutManager(ln);
        customViewHolder.content.setAdapter(new OrderRecyclerAdapter(mContext, allItems));

    }


    @Override
    public int getItemCount() {
        return (null != itemList ? itemList.size() : 0);
    }


    static class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView name;
        protected RecyclerView content;

        public CustomViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.category_name);
            content = view.findViewById(R.id.recycler);
        }
    }
}
