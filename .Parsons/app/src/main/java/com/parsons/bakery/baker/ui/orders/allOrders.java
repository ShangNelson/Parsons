package com.parsons.bakery.baker.ui.orders;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parsons.bakery.DBHandler;
import com.parsons.bakery.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class allOrders extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.orders_individual, container, false);

        DBHandler dbHandler = new DBHandler(getContext());
        List<HashMap<String, String>> results = dbHandler.executeOne("SELECT * FROM orders");
        List<JSONObject> allObjects = new ArrayList<>();
        if (results.size() > 0) {
            List<JSONObject> all = new ArrayList<>();
            for (HashMap<String, String> row : results) {
                try {
                    String json = row.get("order_placed");
                    JSONObject object = new JSONObject(json);
                    all.add(object);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                HashMap<String, List<JSONObject>> sorted = sortItems(all, "type");
                System.out.println(sorted);
                for (String key : sorted.keySet()) {
                    JSONObject object = new JSONObject();
                    object.put(key, sorted.get(key));
                    allObjects.add(object);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            allObjects.add(new JSONObject());
        }

        RecyclerView recycler = root.findViewById(R.id.recycler);
        LinearLayoutManager ln = new LinearLayoutManager(getContext());
        recycler.setLayoutManager(ln);
        AllRecyclerAdapter adapter = new AllRecyclerAdapter(getContext(), allObjects);
        recycler.setAdapter(adapter);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0xfff7f7f7, 0xfff7f7f7});
        drawable.setSize(1,30);
        itemDecoration.setDrawable(drawable);
        recycler.addItemDecoration(itemDecoration);
        return root;

    }

    public static HashMap<String, List<JSONObject>> sortItems(List<JSONObject> items, String key) throws JSONException {
        HashMap<String, List<JSONObject>> groups = new HashMap<>();
        for (JSONObject item : items) {
            int numberOfItems = (int) item.get("numberOfItems");
            for (int times = 0; times < numberOfItems; times++) {
                String value =  ((JSONObject) item.get("order_item_" + times)).getString(key);
                if (!groups.containsKey(value)) {
                    groups.put(value, new ArrayList<>());
                }
                ((JSONObject) item.get("order_item_" + times)).put("time", item.get("time"));
                groups.get(value).add((JSONObject) item.get("order_item_" + times));
            }
        }
        return groups;
    }
}
