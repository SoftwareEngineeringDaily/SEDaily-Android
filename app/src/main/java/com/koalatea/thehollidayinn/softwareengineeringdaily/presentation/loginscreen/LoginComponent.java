package com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.loginscreen;

import com.koalatea.thehollidayinn.softwareengineeringdaily.app.AppComponent;

import dagger.Component;

/**
 * Created by Kurian on 27-Sep-17.
 */
@LoginScope
@Component(modules = LoginModule.class, dependencies = AppComponent.class)
interface LoginComponent {
    LoginPresenter loginPresenter();
}
