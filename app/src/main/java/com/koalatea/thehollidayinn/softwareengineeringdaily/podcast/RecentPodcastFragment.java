package com.koalatea.thehollidayinn.softwareengineeringdaily.podcast;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koalatea.thehollidayinn.softwareengineeringdaily.R;

/**
 * Created by keithholliday on 9/16/17.
 */

public class RecentPodcastFragment extends Fragment {
  RecentPodcastsPageAdapter recentPodcatsPageAdapter;
  ViewPager viewPager;
  TabLayout tabLayout;

  public static RecentPodcastFragment newInstance() {
    RecentPodcastFragment f = new RecentPodcastFragment();
    return f;
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
      ViewGroup container, Bundle savedInstanceState) {
    View rootView = (View) inflater.inflate(
        R.layout.fragment_recent_podcast, container, false);

    tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
    recentPodcatsPageAdapter = new RecentPodcastsPageAdapter(this.getActivity().getSupportFragmentManager());
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
    tab.select();
  }
}