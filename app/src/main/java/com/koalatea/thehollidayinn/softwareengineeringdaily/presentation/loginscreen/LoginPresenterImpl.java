package com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.loginscreen;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.koalatea.thehollidayinn.softwareengineeringdaily.domain.UserRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.base.BasePresenter;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiConsumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Kurian on 27-Sep-17.
 */

class LoginPresenterImpl extends BasePresenter<LoginView> implements LoginPresenter {

    private final UserRepository repository;

    public LoginPresenterImpl(@NonNull UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public void submitLogin(@NonNull String username, @NonNull String password) {
        subscriptions.add(repository.login(username, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BiConsumer<Boolean, Throwable>() {
                    @Override
                    public void accept(Boolean result, Throwable throwable) throws Exception {
                        if(isViewBound()) {
                            loginResult(result);
                        }
                    }
                }));
    }

    @VisibleForTesting
    void loginResult(boolean isSuccess) {
        if(isSuccess) {
            getView().loginSuccess();
        } else {
            getView().showErrorMessage(0);
        }
    }

    @Override
    public String presenterTag() {
        return LoginPresenterImpl.class.getCanonicalName();
    }
}
