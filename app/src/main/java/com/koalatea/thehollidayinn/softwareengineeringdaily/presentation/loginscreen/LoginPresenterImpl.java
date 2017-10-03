package com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.loginscreen;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.koalatea.thehollidayinn.softwareengineeringdaily.R;
import com.koalatea.thehollidayinn.softwareengineeringdaily.app.SDEApp;
import com.koalatea.thehollidayinn.softwareengineeringdaily.domain.UserRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.base.BasePresenter;
import com.koalatea.thehollidayinn.softwareengineeringdaily.utils.LocalTextUtils;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by Kurian on 27-Sep-17.
 */

class LoginPresenterImpl extends BasePresenter<LoginView> implements LoginPresenter {

    private final UserRepository repository;
    private boolean isLoginMode = false;

    @Inject
    LoginPresenterImpl(@NonNull UserRepository repository) {
        Timber.tag(LoginPresenterImpl.class.getCanonicalName());
        this.repository = repository;
    }

    @Override
    public void onModeChanged(boolean isLogin) {
        this.isLoginMode = isLogin;
        if(isViewBound()) {
            if (isLoginMode) {
                getView().showLoginView();
            } else {
                getView().showRegistrationView();
            }
        }
    }

    @Override
    public void submitLogin(@NonNull String username, @NonNull String password) {
        getView().disableModeToggle();
        Timber.d("Login submitted");
        if(isLoginInputValid(username, password)) {
            subscriptions.add(getAuthenticationAction(username, password, isLoginMode)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally(new Action() {
                        @Override
                        public void run() throws Exception {
                            if(isViewBound()) {
                                getView().enableModeToggle();
                            }
                        }
                    })
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean result) throws Exception {
                            if (isViewBound()) {
                                authResult(result);
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Timber.e(throwable, throwable.getMessage());
                        }
                    }));
        } else {
            if (isViewBound()) {
                validateUsername(username);
                validatePassword(password);
            }
        }
    }


    @VisibleForTesting
    Single<Boolean> getAuthenticationAction(String username, String password,
                                            boolean isLoginMode) {
        if(isLoginMode) {
            return repository.login(username, password);
        } else {
            return repository.register(username, password);
        }
    }

    @Override
    public void submitRegistration(@NonNull String username, @NonNull String password,
                                   @NonNull String confirmPassword) {

    }

    @VisibleForTesting
    void validatePassword(String password) {
        if(SDEApp.component().textUtils().isEmpty(password)) {
            if(isViewBound()) {
                getView().showPasswordError(R.string.sign_in_screen_required_field_error);
            }
        }
    }

    @VisibleForTesting
    void validateUsername(String username) {
        if(SDEApp.component().textUtils().isEmpty(username)) {
            if(isViewBound()) {
                getView().showUsernameError(R.string.sign_in_screen_required_field_error);
            }
        }
    }

    @VisibleForTesting
    void authResult(boolean isSuccess) {
        if(isSuccess) {
            getView().loginSuccess();
        } else {
            getView().showErrorMessage(0);
        }
    }

    @VisibleForTesting
    boolean isLoginInputValid(String username, String password) {
        LocalTextUtils textUtils = SDEApp.component().textUtils();
        return !textUtils.isEmpty(username) && !textUtils.isEmpty(password);
    }

    @VisibleForTesting
    boolean doesConfirmationMatchPassword(String password, String confirmation) {
        return SDEApp.component().textUtils().equals(password, confirmation);    }

    @VisibleForTesting
    boolean isRegistrationInputValid(String username, String password,
                                     String confirmationPassword) {
        LocalTextUtils textUtils = SDEApp.component().textUtils();
        return !textUtils.isEmpty(username)
                && !textUtils.isEmpty(password)
                && !textUtils.isEmpty(confirmationPassword)
                && doesConfirmationMatchPassword(password, confirmationPassword);
    }

    @Override
    public void checkLoginStatus() {
        repository.isLoggedIn()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BiConsumer<Boolean, Throwable>() {
                    @Override
                    public void accept(Boolean isLoggedIn, Throwable throwable) throws Exception {
                        if(isLoggedIn && isViewBound()) {
                            getView().dismissView();
                        }
                    }
                });
    }

    @Override
    public String presenterTag() {
        return LoginPresenterImpl.class.getCanonicalName();
    }
}
