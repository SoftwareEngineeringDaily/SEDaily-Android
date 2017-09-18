package com.koalatea.thehollidayinn.softwareengineeringdaily.podcast;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koalatea.thehollidayinn.softwareengineeringdaily.R;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Post;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.APIInterface;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.ApiUtils;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.FilterRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.PostRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by krh12 on 5/22/2017.
 */

public class PodListFragment extends Fragment {

    private String title;
    private String tagId;
    private PodcastAdapter podcastAdapter;
    private FilterRepository filterRepository;

    public static PodListFragment newInstance(String title, String tagId) {
        PodListFragment f = new PodListFragment();
        Bundle args = new Bundle();
        f.title = title;
        f.tagId = tagId;
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView =  (View) inflater.inflate(
                R.layout.fragment_podcast_horizontal, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);

        recyclerView.setHasFixedSize(true);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this.getContext());
        recyclerView.setLayoutManager(mLayoutManager);

        podcastAdapter = new PodcastAdapter(this);
        recyclerView.setAdapter(podcastAdapter);

        filterRepository = FilterRepository.getInstance();
        Subscriber<String> mySubscriber = new Subscriber<String>() {
            @Override
            public void onNext(String s) {
                getPosts(s);
            }

            @Override
            public void onCompleted() { }

            @Override
            public void onError(Throwable e) { }
        };
        filterRepository.getModelChanges().subscribe(mySubscriber);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getPosts("");
    }

    public void getPosts(String search) {
        APIInterface mService = ApiUtils.getKibbleService(getActivity());

        Map<String, String> data = new HashMap<>();
        rx.Observable query = mService.getPosts(data);

        UserRepository userRepository = UserRepository.getInstance(this.getContext());
        final PostRepository postRepository = PostRepository.getInstance();

        if (this.title.equals("Greatest Hits")) {
            data.put("type", "top");
        } else if (this.title.equals("Just For You") && !userRepository.getToken().isEmpty()) {
            query = mService.getRecommendations(data);
        } else if (tagId != null && !tagId.isEmpty()) {
            data.put("categories", tagId);
        }

        if (!search.isEmpty()) {
            data.put("search", search);
        }

        query
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<List<Post>>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    Log.v("keithtest", e.toString());
                }

                @Override
                public void onNext(List<Post> posts) {
                    podcastAdapter.setPosts(posts);
                    postRepository.setPosts(posts);
                }
            });
    }
}
