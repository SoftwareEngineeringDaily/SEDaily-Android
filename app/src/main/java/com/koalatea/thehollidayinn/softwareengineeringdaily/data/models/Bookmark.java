package com.koalatea.thehollidayinn.softwareengineeringdaily.data.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by samuelrey on 11/30/17.
 */

@Entity
public class Bookmark {
    @PrimaryKey
    private String postId;

    public Bookmark(Post post) {
        this.postId = post.getId();
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
}
