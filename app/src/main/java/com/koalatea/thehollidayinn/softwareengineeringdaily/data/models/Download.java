package com.koalatea.thehollidayinn.softwareengineeringdaily.data.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "downloads_table")
public class Download {

  @PrimaryKey
  @NonNull
  @ColumnInfo(name = "postId")
  private String postId;

  public Download(String postId) {this.postId = postId;}

  public String getPostId(){return this.postId;}
}
