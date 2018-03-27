package com.koalatea.thehollidayinn.softwareengineeringdaily.latest;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koalatea.thehollidayinn.softwareengineeringdaily.MainActivity;
import com.koalatea.thehollidayinn.softwareengineeringdaily.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by keithholliday on 9/16/17.
 */

public class RecentPodcastFragment extends Fragment {
  RecentPodcastsPageAdapter recentPodcatsPageAdapter;

  @BindView(R.id.pager)
  ViewPager viewPager;

  private TabLayout tabLayout;

  public static RecentPodcastFragment newInstance() {
    RecentPodcastFragment f = new RecentPodcastFragment();
    return f;
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
      ViewGroup container, Bundle savedInstanceState) {

    View rootView = (View) inflater.inflate(
        R.layout.fragment_recent_podcast, container, false);

    ButterKnife.bind(this, rootView);

    recentPodcatsPageAdapter = new RecentPodcastsPageAdapter(getChildFragmentManager());
    viewPager.setAdapter(recentPodcatsPageAdapter);

    tabLayout = this.getActivity().findViewById(R.id.tabs);
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