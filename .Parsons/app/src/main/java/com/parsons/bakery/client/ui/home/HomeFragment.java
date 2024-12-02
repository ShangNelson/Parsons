package com.parsons.bakery.ui.home;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parsons.bakery.CustomViewPager;
import com.parsons.bakery.DBHandler;
import com.parsons.bakery.PagerAdapter;
import com.parsons.bakery.R;
import com.parsons.bakery.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    PagerAdapter pagerAdapter;
    CustomViewPager viewPager;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        List<Bitmap> list = new ArrayList<>();
        list.add(((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.artisan_bread, getContext().getTheme())).getBitmap());
        list.add(((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.bagels, getContext().getTheme())).getBitmap());
        list.add(((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.brownies, getContext().getTheme())).getBitmap());
        list.add(((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.cinnamon_rolls, getContext().getTheme())).getBitmap());
        list.add(((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.cupcakes, getContext().getTheme())).getBitmap());
        list.add(((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.flower_cookies, getContext().getTheme())).getBitmap());
        list.add(((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.kouign_amman, getContext().getTheme())).getBitmap());
        list.add(((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.rainbow_cookies, getContext().getTheme())).getBitmap());
        list.add(((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.slice_of_cake, getContext().getTheme())).getBitmap());
        pagerAdapter = new PagerAdapter(getChildFragmentManager(), list);
        viewPager = root.findViewById(R.id.viewpagerhome);
        viewPager.setAdapter(pagerAdapter);


        List<HomeItem> itemList = new ArrayList<>();
        DBHandler db = new DBHandler(getContext());
        List<HashMap<String, String>> recents = db.executeOne("SELECT re.name, re.visits, me.name, me.img, me.inner_category, me.use_inner FROM recent re JOIN menu me WHERE re.name = me.name ORDER BY last_visit DESC LIMIT 5");
        System.out.println("Recent: " + recents);
        System.out.println(itemList);
        for (HashMap<String, String> hash : recents) {
            HomeItem item = new HomeItem();
            if (!hash.get("inner_category").equals("null") && Integer.parseInt(hash.get("use_inner")) == 1) {
                item.setName(hash.get("name") + " " + hash.get("inner_category"));
            } else {
                item.setName(hash.get("name"));
            }
            item.setUrl(hash.get("img"));
            itemList.add(item);
        }
        RecyclerView recycler =  root.findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setNestedScrollingEnabled(false);
        RecyclerAdapter adapter = new RecyclerAdapter(root.getContext(), itemList);
        recycler.setAdapter(adapter);
        System.out.println("Count: " + adapter.getItemCount());
        return root;
    }


    @Override
    public void onResume() {
        super.onResume();
        viewPager.getCustomHandler().postDelayed(viewPager.getCustomRunnable(), 5000);
    }


    @Override
    public void onPause() {
        super.onPause();
        viewPager.getCustomHandler().removeCallbacks(viewPager.getCustomRunnable());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}