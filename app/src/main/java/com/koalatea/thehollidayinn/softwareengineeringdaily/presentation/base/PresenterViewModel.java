package com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.base;

import android.arch.lifecycle.ViewModel;

import timber.log.Timber;

/**
 * Created by Kurian on 01-Oct-17.
 * {@link ViewModel} container to preserve {@link com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.base.MVPContract.Presenter
 * android lifecycle and during configuration changes
 */
public class PresenterViewModel<V extends MVPContract.View,
        P extends MVPContract.Presenter> extends ViewModel {

    private P presenter;

    public void setPresenter(P presenter) {
        if(this.presenter == null) {
            Timber.d("Setting presenter %1$s", presenter.getClass().getSimpleName());
            this.presenter = presenter;
        }
    }

    public void bindView(V view) {
        Timber.d("Binding view %1$s", view.getClass().getSimpleName());
        presenter.bindView(view);
    }

    public void unbindView() {
        Timber.d("Unbinding view");
        presenter.unbind();
    }

    public P getPresenter() {
        return this.presenter;
    }

    @Override
    protected void onCleared() {
        Timber.d("Clearing ViewModel");
        super.onCleared();
        this.presenter.destroy();
        this.presenter = null;
    }
}
