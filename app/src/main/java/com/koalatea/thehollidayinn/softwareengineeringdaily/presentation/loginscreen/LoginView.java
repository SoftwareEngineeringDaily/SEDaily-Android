package com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.loginscreen;

import android.support.annotation.StringRes;

import com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.base.MVPContract;

/**
 * Created by Kurian on 27-Sep-17.
 */

public interface LoginView extends MVPContract.View {
    void showErrorMessage(@StringRes int errorString);

    void loginSuccess();

    void showProgressView();

    void hideProgressView();

    void showUsernameError(@StringRes int errorString);

    void showPasswordError(@StringRes int errorString);

    void showConfirmationPasswordError(@StringRes int errorString);

    void dismissView();

    void showRegistrationView();

    void showLoginView();

    void disableModeToggle();

    void enableModeToggle();

    void showPasswordMismatchError(@StringRes int errorString);
}
