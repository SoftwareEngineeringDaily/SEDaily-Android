package com.koalatea.thehollidayinn.softwareengineeringdaily.domain;

import com.koalatea.thehollidayinn.softwareengineeringdaily.data.preference.AuthPreference;
import com.koalatea.thehollidayinn.softwareengineeringdaily.network.api.AuthNetworkService;
import com.koalatea.thehollidayinn.softwareengineeringdaily.network.response.AuthResponse;
import com.koalatea.thehollidayinn.softwareengineeringdaily.test.BaseUnitTest;
import io.reactivex.Single;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Created by Kurian on 26-Sep-17.
 */
public class UserRepositoryImplTest extends BaseUnitTest {

    @Mock
    AuthNetworkService api;
    @Mock
    AuthPreference preference;

    @InjectMocks
    UserRepositoryImpl repo;

    /*
        LOGIN
     */
    @Test
    public void login_returns_true_when_loggedIn_status_success() throws Exception {

        doReturn(Single.just(AuthResponse.create("token")))
                .when(api).login(anyString(), anyString());
        doReturn(true).when(preference).isLoggedIn();
        repo.login("test", "pass").test().assertNoErrors().assertValue(true);
    }

    @Test
    public void login_returns_true_when_loggedIn_status_false() throws Exception {

        doReturn(Single.just(AuthResponse.create("token")))
                .when(api).login(anyString(), anyString());
        doReturn(false).when(preference).isLoggedIn();
        repo.login("test", "pass").test().assertNoErrors().assertValue(false);
    }

    @Test
    public void login_saves_token_to_preferences() throws Exception {

        doReturn(Single.just(AuthResponse.create("token")))
                .when(api).login(anyString(), anyString());
        repo.login("test", "pass").test().assertNoErrors();
        verify(preference).saveToken(eq("token"));
    }

    @Test
    public void login_invokes_analytics_login_event_when_login_true() throws Exception {
        doReturn(Single.just(AuthResponse.create("token")))
            .when(api).login(anyString(), anyString());
        doReturn(true).when(preference).isLoggedIn();
        repo.login("test", "pass").test().assertNoErrors();
        verify(appComponent().analyticsFacade()).trackLogin(eq("test"));
    }

    @Test
    public void login_invokes_analytics_login_event_when_login_false() throws Exception {
        doReturn(Single.just(AuthResponse.create("token")))
            .when(api).login(anyString(), anyString());
        repo.login("test", "pass").test().assertNoErrors();
        verify(appComponent().analyticsFacade(), never()).trackLogin(eq("test"));
    }

    /*
        REGISTRATION
     */
    @Test
    public void registration_returns_true_when_loggedIn_status_success() throws Exception {

        doReturn(Single.just(AuthResponse.create("token")))
                .when(api).register(anyString(), anyString());
        doReturn(true).when(preference).isLoggedIn();
        repo.register("test", "pass").test().assertNoErrors().assertValue(true);
    }

    @Test
    public void registration_returns_true_when_loggedIn_status_false() throws Exception {

        doReturn(Single.just(AuthResponse.create("token")))
                .when(api).register(anyString(), anyString());
        doReturn(false).when(preference).isLoggedIn();
        repo.register("test", "pass").test().assertNoErrors().assertValue(false);
    }

    @Test
    public void register_saves_token_to_preferences() throws Exception {

        doReturn(Single.just(AuthResponse.create("token")))
                .when(api).register(anyString(), anyString());
        repo.register("test", "pass").test().assertNoErrors();
        verify(preference).saveToken(eq("token"));
    }

    @Test
    public void register_invokes_analytics_registration_event_when_login_true() throws Exception {
        doReturn(Single.just(AuthResponse.create("token")))
            .when(api).register(anyString(), anyString());
        doReturn(true).when(preference).isLoggedIn();
        repo.register("test", "pass").test().assertNoErrors();
        verify(appComponent().analyticsFacade()).trackRegistration(eq("test"));
    }

    @Test
    public void register_invokes_analytics_registration_event_when_login_false() throws Exception {
        doReturn(Single.just(AuthResponse.create("token")))
            .when(api).register(anyString(), anyString());
        repo.register("test", "pass").test().assertNoErrors();
        verify(appComponent().analyticsFacade(), never()).trackRegistration(eq("test"));
    }

    //TODO test error-handling from responses

    /*
        SIGN OUT
     */

    @Test
    public void signOut_clears_token() throws Exception {
        repo.signOut().test().assertComplete();
        verify(preference).clearToken();
    }

    @Test
    public void isLoggedIn_returns_false_when_auth_source_is_false() throws Exception {
        doReturn(false).when(preference).isLoggedIn();
        repo.isLoggedIn().test().assertNoErrors().assertComplete().assertValue(false);
    }

    @Test
    public void isLoggedIn_returns_true_when_auth_source_is_true() throws Exception {
        doReturn(true).when(preference).isLoggedIn();
        repo.isLoggedIn().test().assertNoErrors().assertComplete().assertValue(true);
    }
}