package com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.loginscreen;

import android.support.annotation.NonNull;

import com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.base.MVPContract;

/**
 * Created by Kurian on 27-Sep-17.
 */

public interface LoginPresenter extends MVPContract.Presenter<LoginView> {
    void submitLogin(@NonNull String username, @NonNull String password);
}
