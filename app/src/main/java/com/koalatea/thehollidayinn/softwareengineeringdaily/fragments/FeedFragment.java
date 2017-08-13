package com.koalatea.thehollidayinn.softwareengineeringdaily.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koalatea.thehollidayinn.softwareengineeringdaily.ContactChip;
import com.koalatea.thehollidayinn.softwareengineeringdaily.R;
import com.koalatea.thehollidayinn.softwareengineeringdaily.adapters.FeedAdapter;
import com.koalatea.thehollidayinn.softwareengineeringdaily.adapters.PodcastAdapter;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.ChipInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by krh12 on 5/22/2017.
 */

public class FeedFragment extends Fragment {
    public static final String ARG_OBJECT = "object";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView =  (View) inflater.inflate(
                R.layout.fragment_feed, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);

        recyclerView.setHasFixedSize(true);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this.getContext());
        recyclerView.setLayoutManager(mLayoutManager);

        String[] mDataset = {"one", "two", "three", "four"};
        final FeedAdapter feedAdapter = new FeedAdapter(mDataset, this.getActivity());
        recyclerView.setAdapter(feedAdapter);

        ChipsInput chipsInput = (ChipsInput) rootView.findViewById(R.id.chips_input);
        List<ContactChip> contactList = new ArrayList<>();
        contactList.add(new ContactChip(".Net", ".Net", 1200));
        chipsInput.setFilterableList(contactList);

        chipsInput.addChipsListener(new ChipsInput.ChipsListener() {
            @Override
            public void onChipAdded(ChipInterface chip, int newSize) {
                Log.v("keithtest", String.valueOf(chip.getId()));
                feedAdapter.getFeed(String.valueOf(chip.getId()));
            }

            @Override
            public void onChipRemoved(ChipInterface chip, int newSize) {
                // chip removed
                // newSize is the size of the updated selected chip list
            }

            @Override
            public void onTextChanged(CharSequence text) {
                // text changed
            }
        });

        return rootView;
    }
}
