package com.koalatea.thehollidayinn.softwareengineeringdaily.podcast;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ethanhua.skeleton.RecyclerViewSkeletonScreen;
import com.ethanhua.skeleton.Skeleton;
import com.koalatea.thehollidayinn.softwareengineeringdaily.MainActivity;
import com.koalatea.thehollidayinn.softwareengineeringdaily.R;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Post;
import com.koalatea.thehollidayinn.softwareengineeringdaily.repositories.FilterRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.util.ReactiveUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by keithholliday on 2/4/18.
 */

public class TopRecListFragment extends Fragment {
    private static String TAG = "PodList";
    private String title;
    private String tagId;
    private PodcastAdapter podcastAdapter;
    private DisposableObserver<String> myDisposableObserver;
    private RecyclerViewSkeletonScreen skeletonScreen;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PodcastListViewModel podcastListViewModel;

    public static TopRecListFragment newInstance(String title, String tagId) {
        TopRecListFragment f = new TopRecListFragment();
        Bundle args = new Bundle();
        f.title = title;
        f.tagId = tagId;
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_toprec_list, container, false);

        ButterKnife.bind(this, rootView);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this.getContext());
        recyclerView.setLayoutManager(mLayoutManager);

        podcastAdapter = new PodcastAdapter();
        recyclerView.setAdapter(podcastAdapter);

        skeletonScreen = Skeleton.bind(recyclerView)
                .adapter(podcastAdapter)
                .load(R.layout.item_skeleton_news)
                .shimmer(true)
                .show();

        swipeRefreshLayout = rootView.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        podcastListViewModel.getPosts("", title, tagId);
                    }
                }
        );

        podcastListViewModel = ViewModelProviders
                .of(this)
                .get(PodcastListViewModel.class);
        podcastListViewModel.getPostList().observe(this, posts -> {
            Timber.v("podcastListViewModel" + tagId);
            if (posts != null) updatePosts(posts);
        });

        this.setUpSubscription();

        return rootView;
    }

    public void setUpSubscription() {
        if (myDisposableObserver != null) {
            return;
        }

        myDisposableObserver = new DisposableObserver<String>() {
            @Override
            public void onNext(String s) {
                podcastListViewModel.getPosts(s, title, tagId);
            }

            @Override
            public void onComplete() { }

            @Override
            public void onError(Throwable e) { }
        };
        FilterRepository filterRepository = FilterRepository.getInstance();
        filterRepository.getModelChanges().subscribe(myDisposableObserver);

        podcastAdapter.getPositionClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ReactiveUtil.toDetailObservable(getActivity()));
    }

    @Override
    public void onStart() {
        super.onStart();
        podcastListViewModel.getPosts("", title, tagId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (myDisposableObserver != null) {
            myDisposableObserver.isDisposed();
        }
    }

    private void updatePosts(List<Post> postList) {
        podcastAdapter.setPosts(postList);
        swipeRefreshLayout.setRefreshing(false);
        skeletonScreen.hide();
    }
}
