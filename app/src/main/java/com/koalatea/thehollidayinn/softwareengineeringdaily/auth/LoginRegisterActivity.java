package com.koalatea.thehollidayinn.softwareengineeringdaily.auth;

import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import com.koalatea.thehollidayinn.softwareengineeringdaily.app.SEDApp;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.User;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.APIInterface;
import com.koalatea.thehollidayinn.softwareengineeringdaily.repositories.UserRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.AppDatabase;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Bookmark;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Post;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.BookmarkDao;
import com.koalatea.thehollidayinn.softwareengineeringdaily.util.AlertUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import retrofit2.Response;

public class LoginRegisterActivity extends AppCompatActivity {
    private Boolean register = false;

    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.username)
    EditText usernameEditText;

    @BindView(R.id.email)
    EditText emailEditText;

    @BindView(R.id.password)
    EditText passwordEditText;

    @BindView(R.id.loginRegButton)
    Button loginRegButton;

    @BindView(R.id.toggleButton)
    Button toggleButton;

    @BindView(R.id.forgotPassword)
    Button forgotPassword;

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
              String email = emailEditText.getText().toString();

              loginReg(username, email, password);
          }
      });

      toggleButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            if (register) {
                setUpLoginView();
                return;
            }
            setUpRegisterView();
          }
      });

      setUpLoginView();
    }

    private void setUpLoginView () {
        register = false;
        title.setText(getString(R.string.login));
        toggleButton.setText(getString(R.string.register));
        loginRegButton.setText(getString(R.string.login));
        usernameEditText.setHint(R.string.usernameOrEmail);
        emailEditText.setVisibility(View.INVISIBLE);
    }

    private void setUpRegisterView () {
        register = true;
        title.setText(getString(R.string.register));
        toggleButton.setText(getString(R.string.login));
        loginRegButton.setText(getString(R.string.register));
        usernameEditText.setHint(R.string.username);
        emailEditText.setVisibility(View.VISIBLE);
    }

    private void displayMessage (String message) {
      loginRegButton.setEnabled(true);

      AlertUtil.displayMessage(this, message);
    }

    private void loginReg(String username, String email, String password) {
        loginRegButton.setEnabled(false);

        String type = getType();
        logLoginRegAnalytics(username, type);

        // Check if user is using email for login
        if (email.isEmpty() && username.contains("@")) {
            email = username;
        }

        getQuery(username, email, password)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new DisposableObserver<User>() {
                @Override
                public void onComplete() {
                    loginRegButton.setEnabled(true);
                }

                @Override
                public void onError(Throwable error) {
                    try {
                        // We had non-200 http error
                        if (error instanceof HttpException) {
                            HttpException httpException = (HttpException) error;
                            Response response = httpException.response();
                            displayMessage(response.errorBody().string());
                        } else {
                            displayMessage(error.getMessage());
                        }
                    } catch (Exception e) {
                        displayMessage(e.getMessage());
                    }
                }

                @Override
                public void onNext(User user) {
                    userRepository.setToken(user.getToken());
                    loadBookmarks();
                    Intent intent = new Intent(LoginRegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });
    }

    private void loadBookmarks() {
      APIInterface service = SEDApp.component().kibblService();
      service.getBookmarks()
        .subscribeOn(Schedulers.io())
        .subscribe(new DisposableObserver<List<Post>>() {
          @Override
          public void onComplete() {
          }

          @Override
          public void onError(Throwable e) {
          }

          @Override
          public void onNext(List<Post> posts) {
            ArrayList<Bookmark> bookmarks = new ArrayList<>();

            for(Post post: posts) {
              bookmarks.add(new Bookmark(post));
            }

            AppDatabase db = AppDatabase.getDatabase();
            BookmarkDao bookmarkDao = db.bookmarkDao();
            bookmarkDao.insertAll(bookmarks);
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

    private Observable<User> getQuery (String username, String email, String password) {
      APIInterface mService = SEDApp.component.kibblService();
      if (register) {
        return mService.register(username, email, password);
      }

      return mService.login(username, email, password);
    }

    @OnClick(R.id.forgotPassword)
    public void forgotPasswordClick() {
      Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.softwaredaily.com/forgot-password"));
      startActivity(browserIntent);
    }
}
