package com.koalatea.thehollidayinn.softwareengineeringdaily.presentation;

import com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.base.MVPContract;
import com.koalatea.thehollidayinn.softwareengineeringdaily.test.BaseUnitTest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Created by Kurian on 27-Sep-17.
 */
public class PresenterCacheTest extends BaseUnitTest {

    private PresenterCache cache;

    @Override
    public void setUp() {
        super.setUp();
        cache = appComponent().presenterCache();
    }

    @Test
    public void caching_presenter_updates_size_value() throws Exception {
        MVPContract.Presenter mockPresenter = mock(MVPContract.Presenter.class);
        assertEquals(0, cache.size());
        cache.cachePresenter("test", mockPresenter);
        assertEquals(1, cache.size());
    }

    @Test
    public void getPresenter_returns_expected_presenter() throws Exception {
        MVPContract.Presenter mockPresenter = mock(MVPContract.Presenter.class);
        cache.cachePresenter("test", mockPresenter);
        assertEquals(mockPresenter, cache.getPresenter("test"));
    }

    @Test
    public void getPresenter_with_empty_cache_returns_null() throws Exception {
        assertNull(cache.getPresenter("test"));
    }

    @Test
    public void removePresenter_updates_size_value() throws Exception {
        MVPContract.Presenter mockPresenter = mock(MVPContract.Presenter.class);
        doReturn("test").when(mockPresenter).presenterTag();
        cache.cachePresenter("test", mockPresenter);
        cache.cachePresenter("test1", mock(MVPContract.Presenter.class));
        assertEquals(2, cache.size());
        cache.removePresenter("test");
        assertEquals(1, cache.size());
    }

    @Test
    public void clear_sets_cache_size_to_zero() throws Exception {
        cache.cachePresenter("test", mock(MVPContract.Presenter.class));
        cache.cachePresenter("test1", mock(MVPContract.Presenter.class));
        cache.cachePresenter("test2", mock(MVPContract.Presenter.class));
        cache.cachePresenter("test3", mock(MVPContract.Presenter.class));
        assertEquals(4, cache.size());
        cache.clear();
        assertEquals(0, cache.size());
    }
}