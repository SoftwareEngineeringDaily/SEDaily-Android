package com.koalatea.thehollidayinn.softwareengineeringdaily.podcast;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.koalatea.thehollidayinn.softwareengineeringdaily.app.SEDApp;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Post;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.APIInterface;
import com.koalatea.thehollidayinn.softwareengineeringdaily.repositories.PostRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.repositories.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by keithholliday on 12/22/17.
 */

public class PodcastListViewModel extends ViewModel {
    private MutableLiveData<List<Post>> postList;

    public PodcastListViewModel(){
        postList = new MutableLiveData<>();
    }

    public MutableLiveData<List<Post>> getPostList() {
        return this.postList;
    }

    public void setPostList(List<Post> postList) {
        this.postList.postValue(postList);
    }

    public void getPosts(String search, String title, String tagId) {
        APIInterface mService = SEDApp.component.kibblService();

        // @TODO: Replace tmp with query

        Map<String, String> data = new HashMap<>();
        Observable<List<Post>> query = mService.getPosts(data);

        UserRepository userRepository = UserRepository.getInstance(SEDApp.component().context());
        final PostRepository postRepository = PostRepository.getInstance();

        if (title != null && title.equals("Greatest Hits")) {
            data.put("type", "top");
        } else if (title != null && title.equals("Just For You") && !userRepository.getToken().isEmpty()) {
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
                .subscribe(new DisposableObserver<List<Post>>() {
                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
//                        Log.v(TAG, e.toString());
                    }

                    @Override
                    public void onNext(List<Post> posts) {
                        setPostList(posts);
                        postRepository.setPosts(posts);
                    }
                });
    }
}
