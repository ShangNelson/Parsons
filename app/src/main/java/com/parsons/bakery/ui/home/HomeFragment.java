package com.parsons.bakery.ui.home;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parsons.bakery.DBHandler;
import com.parsons.bakery.LocalImageLoader;
import com.parsons.bakery.R;
import com.parsons.bakery.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private PagerAdapter pagerAdapter;
    private CustomViewPager viewPager;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Fetch and load home images asynchronously
        new Thread(() -> fetchHomeImages(root)).start();

        // Load recent items asynchronously
        new Thread(() -> loadRecentItems(root)).start();

        return root;
    }

    private void fetchHomeImages(View root) {
        List<HashMap<String, String>> updatedImages = new DBHandler(getContext()).executeOne("SELECT * FROM " + DBHandler.TABLE_HOME_IMAGES);
        List<String> imagePaths = new ArrayList<>();
        for (HashMap<String, String> image : updatedImages) {
            imagePaths.add(image.get(DBHandler.COLUMN_HOME_IMAGES_PATH));
        }

        List<Bitmap> bitmapList = LocalImageLoader.loadImages(getContext(), imagePaths);

        mainHandler.post(() -> {
            pagerAdapter = new PagerAdapter(getChildFragmentManager(), bitmapList);
            viewPager = root.findViewById(R.id.viewpagerhome);
            viewPager.setAdapter(pagerAdapter);
            viewPager.getCustomHandler().postDelayed(viewPager.getCustomRunnable(), 0);
        });
    }

    private void loadRecentItems(View root) {
        // Use the shared executor service to submit the task
        DBHandler dbHandler = new DBHandler(getContext());
        List<HashMap<String, String>> recents = dbHandler.executeOne(
                "SELECT re.name, re.visits, me.name, me.img, me.inner_category, me.use_inner " +
                        "FROM recent re JOIN menu me WHERE re.name = me.name " +
                        "ORDER BY last_visit DESC LIMIT 5"
        );

        List<HomeItem> itemList = new ArrayList<>();
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

        // Now use the mainHandler to post to the main thread
        mainHandler.post(() -> {
            RecyclerView recycler = root.findViewById(R.id.recycler);
            recycler.setLayoutManager(new LinearLayoutManager(getContext()));
            recycler.setNestedScrollingEnabled(false);
            HomeRecyclerAdapter adapter = new HomeRecyclerAdapter(root.getContext(), itemList);
            recycler.setAdapter(adapter);
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        if (viewPager != null && viewPager.getCustomHandler() != null) {
            viewPager.getCustomHandler().postDelayed(viewPager.getCustomRunnable(), 2500);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (viewPager != null && viewPager.getCustomHandler() != null) {
            viewPager.getCustomHandler().removeCallbacks(viewPager.getCustomRunnable());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
