package com.koalatea.thehollidayinn.softwareengineeringdaily.data.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "downloads_table")
public class Download {

  @PrimaryKey
  @NonNull
  @ColumnInfo(name = "postId")
  private String postId;

  public Download(String postId) {this.postId = postId;}

  public String getPostId(){return this.postId;}
}
