package com.koalatea.thehollidayinn.softwareengineeringdaily.network.response;

import com.google.gson.TypeAdapterFactory;
import com.ryanharter.auto.value.gson.GsonTypeAdapterFactory;

/**
 * Type adapter for handling automatic deserialisation to class
 * Created by Kurian on 25-Sep-17.
 */
@GsonTypeAdapterFactory
public abstract class ResponseTypeAdaptorFactory implements TypeAdapterFactory {

    // Static factory method to access the package
    // private generated implementation
    public static TypeAdapterFactory create() {
        return new AutoValueGson_ResponseTypeAdaptorFactory();
    }
}
