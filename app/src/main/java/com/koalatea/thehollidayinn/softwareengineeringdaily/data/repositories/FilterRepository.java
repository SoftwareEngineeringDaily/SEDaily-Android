package com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories;

import rx.Observable;
import rx.subjects.PublishSubject;

/*
 * Created by keithholliday on 9/16/17.
 */

public class FilterRepository {
    private final PublishSubject<String> changeObservable = PublishSubject.create();
    private static FilterRepository instance = null;

    private FilterRepository() {
    }

    public static FilterRepository getInstance() {
        if(instance == null) {
            instance = new FilterRepository();
        }
        return instance;
    }

    public Observable<String> getModelChanges() {
        return changeObservable;
    }

    public void setSearch(String search) {
        changeObservable.onNext(search);
    }
}
