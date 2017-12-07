package com.koalatea.thehollidayinn.softwareengineeringdaily.auth;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.koalatea.thehollidayinn.softwareengineeringdaily.MainActivity;
import com.koalatea.thehollidayinn.softwareengineeringdaily.R;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.User;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.APIInterface;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.ApiUtils;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.UserRepository;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


public class LoginRegisterActivity extends AppCompatActivity {
    private Boolean register = false;

    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.username)
    EditText usernameEditText;

    @BindView(R.id.password)
    EditText passwordEditText;

    @BindView(R.id.loginRegButton)
    Button loginRegButton;

    @BindView(R.id.toggleButton)
    Button toggleButton;

    private UserRepository userRepository;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_login_register);
      Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);
      getSupportActionBar().setDisplayShowTitleEnabled(false);

      ButterKnife.bind(this);

      mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
      userRepository = UserRepository.getInstance(this);

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
          title.setText(getString(R.string.login));
          toggleButton.setText(getString(R.string.register));
          loginRegButton.setText(getString(R.string.login));
        } else {
          register = true;
          title.setText(getString(R.string.register));
          toggleButton.setText(getString(R.string.login));
          loginRegButton.setText(getString(R.string.register));
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

        loginRegButton.setEnabled(true);

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

        String type = getType();
        logLoginRegAnalytics(username, type);

        getQuery(username, password)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new DisposableObserver<User>() {
                @Override
                public void onComplete() {
                    loginRegButton.setEnabled(true);
                }

                @Override
                public void onError(Throwable e) {
//                    HttpException exception = (HttpException) e;
//                    Response response = exception.response();
//                    try {
//                        JSONObject jsonResponse = new JSONObject(response.errorBody().string());
//                        displayMessage(jsonResponse.getString("message"));
//                    } catch (IOException | JSONException e1) {
//                        e1.printStackTrace();
//                    }
                    displayMessage("Incorrect username or password");
                }

                @Override
                public void onNext(User user) {
                    userRepository.setToken(user.getToken());
                    Intent intent = new Intent(LoginRegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });
    }

    private void logLoginRegAnalytics(String username, String type) {
      Bundle bundle = new Bundle();
      bundle.putString(FirebaseAnalytics.Param.ITEM_ID, username);
      bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
      mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    private String getType () {
        if (register) {
            return getString(R.string.register);
        }

        return getString(R.string.login);
    }

    private Observable<User> getQuery (String username, String password) {
      APIInterface mService = ApiUtils.getKibbleService(this);
      if (register) {
        return mService.register(username, password);
      }

      return mService.login(username, password);
    }
}
