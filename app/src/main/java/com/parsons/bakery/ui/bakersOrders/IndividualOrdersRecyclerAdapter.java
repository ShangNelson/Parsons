package com.parsons.bakery.ui.bakersOrders;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parsons.bakery.DBHandler;
import com.parsons.bakery.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndividualOrdersRecyclerAdapter extends RecyclerView.Adapter<IndividualOrdersRecyclerAdapter.CustomViewHolder> {

    private final List<HashMap<String, String>> orderList;
    private final Map<String,List<HashMap<String,String>>> orderItemList;
    private final Context mContext;

    public IndividualOrdersRecyclerAdapter(Context context, List<HashMap<String,String>> orderList, Map<String,List<HashMap<String,String>>> orderItemList) {
        this.orderList = orderList;
        this.orderItemList = orderItemList;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.individual_order_recycler_item, null);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        HashMap<String, String> order = orderList.get(i);
        /* ORDER
             * order_id
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

        customViewHolder.name.setText(order.get(DBHandler.COLUMN_ORDERS_NAME));
        List<HashMap<String, String>> orderItems = orderItemList.get(order.get(DBHandler.COLUMN_ORDER_ITEMS_ORDER_ID));

        customViewHolder.time.setText(order.get(DBHandler.COLUMN_ORDERS_TIME_PICKUP));
        customViewHolder.number.setText(order.get(DBHandler.COLUMN_ORDERS_NUMBER));
        LinearLayoutManager ln = new LinearLayoutManager(mContext);
        customViewHolder.content.setLayoutManager(ln);
        customViewHolder.content.setAdapter(new OrderItemRecyclerAdapter(mContext, orderItems));
        DividerItemDecoration itemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0xff000000, 0xFF000000});
        drawable.setSize(1, 3);
        itemDecoration.setDrawable(drawable);
        customViewHolder.content.addItemDecoration(itemDecoration);
    }


    @Override
    public int getItemCount() {
        return (null != orderList ? orderList.size() : 0);
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
