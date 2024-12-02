package com.parsons.bakery.baker.ui.orders;

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

public class individualOrders extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.orders_individual, container, false);

        DBHandler dbHandler = new DBHandler(getContext());
        List<HashMap<String, String>> results = dbHandler.executeOne("SELECT * FROM orders ORDER BY time_placed");
        List<Order> allOrders = new ArrayList<>();
        if (results.size() > 0) {
            for (HashMap<String, String> order : results) {
                Order thisOrder = new Order();
                thisOrder.setOrder(order.get("order_placed"));
                allOrders.add(thisOrder);
            }
        } else {
            allOrders.add(new Order("{\"name\":\"No Orders\"}"));
        }
        RecyclerView recycler = root.findViewById(R.id.recycler);
        LinearLayoutManager ln = new LinearLayoutManager(getContext());
        recycler.setLayoutManager(ln);
        RecyclerAdapter adapter = new RecyclerAdapter(getContext(), allOrders);
        recycler.setAdapter(adapter);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0xfff7f7f7, 0xfff7f7f7});
        drawable.setSize(1,30);
        itemDecoration.setDrawable(drawable);
        recycler.addItemDecoration(itemDecoration);
        return root;

    }
}
