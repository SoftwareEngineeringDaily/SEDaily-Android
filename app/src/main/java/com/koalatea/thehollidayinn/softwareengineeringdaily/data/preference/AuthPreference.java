package com.koalatea.thehollidayinn.softwareengineeringdaily.data.preference;

import android.support.annotation.NonNull;

/**
 * Created by Kurian on 25-Sep-17.
 */
public interface AuthPreference {

    String TOKEN_DEFAULT = "";

    void saveToken(@NonNull String token);
    String getToken();
}
