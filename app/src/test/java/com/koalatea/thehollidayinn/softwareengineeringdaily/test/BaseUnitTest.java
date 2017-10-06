package com.koalatea.thehollidayinn.softwareengineeringdaily.test;

import com.koalatea.thehollidayinn.softwareengineeringdaily.app.SDEApp;
import com.koalatea.thehollidayinn.softwareengineeringdaily.test.dagger.UnitTestAppComponent;
import com.koalatea.thehollidayinn.softwareengineeringdaily.test.rule
        .ImmediateRxSchedulersOverrideRule;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import io.reactivex.Scheduler;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.internal.schedulers.ExecutorScheduler;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * Base test class to initiate any globally required components
 * Created by Kurian on 25-Sep-17.
 */
public abstract class BaseUnitTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public final ImmediateRxSchedulersOverrideRule overrideSchedulersRule =
            new ImmediateRxSchedulersOverrideRule();

    @Before
    public void setUp() {
        createAppComponent();
    }

    @After
    public void tearDown() {
    }

    private void createAppComponent() {
        //the compiler may complain but the tests run
        SDEApp.component = com.koalatea.thehollidayinn.softwareengineeringdaily.test.dagger
                .DaggerUnitTestAppComponent.create();
    }

    protected UnitTestAppComponent appComponent() {
        return (UnitTestAppComponent) SDEApp.component();
    }
}
