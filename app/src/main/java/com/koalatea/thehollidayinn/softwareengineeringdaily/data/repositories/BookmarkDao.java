package com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories;

import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Bookmark;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;


/**
 * Created by samuelrey on 11/30/17.
 */

@Dao
public interface BookmarkDao {
    @Query("SELECT * FROM bookmark")
    List<Bookmark> getAll();

    @Query("SELECT * FROM bookmark WHERE postId == :postId")
    Bookmark loadById(String postId);

    @Insert
    void insertOne(Bookmark bookmark);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Bookmark> bookmarks);

    @Delete
    void delete(Bookmark bookmark);

    @Query("DELETE FROM bookmark")
    void deleteAll();
}
