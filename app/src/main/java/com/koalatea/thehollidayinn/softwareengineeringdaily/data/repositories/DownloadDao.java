package com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories;

import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Download;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

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
