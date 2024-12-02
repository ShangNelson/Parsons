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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class AllOrders extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.orders_individual, container, false);

        DBHandler dbHandler = new DBHandler(getContext());
        List<HashMap<String, String>> orderInformation = dbHandler.executeOne(
                "SELECT " +
                        "oi." + DBHandler.COLUMN_ORDER_ITEMS_COUNT + "," +
                        "oi." + DBHandler.COLUMN_ORDER_ITEMS_ITEM + "," +
                        "m." + DBHandler.COLUMN_MENU_CATEGORY + "," +
                        "o." + DBHandler.COLUMN_ORDERS_TIME_PICKUP + " " +
                        "FROM " + DBHandler.TABLE_ORDER_ITEMS + " oi " +
                        "JOIN " + DBHandler.TABLE_MENU + " m " +
                        "ON m." + DBHandler.COLUMN_MENU_NAME + " = oi." + DBHandler.COLUMN_ORDER_ITEMS_ITEM + " " +
                        "JOIN " + DBHandler.TABLE_ORDERS + " o " +
                        "ON o." + DBHandler.COLUMN_ORDERS_ORDER_ID + " = oi." + DBHandler.COLUMN_ORDER_ITEMS_ORDER_ID + " " +
                        "WHERE o." + DBHandler.COLUMN_ORDERS_IS_CURRENT + " = 1 " +
                        "ORDER BY m." + DBHandler.COLUMN_MENU_CATEGORY
        );
        List<List<HashMap<String, String>>> separatedByCategoryOrderInformation = new ArrayList<>();
        List<String> categoriesAllreadySorted = new ArrayList<>();
        for (HashMap<String, String> information : orderInformation) {
            if (categoriesAllreadySorted.contains(information.get(DBHandler.COLUMN_MENU_CATEGORY))) {
                continue;
            }
            categoriesAllreadySorted.add(information.get(DBHandler.COLUMN_MENU_CATEGORY));
            List<HashMap<String,String>> selectedInformation = pullOutCategory(orderInformation, information.get(DBHandler.COLUMN_MENU_CATEGORY));
            separatedByCategoryOrderInformation.add(selectedInformation);
        }
        RecyclerView recycler = root.findViewById(R.id.recycler);
        LinearLayoutManager ln = new LinearLayoutManager(getContext());
        recycler.setLayoutManager(ln);
        AllOrdersRecyclerAdapter adapter = new AllOrdersRecyclerAdapter(getContext(), separatedByCategoryOrderInformation);
        recycler.setAdapter(adapter);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0xfff7f7f7, 0xfff7f7f7});
        drawable.setSize(1,3);
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

    List<HashMap<String,String>> pullOutCategory(List<HashMap<String, String>> orderInformation, String category) {
        List<HashMap<String, String>> separatedInformation = new ArrayList<>();
        for (HashMap<String, String> information : orderInformation) {
            if (information.get(DBHandler.COLUMN_MENU_CATEGORY).equals(category)) {
                separatedInformation.add(information);
            }
        }
        return separatedInformation;
    }
}
