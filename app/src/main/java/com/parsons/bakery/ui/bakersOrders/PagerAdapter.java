package com.parsons.bakery.ui.bakersOrders;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class PagerAdapter extends FragmentPagerAdapter {

    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new IndividualOrders();
            case 1:
                return new AllOrders();
            default:
                return null;
        }    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Individual Orders";
            case 1:
                return "Combined Orders";
            default:
                return null;
        }
    }

}
