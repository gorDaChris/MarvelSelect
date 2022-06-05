package com.example.marvelselect;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class ViewPagerAdapterInfo extends FragmentStatePagerAdapter {

    //variables
    private List<Fragment> fragmentList;

    //constructor
    public ViewPagerAdapterInfo(@NonNull FragmentManager fm, List<Fragment> fragmentList) {
        super(fm);
        this.fragmentList = fragmentList;
    }//ViewPagerAdapterInfo

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }//getItem

    @Override
    public int getCount() {
        return fragmentList.size();
    }//getCount
}
