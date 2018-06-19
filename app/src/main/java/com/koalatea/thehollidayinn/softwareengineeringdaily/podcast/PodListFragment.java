package com.koalatea.thehollidayinn.softwareengineeringdaily.podcast;

import android.arch.lifecycle.ViewModelProviders;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
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

    RecyclerView recyclerView  = getRecylceView(rootView);
    createSkeltonScreen(recyclerView);
    setUpSwipeRefresh(rootView);
    setUpViewModel();
    this.setUpSubscription();

    return rootView;
  }

  private RecyclerView getRecylceView(View rootView) {
    RecyclerView recyclerView = rootView.findViewById(R.id.my_recycler_view);
    recyclerView.setHasFixedSize(true);

    GridLayoutManager mLayoutManager = new GridLayoutManager(this.getContext(), 2);
    recyclerView.setLayoutManager(mLayoutManager);

    podcastAdapter = new PodcastAdapter();
    recyclerView.setAdapter(podcastAdapter);
    podcastAdapter.getPositionClicks()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ReactiveUtil.toDetailObservable(getActivity()));

    return recyclerView;
  }

  private void createSkeltonScreen(RecyclerView recyclerView) {
    skeletonScreen = Skeleton.bind(recyclerView)
            .adapter(podcastAdapter)
            .load(R.layout.item_skeleton_news)
            .shimmer(true)
            .show();
  }

  private void setUpSwipeRefresh(View rootView) {
    swipeRefreshLayout = rootView.findViewById(R.id.swiperefresh);
    swipeRefreshLayout.setOnRefreshListener(
            new SwipeRefreshLayout.OnRefreshListener() {
              @Override
              public void onRefresh() {
                // @TODO: make bookmark fragment
                if (title != null && title.equals("Bookmarks")) {
                  getPosts("");
                  return;
                }
                podcastListViewModel.getPosts("", title, tagId);
              }
            }
    );
  }

  private void setUpViewModel() {
    podcastListViewModel = ViewModelProviders
            .of(this)
            .get(PodcastListViewModel.class);
    podcastListViewModel.getPostList().observe(this, posts -> {
      if (posts != null) updatePosts(posts);
    });
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

  private Observable<List<Post>> getQuery(String search) {
    APIInterface mService = SEDApp.component.kibblService();
    Map<String, String> data = new HashMap<>();
    Observable<List<Post>> query = mService.getPosts(data);

    UserRepository userRepository = SEDApp.component.userRepository();

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

    return query;
  }

  private void setUpBookmarks(List<Post> posts) {
    ArrayList<Bookmark> bookmarks = new ArrayList<>();
    for(Post post: posts) {
      bookmarks.add(new Bookmark(post));
    }

    insertBookmarks(bookmarks);

    podcastListViewModel.setPostList(posts);
    podcastAdapter.setPosts(posts);
  }

  private void insertBookmarks(ArrayList<Bookmark> bookmarks) {
    Context context = SEDApp.component.context();
    AppDatabase db = AppDatabase.getDatabase();
    Observable.just(db)
            .subscribeOn(Schedulers.io())
            .subscribe(bookmarkdb -> {
              BookmarkDao bookmarkDao = db.bookmarkDao();
              bookmarkDao.insertAll(bookmarks);
            });
  }

  private void getPosts(String search) {
    getQuery(search)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(new DisposableObserver<List<Post>>() {
        @Override
        public void onComplete() {
          skeletonScreen.hide();
        }

        @Override
        public void onError(Throwable e) {}

        @Override
        public void onNext(List<Post> posts) {
          podcastAdapter.setPosts(posts);

          PostRepository postRepository = PostRepository.getInstance();
          postRepository.setPosts(posts);

          if (title != null && title.equals("Bookmarks")) {
            setUpBookmarks(posts);
          }

          swipeRefreshLayout.setRefreshing(false);
        }
      });
  }
}
