package com.koalatea.thehollidayinn.softwareengineeringdaily.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koalatea.thehollidayinn.softwareengineeringdaily.R;

/**
 * Created by krh12 on 5/22/2017.
 */

public class PodcastFragment extends Fragment {
    public static final String ARG_OBJECT = "object";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(
                R.layout.fragment_podcast, container, false);


        PodCardFragment firstFragment = PodCardFragment.newInstance("Latest", "");
        this.getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.new_podcasts, firstFragment).commit();

        PodCardFragment firstFragment2 = PodCardFragment.newInstance("Just For You", "");
        this.getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.new_podcasts2, firstFragment2).commit();

        PodCardFragment firstFragment3 = PodCardFragment.newInstance("Greatest Hits", "");
        this.getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.new_podcasts3, firstFragment3).commit();

        return rootView;
    }

    private void playPodcast () {

    }
}
