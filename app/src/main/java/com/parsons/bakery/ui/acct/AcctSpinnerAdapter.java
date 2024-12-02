package com.parsons.bakery.ui.acct;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parsons.bakery.ui.chat.object;

import java.util.List;

public class AcctSpinnerAdapter extends ArrayAdapter<object> {
    private final int mResource;
    private final int mDropdownResource;
    public AcctSpinnerAdapter(Context context, int resource, int textViewResourceId, List<object> objects, int dropdownResource) {
        super(context, resource, textViewResourceId, objects);
        mResource = resource;
        mDropdownResource = dropdownResource;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mResource);
    }
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mDropdownResource);
    }
    private View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
        View view;
        TextView text;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }
        text = (TextView) view;
        text.setText(getItem(position).getDisplayText());
        return view;
    }
}