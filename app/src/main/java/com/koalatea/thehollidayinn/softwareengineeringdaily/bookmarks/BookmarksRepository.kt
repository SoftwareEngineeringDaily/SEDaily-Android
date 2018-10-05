package com.koalatea.thehollidayinn.softwareengineeringdaily.bookmarks

import com.koalatea.thehollidayinn.softwareengineeringdaily.app.SEDApp
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.AppDatabase
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Bookmark
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Post
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import java.util.ArrayList

class BookmarksRepository {
    companion object {
        fun loadBookmarks() {
            val service = SEDApp.component().kibblService()
            service.bookmarks
                .subscribeOn(Schedulers.io())
                .subscribe(object : DisposableObserver<List<Post>>() {
                    override fun onComplete() {}

                    override fun onError(e: Throwable) {}

                    override fun onNext(posts: List<Post>) {
                        val bookmarks = ArrayList<Bookmark>()

                        for (post in posts) {
                            bookmarks.add(Bookmark(post))
                        }

                        val db = AppDatabase.getDatabase()
                        val bookmarkDao = db.bookmarkDao()
                        bookmarkDao.insertAll(bookmarks)
                    }
                })
        }
    }
}