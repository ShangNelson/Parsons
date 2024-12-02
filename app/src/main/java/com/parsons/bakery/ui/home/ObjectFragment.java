package com.parsons.bakery.ui.home;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.parsons.bakery.R;

import java.util.List;

public class ObjectFragment extends Fragment {
    List<Bitmap> pics;
    int i;
    public ObjectFragment(List<Bitmap> pics, int i) {
        this.pics = pics;
        this.i = i;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.object, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ((ImageView) view.findViewById(R.id.image1)).setImageBitmap(pics.get(i));
    }
}