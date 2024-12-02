package com.parsons.bakery.ui.bakersOrders;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parsons.bakery.DBHandler;
import com.parsons.bakery.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndividualOrders extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.orders_individual, container, false);

        DBHandler dbHandler = new DBHandler(getContext());
        List<HashMap<String, String>> allOrders = dbHandler.executeOne("SELECT * FROM " + DBHandler.TABLE_ORDERS + " WHERE " + DBHandler.COLUMN_ORDERS_IS_CURRENT + " = 1");
        List<HashMap<String, String>> allOrderItemsRAW = dbHandler.executeOne("SELECT * FROM " + DBHandler.TABLE_ORDER_ITEMS);
        if (allOrders.size() == 0) {

        }
        Map<String,List<HashMap<String,String>>> allOrderItems = new HashMap<>();
        List<String> keys = new ArrayList<>();
        for (HashMap<String, String> entry : allOrderItemsRAW) {
            if (!keys.contains(entry.get(DBHandler.COLUMN_ORDER_ITEMS_ORDER_ID))) {
                keys.add(entry.get(DBHandler.COLUMN_ORDER_ITEMS_ORDER_ID));
                allOrderItems.put(entry.get(DBHandler.COLUMN_ORDER_ITEMS_ORDER_ID), new ArrayList<>());
            }
            allOrderItems.get(entry.get(DBHandler.COLUMN_ORDER_ITEMS_ORDER_ID)).add(entry);
        }
        RecyclerView recycler = root.findViewById(R.id.recycler);
        LinearLayoutManager ln = new LinearLayoutManager(getContext());
        recycler.setLayoutManager(ln);
        IndividualOrdersRecyclerAdapter adapter = new IndividualOrdersRecyclerAdapter(getContext(), allOrders, allOrderItems);
        recycler.setAdapter(adapter);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0xffcdcdcd, 0xffcdcdcd});
        drawable.setSize(1,5);
        itemDecoration.setDrawable(drawable);
        recycler.addItemDecoration(itemDecoration);
        return root;

    }
}
