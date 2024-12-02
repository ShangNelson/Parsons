package com.parsons.bakery;

import android.graphics.Bitmap;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.parsons.bakery.client.ObjectFragment;

import java.util.List;

public class PagerAdapter extends FragmentStatePagerAdapter {
    List<Bitmap> pics;
    public PagerAdapter(FragmentManager fm, List<Bitmap> pics) {
        super(fm);
        this.pics = pics;
    }

    @Override
    public Fragment getItem(int i) {
        return new ObjectFragment(pics, i);
    }

    @Override
    public int getCount() {
        return pics.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Image " + position;
    }
}


