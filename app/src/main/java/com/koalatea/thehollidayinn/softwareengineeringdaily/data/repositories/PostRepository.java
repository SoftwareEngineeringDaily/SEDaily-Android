package com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories;

import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Post;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;

/**
 * Created by krh12 on 6/21/2017.
 */

public class PostRepository {
    private static PostRepository instance = null;

    List<Post> posts;
    Map<String, Post> postsById;

    private PostRepository() {
    }

    public static PostRepository getInstance() {
        if(instance == null) {
            instance = new PostRepository();
        }
        return instance;
    }

    public void setPosts (List<Post> posts) {
        this.posts = posts;

        if (postsById == null) {
            postsById = new HashMap<String, Post>();
        }

        for (Post post : posts) {
            postsById.put(post._id, post);
        }
    }

    public Post getPostById (String postId) {
        if (this.posts == null || this.postsById == null) return null;

        return postsById.get(postId);
    }
}
