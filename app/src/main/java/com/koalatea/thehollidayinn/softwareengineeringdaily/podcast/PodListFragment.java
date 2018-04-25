package com.koalatea.thehollidayinn.softwareengineeringdaily.podcast;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.arch.persistence.room.Room;
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
import com.koalatea.thehollidayinn.softwareengineeringdaily.app.SEDApp;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.AppDatabase;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Bookmark;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Post;
import com.koalatea.thehollidayinn.softwareengineeringdaily.repositories.FilterRepository;

import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.APIInterface;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.BookmarkDao;
import com.koalatea.thehollidayinn.softwareengineeringdaily.repositories.PostRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.repositories.UserRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.util.ReactiveUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/*
 * Created by krh12 on 5/22/2017.
 */

public class PodListFragment extends Fragment {
  private static String TAG = "PodList";
  private String title;
  private String tagId;
  private PodcastAdapter podcastAdapter;
  private DisposableObserver<String> myDisposableObserver;
  private RecyclerViewSkeletonScreen skeletonScreen;
  private SwipeRefreshLayout swipeRefreshLayout;
  private PodcastListViewModel podcastListViewModel;

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

    RecyclerView recyclerView = rootView.findViewById(R.id.my_recycler_view);
    recyclerView.setHasFixedSize(true);

    LinearLayoutManager mLayoutManager = new LinearLayoutManager(this.getContext());
    recyclerView.setLayoutManager(mLayoutManager);

    podcastAdapter = new PodcastAdapter();
    recyclerView.setAdapter(podcastAdapter);
    podcastAdapter.getPositionClicks()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ReactiveUtil.toDetailObservable(getActivity()));

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
          if (title != null && title.equals("Bookmarks")) {
            getPosts("");
            return;
          }
          podcastListViewModel.getPosts("", title, tagId);
        }
      }
    );

    podcastListViewModel = ViewModelProviders
            .of(this)
            .get(PodcastListViewModel.class);
    podcastListViewModel.getPostList().observe(this, posts -> {
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
        if (title != null && title.equals("Bookmarks")) {
          getPosts("");
          return;
        }
        podcastListViewModel.getPosts(s, title, tagId);
      }

      @Override
      public void onComplete() { }

      @Override
      public void onError(Throwable e) { }
    };
    FilterRepository filterRepository = FilterRepository.getInstance();
    filterRepository.getModelChanges().subscribe(myDisposableObserver);
  }

  @Override
  public void onStart() {
    super.onStart();
    if (title != null && title.equals("Bookmarks")) {
      getPosts("");
      return;
    }
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

  private void getPosts(String search) {
    APIInterface mService = SEDApp.component.kibblService();

    // @TODO: Replace tmp with query

    Map<String, String> data = new HashMap<>();
    Observable<List<Post>> query = mService.getPosts(data);

    UserRepository userRepository = SEDApp.component.userRepository();
    PostRepository postRepository = PostRepository.getInstance();

    if (this.title != null && this.title.equals("Greatest Hits")) {
      data.put("type", "top");
    } else if (this.title != null && this.title.equals("Just For You") && !userRepository.getToken().isEmpty()) {
      query = mService.getRecommendations(data);
    } else if (this.title != null && this.title.equals("Bookmarks")) {
      query = mService.getBookmarks();
    } else if (tagId != null && !tagId.isEmpty()) {
      data.put("categories", tagId);
    }

    if (!search.isEmpty()) {
      data.put("search", search);
    }

    query
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(new DisposableObserver<List<Post>>() {
        @Override
        public void onComplete() {
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
          if (title != null && title.equals("Bookmarks")) {
            ArrayList<Bookmark> bookmarks = new ArrayList<>();
            for(Post post: posts) {
              bookmarks.add(new Bookmark(post));
            }

            AppDatabase db = Room.databaseBuilder(SEDApp.component.context(), AppDatabase.class, "sed-db").build();
            Observable.just(db)
                    .subscribeOn(Schedulers.io())
                    .subscribe(bookmarkdb -> {
                      BookmarkDao bookmarkDao = db.bookmarkDao();
                      bookmarkDao.insertAll(bookmarks);
                    });

            podcastListViewModel.setPostList(posts);
            podcastAdapter.setPosts(posts);
          }
          swipeRefreshLayout.setRefreshing(false);
        }
      });
  }
}
