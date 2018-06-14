package com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Download;

import java.util.List;

@Dao
public interface DownloadDao {
  @Query("SELECT * FROM downloads_table")
  List<Download> getAll();

  @Query("SELECT * FROM bookmark WHERE postId == :postId")
  Download loadById(String postId);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertOne(Download download);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertAll(List<Download> downloads);

  @Delete
  void delete(Download download);

  @Query("DELETE FROM downloads_table")
  void deleteAll();
}
