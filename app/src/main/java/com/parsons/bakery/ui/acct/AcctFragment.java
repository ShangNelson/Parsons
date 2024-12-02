package com.parsons.bakery.ui.acct;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavHostController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parsons.bakery.DBHandler;
import com.parsons.bakery.R;
import com.parsons.bakery.databinding.FragmentAcctBinding;
import com.parsons.bakery.ui.bakersOrders.AllOrdersRecyclerAdapter;
import com.parsons.bakery.ui.order.OrderFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AcctFragment extends Fragment {

    private FragmentAcctBinding binding;
    public static View rootBase = null;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAcctBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        rootBase = root;
        DBHandler dbHandler = new DBHandler(getContext());
        final int accountType = Integer.parseInt(dbHandler.executeOne("SELECT " + DBHandler.COLUMN_ACCT_ACCOUNT_TYPE + " FROM " + DBHandler.TABLE_ACCT).get(0).get(DBHandler.COLUMN_ACCT_ACCOUNT_TYPE));
        View navBar = getActivity().getWindow().findViewById(R.id.nav_view);
        if (navBar != null) {
            int navBarHeight = navBar.getHeight();
            (root.findViewById(R.id.AccountLayout)).setPadding(0, 0, 0, navBarHeight);
        }

        if (accountType == -1) {
            root.findViewById(R.id.isSignedIn).setVisibility(View.GONE);
            root.findViewById(R.id.notSignedIn).setVisibility(View.VISIBLE);
            Button signIn = root.findViewById(R.id.login);
            Button signUp = root.findViewById(R.id.register);
            signIn.setOnClickListener(v -> startActivity(new Intent(getContext(), Login.class)));
            signUp.setOnClickListener(view -> root.getContext().startActivity(new Intent(getContext(), Registration.class)));
        } else {
            root.findViewById(R.id.isSignedIn).setVisibility(View.VISIBLE);
            root.findViewById(R.id.notSignedIn).setVisibility(View.GONE);
            TextView welcome = root.findViewById(R.id.welcome);
            welcome.setText("Welcome " + dbHandler.executeOne("SELECT " + DBHandler.COLUMN_ACCT_FIRSTNAME + " FROM " + DBHandler.TABLE_ACCT).get(0).get(DBHandler.COLUMN_ACCT_FIRSTNAME) + "!");
            /* ORDER
             * Most item - Order Items
             * Order Date - Orders
             * Image Directory - Menu
             * Price - Orders
             * Other Item Count - Calculated
             * 2nd item (if exists) - Order Items
             * 3rd item (if exists) - Order Items
             * Order ID (For reorder) - Orders
                 * SELECT oi.item,o.time_pickup,m.img,oi.order_id,oi.count,m.price
                 * FROM order_items oi
                 * JOIN menu m ON m.name=oi.item
                 * JOIN orders o ON o.order_id=oi.order_id
                 * ORDER BY o.time_pickup DESC,o.order_id,oi.count DESC
             */
            List<HashMap<String, String>> previousOrders = dbHandler.executeOne(
                    "SELECT oi." + DBHandler.COLUMN_ORDER_ITEMS_ITEM +
                            ",o." + DBHandler.COLUMN_ORDERS_TIME_PICKUP +
                            ",m." + DBHandler.COLUMN_MENU_IMAGE +
                            ",oi." + DBHandler.COLUMN_ORDER_ITEMS_ORDER_ID +
                            ",oi." + DBHandler.COLUMN_ORDER_ITEMS_COUNT +
                            ",o." + DBHandler.COLUMN_ORDERS_PRICE +
                            ",o." + DBHandler.COLUMN_ORDERS_NUMBER_OF_ITEMS +
                            " FROM " + DBHandler.TABLE_ORDER_ITEMS +
                            " oi JOIN " + DBHandler.TABLE_MENU +
                            " m ON m." + DBHandler.COLUMN_MENU_NAME + "=oi." + DBHandler.COLUMN_ORDER_ITEMS_ITEM +
                            " JOIN " + DBHandler.TABLE_ORDERS +
                            " o ON o." + DBHandler.COLUMN_ORDERS_ORDER_ID + "=oi." + DBHandler.COLUMN_ORDER_ITEMS_ORDER_ID +
                            " JOIN " + DBHandler.TABLE_ACCT +
                            " ac ON o." + DBHandler.COLUMN_ORDERS_USERID + "=ac." + DBHandler.COLUMN_ACCT_UNIQUE_ID +
                            " ORDER BY o." + DBHandler.COLUMN_ORDERS_TIME_PICKUP + " DESC," +
                            "o." + DBHandler.COLUMN_ORDERS_ORDER_ID + "," +
                            "oi." + DBHandler.COLUMN_ORDER_ITEMS_COUNT + " DESC");
            List<List<HashMap<String, String>>> organizedPreviousOrders = new ArrayList<>();
            List<String> hitOrders = new ArrayList<>();
            for (HashMap<String, String> orderItem : previousOrders) {
                if (!hitOrders.contains(orderItem.get(DBHandler.COLUMN_ORDER_ITEMS_ORDER_ID))) {
                    organizedPreviousOrders.add(filterListForOrders(previousOrders, orderItem.get(DBHandler.COLUMN_ORDER_ITEMS_ORDER_ID)));
                    hitOrders.add(orderItem.get(DBHandler.COLUMN_ORDER_ITEMS_ORDER_ID));
                }
            }
            RecyclerView recycler = root.findViewById(R.id.recycler);
            LinearLayout emptyState = root.findViewById(R.id.empty_state_view);
            if (previousOrders.size() == 0) {
                recycler.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
                Button actionButton = root.findViewById(R.id.empty_state_action_button);
                actionButton.setOnClickListener(v -> {
                    NavController navController = NavHostFragment.findNavController(AcctFragment.this);
                    navController.navigate(R.id.toOrderFragment,
                            null,
                            new NavOptions.Builder()
                                    .setPopUpTo(R.id.navigation_acct, true)
                                    .build());
                });
            } else {
                recycler.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);

                LinearLayoutManager ln = new LinearLayoutManager(getContext());
                recycler.setLayoutManager(ln);
                PreviousOrdersRecyclerAdapter adapter = new PreviousOrdersRecyclerAdapter(getContext(), organizedPreviousOrders);
                recycler.setAdapter(adapter);
                DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
                GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0xfff7f7f7, 0xfff7f7f7});
                drawable.setSize(1,3);
                itemDecoration.setDrawable(drawable);
                recycler.addItemDecoration(itemDecoration);

            }
            Fragment childFragment = getChildFragmentManager().findFragmentById(R.id.fragContainer);
            if (childFragment == null) {
                getChildFragmentManager().beginTransaction()
                        .add(R.id.fragContainer, new Settings())
                        .commit();

            }
        }
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public List<HashMap<String, String>> filterListForOrders(List<HashMap<String, String>> orderSet, String orderID) {
        List<HashMap<String, String>> filteredLines = new ArrayList<>();
        for (HashMap<String, String> line : orderSet) {
            if (line.get(DBHandler.COLUMN_ORDER_ITEMS_ORDER_ID).equals(orderID)) {
                filteredLines.add(line);
            }
        }
        return filteredLines;
    }
}

