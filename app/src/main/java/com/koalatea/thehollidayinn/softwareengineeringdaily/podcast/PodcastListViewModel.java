package com.koalatea.thehollidayinn.softwareengineeringdaily.podcast;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Post;

import java.util.List;

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
}
