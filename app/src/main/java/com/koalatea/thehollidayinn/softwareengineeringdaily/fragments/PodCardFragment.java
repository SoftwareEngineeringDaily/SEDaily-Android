package com.koalatea.thehollidayinn.softwareengineeringdaily.fragments;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.koalatea.thehollidayinn.softwareengineeringdaily.audio.MusicService;
import com.koalatea.thehollidayinn.softwareengineeringdaily.adapters.PodcastAdapter;
import com.koalatea.thehollidayinn.softwareengineeringdaily.R;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Post;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.APIInterface;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.ApiUtils;
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

public class PodCardFragment extends Fragment {

    private String title;
    private static final String TAG = PodCardFragment.class.getSimpleName();
    private PodcastAdapter podcastAdapter;

    public static PodCardFragment newInstance(String title, String mediaId) {
        PodCardFragment f = new PodCardFragment();
        Bundle args = new Bundle();
        f.title = title;
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

        getPosts();

        return rootView;
    }

    public void getPosts() {
        APIInterface mService = ApiUtils.getKibbleService(getActivity());

        Map<String, String> data = new HashMap<>();
        rx.Observable query = mService.getPosts(data);

        UserRepository userRepository = UserRepository.getInstance(this.getContext());
        final PostRepository postRepository = PostRepository.getInstance();

        if (this.title.equals("Greatest Hits")) {
            data.put("type", "top");
        } else if (this.title.equals("Just For You") && !userRepository.getToken().isEmpty()) {
            query = mService.getRecommendations(data);
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
