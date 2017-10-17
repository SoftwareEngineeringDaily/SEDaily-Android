package com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.loginscreen;

import android.support.annotation.NonNull;

import com.koalatea.thehollidayinn.softwareengineeringdaily.domain.UserRepository;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Kurian on 27-Sep-17.
 */
@Module
class LoginModule {

    private final LoginFragment fragment;

    LoginModule(@NonNull LoginFragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    @LoginScope
    LoginPresenter providesLoginPresenter(@NonNull UserRepository userRepository) {
        return new LoginPresenterImpl(userRepository);
    }
}
