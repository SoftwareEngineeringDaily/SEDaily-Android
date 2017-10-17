package com.koalatea.thehollidayinn.softwareengineeringdaily.utils;

import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Created by Kurian on 26-Sep-17.
 * Duplicated methods to avoid android dependencies
 */
public class LocalTextUtils {

    /**
     * Returns true if the string is null or 0-length.
     * @param str the string to be examined
     * @return true if str is null or zero length
     */
    public boolean isEmpty(@Nullable CharSequence str) {
        return TextUtils.isEmpty(str);
    }

    /**
     * Returns true if two strings are equal
     * @param a
     * @param b
     * @return true if both arguments are equal
     */
    public boolean equals(CharSequence a, CharSequence b) {
        return TextUtils.equals(a, b);
    }
}
