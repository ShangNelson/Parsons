package com.parsons.bakery.client;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.parsons.bakery.DBHandler;
import com.parsons.bakery.OutlineDrawable;
import com.parsons.bakery.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Cart extends AppCompatActivity {
    EditText name;
    TextView date;
    EditText phone;
    DBHandler dbHandler;
    RecyclerView recyclerView;
    TextView calendar;
    Button clear;
    Button next;
    CardView cart;
    RelativeLayout parent;
    boolean success = true;
    List<Integer> req = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        name = findViewById(R.id.nameText);
        date = findViewById(R.id.dateText);
        phone = findViewById(R.id.phoneText);
        recyclerView = findViewById(R.id.recycler);
        clear = findViewById(R.id.clear);
        next = findViewById(R.id.next);
        cart = findViewById(R.id.content);
        dbHandler = new DBHandler(this);
        parent = findViewById(R.id.fullParent);
        calendar = findViewById(R.id.dateCal);
        OutlineDrawable outlineDrawable = new OutlineDrawable(5, Color.BLACK);
        calendar.setBackground(outlineDrawable);

        Calendar calendarInstance = Calendar.getInstance();
        calendarInstance.set(calendarInstance.get(Calendar.YEAR), calendarInstance.get(Calendar.MONTH), (calendarInstance.get(Calendar.DATE))+1);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        String dateText = simpleDateFormat.format(calendarInstance.getTime());
        calendar.setText(dateText);

        date.setText(calendarInstance.get(Calendar.HOUR_OF_DAY) + ":00");

        DialogFragment dialogFragment = new CalendarDialog(calendar, getApplicationContext());
        DialogFragment dialogFragment1 = new TimeDialog(date, getApplicationContext());

        calendar.setOnClickListener(v -> dialogFragment.show(getSupportFragmentManager(), "calendar"));
        date.setOnClickListener(q -> dialogFragment1.show(getSupportFragmentManager(), "time"));

        List<HashMap<String, String>> returnedCartItems = dbHandler.executeOne("SELECT ct.id,ct.name,ct.count,ct.customizations,mn.img,mn.inner_category,mn.use_inner,mn.req FROM cart ct JOIN menu mn WHERE ct.name=mn.name");
        List<CartItem> itemsInCart = new ArrayList<>();
        List<HashMap<String, String>> otherItems = new ArrayList<>();
        for (HashMap<String, String> cartItem : returnedCartItems) {
            HashMap<String, String> thisItem = new HashMap<>();
            thisItem.put("localId", String.valueOf(returnedCartItems.indexOf(cartItem)));
            thisItem.put("id", cartItem.get("id"));
            thisItem.put("count", cartItem.get("count"));
            thisItem.put("name", cartItem.get("name"));
            thisItem.put("desc", cartItem.get("customizations"));
            for (HashMap<String, String> otherItem : otherItems) {
                if (otherItem.get("name").equals(cartItem.get("name")) && otherItem.get("desc").equals(cartItem.get("customizations"))) {
                    int previousCount = Integer.parseInt(otherItem.get("count"));
                    itemsInCart.remove(Integer.parseInt(otherItem.get("localId")));
                    cartItem.put("count", String.valueOf(Integer.parseInt(cartItem.get("count")) + previousCount));
                    dbHandler.executeOne("DELETE FROM cart WHERE id=" + otherItem.get("id"));
                    dbHandler.executeOne("UPDATE cart SET count=" + (Integer.parseInt(cartItem.get("count"))) + " WHERE id=" + cartItem.get("id"));
                }
            }
            otherItems.add(thisItem);
            if (!cartItem.get("req").equals("null")) {
                if (!req.contains(Integer.parseInt(cartItem.get("req").split("\\|")[0]))) {
                    req.add(Integer.parseInt(cartItem.get("req").split("\\|")[0]));
                }
            }
            CartItem item = new CartItem();
            item.setId(Integer.parseInt(cartItem.get("id")));
            item.setCount(cartItem.get("count"));
            if (!cartItem.get("inner_category").equals("null") && Integer.parseInt(cartItem.get("use_inner")) == 1) {
                item.setName(cartItem.get("name") + "\n" + cartItem.get("inner_category"));
            } else {
                item.setName(cartItem.get("name"));
            }
            item.setUrl(cartItem.get("img"));
            item.setCustomizations(cartItem.get("customizations"));
            itemsInCart.add(item);
        }

        LinearLayoutManager ln = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(ln);
        recyclerView.setNestedScrollingEnabled(false);
        CartRecyclerAdapter adapter = new CartRecyclerAdapter(this, itemsInCart);
        recyclerView.setAdapter(adapter);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0xfff7f7f7, 0xfff7f7f7});
        drawable.setSize(1,30);
        itemDecoration.setDrawable(drawable);
        recyclerView.addItemDecoration(itemDecoration);

        clear.setOnClickListener(view -> {
            adapter.clear();
            dbHandler.executeOne("DELETE FROM cart");
        });
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String savedName = sharedPreferences.getString("name", "");
        String savedNumber = sharedPreferences.getString("number", "");
        if (!savedNumber.equals("")) {
            String formattedPhoneNumber = PhoneNumberUtils.formatNumber(savedNumber);
            phone.setText(formattedPhoneNumber);
        } else {
            phone.setText(savedNumber);
        }

        name.setText(savedName);

        next.setOnClickListener(view -> {

            List<HashMap<String, String>> returnedCartItems1 = dbHandler.executeOne("SELECT ct.id,ct.name,ct.count,ct.customizations,mn.img,mn.inner_category,mn.use_inner,mn.req,mn.category,ct.other FROM cart ct JOIN menu mn WHERE ct.name=mn.name");

            if (returnedCartItems1.isEmpty()) {
                Snackbar.make(parent, "Please add items to cart before ordering.", Snackbar.LENGTH_LONG).show();
                cart.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(255, 187, 187)));
                return;
            }

            String nameString = name.getText().toString();
            String dateString = calendar.getText().toString();
            String timeString = date.getText().toString();
            String phoneString = phone.getText().toString();
            int nearest = 0;
            if (req.size() > 0) {
                nearest = Collections.max(Collections.unmodifiableList(req));
            }
            String phoneTestNumber =phoneString.replace("-", "");
            if (nameString.equals("")) {
                name.setBackgroundColor(Color.rgb(255, 187, 187));
                name.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if (!editable.toString().equals("")) {
                            name.setBackgroundColor(Color.rgb(240, 240, 240));
                        }
                    }
                });
                Snackbar.make(parent, "Please enter name for order.", Snackbar.LENGTH_LONG).show();
                return;
            } else if (phoneTestNumber.equals("") || (phoneTestNumber.length() < 10 || phoneTestNumber.length() > 14)) {
                phone.setBackgroundColor(Color.rgb(255, 187, 187));
                phone.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if (!editable.toString().equals("")) {
                            phone.setBackgroundColor(Color.rgb(240, 240, 240));
                        }
                    }
                });
                Snackbar.make(parent, "Please add a valid contact number in case of questions.", Snackbar.LENGTH_LONG).show();
                return;
            }
            String formattedPhoneNumber = PhoneNumberUtils.formatNumber(phoneString, "US");
            String[] dates = dateString.split("/");
            String[] times = timeString.split(":");

            Calendar future = Calendar.getInstance();
            future.set(Integer.parseInt(dates[2]), Integer.parseInt(dates[0])-1, Integer.parseInt(dates[1]), Integer.parseInt(times[0]), Integer.parseInt(times[1]));

            Calendar now = Calendar.getInstance();

            long differenceInMilliseconds = future.getTimeInMillis() - now.getTimeInMillis();

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String nowTime = dateFormat.format(future.getTime().getTime());
            if ((differenceInMilliseconds) < nearest*3600000L) {
                date.setBackgroundColor(Color.rgb(255, 187, 187));
                calendar.setBackgroundColor(Color.rgb(255, 187, 187));
                Snackbar.make(parent, "An item in your cart requires to be ordered further in advance.", Snackbar.LENGTH_LONG).show();
                return;
            }
            date.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    date.setBackgroundColor(Color.rgb(240, 240, 240));
                    calendar.setBackgroundColor(Color.rgb(240, 240, 240));
                }
            });
            calendar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    date.setBackgroundColor(Color.rgb(240, 240, 240));
                    calendar.setBackgroundColor(Color.rgb(240, 240, 240));
                }
            });

            AlertDialog.Builder builder = new AlertDialog.Builder(Cart.this);
            builder.setTitle("Confirmation");
            builder.setMessage("Are you ready to send your order?");

            builder.setPositiveButton("Yes", (dialog, which) -> {
                try {
                    boolean verification = false;
                    JSONObject order = new JSONObject();
                    order.put("name", nameString);
                    order.put("number", formattedPhoneNumber);
                    order.put("time", nowTime);
                    order.put("numberOfItems", returnedCartItems1.size());
                    if (returnedCartItems1.size() > 5) {
                        verification = true;
                    }
                    if (DateUtils.isToday(future.getTime().getTime())) {
                        verification = true;
                    }
                    int numOfOrdersAdded = 0;
                    for (HashMap<String, String> cartItem : returnedCartItems1) {
                        JSONObject orderItem = new JSONObject();
                        if (!cartItem.get("inner_category").equals("null") && Integer.parseInt(cartItem.get("use_inner")) == 1) {
                            orderItem.put("item", cartItem.get("name") + "\n" + cartItem.get("inner_category"));
                        } else {
                            orderItem.put("item", cartItem.get("name"));
                        }
                        if (cartItem.get("category").equals("Cakes & Cupcakes")) {
                            verification = true;
                        }
                        if (Integer.parseInt(cartItem.get("count")) > 60) {
                            verification = true;
                        }
                        if (Integer.parseInt(cartItem.get("other")) == 1) {
                            verification = true;
                        }
                        orderItem.put("type", cartItem.get("category"));
                        orderItem.put("count", cartItem.get("count"));
                        String customs = cartItem.get("customizations");
                        customs = customs.replace("\"", "\\\"").replace("\'", "\\\'");
                        orderItem.put("customizations", customs);
                        order.put("order_item_" + numOfOrdersAdded, orderItem);
                        numOfOrdersAdded++;
                    }

                    Intent intent = new Intent(getApplicationContext(), sendingOrder.class);
                    intent.putExtra("order", order.toString());
                    intent.putExtra("verification", verification);
                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Snackbar.make(parent, "An error occurred, try again in a few minutes.", Snackbar.LENGTH_LONG).show();
                }
            });
            builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();

        });
    }

}