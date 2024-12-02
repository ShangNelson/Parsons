package com.parsons.bakery.client;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parsons.bakery.DBHandler;
import com.parsons.bakery.ImageAnimator;
import com.parsons.bakery.R;
import com.parsons.bakery.databinding.ActivityMenuCustomizationBinding;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MenuCustomization extends AppCompatActivity {
    private Menu menu;

    public static String order;
    public static String orderFull;
    public static String orderImage;
    public boolean showing = false;
    private ActivityMenuCustomizationBinding binding;
    DBHandler db;
    FloatingActionButton fab;
    List<String> customizationsInOrder = new ArrayList<>();
    HashMap<Integer, RadioGroup> radios = new HashMap<>();
    HashMap<Integer, EditText> others= new HashMap<>();
    List<HashMap<String, CheckBox>> checkboxes = new ArrayList<>();
    int locationInList = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMenuCustomizationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        final Toolbar mToolbarInner = findViewById(R.id.toolbarInner);
        setSupportActionBar(mToolbarInner);
        final CollapsingToolbarLayout mToolbar = findViewById(R.id.menuToolbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), Cart.class)));


        db = new DBHandler(this);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        order = bundle.getString("name");

        boolean has = false;
        int value = 1;
        List<HashMap<String, String>> returnedRecents = db.executeOne("SELECT * FROM recent");
        for (HashMap<String, String> hashy : returnedRecents) {
            if (hashy.get("name").equals(bundle.getString("name"))) {
                has = true;
                value = Integer.parseInt(hashy.get("visits")) + 1;
            }
        }
        if (has) {
            db.executeOne("UPDATE recent SET visits='" + value + "',last_visit='" + System.currentTimeMillis() / 1000 + "' WHERE name='" + order + "'");
        } else {
            db.executeOne("INSERT INTO recent (name,visits,last_visit) VALUES ('" + order + "',1,'" + System.currentTimeMillis() / 1000 + "')");
        }
        List<HashMap<String, String>> recents = db.executeOne("SELECT * FROM menu WHERE name='" + order + "'");
        if (!recents.get(0).get("inner_category").equals("null") && Integer.parseInt(recents.get(0).get("use_inner")) == 1) {
            mToolbar.setTitle(recents.get(0).get("name") + " " + recents.get(0).get("inner_category"));
        } else {
            mToolbar.setTitle(recents.get(0).get("name"));
        }


        String url = bundle.getString("url");
        orderImage = url;
        ImageView view = findViewById(R.id.menuItemImage);
        Bitmap b = loadImageFromStorage(new ContextWrapper(this).getDir("imageDir", Context.MODE_PRIVATE).getPath(), url.split("/")[url.split("/").length - 1]);
        view.setImageBitmap(b);

        LinearLayout linearView = findViewById(R.id.container);
        List<HashMap<String, String>> customizations = db.executeOne("SELECT * FROM customizations WHERE item='" + bundle.get("name") + "' ORDER BY order_of_options");
        CardView countCardView = findViewById(R.id.countCard);
        countCardView.setCardBackgroundColor(Color.rgb(240, 240, 240));
        countCardView.setRadius(70);

        ImageButton add = findViewById(R.id.counterAdd);
        ImageButton subtract = findViewById(R.id.counterSubtract);
        EditText count = findViewById(R.id.counter);

        add.setOnClickListener(view1 -> count.setText(String.valueOf(Integer.parseInt(count.getText().toString())+1)));
        subtract.setOnClickListener(view1 -> {
            if (Integer.parseInt(count.getText().toString()) > 1)
                count.setText(String.valueOf(Integer.parseInt(count.getText().toString())-1));
        });

        AppBarLayout mAppBarLayout = findViewById(R.id.menu_app_bar);
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
                    showing = true;
                    showOption(R.id.cart);
                } else if (isShow) {
                    isShow = false;
                    showing = false;
                    hideOption(R.id.cart);
                }
            }
        });


        count.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (count.getText().toString().equals("")) {
                    count.setText("1");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (count.getText().toString().equals("")) {
                    count.setText("1");
                }
            }
        });

        //OTHER CUSTOMIZATIONS
        System.out.println(customizations);
        for (HashMap<String, String> map : customizations) {
            CardView cardView = new CardView(this);
            cardView.setCardBackgroundColor(Color.rgb(240,240,240));
            cardView.setRadius(70);
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            if (!map.get("title").equals("null")) {
                TextView title = new TextView(this);
                title.setText(map.get("title"));
                title.setTypeface(Typeface.DEFAULT_BOLD);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(20, 0, 0, 0);
                title.setLayoutParams(params);

                title.setTextSize(20);

                layout.addView(title);
            }
            switch(Objects.requireNonNull(map.get("type"))) {
                case "select":
                    RadioGroup group = new RadioGroup(this);

                    String[] options = map.get("options").split("\\|");
                    EditText other = new EditText(this);
                    for (String option : options) {
                        RadioButton radioButton = new RadioButton(this);
                        radioButton.setId(View.generateViewId());
                        radioButton.setText(option);
                        group.addView(radioButton);
                        if (option.equals("Other")) {
                            radioButton.setOnCheckedChangeListener((compoundButton, b1) -> {
                                if (b1 && compoundButton.getText().toString().equals("Other")) {
                                    radioButton.setText(option + ": ");
                                    other.setVisibility(View.VISIBLE);
                                } else {
                                    radioButton.setText(option);
                                    other.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                    final int[] otherBool = {0};
                    final int[] localLocation = {locationInList};
                    other.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            if (!charSequence.toString().equals("") && !others.containsKey(locationInList)) {
                                others.put(localLocation[0], other);
                                System.out.println("Adding");
                            } else if (charSequence.toString().equals("")) {
                                others.remove(localLocation[0]);
                                System.out.println("Removing");
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            if (!editable.toString().equals("") && !others.containsKey(locationInList)) {
                                others.put(localLocation[0], other);
                                System.out.println("Adding");
                            } else if (editable.toString().equals("")) {
                                others.remove(localLocation[0]);
                                System.out.println("Removing");
                            }
                        }
                    });
                    radios.put(locationInList, group);
                    group.setOnCheckedChangeListener((radioGroup, i) -> group.setBackgroundColor(Color.rgb(240, 240, 240)));
                    layout.addView(group);
                    other.setVisibility(View.GONE);
                    other.setSingleLine(false);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layout.addView(other, params);
                    customizationsInOrder.add("radioGroup|" + map.get("title") + "|" + map.get("is_required"));
                    break;
                case "checkbox":
                    String[] optionsChecks = map.get("options").split("\\|");
                    int i = 0;
                    for (String option : optionsChecks) {
                        LinearLayout horiLayout = new LinearLayout(this);
                        horiLayout.setOrientation(LinearLayout.HORIZONTAL);

                        TextView label = new TextView(this);
                        label.setText(option);
                        label.setTextSize(20);

                        CheckBox checkBox = new CheckBox(this);
                        horiLayout.addView(checkBox);
                        horiLayout.addView(label);
                        layout.addView(horiLayout);

                        HashMap<String, CheckBox> checkBoxHashMap = new HashMap<>();
                        checkBoxHashMap.put(option, checkBox);
                        checkboxes.add(checkBoxHashMap);
                    }
                    customizationsInOrder.add("checkbox|" + map.get("title") + "|" + map.get("is_required"));
                    break;
            }
            cardView.addView(layout);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 25, 0, 25);
            cardView.setLayoutParams(cardParams);
            linearView.addView(cardView);
            locationInList++;
        }
        CardView customCardView = new CardView(this);

        customCardView.setCardBackgroundColor(Color.rgb(240,240,240));
        customCardView.setRadius(70);
        LinearLayout customLinearLayout = new LinearLayout(this);
        customLinearLayout.setOrientation(LinearLayout.VERTICAL);
        EditText customText = new EditText(this);
        customText.setHint("Customizations");
        TextView customTextView = new TextView(this);
        customTextView.setText("Custom Requests:");

        customTextView.setTypeface(Typeface.DEFAULT_BOLD);

        RelativeLayout addToOrder = findViewById(R.id.addToOrder);

        LinearLayout.LayoutParams customParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        customParams.setMargins(20, 0, 0, 0);
        customTextView.setLayoutParams(customParams);

        customLinearLayout.addView(customTextView, customParams);

        LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        customText.setSingleLine(false);
        customText.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        customLinearLayout.addView(customText, editTextParams);
        customCardView.addView(customLinearLayout);
        linearView.addView(customCardView);


        addToOrder.setOnClickListener(view12 -> {
            boolean hadError = false;
            StringBuilder custom = new StringBuilder();
            boolean other = false;
            locationInList = 0;
            for (String item : customizationsInOrder) {
                if (item.contains("radioGroup")) {
                    try {
                        RadioGroup group = radios.get(locationInList);
                        RadioButton button = findViewById(group.getCheckedRadioButtonId());
                        if (button.getText().equals("Other: ")) {
                            System.out.println(others);
                            System.out.println("Pulling others, " + locationInList);
                            other = true;
                            custom.append("With " + others.get(locationInList).getText().toString().replaceFirst("^.", others.get(locationInList).getText().toString().substring(0,1).toUpperCase()) + " " + item.split("\\|")[1] + "\n");
                        } else {
                            custom.append("With " + button.getText().toString() + " " + item.split("\\|")[1] + "\n");
                        }
                    } catch (NullPointerException e) {
                        if (item.split("\\|")[2].equals("1")) {
                            showError(radios.get(locationInList));
                            hadError = true;
                        }
                        e.printStackTrace();
                     }
                } else if(item.contains("checkbox")) {
                        try {
                            int i = 0;
                            for (HashMap<String, CheckBox> hash : checkboxes) {
                                HashMap<String, String> everything = customizations.get(locationInList);
                                String[] options = customizations.get(locationInList).get("options").split("\\|");
                                if (hash.get(options[i]).isChecked()) {
                                    custom.append("With " + options[i] + " " + item.split("\\|")[1] + "\n");
                                }
                                i++;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                } else {
                    System.out.println("Nothing");
                }
                locationInList++;
            }
            if (!hadError) {
                int xTargetCoord;
                int yTargetCoord;
                if (showing) {
                    CollapsingToolbarLayout appBarLayout = findViewById(R.id.menuToolbar);
                    View iconView = appBarLayout.findViewById(R.id.cart);
                    int iconViewWidth = iconView.getWidth();
                    int iconViewHeight = iconView.getHeight();
                    int[] screens = new int[2];
                    iconView.getLocationOnScreen(screens);
                    xTargetCoord = Math.round(screens[0] + iconViewWidth/4);
                    yTargetCoord = Math.round(iconView.getY() + iconViewHeight/4);
                } else {
                    FloatingActionButton fab = findViewById(R.id.fab);
                    int fabWidth = fab.getWidth();
                    int fabHeight = fab.getHeight();
                    xTargetCoord = Math.round(fab.getX() + fabWidth/4);
                    yTargetCoord = Math.round(fab.getY() + fabHeight/4);
                }
                View iconViewStart = findViewById(R.id.buttonImage);
                int[] iconLocationStart = new int[2];
                iconViewStart.getLocationOnScreen(iconLocationStart);
                int xStartCoord = iconLocationStart[0];
                int yStartCoord = iconLocationStart[1] - iconViewStart.getHeight();
                CoordinatorLayout viewParent = findViewById(R.id.parentView);

                VectorDrawableCompat vectorDrawable = VectorDrawableCompat.create(getResources(), R.drawable.ic_baseline_shopping_cart_24, null);
                Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                        vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                vectorDrawable.draw(canvas);

                Thread newThread = new Thread(new animater(viewParent, bitmap, xTargetCoord, yTargetCoord, xStartCoord, yStartCoord, getApplicationContext()));
                newThread.start();

                DBHandler handler = new DBHandler(getApplicationContext());
                custom.append(customText.getText().toString());
                int otherInt = 0;
                if (other) {
                    otherInt = 1;
                }
                handler.executeOne("INSERT INTO cart (name,count,customizations,other) VALUES ('" + order + "','" + count.getText().toString() + "','" + custom + "'," + otherInt + ")");
            }
        });
    }

    class animater implements Runnable {

        private final CoordinatorLayout viewParent;
        private final Bitmap bitmap;
        private final int xTarget;
        private final int yTarget;
        private final int xStart;
        private final int yStart;
        private final Context context;

        public animater(CoordinatorLayout viewParent, Bitmap bitmap, int xTarget, int yTarget, int xStart, int yStart, Context context) {
            this.viewParent = viewParent;
            this.bitmap = bitmap;
            this.xTarget = xTarget;
            this.yTarget = yTarget;
            this.xStart = xStart;
            this.yStart = yStart;
            this.context = context;
        }

        @Override
        public void run() {
            ImageAnimator imageAnimator = new ImageAnimator(getApplicationContext(), bitmap, 2000);
            imageAnimator.setVisibility(View.VISIBLE);
            imageAnimator.setTargetPosition(xTarget, yTarget);
            imageAnimator.setStartingPosition(xStart, yStart);
            ContextCompat.getMainExecutor(context).execute(() -> viewParent.addView(imageAnimator));
            imageAnimator.startAnimation();

            synchronized (ImageAnimator.finished) {
                try {
                    ImageAnimator.finished.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            ContextCompat.getMainExecutor(context).execute(() -> viewParent.removeView(imageAnimator));
        }
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
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                }
            }
        }
        return super.dispatchTouchEvent( event );
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

    void showError(View view) {
        view.setBackgroundColor(Color.rgb(255, 187, 187));
    }
}