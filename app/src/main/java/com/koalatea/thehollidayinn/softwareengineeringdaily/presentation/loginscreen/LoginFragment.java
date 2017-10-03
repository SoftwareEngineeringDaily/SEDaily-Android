package com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.loginscreen;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.koalatea.thehollidayinn.softwareengineeringdaily.R;
import com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.base.BaseDialogFragment;

import javax.inject.Inject;

import butterknife.BindView;

/**
 * Created by Kurian on 27-Sep-17.
 */

public class LoginFragment extends BaseDialogFragment<LoginView, LoginPresenter>
        implements LoginView {

    public static final String TAG = LoginFragment.class.getCanonicalName();

    public static void show(FragmentManager fm) {
        LoginFragment fragment = new LoginFragment();
        fragment.show(fm, TAG);
    }

    @BindView(R.id.sign_in_username_input)
    TextInputLayout usernameInput;
    @BindView(R.id.sign_in_password_input)
    TextInputLayout passwordInput;
    @BindView(R.id.sign_in_password_confirm_input)
    TextInputLayout confirmPasswordInput;
    @BindView(R.id.sign_in_username)
    EditText usernameField;
    @BindView(R.id.sign_in_password)
    EditText passwordField;
    @BindView(R.id.sign_in_password_confirm)
    EditText confirmPasswordField;
    @BindView(R.id.sign_in_mode_toggle)
    CheckBox modeToggle;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View view
                = inflateView(LayoutInflater.from(getContext()), null, R.layout.login_screen);

        initMode();

        final Dialog alert = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.sign_in_screen_register_title)
                .setView(view)
                .setPositiveButton(R.string.sign_in_screen_submit, null)
                .setNegativeButton(R.string.sign_in_screen_cancel, null)
                .create();
        return alert;
    }

    private void initMode() {
        if(modeToggle.isChecked()) {
            confirmPasswordInput.setVisibility(View.GONE);
            //TODO set IME action labels depending on mode
        } else {
            confirmPasswordInput.setVisibility(View.VISIBLE);
        }
        modeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                getPresenter().onModeChanged(b);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        //Work-around to prevent auto-dismissal of the dialog when the positive button has been clicked
        Button save = ((android.support.v7.app.AlertDialog)getDialog())
                .getButton(DialogInterface.BUTTON_POSITIVE);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitFieldValues();
            }
        });
    }

    @Override
    public void dismissView() {
        dismiss();
    }

    void submitFieldValues() {
        passwordInput.setErrorEnabled(false);
        usernameInput.setErrorEnabled(false);
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();
        getPresenter().submitLogin(username, password);
    }

    @Override
    public void showErrorMessage(@StringRes int errorString) {
        passwordField.setText("");
    }

    @Override
    public void loginSuccess() {
        dismiss();
    }

    @Override
    public void showRegistrationView() {
        confirmPasswordInput.setVisibility(View.VISIBLE);
        confirmPasswordField.setVisibility(View.VISIBLE);
        getDialog().setTitle(R.string.sign_in_screen_register_title);

    }

    @Override
    public void showLoginView() {
        confirmPasswordInput.setVisibility(View.GONE);
        confirmPasswordField.setVisibility(View.GONE);
        getDialog().setTitle(R.string.sign_in_screen_sign_in_title);
    }

    @Override
    public void showProgressView() {
    }

    @Override
    public void hideProgressView() {
    }

    @Override
    public void disableModeToggle() {
        modeToggle.setEnabled(false);
    }

    @Override
    public void enableModeToggle() {
        modeToggle.setEnabled(true);
    }

    @Override
    public void showUsernameError(@StringRes int errorString) {
        usernameInput.setErrorEnabled(true);
        usernameInput.setError(getString(errorString));
    }

    @Override
    public void showPasswordError(@StringRes int errorString) {
        passwordInput.setErrorEnabled(true);
        passwordInput.setError(getString(errorString));
    }

    @Override
    protected LoginPresenter createPresenter() {
        return DaggerLoginComponent.builder()
                .loginModule(new LoginModule(this))
                .appComponent(getAppComponent())
                .build()
                .loginPresenter();
    }

    @Override
    protected LoginView getMvpView() {
        return this;
    }
}
