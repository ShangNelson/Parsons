package com.parsons.bakery.ui.chat;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.parsons.bakery.DBHandler;
import com.parsons.bakery.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.CustomViewHolder>  {
    private List<Message> itemList;
    private final Context mContext;

    public MessageAdapter(Context context, List<Message> feedItemList) {
        this.itemList = feedItemList;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message, null);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        Message message = itemList.get(i);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) customViewHolder.message.getLayoutParams();
        DBHandler dbHandler = new DBHandler(mContext);
        String me = dbHandler.executeOne("SELECT name FROM acct").get(0).get("name");
        if (message.getSender().equals(me)) {
            System.out.println("END - MINE");
            params.gravity = Gravity.END;
        } else {
            System.out.println("START - OTHER PERSON");
            params.gravity = Gravity.START;
        }
        customViewHolder.message.setLayoutParams(params);
        customViewHolder.message.setText(message.getMessage());
    }


    @Override
    public int getItemCount() {
        return (null != itemList ? itemList.size() : 0);
    }


    static class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView message;
        protected CardView parent;

        public CustomViewHolder(View view) {
            super(view);
            parent = view.findViewById(R.id.parent);
            message = view.findViewById(R.id.message);
        }
    }

    public void updateInfo(List<Message> newInfo) {
        itemList = newInfo;
        notifyDataSetChanged();
    }
}
