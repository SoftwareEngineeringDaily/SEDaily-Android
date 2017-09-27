package com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.base;

import android.support.annotation.VisibleForTesting;

import java.lang.ref.WeakReference;

/**
 * Created by Kurian on 27-Sep-17.
 */
public abstract class BasePresenter<V extends MVPContract.View>
        implements MVPContract.Presenter<V> {

    @VisibleForTesting
    WeakReference<V> viewRef;

    @Override
    public void bindView(V view) {
        viewRef = new WeakReference<>(view);
    }

    @Override
    public void unbind() {
        viewRef = null;
    }

    @Override
    public V getView() {
        return viewRef.get();
    }

    /**
     * Check that there's a view bound to the presenter
     * @return true if a view is bound to the presenter
     */
    protected boolean isViewBound() {
        return (viewRef != null) && (viewRef.get() != null);
    }
}
