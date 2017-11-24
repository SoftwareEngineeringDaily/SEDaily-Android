package com.koalatea.thehollidayinn.softwareengineeringdaily.test.rule;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

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
 * Created by Kurian on 04-Oct-17.
 * <br/>
 * This rule registers SchedulerHooks for RxJava and RxAndroid to ensure that subscriptions
 * always subscribeOn and observeOn Schedulers.immediate().
 * Warning, this rule will resetProcedureStatus RxAndroidPlugins and RxJavaPlugins after each test
 * so
 * if the application code uses RxJava plugins this may affect the behaviour of the testing method.
 * <p>
 *
 * See https://medium.com/@fabioCollini/testing-asynchronous-rxjava-code-using-mockito
 * -8ad831a16877#.ahj5h7jmg
 * See https://github.com/fabioCollini/TestingRxJavaUsingMockito/blob/master/app/src/test/java/it
 * /codingjam/testingrxjava/TestSchedulerRule.java
 */
public class ImmediateRxSchedulersOverrideRule implements TestRule {

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                RxAndroidPlugins.setInitMainThreadSchedulerHandler(
                        new Function<Callable<Scheduler>, Scheduler>() {
                            @Override
                            public Scheduler apply(@NonNull Callable<Scheduler> schedulerCallable)
                                    throws Exception {
                                return immediate;
                            }
                        });

                RxJavaPlugins.setInitComputationSchedulerHandler(
                        new Function<Callable<Scheduler>, Scheduler>() {
                            @Override
                            public Scheduler apply(@NonNull Callable<Scheduler> schedulerCallable)
                                    throws Exception {
                                return immediate;
                            }
                        });

                RxJavaPlugins.setInitIoSchedulerHandler(
                        new Function<Callable<Scheduler>, Scheduler>() {
                            @Override
                            public Scheduler apply(@NonNull Callable<Scheduler> schedulerCallable)
                                    throws Exception {
                                return immediate;
                            }
                        });

                RxJavaPlugins.setInitNewThreadSchedulerHandler(
                        new Function<Callable<Scheduler>, Scheduler>() {
                            @Override
                            public Scheduler apply(@NonNull Callable<Scheduler> schedulerCallable)
                                    throws Exception {
                                return immediate;
                            }
                        });

                RxAndroidPlugins.setInitMainThreadSchedulerHandler(
                        new Function<Callable<Scheduler>, Scheduler>() {
                            @Override
                            public Scheduler apply(@NonNull Callable<Scheduler> schedulerCallable)
                                    throws Exception {
                                return immediate;
                            }
                        });

        /*
        If we add
        RxJavaPlugins.setInitSingleSchedulerHandler(scheduler -> immediate);
        any test depending on debounce/timer/interval will fail with StackOverflowException
         */
                try {
                    base.evaluate();
                } finally {
                    RxJavaPlugins.reset();
                    RxAndroidPlugins.reset();
                }
            }
        };
    }

    /*
    private Scheduler immediate = new Scheduler() {
        @Override
        public Disposable scheduleDirect(@NonNull Runnable run, long delay, @NonNull TimeUnit unit) {
            // this prevents StackOverflowErrors when scheduling with a delay
            return super.scheduleDirect(run, 0, unit);
        }

        @Override
        public Worker createWorker() {
            return new ExecutorScheduler.ExecutorWorker(new Executor() {
                @Override
                public void execute(@android.support.annotation.NonNull Runnable runnable) {
                    runnable.run();
                }
            });
        }
    };
    */



    private final Scheduler immediate = new Scheduler() {
        @Override
        public Worker createWorker() {
            return new ExecutorScheduler.ExecutorWorker(new Executor() {
                @Override
                public void execute(@android.support.annotation.NonNull Runnable runnable) {
                    runnable.run();
                }
            });
        }
    };

}
