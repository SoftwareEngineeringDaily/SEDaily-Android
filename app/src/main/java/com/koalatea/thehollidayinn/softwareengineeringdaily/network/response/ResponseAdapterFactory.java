package com.koalatea.thehollidayinn.softwareengineeringdaily.network.response;

import com.google.gson.TypeAdapterFactory;
import com.ryanharter.auto.value.gson.GsonTypeAdapterFactory;

/**
 * Created by Kurian on 29-Sep-17.
 */
@GsonTypeAdapterFactory
public abstract class ResponseAdapterFactory implements TypeAdapterFactory {

    public static ResponseAdapterFactory create() {
        return new AutoValueGson_ResponseAdapterFactory();
    }
}
