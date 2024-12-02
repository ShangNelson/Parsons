package com.parsons.bakery.baker.ui.orders;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parsons.bakery.DBHandler;
import com.parsons.bakery.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.CustomViewHolder> {
    private final List<Order> itemList;
    private final Context mContext;

    public RecyclerAdapter(Context context, List<Order> feedItemList) {
        this.itemList = feedItemList;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.order_display, null);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        Order feedItem = itemList.get(i);
        String order = feedItem.getOrder();
        try {
            /* ORDER
             * name
             * number
             * time
             * numberOfItems
             * order_item_#
                 * item
                 * type
                 * count
                 * customizations
             */

            JSONObject jsonOrder = new JSONObject(order);
            customViewHolder.name.setText(jsonOrder.getString("name"));
            if (!jsonOrder.getString("name").equals("No Orders")) {
                List<OrderItem> allItems = new ArrayList<>();
                for (int j = 0; j < Integer.parseInt(jsonOrder.getString("numberOfItems")); j++) {
                    JSONObject orderItem = jsonOrder.getJSONObject("order_item_" + j);
                    OrderItem item = new OrderItem();
                    item.setItem(orderItem.getString("item"));
                    item.setType(orderItem.getString("type"));
                    item.setCustomizations(orderItem.getString("customizations"));
                    item.setTime(jsonOrder.getString("time"));
                    item.setCount(Integer.parseInt(orderItem.getString("count")));
                    allItems.add(item);
                }
                customViewHolder.time.setText(jsonOrder.getString("time"));
                customViewHolder.number.setText(jsonOrder.getString("number"));
                LinearLayoutManager ln = new LinearLayoutManager(mContext);
                customViewHolder.content.setLayoutManager(ln);
                customViewHolder.content.setAdapter(new OrderRecyclerAdapter(mContext, allItems));
                DividerItemDecoration itemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
                GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0xfff7f7f7, 0xfff7f7f7});
                drawable.setSize(1, 30);
                itemDecoration.setDrawable(drawable);
                customViewHolder.content.addItemDecoration(itemDecoration);
            }
        }   catch (JSONException e) {
            e.printStackTrace();
            System.out.println(order);
        }
    }


    @Override
    public int getItemCount() {
        return (null != itemList ? itemList.size() : 0);
    }


    static class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView name;
        protected RecyclerView content;
        protected TextView time;
        protected TextView number;

        public CustomViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.order_name);
            content = view.findViewById(R.id.order_content);
            time = view.findViewById(R.id.order_time);
            number = view.findViewById(R.id.order_number);
        }
    }
}
