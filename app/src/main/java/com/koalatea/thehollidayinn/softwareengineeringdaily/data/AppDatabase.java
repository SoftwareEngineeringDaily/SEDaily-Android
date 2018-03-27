package com.koalatea.thehollidayinn.softwareengineeringdaily.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Bookmark;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.BookmarkDao;

/**
 * Created by samuelrey on 12/1/17.
 */

@Database(entities = {Bookmark.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract BookmarkDao bookmarkDao();
}
