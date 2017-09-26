package com.koalatea.thehollidayinn.softwareengineeringdaily.data.preference;

import com.koalatea.thehollidayinn.softwareengineeringdaily.test.BaseUnitTest;

import org.junit.Test;
import org.mockito.Spy;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

/**
 * Created by Kurian on 25-Sep-17.
 */
public class AuthPreferenceImplTest extends BaseUnitTest {

    @Spy AuthPreferenceImpl prefs;

    @Test
    public void verify_getToken_gets_expected_value_from_preference() throws Exception {
        doReturn("test").when(appComponent().sharedPreferences()).getString(anyString(),
                anyString());
        assertEquals("test", prefs.getToken());
    }
}