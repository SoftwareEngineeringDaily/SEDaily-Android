package com.koalatea.thehollidayinn.softwareengineeringdaily.presentation;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.base.MVPContract;

import java.util.Map;

import timber.log.Timber;

/**
 * Created by Kurian on 27-Sep-17.
 * <br/>
 * A cache to store/retrieve presenters
 * <br/>
 * Derived from <a href="https://github.com/Syex/mvp_with_dagger">Syex's MVP with Dagger Repo</a>
 */
public class PresenterCache {

    private final Map<String, MVPContract.Presenter> presenterMap;

    public PresenterCache(Map<String, MVPContract.Presenter> presenterMap) {
        Timber.tag(PresenterCache.class.getSimpleName());
        this.presenterMap = presenterMap;
    }

    /**
     * Get a presenter from the cache
     * @param tag identifier of entry
     * @param <P> presenter of type {@link com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.base.MVPContract.Presenter}
     * @return {@link com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.base.MVPContract.Presenter} item
     */
    @Nullable
    public <P extends MVPContract.Presenter> P getPresenter(@NonNull String tag) {
        Timber.d("getPresenter: %1$s", tag);
        return (P) presenterMap.get(tag);
    }

    /**
     * Add presenter to cache
     * @param tag identifier to associate with the presenter
     * @param presenter to cache
     */
    public void cachePresenter(@NonNull String tag, @NonNull MVPContract.Presenter presenter) {
        Timber.d("cachePresenter: %1$s", tag);
        presenterMap.put(tag, presenter);
    }

    /**
     * Remove presenter from cache
     * @param presenter to remove
     */
    public void removePresenter(@NonNull MVPContract.Presenter presenter) {
        MVPContract.Presenter p = presenterMap.remove(presenter.presenterTag());
        if(p != null) {
            Timber.d("removePresenter: %1$s", p.presenterTag());
        } else {
            Timber.w("removePresenter: no presenter was cached");
        }
    }

    /**
     * Get the size of the cache
     * @return number of presenters cached
     */
    public int size() {
        return presenterMap.size();
    }

    /**
     * Clear the cache
     */
    public void clear() {
        presenterMap.clear();
    }
}
