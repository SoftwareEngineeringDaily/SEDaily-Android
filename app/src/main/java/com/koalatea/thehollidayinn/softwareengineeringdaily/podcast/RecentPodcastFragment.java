package com.koalatea.thehollidayinn.softwareengineeringdaily.podcast;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koalatea.thehollidayinn.softwareengineeringdaily.R;

/*
 * Created by keithholliday on 9/16/17.
 */

public class RecentPodcastFragment extends Fragment {
    private ViewPager viewPager;
    private TabLayout tabLayout;

    public static RecentPodcastFragment newInstance() {
        return new RecentPodcastFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_recent_podcast, container, false);

        tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        RecentPodcatsPageAdapter recentPodcatsPageAdapter = new RecentPodcatsPageAdapter(this.getActivity().getSupportFragmentManager());
        viewPager = (ViewPager) rootView.findViewById(R.id.pager);
        viewPager.setAdapter(recentPodcatsPageAdapter);
        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
            }
        });

        return rootView;
    }

    public void goHome () {
        TabLayout.Tab tab = tabLayout.getTabAt(0);
        if (tab == null) {
            return;
        }
        tab.select();
    }
}