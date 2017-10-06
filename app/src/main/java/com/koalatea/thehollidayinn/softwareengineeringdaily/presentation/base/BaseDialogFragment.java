package com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.base;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koalatea.thehollidayinn.softwareengineeringdaily.app.AppComponent;
import com.koalatea.thehollidayinn.softwareengineeringdaily.app.SDEApp;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

/**
 * Created by Kurian on 27-Sep-17.
 */

public abstract class BaseDialogFragment<V extends MVPContract.View,
        P extends MVPContract.Presenter>
        extends DialogFragment
        implements MVPContract.View {

    private Unbinder unbinder;
    private PresenterViewModel<V, P> viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("Calling class: %1$s", this.getClass().getSimpleName());
        this.viewModel = ViewModelProviders.of(this).get(PresenterViewModel.class);
        this.viewModel.setPresenter(createPresenter());
    }

    protected View inflateView(LayoutInflater inflater, ViewGroup container, int layoutResId) {
        View view = inflater.inflate(layoutResId, container, false);
        bindView(view);
        return view;
    }

    private void bindView(View view) {
        Timber.d("Binding View");
        if (unbinder != null) {
            unbinder.unbind();
        }
        unbinder = ButterKnife.bind(this, view);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.d("onViewCreated");

        V lol = (V) this;
        Timber.d("View of type: %1$s", lol.getClass().getSimpleName());

        viewModel.bindView(getMvpView());
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.d("onStart");
        getViewModel().bindView(getMvpView());
    }

    @Override
    public void onStop() {
        Timber.d("onStop");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Timber.d("onDestroyView");
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        getViewModel().unbindView();
        super.onDestroyView();
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
