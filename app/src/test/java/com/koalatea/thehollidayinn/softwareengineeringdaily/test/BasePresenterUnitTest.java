package com.koalatea.thehollidayinn.softwareengineeringdaily.test;

import android.support.annotation.NonNull;

import com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.base.MVPContract;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * Created by Kurian on 27-Sep-17.
 */
public abstract class BasePresenterUnitTest<V extends MVPContract.View, P extends MVPContract.Presenter<V>>
        extends BaseUnitTest {

    protected P presenter;
    protected final V view;

    public BasePresenterUnitTest(Class<V> viewClass) {
        super();
        this.view = mock(viewClass);
    }

    @NonNull
    protected abstract P createPresenter();

    protected void initPresenter() {
        presenter = spy(createPresenter());
        presenter.bindView(view);
    }
}
