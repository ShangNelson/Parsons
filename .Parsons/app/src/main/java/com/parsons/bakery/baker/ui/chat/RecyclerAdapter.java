package com.parsons.bakery.baker.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parsons.bakery.DBHandler;
import com.parsons.bakery.R;
import com.parsons.bakery.baker.ui.orders.Order;
import com.parsons.bakery.baker.ui.orders.OrderItem;
import com.parsons.bakery.baker.ui.orders.OrderRecyclerAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.CustomViewHolder>  {
    private final List<Name> itemList;
    private final Context mContext;

    public RecyclerAdapter(Context context, List<Name> feedItemList) {
        this.itemList = feedItemList;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.person, null);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        Name name = itemList.get(i);
        customViewHolder.name.setText(name.getName());

        customViewHolder.total.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, Chat.class);
                intent.putExtra("name", name.getName());
                intent.putExtra("id", name.getId());
                mContext.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return (null != itemList ? itemList.size() : 0);
    }


    static class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView name;
        protected RelativeLayout total;

        public CustomViewHolder(View view) {
            super(view);
            total = view.findViewById(R.id.totalView);
            name = view.findViewById(R.id.name);
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);

        public void onLongItemClick(View view, int position);
    }
}
