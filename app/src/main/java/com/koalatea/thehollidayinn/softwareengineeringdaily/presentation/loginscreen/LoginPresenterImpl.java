package com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.loginscreen;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.koalatea.thehollidayinn.softwareengineeringdaily.R;
import com.koalatea.thehollidayinn.softwareengineeringdaily.app.SDEApp;
import com.koalatea.thehollidayinn.softwareengineeringdaily.domain.UserRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.network.response.ErrorResponse;
import com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.base.BasePresenter;
import com.koalatea.thehollidayinn.softwareengineeringdaily.utils.LocalTextUtils;

import java.io.IOException;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import timber.log.Timber;

/**
 * Created by Kurian on 27-Sep-17.
 */

class LoginPresenterImpl extends BasePresenter<LoginView> implements LoginPresenter {

    private final UserRepository repository;

    @Inject
    LoginPresenterImpl(@NonNull UserRepository repository) {
        Timber.tag(LoginPresenterImpl.class.getCanonicalName());
        this.repository = repository;
    }

    @Override
    public void onModeChanged(boolean isLogin) {
        if (isViewBound()) {
            if (isLogin) {
                getView().showLoginView();
            } else {
                getView().showRegistrationView();
            }
        }
    }

    @Override
    public void submitLogin(@NonNull String username, @NonNull String password) {
        enableSignInModeToggle(false);
        if (isLoginInputValid(username, password)) {
            login(username, password);
        } else {
            enableSignInModeToggle(true);
            validateUsername(username);
            validatePassword(password);
        }
    }

    @Override
    public void submitRegistration(@NonNull String username, @NonNull String password,
            @NonNull String confirmPassword) {
        getView().disableModeToggle();
        if (!doesConfirmationMatchPassword(password, confirmPassword)) {
            enableSignInModeToggle(true);
            if (isViewBound()) {
                getView().showPasswordMismatchError(R.string.sign_in_screen_confirmation_error);
            }
        } else if (!isRegistrationInputValid(username, password, confirmPassword)) {
            enableSignInModeToggle(true);
            validateUsername(username);
            validatePassword(password);
            validateConfirmation(confirmPassword);
        } else {
            registerUser(username, password);
        }
    }

    @VisibleForTesting
    void registerUser(@NonNull String username, @NonNull String password) {
        repository.register(username, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(new Action() {
                    @Override
                    public void run() throws Exception {
                        enableSignInModeToggle(true);
                    }
                })
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean success) throws Exception {
                        authResult(success);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Timber.e(throwable, throwable.getMessage());
                    }
                });
    }

    @VisibleForTesting
    void login(@NonNull String username, @NonNull String password) {
        subscriptions.add(repository.login(username, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean success) throws Exception {
                        authResult(success);
                    }
                }, authErrorConsumer));
    }

    @VisibleForTesting
    void validatePassword(String password) {
        if (!isPasswordValid(password)) {
            if (isViewBound()) {
                getView().showPasswordError(R.string.sign_in_screen_required_field_error);
            }
        }
    }

    @VisibleForTesting
    void validateConfirmation(String password) {
        if (!isPasswordValid(password)) {
            if (isViewBound()) {
                getView().showConfirmationPasswordError(
                        R.string.sign_in_screen_required_field_error);
            }
        }
    }

    @VisibleForTesting
    void validateUsername(String username) {
        if (SDEApp.component().textUtils().isEmpty(username)) {
            if (isViewBound()) {
                getView().showUsernameError(R.string.sign_in_screen_required_field_error);
            }
        }
    }

    @VisibleForTesting
    void authResult(boolean isSuccess) {
        if(isViewBound()) {
            if (isSuccess) {
                getView().loginSuccess();
            } else {
                getView().showErrorMessage(R.string.sign_in_screen_auth_failed);
            }
        }
    }

    @VisibleForTesting
    boolean isLoginInputValid(String username, String password) {
        LocalTextUtils textUtils = SDEApp.component().textUtils();
        return !textUtils.isEmpty(username) && !textUtils.isEmpty(password);
    }

    @VisibleForTesting
    boolean isPasswordValid(String password) {
        return !SDEApp.component().textUtils().isEmpty(password);
    }

    @VisibleForTesting
    boolean isUsernameValid(String username) {
        return !SDEApp.component().textUtils().isEmpty(username);
    }

    @VisibleForTesting
    boolean doesConfirmationMatchPassword(String password, String confirmation) {
        return SDEApp.component().textUtils().equals(password, confirmation);
    }

    @VisibleForTesting
    boolean isRegistrationInputValid(String username, String password,
            String confirmationPassword) {
        LocalTextUtils textUtils = SDEApp.component().textUtils();
        return !textUtils.isEmpty(username)
                && !textUtils.isEmpty(password)
                && !textUtils.isEmpty(confirmationPassword)
                && doesConfirmationMatchPassword(password, confirmationPassword);
    }

    @VisibleForTesting
    void enableSignInModeToggle(boolean enable) {
        if (isViewBound()) {
            if (enable) {
                getView().enableModeToggle();
            } else {
                getView().disableModeToggle();
            }
        }
    }

    @Override
    public void checkLoginStatus() {
        repository.isLoggedIn()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BiConsumer<Boolean, Throwable>() {
                    @Override
                    public void accept(Boolean isLoggedIn, Throwable throwable) throws Exception {
                        if (isLoggedIn && isViewBound()) {
                            getView().dismissView();
                        }
                    }
                });
    }

    @VisibleForTesting
    void showNetworkError(String error) {
        if(isViewBound()) {
            getView().showErrorMessage(error);
        }
    }

    @VisibleForTesting
    Consumer<Throwable> authErrorConsumer = new Consumer<Throwable>() {
        @Override
        public void accept(Throwable throwable) throws Exception {
            Timber.e(throwable, throwable.getMessage());
            if (throwable instanceof HttpException) {
                HttpException e = (HttpException) throwable;
                //TODO standardize deserialize error response and extract message
                ErrorResponse error = SDEApp.component().gson().fromJson(
                        e.response().errorBody().charStream(),
                        ErrorResponse.class);
                showNetworkError(error.message());
            } else if (throwable instanceof IOException) {
                if (isViewBound()) {
                    getView().showErrorMessage(R.string.sign_in_screen_unable_to_connect_error);
                }
            }
        }
    };

    @Override
    public String presenterTag() {
        return LoginPresenterImpl.class.getCanonicalName();
    }
}
