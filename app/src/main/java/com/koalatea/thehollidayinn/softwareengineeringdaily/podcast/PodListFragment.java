package com.koalatea.thehollidayinn.softwareengineeringdaily.podcast;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ethanhua.skeleton.RecyclerViewSkeletonScreen;
import com.ethanhua.skeleton.Skeleton;
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
import timber.log.Timber;

/*
 * Created by krh12 on 5/22/2017.
 */

public class PodListFragment extends Fragment {
  private static String TAG = "PodList";
  private String title;
  private String tagId;
  private PodcastAdapter podcastAdapter;
  private Subscriber<String> mySubscriber;
  private RecyclerViewSkeletonScreen skeletonScreen;
  private SwipeRefreshLayout swipeRefreshLayout;

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
    View rootView = inflater.inflate(
      R.layout.fragment_podcast_horizontal, container, false);

    RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);
    recyclerView.setHasFixedSize(true);

    LinearLayoutManager mLayoutManager = new LinearLayoutManager(this.getContext());
    recyclerView.setLayoutManager(mLayoutManager);

    podcastAdapter = new PodcastAdapter(this);
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
          getPosts("");
        }
      }
    );


    this.setUpSubscription();

    return rootView;
  }

  public void setUpSubscription() {
    if (mySubscriber != null) {
      return;
    }

    mySubscriber = new Subscriber<String>() {
      @Override
      public void onNext(String s) {
        getPosts(s);
      }

      @Override
      public void onCompleted() { }

      @Override
      public void onError(Throwable e) { }
    };
    FilterRepository filterRepository = FilterRepository.getInstance();
    filterRepository.getModelChanges().subscribe(mySubscriber);
  }

  @Override
  public void onStart() {
    super.onStart();
    this.getPosts("");
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (mySubscriber != null) {
      mySubscriber.unsubscribe();
    }
  }

  private void getPosts(String search) {
    APIInterface mService = ApiUtils.getKibbleService(getActivity());

    // @TODO: Replace tmp with query

    Map<String, String> data = new HashMap<>();
    rx.Observable<List<Post>> query = mService.getPosts(data);

    UserRepository userRepository = UserRepository.getInstance(this.getContext());
    final PostRepository postRepository = PostRepository.getInstance();

    if (this.title != null && this.title.equals("Greatest Hits")) {
      data.put("type", "top");
    } else if (this.title != null && this.title.equals("Just For You") && !userRepository.getToken().isEmpty()) {
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
          skeletonScreen.hide();
        }

        @Override
        public void onError(Throwable e) {
            Log.v(TAG, e.toString());
        }

        @Override
        public void onNext(List<Post> posts) {
          podcastAdapter.setPosts(posts);
          postRepository.setPosts(posts);
          swipeRefreshLayout.setRefreshing(false);
        }
      });
  }
}
