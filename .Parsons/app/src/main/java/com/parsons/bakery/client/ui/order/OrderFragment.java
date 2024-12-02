package com.parsons.bakery.ui.order;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.parsons.bakery.DBHandler;
import com.parsons.bakery.R;
import com.parsons.bakery.databinding.FragmentOrderBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OrderFragment extends Fragment {

    private FragmentOrderBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOrderBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        List<OrderItem> itemList = new ArrayList<>();
        DBHandler db = new DBHandler(getContext());
        List<HashMap<String, String>> dbItems = db.executeOne("SELECT * FROM categories WHERE level=1");
        for (HashMap<String, String> item: dbItems) {
            itemList.add(new OrderItem(item.get("name"), item.get("img"), Integer.parseInt(item.get("has_levels"))));
        }

        System.out.println(itemList);
        RecyclerView recycler =  root.findViewById(R.id.orderRecycler);
        LinearLayoutManager ln = new LinearLayoutManager(getContext());
        recycler.setLayoutManager(ln);
        recycler.setNestedScrollingEnabled(false);
        RecyclerAdapter adapter = new RecyclerAdapter(root.getContext(), itemList);
        recycler.setAdapter(adapter);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0xfff7f7f7, 0xfff7f7f7});
        drawable.setSize(1,30);
        itemDecoration.setDrawable(drawable);
        recycler.addItemDecoration(itemDecoration);


        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}