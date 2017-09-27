package com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.loginscreen;

import android.support.annotation.NonNull;

import com.koalatea.thehollidayinn.softwareengineeringdaily.domain.UserRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.test.BasePresenterUnitTest;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;

/**
 * Created by Kurian on 27-Sep-17.
 */
public class LoginPresenterImplTest extends BasePresenterUnitTest<LoginView, LoginPresenterImpl> {

    @Mock
    UserRepository userRepository;

    public LoginPresenterImplTest() {
        super(LoginView.class);
    }

    @Test
    public void presenter_tag_returns_expected() throws Exception {
        initPresenter();
        assertEquals(LoginPresenterImpl.class.getCanonicalName(), presenter.presenterTag());
    }

    @Test
    public void loginResult_invokes_view_success_when_result_true() throws Exception {
        initPresenter();
        presenter.loginResult(true);
        verify(view).loginSuccess();
    }

    @Ignore
    @Test
    public void loginResult_shows_error_message_when_result_false() throws Exception {
        initPresenter();
        presenter.loginResult(false);
        verify(view).showErrorMessage(anyInt());
    }

    @NonNull
    @Override
    protected LoginPresenterImpl createPresenter() {
        return new LoginPresenterImpl(userRepository);
    }
}