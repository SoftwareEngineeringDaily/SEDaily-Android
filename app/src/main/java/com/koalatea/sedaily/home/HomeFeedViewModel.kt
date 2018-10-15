package com.koalatea.sedaily.home

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.koalatea.sedaily.R
import com.koalatea.sedaily.SingleLiveEvent
import com.koalatea.sedaily.models.Episode
import com.koalatea.sedaily.models.EpisodeDao
import com.koalatea.sedaily.network.NetworkHelper
import com.koalatea.sedaily.network.SEDailyApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class HomeFeedViewModel internal constructor(
    private val episodeDao: EpisodeDao
) : ViewModel() {
    var sedailyApi: SEDailyApi = NetworkHelper.getApi()
    val homeFeedListAdapter: HomeFeedListAdapter = HomeFeedListAdapter(this)

    val loadingVisibility: MutableLiveData<Int> = MutableLiveData()
    val errorMessage: MutableLiveData<Int> = MutableLiveData()
    val errorClickListener = View.OnClickListener { loadHomeFeed() }
    val playRequested = SingleLiveEvent<Episode>()

    private lateinit var subscription: Disposable

    init {
        loadHomeFeed()
    }

    override fun onCleared() {
        super.onCleared()
        subscription.dispose()
    }

    private fun loadHomeFeed() {
        val map = mutableMapOf<String, String>()

        subscription = Observable.fromCallable { episodeDao.all }
                .concatMap {
                    dbEpisodeList ->
                        if (dbEpisodeList.isEmpty()) {
                            sedailyApi.getPosts(map).concatMap {
                                apiPostList -> episodeDao.inserAll(*apiPostList.toTypedArray())
                                Observable.just(apiPostList)
                            }
                        } else {
                            Observable.just(dbEpisodeList)
                        }
                }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onRetrivePostListStart() }
                .doOnTerminate { onRetrievePostListFinish() }
                .subscribe(
                    { result -> onRetrievePostListSuccess(result) },
                    {
                        Log.v("keithtest", it.localizedMessage)
                        onRetrievePostListError()
                    }
                )
    }

    private fun onRetrivePostListStart() {
        loadingVisibility.value = View.VISIBLE
        errorMessage.value = null
    }

    private fun onRetrievePostListFinish() {
        loadingVisibility.value = View.GONE
    }

    private fun onRetrievePostListSuccess(feedList: List<Episode>) {
        homeFeedListAdapter.updateFeedList(feedList)
    }

    private fun onRetrievePostListError() {
        errorMessage.value = R.string.post_error
    }

    fun play(episode: Episode) {
        playRequested.value = episode
    }
}