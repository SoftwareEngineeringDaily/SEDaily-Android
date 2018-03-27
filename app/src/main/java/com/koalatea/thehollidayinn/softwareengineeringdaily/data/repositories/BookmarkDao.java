package com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Bookmark;

import java.util.List;

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
