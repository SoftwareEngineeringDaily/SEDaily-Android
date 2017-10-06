package com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.loginscreen;

import android.support.annotation.NonNull;

import com.koalatea.thehollidayinn.softwareengineeringdaily.R;
import com.koalatea.thehollidayinn.softwareengineeringdaily.domain.UserRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.test.BasePresenterUnitTest;

import org.junit.Test;
import org.mockito.Mock;

import io.reactivex.Single;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
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
        presenter.authResult(true);
        verify(view).loginSuccess();
    }

    @Test
    public void loginResult_shows_error_message_when_result_false() throws Exception {
        initPresenter();
        presenter.authResult(false);
        verify(view).showErrorMessage(eq(R.string.sign_in_screen_auth_failed));
    }

    @Test
    public void isInputValid_returns_false_when_username_is_empty() throws Exception {
        initPresenter();
        assertFalse(presenter.isLoginInputValid(null, "password"));
    }

    @Test
    public void isInputValid_returns_false_when_password_is_empty() throws Exception {
        initPresenter();
        assertFalse(presenter.isLoginInputValid("username", null));
    }

    @Test
    public void isInputValid_returns_true_when_password_and_username_is_valid() throws Exception {
        initPresenter();
        assertTrue(presenter.isLoginInputValid("username", "password"));
    }

    @Test
    public void validateUsername_shows_error_to_view_when_invalid() throws Exception {
        initPresenter();
        presenter.validateUsername(null);
        verify(view).showUsernameError(eq(R.string.sign_in_screen_required_field_error));
    }

    @Test
    public void validateUsername_does_not_show_error_when_valid_username() throws Exception {
        initPresenter();
        presenter.validateUsername("username");
        verify(view, never()).showUsernameError(eq(R.string.sign_in_screen_required_field_error));
    }

    @Test
    public void validatePassword_shows_error_to_view_when_invalid() throws Exception {
        initPresenter();
        presenter.validatePassword(null);
        verify(view).showPasswordError(eq(R.string.sign_in_screen_required_field_error));
    }

    @Test
    public void validatePassword_does_not_show_error_when_valid_password() throws Exception {
        initPresenter();
        presenter.validatePassword("password");
        verify(view, never()).showPasswordError(eq(R.string.sign_in_screen_required_field_error));
    }

    @Test
    public void validateConfirmation_shows_error_to_view_when_invalid() throws Exception {
        initPresenter();
        presenter.validateConfirmation(null);
        verify(view).showConfirmationPasswordError(
                eq(R.string.sign_in_screen_required_field_error));
    }

    @Test
    public void validateConfirmation_does_not_show_error_when_valid_password() throws Exception {
        initPresenter();
        presenter.validateConfirmation("password");
        verify(view, never()).showConfirmationPasswordError(
                eq(R.string.sign_in_screen_required_field_error));
    }

    @Test
    public void doesConfirmationMatchPassword_returns_true_when_passwords_match() throws Exception {
        initPresenter();
        assertTrue(presenter.doesConfirmationMatchPassword("password", "password"));
    }

    @Test
    public void doesConfirmationMatchPassword_returns_false_when_passwords_match()
            throws Exception {
        initPresenter();
        assertFalse(presenter.doesConfirmationMatchPassword("password", "confirmation"));
    }

    @Test
    public void doesConfirmationMatchPassword_returns_true_when_passwords_empty() throws Exception {
        initPresenter();
        assertTrue(presenter.doesConfirmationMatchPassword("", ""));
    }

    @Test
    public void isRegistrationInputValid_returns_true_when_inputs_are_valid() throws Exception {
        initPresenter();
        final boolean result = presenter.isRegistrationInputValid("user", "password", "password");
        assertTrue(result);
    }

    @Test
    public void isRegistrationInputValid_returns_false_when_username_is_invalid() throws Exception {
        initPresenter();
        final boolean result = presenter.isRegistrationInputValid("", "password", "password");
        assertFalse(result);
    }

    @Test
    public void isRegistrationInputValid_returns_false_when_password_and_confirmation_mismatch()
            throws Exception {
        initPresenter();
        final boolean result = presenter.isRegistrationInputValid("user", "password",
                "confirmation");
        assertFalse(result);
    }

    @Test
    public void isRegistrationInputValid_returns_false_when_password_and_confirmation_is_empty()
            throws Exception {
        initPresenter();
        final boolean result = presenter.isRegistrationInputValid("user", "", "");
        assertFalse(result);
    }

    @Test
    public void isRegistrationInputValid_returns_false_when_password_is_empty()
            throws Exception {
        initPresenter();
        final boolean result = presenter.isRegistrationInputValid("user", "", "password");
        assertFalse(result);
    }

    @Test
    public void isRegistrationInputValid_returns_false_when_confirmation_is_empty()
            throws Exception {
        initPresenter();
        final boolean result = presenter.isRegistrationInputValid("user", "password", "");
        assertFalse(result);
    }

    @Test
    public void isPasswordValid_returns_false_with_empty_input() throws Exception {
        initPresenter();
        assertFalse(presenter.isPasswordValid(""));
    }

    @Test
    public void isPasswordValid_returns_true_with_valid_input() throws Exception {
        initPresenter();
        assertTrue(presenter.isPasswordValid("password"));
    }

    @Test
    public void isUsernameValid_returns_false_with_valid_input() throws Exception {
        initPresenter();
        assertFalse(presenter.isUsernameValid(""));
    }

    @Test
    public void isUsernameValid_returns_true_with_valid_input() throws Exception {
        initPresenter();
        assertTrue(presenter.isUsernameValid("username"));
    }

    @Test
    public void enableSignInModeToggle_enables_sign_in_mode_when_true() throws Exception {
        initPresenter();
        presenter.enableSignInModeToggle(true);
        verify(view).enableModeToggle();
    }

    @Test
    public void enableSignInModeToggle_disables_sign_in_mode_when_false() throws Exception {
        initPresenter();
        presenter.enableSignInModeToggle(false);
        verify(view).disableModeToggle();
    }

    @Test
    public void onModeChanged_shows_login_view_when_true() throws Exception {
        initPresenter();
        presenter.onModeChanged(true);
        verify(view).showLoginView();
    }

    @Test
    public void onModeChanged_shows_registration_view_when_true() throws Exception {
        initPresenter();
        presenter.onModeChanged(false);
        verify(view).showRegistrationView();
    }

    @Test
    public void submitLogin_disables_loginToggle() throws Exception {
        initPresenter();
        presenter.submitLogin("", "");
        verify(view).disableModeToggle();
    }

    @Test
    public void submitLogin_enable_loginToggle() throws Exception {
        initPresenter();
        presenter.submitLogin("", "");
        verify(view).enableModeToggle();
    }

    @Test
    public void submitLogin_sends_authResult_when_result_true() throws Exception {
        initPresenter();
        doReturn(Single.just(true)).when(userRepository).login(anyString(), anyString());
        presenter.submitLogin("user", "password");
        verify(view).loginSuccess();
    }

    @Test
    public void submitLogin_sends_authResult_when_result_false() throws Exception {
        initPresenter();
        doReturn(Single.just(false)).when(userRepository).login(anyString(), anyString());
        presenter.submitLogin("user", "password");
        verify(view).showErrorMessage(eq(R.string.sign_in_screen_auth_failed));
    }

    @Test
    public void submitLogin_showPasswordError_when_username_empty() throws Exception {
        initPresenter();
        presenter.submitLogin("", "password");
        verify(view).showUsernameError(eq(R.string.sign_in_screen_required_field_error));
    }

    @Test
    public void submitLogin_showUsernameError_when_password_empty() throws Exception {
        initPresenter();
        presenter.submitLogin("user", "");
        verify(view).showPasswordError(eq(R.string.sign_in_screen_required_field_error));
    }

    @Test
    public void submitRegistration_sends_authResult_when_result_true() throws Exception {
        initPresenter();
        doReturn(Single.just(true)).when(userRepository).register(anyString(), anyString());
        presenter.submitRegistration("user", "password", "password");
        verify(view).loginSuccess();
    }

    @Test
    public void submitRegistration_sends_authResult_when_result_false() throws Exception {
        initPresenter();
        doReturn(Single.just(false)).when(userRepository).register(anyString(), anyString());
        presenter.submitRegistration("user", "password", "password");
        verify(view).showErrorMessage(eq(R.string.sign_in_screen_auth_failed));
    }

    @Test
    public void submitRegistration_showUsernameError_when_password_empty() throws Exception {
        initPresenter();
        presenter.submitRegistration("", "password", "password");
        verify(view).showUsernameError(eq(R.string.sign_in_screen_required_field_error));
    }

    @Test
    public void submitRegistration_showPasswordError_when_password_empty() throws Exception {
        initPresenter();
        presenter.submitRegistration("username", "", "");
        verify(view).showPasswordError(eq(R.string.sign_in_screen_required_field_error));
    }

    @Test
    public void submitRegistration_showConfirmationPasswordError_when_confirmation_empty()
            throws Exception {
        initPresenter();
        presenter.submitRegistration("username", "", "");
        verify(view).showConfirmationPasswordError(
                eq(R.string.sign_in_screen_required_field_error));
    }

    @Test
    public void submitRegistration_showPasswordMismatchError_when_password_empty()
            throws Exception {
        initPresenter();
        presenter.submitRegistration("user", "", "password");
        verify(view).showPasswordMismatchError(eq(R.string.sign_in_screen_confirmation_error));
    }

    @Test
    public void submitRegistration_showPasswordMismatchError_when_confirmation_empty()
            throws Exception {
        initPresenter();
        presenter.submitRegistration("user", "password", "");
        verify(view).showPasswordMismatchError(eq(R.string.sign_in_screen_confirmation_error));
    }

    @NonNull
    @Override
    protected LoginPresenterImpl createPresenter() {
        return new LoginPresenterImpl(userRepository);
    }
}