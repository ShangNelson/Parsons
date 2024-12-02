package com.parsons.bakery.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parsons.bakery.DBHandler;
import com.parsons.bakery.R;
import com.parsons.bakery.ui.order.OrderItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class Category extends AppCompatActivity {
    private Menu menu;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        final CollapsingToolbarLayout mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle(getIntent().getExtras().getString("name"));

        final Toolbar mToolbarInner = findViewById(R.id.toolbarInner);
        setSupportActionBar(mToolbarInner);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), Cart.class)));
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String url = bundle.getString("url");
        ImageView view = findViewById(R.id.itemImage);
        Bitmap b = loadImageFromStorage(new ContextWrapper(this).getDir("imageDir", Context.MODE_PRIVATE).getPath(), url.split("/")[url.split("/").length - 1]);
        view.setImageBitmap(b);


        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view2 -> startActivity(new Intent(getApplicationContext(), Cart.class)));

        DBHandler dbHandler = new DBHandler(this);
        List<OrderItem> items = new ArrayList<>();
        System.out.println("Category: " + bundle.getString("name"));
        if (bundle.getInt("has") == 0) {
            List<HashMap<String, String>> query = dbHandler.executeOne("SELECT * FROM menu WHERE category = '" + bundle.getString("name") + "' ORDER BY order_of_options");
            System.out.println("Pulled: " + query);
            for (HashMap<String, String> map : query) {
                items.add(new OrderItem(map.get("name"), map.get("img"), map.get("description"), map.get("inner_category"), map.get("req")));
            }
        } else if (bundle.getInt("has") == 1){
            List<HashMap<String, String>> query = dbHandler.executeOne("SELECT * FROM menu WHERE category = '" + bundle.getString("name") + "' AND inner_category='label' ORDER BY order_of_options");
            for (HashMap<String, String> map : query) {
                List<HashMap<String, String>> Objects = dbHandler.executeOne("SELECT * FROM menu WHERE inner_category='" + map.get("name") + "' AND category='" + bundle.getString("name") + "'");
                if (!Objects.isEmpty()) {
                    OrderItem item = new OrderItem(map.get("name"));
                    items.add(item);
                    for (HashMap<String, String> object : Objects) {
                        boolean inSeason = true;
                        if (!object.get("req").equals("null")) {
                            String req = object.get("req");
                            String[] reqs = req.split("\\|");
                            String season = null;
                            if (reqs.length > 1) {
                                season = reqs[1];
                            }
                            if (season != null) {
                                Calendar calendar = Calendar.getInstance();
                                String[] seasons = season.split(",");
                                boolean foundSeason = false;
                                for (String currentSeason : seasons) {
                                    if (calendar.get(Calendar.MONTH) + 1 == Integer.parseInt(currentSeason) && !foundSeason) {
                                        foundSeason = true;
                                    }
                                }
                                if (!foundSeason) {
                                    inSeason = false;
                                }
                            }
                        }
                        int numberAdded = 0;
                        if (inSeason) {
                            items.add(new OrderItem(object.get("name"), object.get("img"), object.get("description"), object.get("inner_category"), object.get("req")));
                            numberAdded++;
                        }
                        if (numberAdded == 0) {
                            items.remove(item);
                        }
                    }
                }
            }
        }

        AppBarLayout mAppBarLayout = findViewById(R.id.app_bar);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    isShow = true;
                    showOption(R.id.cart);
                } else if (isShow) {
                    isShow = false;
                    hideOption(R.id.cart);
                }
            }
        });

        RecyclerView recycler =  findViewById(R.id.recyclerItems);
        LinearLayoutManager ln = new LinearLayoutManager(this);
        recycler.setLayoutManager(ln);
        recycler.setNestedScrollingEnabled(false);
        RecyclerAdapterCategory adapter = new RecyclerAdapterCategory(this, items);
        recycler.setAdapter(adapter);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0xfff7f7f7, 0xfff7f7f7});
        drawable.setSize(1,30);
        itemDecoration.setDrawable(drawable);
        recycler.addItemDecoration(itemDecoration);


    }
    private Bitmap loadImageFromStorage(String path, String file)
    {
        try {
            File f=new File(path, file);
            DBHandler db = new DBHandler(this);
            String name = path + "/" + file;
            long seconds = System.currentTimeMillis() / 1000;
            boolean has = false;
            List<HashMap<String, String>> returnedRecents = db.executeOne("SELECT * FROM downloaded");
            for (HashMap<String, String> hashy : returnedRecents) {
                if (hashy.get("url").equals(name)) {
                    has = true;
                }
            }
            if (has) {
                db.executeOne("UPDATE downloaded SET lastAccessed='" + seconds + "' WHERE url='" + name + "'");
            } else {
                db.executeOne("INSERT INTO downloaded (url,lastAccessed) VALUES ('" + name + "','" + seconds + "')");
            }
            return BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        this.menu = menu;
        inflater.inflate(R.menu.cart, menu);
        hideOption(R.id.cart);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.cart)
            startActivity(new Intent(getApplicationContext(), Cart.class));
        return super.onOptionsItemSelected(item);
    }

    private void hideOption(int id) {
        MenuItem item = menu.findItem(id);
        item.setVisible(false);
    }

    private void showOption(int id) {
        MenuItem item = menu.findItem(id);
        item.setVisible(true);
    }

}