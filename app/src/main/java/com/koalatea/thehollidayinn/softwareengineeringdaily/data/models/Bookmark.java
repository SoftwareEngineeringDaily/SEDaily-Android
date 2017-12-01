package com.koalatea.thehollidayinn.softwareengineeringdaily.data.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by samuelrey on 11/30/17.
 */

@Entity(tableName = "bookmark")
public class Bookmark {
    @PrimaryKey
    private String postId;

    @ColumnInfo(name = "active")
    private Boolean active;

    public Bookmark(Post post) {
        this.postId = post.getId();
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
