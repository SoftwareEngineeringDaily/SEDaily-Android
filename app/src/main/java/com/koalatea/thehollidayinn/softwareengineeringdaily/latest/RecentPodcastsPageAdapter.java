package com.koalatea.thehollidayinn.softwareengineeringdaily.latest;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.koalatea.thehollidayinn.softwareengineeringdaily.podcast.PodListFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * Created by keithholliday on 9/16/17.
 */

class RecentPodcastsPageAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragmentList = new ArrayList<>();
    private List<String> titles;

    public RecentPodcastsPageAdapter(FragmentManager fm) {
        super(fm);

        String LATEST = "Latest";
        titles = Arrays.asList(
                "All",
                "Business and Philosophy",
                "Blockchain",
                "Cloud Engineering",
                "Data",
                "JavaScript",
                "Machine Learning",
                "Open Source",
                "Security",
                "Hackers",
                "Greatest Hits"
        );

        List<String> categories = Arrays.asList(
                "",
                "1068",
                "1082",
                "1079",
                "1081",
                "1084",
                "1080",
                "1078",
                "1083",
                "1085",
                "1069"
        );

        for (String category : categories) {
            fragmentList.add(PodListFragment.newInstance(LATEST, category));
        }
    }

    @Override
    public int getCount() {
        return titles.size();
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }
}
