package com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.base;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.koalatea.thehollidayinn.softwareengineeringdaily.app.AppComponent;
import com.koalatea.thehollidayinn.softwareengineeringdaily.app.SDEApp;

import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by Kurian on 27-Sep-17.
 */

public abstract class BaseMvpActivity<V extends MVPContract.View,
        P extends MVPContract.Presenter>
        extends AppCompatActivity
        implements MVPContract.View {

    private PresenterViewModel<V, P> viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("Calling class: %1$s", this.getClass().getSimpleName());
        this.viewModel = ViewModelProviders.of(this).get(PresenterViewModel.class);
        this.viewModel.setPresenter(createPresenter());
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.d("onStart");
        viewModel.bindView(getMvpView());
    }

    @Override
    public void onStop() {
        Timber.d("onStop");
        viewModel.unbindView();
        super.onStop();
    }

    protected AppComponent getAppComponent() {
        return SDEApp.component();
    }

    protected PresenterViewModel<V, P> getViewModel() {
        return this.viewModel;
    }

    /**
     * Perform your dependency injection
     */
    protected abstract P createPresenter();

    protected P getPresenter() {
        return viewModel.getPresenter();
    }

    protected abstract V getMvpView();
}
