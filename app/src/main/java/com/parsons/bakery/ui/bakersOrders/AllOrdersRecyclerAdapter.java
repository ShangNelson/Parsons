package com.parsons.bakery.ui.bakersOrders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parsons.bakery.DBHandler;
import com.parsons.bakery.R;

import java.util.HashMap;
import java.util.List;

public class AllOrdersRecyclerAdapter extends RecyclerView.Adapter<AllOrdersRecyclerAdapter.CustomViewHolder> {
    private final List<List<HashMap<String, String>>> orderList;
    private final Context mContext;

    public AllOrdersRecyclerAdapter(Context context, List<List<HashMap<String,String>>> orderList) {
        this.orderList = orderList;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_orders_recycler_item, null);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        List<HashMap<String, String>> thisCategory = orderList.get(i);
        /*
            LIST OF ITEMS:
                Menu Category
                Order Time
                Order Item Name
                Order Item Count
        */
        customViewHolder.name.setText(thisCategory.get(0).get(DBHandler.COLUMN_MENU_CATEGORY));
        LinearLayoutManager ln = new LinearLayoutManager(mContext);
        customViewHolder.content.setLayoutManager(ln);
        customViewHolder.content.setAdapter(new AllItemsCategoryRecyclerAdapter(mContext, thisCategory));
    }


    @Override
    public int getItemCount() {
        return (null != orderList ? orderList.size() : 0);
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
