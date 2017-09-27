package com.koalatea.thehollidayinn.softwareengineeringdaily.data.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Kurian on 26-Sep-17.
 */
abstract class BaseDataMapper<S, T> implements LocalDataMapper<S, T> {

    @Override
    public abstract T map(S source);

    @Override
    public List<T> mapAll(Collection<S> sources) {
        if(sources == null || sources.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<T> result = new ArrayList<>(sources.size());
            for(S item : sources) {
                result.add(map(item));
            }
            return result;
        }
    }
}
