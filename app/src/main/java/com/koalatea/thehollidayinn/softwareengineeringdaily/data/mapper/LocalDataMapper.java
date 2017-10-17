package com.koalatea.thehollidayinn.softwareengineeringdaily.data.mapper;

import java.util.Collection;
import java.util.List;

/**
 * Created by Kurian on 26-Sep-17.
 */

public interface LocalDataMapper<S, T> {
    T map(S source);
    List<T> mapAll(Collection<S> sources);
}
