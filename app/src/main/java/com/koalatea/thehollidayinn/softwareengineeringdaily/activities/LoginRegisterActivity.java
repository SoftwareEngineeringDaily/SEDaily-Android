package com.koalatea.thehollidayinn.softwareengineeringdaily.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.koalatea.thehollidayinn.softwareengineeringdaily.MainActivity;
import com.koalatea.thehollidayinn.softwareengineeringdaily.R;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.User;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.APIInterface;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.ApiUtils;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.UserRepository;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Response;
import retrofit2.adapter.rxjava.HttpException;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LoginRegisterActivity extends AppCompatActivity {
    private Boolean register = false;
    private TextView title;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginRegButton;
    private Button toggleButton;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userRepository = UserRepository.getInstance(this);
        title = (TextView) findViewById(R.id.title);
        usernameEditText = (EditText) findViewById(R.id.username);
        passwordEditText = (EditText) findViewById(R.id.password);
        loginRegButton = (Button) findViewById(R.id.loginRegButton);
        toggleButton = (Button) findViewById(R.id.toggleButton);

        loginRegButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                loginReg(username, password);
            }
        });

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (register) {
                    register = false;
                    title.setText("Login");
                    toggleButton.setText("Register");
                    loginRegButton.setText("Login");
                } else {
                    register = true;
                    title.setText("Register");
                    toggleButton.setText("Login");
                    loginRegButton.setText("Register");
                }
            }
        });
    }

    private void displayMessage (String message) {
        AlertDialog.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }

        builder.setTitle("Error")
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void loginReg(String username, String password) {
        loginRegButton.setEnabled(false);

        APIInterface mService = ApiUtils.getKibbleService(this);
        rx.Observable query;

        if (register) {
            query = mService.register(username, password);
        } else {
            query = mService.login(username, password);
        }

        query
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<User>() {
                @Override
                public void onCompleted() {
                    loginRegButton.setEnabled(true);
                }

                @Override
                public void onError(Throwable e) {
                    HttpException exception = (HttpException) e;
                    Response response = exception.response();
                    try {
                        JSONObject jsonResponse = new JSONObject(response.errorBody().string());
                        displayMessage(jsonResponse.getString("message"));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }

                @Override
                public void onNext(User user) {
                    userRepository.setToken(user.token);
                    Intent intent = new Intent(LoginRegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });
    }
}
