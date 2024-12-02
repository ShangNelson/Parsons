package com.parsons.bakery.baker.ui.orders;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.parsons.bakery.R;
import com.parsons.bakery.databinding.FragmentOrdersBinding;


public class OrdersFragment extends Fragment {

    private FragmentOrdersBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ViewPager pager = root.findViewById(R.id.pager);
        TabLayout tabLayout = root.findViewById(R.id.tabs);;
        tabLayout.setupWithViewPager(pager);
        PagerAdapter adapter = new PagerAdapter(getChildFragmentManager());
        pager.setAdapter(adapter);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}