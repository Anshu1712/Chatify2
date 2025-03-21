package com.example.chatify.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class MyAdapter extends FragmentStateAdapter {

    private ArrayList<Fragment> fragments = new ArrayList<>();

    public MyAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    public void addFragment(Fragment fragment) {
        fragments.add(fragment);
    }
}
