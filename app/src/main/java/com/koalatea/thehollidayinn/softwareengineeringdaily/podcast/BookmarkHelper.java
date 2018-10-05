package com.koalatea.thehollidayinn.softwareengineeringdaily.podcast;

import com.koalatea.thehollidayinn.softwareengineeringdaily.data.AppDatabase;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Bookmark;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Post;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.APIInterface;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.BookmarkDao;
import com.koalatea.thehollidayinn.softwareengineeringdaily.util.ReactiveUtil;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BookmarkHelper {
    private static BookmarkHelper instance = null;


    public static BookmarkHelper getInstance() {
        if (instance == null) {
            instance = new BookmarkHelper();
        }

        return instance;
    }

    // @TODO: Move APIInterface to Dependency Injection
    public void addBookmark(Post post, APIInterface mService) {
        if (post == null) return;

        mService.addBookmark(post.get_id())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ReactiveUtil.getEmptyObservable());

        AppDatabase db = AppDatabase.getDatabase();

        Observable.just(db)
            .subscribeOn(Schedulers.io())
            .subscribe(bookmarkdb -> {
                BookmarkDao bookmarkDao = db.bookmarkDao();
                Bookmark bookmarkFound = bookmarkDao.loadById(post.get_id());

                if (bookmarkFound != null) return;

                Bookmark bookmark = new Bookmark(post);
                bookmarkDao.insertOne(bookmark);
            });
    }

    public void removeBookmark(Post post, APIInterface mService) {
        if (post == null) return;

        AppDatabase db = AppDatabase.getDatabase();

        Observable.just(db)
            .subscribeOn(Schedulers.io())
            .subscribe(bookmarkdb -> {
                BookmarkDao bookmarkDao = db.bookmarkDao();
                Bookmark bookmark = bookmarkDao.loadById(post.get_id());
                if (bookmark != null) bookmarkDao.delete(bookmark);
            });

        mService.removeBookmark(post.get_id())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ReactiveUtil.getEmptyObservable());
    }
}
