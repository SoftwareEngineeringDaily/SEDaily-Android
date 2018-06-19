package com.koalatea.thehollidayinn.softwareengineeringdaily.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.koalatea.thehollidayinn.softwareengineeringdaily.app.SEDApp;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Bookmark;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Download;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.BookmarkDao;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.DownloadDao;

/**
 * Created by samuelrey on 12/1/17.
 */

@Database(entities = {Bookmark.class, Download.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract BookmarkDao bookmarkDao();
    public abstract DownloadDao downloadDao();

    private static AppDatabase INSTANCE;

    public static AppDatabase getDatabase() {
        Context context = SEDApp.component().context();

        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "sed-db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
