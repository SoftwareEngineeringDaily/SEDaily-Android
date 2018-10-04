package com.koalatea.thehollidayinn.softwareengineeringdaily.data.models;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Created by samuelrey on 11/30/17.
 */

@Entity(tableName = "bookmark")
public class Bookmark {
    @PrimaryKey
    @NonNull
    private String postId;

    @ColumnInfo(name = "active")
    private Boolean active;

    public Bookmark(@NonNull Post post) {
        this.postId = post.get_id();
    }

    public Bookmark(String postId, Boolean active) {
        this.postId = postId;
        this.active = active;
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
