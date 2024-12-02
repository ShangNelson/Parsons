package com.parsons.bakery.ui.bakersOrders;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.parsons.bakery.DBHandler;
import com.parsons.bakery.R;
import com.parsons.bakery.databinding.FragmentOrdersBinding;

import java.util.HashMap;
import java.util.List;


public class OrdersFragment extends Fragment {

    private FragmentOrdersBinding binding;
    RelativeLayout tabs,emptyState;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        tabs = root.findViewById(R.id.tab_layout);
        emptyState = root.findViewById(R.id.empty_state_container);
        DBHandler dbHandler = new DBHandler(getContext());
        List<HashMap<String, String>> orders = dbHandler.executeOne("SELECT * FROM " + DBHandler.TABLE_ORDERS + " WHERE " + DBHandler.COLUMN_ORDERS_IS_CURRENT + "=1");
        if (orders.size() == 0) {
            tabs.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            tabs.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
            ViewPager pager = root.findViewById(R.id.pager);
            TabLayout tabLayout = root.findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(pager);
            PagerAdapter adapter = new PagerAdapter(getChildFragmentManager());
            pager.setAdapter(adapter);
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}