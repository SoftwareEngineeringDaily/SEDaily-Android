package com.koalatea.thehollidayinn.softwareengineeringdaily.test.dagger;

import com.koalatea.thehollidayinn.softwareengineeringdaily.app.AppComponent;
import com.koalatea.thehollidayinn.softwareengineeringdaily.app.AppScope;
import com.koalatea.thehollidayinn.softwareengineeringdaily.network.NetworkModule;

import dagger.Component;

/**
 * Created by Kurian on 25-Sep-17.
 */
@AppScope
@Component(modules = {UnitTestAppModule.class, UnitTestDataModule.class,
        UnitTestDomainModule.class, NetworkModule.class})
public interface UnitTestAppComponent extends AppComponent {
}
