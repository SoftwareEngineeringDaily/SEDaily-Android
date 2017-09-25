package com.koalatea.thehollidayinn.softwareengineeringdaily.network.response;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Kurian on 25-Sep-17.
 */
@AutoValue
public abstract class AuthResponse {
    @SerializedName("token")
    public abstract String token();

    public static TypeAdapter<AuthResponse> typeAdapter(Gson gson) {
        return new AutoValue_AuthResponse.GsonTypeAdapter(gson);
    }
}
