package com.koalatea.thehollidayinn.softwareengineeringdaily.test.mock;

import android.support.annotation.Nullable;

import com.koalatea.thehollidayinn.softwareengineeringdaily.utils.LocalTextUtils;

/**
 * Created by Kurian on 26-Sep-17.
 */
public class TestTextUtils extends LocalTextUtils {

    /**
     * Returns true if the string is null or 0-length.
     *
     * Copy from TextUtils, since unit tests don't have access to that code
     *
     * @param str the string to be examined
     * @return true if str is null or zero length
     */
    @Override
    public boolean isEmpty(@Nullable CharSequence str) {
        if (str == null || str.toString().trim().length() == 0)
            return true;
        else
            return false;
    }

    /**
     * Returns true if a and b are equal, including if they are both null.
     * <p><i>Note: In platform versions 1.1 and earlier, this method only worked well if
     * both the arguments were instances of String.</i></p>
     *
     * Copy from TextUtils, since unit tests don't have access to that code
     *
     * @param a first CharSequence to check
     * @param b second CharSequence to check
     * @return true if a and b are equal
     */
    public boolean equals(CharSequence a, CharSequence b) {
        if (a == b) return true;
        int length;
        if (a != null && b != null && (length = a.length()) == b.length()) {
            if (a instanceof String && b instanceof String) {
                return a.equals(b);
            } else {
                for (int i = 0; i < length; i++) {
                    if (a.charAt(i) != b.charAt(i)) return false;
                }
                return true;
            }
        }
        return false;
    }
}
