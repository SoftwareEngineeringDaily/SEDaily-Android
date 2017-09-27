package com.koalatea.thehollidayinn.softwareengineeringdaily.data.preference;

import android.content.SharedPreferences;

import com.koalatea.thehollidayinn.softwareengineeringdaily.test.BaseUnitTest;

import org.junit.Test;
import org.mockito.Spy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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

    @Test
    public void verify_isLoggedIn_returns_true_when_token_exists() throws Exception {
        doReturn("test").when(appComponent().sharedPreferences()).getString(anyString(), anyString());
        assertTrue(prefs.isLoggedIn());
    }

    @Test
    public void verify_isLoggedIn_returns_false_when_token_is_empty() throws Exception {
        doReturn("").when(appComponent().sharedPreferences()).getString(anyString(), anyString());
        assertFalse(prefs.isLoggedIn());
    }

    @Test
    public void save_token_in_preference() throws Exception {
        SharedPreferences.Editor editor = mock(SharedPreferences.Editor.class);
        doReturn(editor).when(editor).putString(anyString(), anyString());
        doReturn(editor).when(appComponent().sharedPreferences()).edit();
        prefs.saveToken("test");
        verify(editor).putString(eq(AuthPreferenceImpl.AUTH_TOKEN), eq("test"));
    }
}