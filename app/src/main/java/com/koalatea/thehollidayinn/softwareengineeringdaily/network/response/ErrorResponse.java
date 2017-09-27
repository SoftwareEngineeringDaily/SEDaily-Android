package com.koalatea.thehollidayinn.softwareengineeringdaily.network.response;

import android.support.annotation.VisibleForTesting;

import com.google.auto.value.AutoValue;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Kurian on 26-Sep-17.
 */
@AutoValue
public abstract class ErrorResponse {
    @SerializedName("message")
    public abstract String message();

    @VisibleForTesting
    public static ErrorResponse create(String message) {
        return new AutoValue_ErrorResponse(message);
    }
}
