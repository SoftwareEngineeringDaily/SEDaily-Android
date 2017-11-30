package com.koalatea.thehollidayinn.softwareengineeringdaily.podcast;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.koalatea.thehollidayinn.softwareengineeringdaily.PlaybackControllerActivity;
import com.koalatea.thehollidayinn.softwareengineeringdaily.R;
import com.koalatea.thehollidayinn.softwareengineeringdaily.app.SDEApp;
import com.koalatea.thehollidayinn.softwareengineeringdaily.audio.MusicProvider;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Content;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Post;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Title;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.APIInterface;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.ApiUtils;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.PodcastDownloadsRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.PostRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.UserRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.downloads.DownloadTask;
import com.koalatea.thehollidayinn.softwareengineeringdaily.downloads.MP3FileManager;
import java.io.File;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class PodcastDetailActivity extends PlaybackControllerActivity {
  private static String TAG = "PodcastDetail";
  private PostRepository postRepository;
  private UserRepository userRepository;
  private Subscriber mySubscriber;
  private boolean isBookmarked;

  @BindView(R.id.scoreTextView)
  TextView scoreText;

  @BindView(R.id.deleteButton)
  Button deleteButton;

  @BindView(R.id.playButton)
  Button playButton;

  @BindView(R.id.bookmark_button)
  ImageView bookmarkButton;

  private Post post;
  private APIInterface mService;
  private FirebaseAnalytics mFirebaseAnalytics;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_podcast_detail);
    ButterKnife.bind(this);

    setUp();

    mFirebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    toolbar.setTitle("");
    setSupportActionBar(toolbar);

    // @TODO: This doesn't seem to work
    getSupportActionBar().setDisplayShowTitleEnabled(false);

    mService = ApiUtils.getKibbleService(this);
    userRepository = UserRepository.getInstance(this);

    Intent intent = getIntent(); // gets the previously created intent
    String postId = intent.getStringExtra("POST_ID");

    postRepository = PostRepository.getInstance();
    loadPost(postId);

    // TODO: check if post is already liked
    isBookmarked = false;

    if(isBookmarked) {
      bookmarkButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_bookmark_set));
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    if (mySubscriber != null) {
      mySubscriber.unsubscribe();
    }

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
        public void onClick(DialogInterface dialog, int which) {}
      })
      .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {}
      })
      .setIcon(android.R.drawable.ic_dialog_alert)
      .show();
  }

  private void loadPost (final String postId) {
    post = postRepository.getPostById(postId);

    // @TODO: Why would this be null?
    if (post == null) {
      return;
    }

    Title postTile = post.getTitle();
    Date postDate = post.getDate();
    final Content postContent = post.getContent();

    TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
    titleTextView.setText(postTile.getRendered());

    String dayString = android.text.format.DateFormat.format("MMMM dd, yyyy", postDate.getTime()).toString();
    TextView secondaryTextView = (TextView) findViewById(R.id.secondaryTextView);
    secondaryTextView.setText(dayString);

    WebView descriptionWebView = findViewById(R.id.description);
    descriptionWebView.loadData(postContent.getRendered(), "text/html", "UTF-8");


    scoreText.setText(String.valueOf(post.getScore()));

    final ImageView upButton = (ImageView) findViewById(R.id.up_button);
    final ImageView downButton = (ImageView) findViewById(R.id.down_button);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      if (post.getUpvoted() != null && post.getUpvoted()) {
        upButton.getDrawable().setTint(ContextCompat.getColor(this, R.color.colorAccent));
      }
    }

    upButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (userRepository.getToken().isEmpty()) {
          displayMessage("You must login to vote");
          return;
        }

        Integer newScore = post.getScore();

        if (post.getUpvoted() != null && post.getUpvoted()) {
          newScore -= 1;
          post.setUpvoted(false);
          post.setDownvoted(false);
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
              upButton.getDrawable().setTint(ContextCompat.getColor(getApplicationContext(), R.color.button_grey));
              downButton.getDrawable().setTint(ContextCompat.getColor(getApplicationContext(), R.color.button_grey));
          }
        } else {
          newScore += 1;

          if (post.getDownvoted()) {
              newScore += 1;
          }

          post.setUpvoted(true);
          post.setDownvoted(false);
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
              upButton.getDrawable().setTint(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
              downButton.getDrawable().setTint(ContextCompat.getColor(getApplicationContext(), R.color.button_grey));
          }
        }

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, postId);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "UP");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "VOTE");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        scoreText.setText(String.valueOf(newScore));

        mService.upVote(post.get_id())
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(new Subscriber<Void>() {
            @Override
            public void onCompleted() {}
            @Override
            public void onError(Throwable e) {
                Log.v(TAG, e.toString());
            }
            @Override
            public void onNext(Void posts) {}
          });
      }
    });

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      if (post.getDownvoted() != null && post.getDownvoted()) {
        downButton.getDrawable().setTint(ContextCompat.getColor(this, R.color.colorAccent));
      }
    }

    downButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (userRepository.getToken().isEmpty()) {
            displayMessage("You must login to vote");
            return;
        }

        Integer newScore = post.getScore();

        if (post.getDownvoted() != null && post.getDownvoted()) {
          newScore += 1;
          post.setDownvoted(false);
          post.setUpvoted(false);
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
              downButton.getDrawable().setTint(ContextCompat.getColor(getApplicationContext(), R.color.button_grey));
              upButton.getDrawable().setTint(ContextCompat.getColor(getApplicationContext(), R.color.button_grey));
          }
        } else {
          newScore -= 1;

          if (post.getUpvoted()) {
            newScore -= 1;
          }

          post.setDownvoted(true);
          post.setUpvoted(false);
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
              downButton.getDrawable().setTint(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
              upButton.getDrawable().setTint(ContextCompat.getColor(getApplicationContext(), R.color.button_grey));
          }
        }

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, postId);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "DOWN");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "VOTE");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        scoreText.setText(String.valueOf(newScore));

        mService.downVote(post.get_id())
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(new Subscriber<Void>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.v(TAG, e.toString());
            }

            @Override
            public void onNext(Void posts) {

            }
          });
        }
    });

    playButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        playClick(post);
      }
    });
    deleteButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        confirmRemoveLocalDownload();
      }
    });

    if (!PodcastDownloadsRepository.getInstance().isPodcastDownloaded(post)) {
      setUpNotDownloadedState();
    }

    if (PodcastDownloadsRepository.getInstance().isDownloading(post.get_id())) {
      playButton.setText(R.string.downloading);
      deleteButton.setVisibility(View.INVISIBLE);
    }

    mySubscriber = new Subscriber<String>() {
      @Override
      public void onNext(String s) {
        Boolean downloaded = PodcastDownloadsRepository.getInstance().isPodcastDownloaded(post);
        if (downloaded) {
          setUpDownloadedState();
        } else {
          setUpNotDownloadedState();
        }
      }

      @Override
      public void onCompleted() { }

      @Override
      public void onError(Throwable e) { }
    };

    PodcastDownloadsRepository podcastDownloadsRepository = PodcastDownloadsRepository.getInstance();
    podcastDownloadsRepository
        .getDownloadChanges()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(mySubscriber);

  }

  private void confirmRemoveLocalDownload() {
    new AlertDialog.Builder(this)
      .setMessage(R.string.confirm_remove_download)
      .setIcon(android.R.drawable.ic_dialog_alert)
      .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
          PodcastDownloadsRepository.getInstance().removeFileForPost(post);
        }})
      .setNegativeButton(android.R.string.no, null).show();
  }

  public void setUpDownloadedState() {
    playButton.setText(R.string.label_play);
    deleteButton.setVisibility(View.VISIBLE);
  }

  public void setUpNotDownloadedState() {
    playButton.setText(R.string.download);
    deleteButton.setVisibility(View.INVISIBLE);
  }

  private void playClick (Post post) {
    if (post.getMp3() == null || post.getMp3().isEmpty()) {
      return;
    }

    // @TODO: Check download queue instead
    if (playButton.getText().equals(getString(R.string.downloading))) {
      PodcastDownloadsRepository.getInstance().cancelDownload(post);
      return;
    }

    if (!PodcastDownloadsRepository.getInstance().isPodcastDownloaded(post)) {
      playButton.setText(R.string.downloading);
      PodcastDownloadsRepository.getInstance().displayDownloadNotification(post);
      return;
    }

    File file = new MP3FileManager().getFileFromUrl(post.getMp3(), SDEApp.component().context());

    String source = post.getMp3();
    String id = String.valueOf(source.hashCode());

    MusicProvider mMusicProvider = MusicProvider.getInstance();
    MediaMetadataCompat item = mMusicProvider.getMusic(id);

    if (item == null) {
      item = new MediaMetadataCompat.Builder()
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, file.getAbsolutePath())
        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, post.getTitle().getRendered())
        .build();

      mMusicProvider.updateMusic(id, item);
    }

    MediaBrowserCompat.MediaItem bItem =
      new MediaBrowserCompat.MediaItem(item.getDescription(),
        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);

    boolean isSameMedia = id.equals(getPlayingMediaId());
    onMediaItemSelected(bItem, isSameMedia);
  }

  @OnClick(R.id.bookmark_button)
  public void onClickBookmarkButton() {
    if(userRepository.getToken().isEmpty()) {
      displayMessage("You must login to vote");
      return;
    }
    if(isBookmarked) {
      mService.removeBookmark(post.get_id())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<Void>() {
          @Override
          public void onCompleted() {
            bookmarkButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_bookmark));
          }

          @Override
          public void onError(Throwable e) {
            Log.v(TAG, e.toString());
          }

          @Override
          public void onNext(Void posts) {

          }
        });
    }
    else {
      mService.addBookmark(post.get_id())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<Void>() {
          @Override
          public void onCompleted() {
            bookmarkButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_bookmark_set));
          }

          @Override
          public void onError(Throwable e) {
            Log.v(TAG, e.toString());
          }

          @Override
          public void onNext(Void posts) {

          }
        });
    }
    isBookmarked = !isBookmarked;
  }
}
