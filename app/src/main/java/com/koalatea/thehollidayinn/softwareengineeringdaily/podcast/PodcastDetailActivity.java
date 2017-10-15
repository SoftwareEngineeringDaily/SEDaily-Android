package com.koalatea.thehollidayinn.softwareengineeringdaily.podcast;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.koalatea.thehollidayinn.softwareengineeringdaily.PlaybackControllerActivity;
import com.koalatea.thehollidayinn.softwareengineeringdaily.R;
import com.koalatea.thehollidayinn.softwareengineeringdaily.audio.MusicProvider;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Content;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Post;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Title;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.APIInterface;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.ApiUtils;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.PostRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.UserRepository;

import com.koalatea.thehollidayinn.softwareengineeringdaily.downloads.DownloadTask;
import com.koalatea.thehollidayinn.softwareengineeringdaily.downloads.MP3FileManager;
import java.io.File;
import java.util.Date;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PodcastDetailActivity extends PlaybackControllerActivity {
  private static String TAG = "PodcastDetail";
  private PostRepository postRepository;
  private UserRepository userRepository;
  private TextView scoreText;
  private Post post;
  private APIInterface mService;
  private FirebaseAnalytics mFirebaseAnalytics;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_podcast_detail);

    setUp();

    mFirebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    mService = ApiUtils.getKibbleService(this);
    userRepository = UserRepository.getInstance(this);

    Intent intent = getIntent(); // gets the previously created intent
    String postId = intent.getStringExtra("POST_ID");

    postRepository = PostRepository.getInstance();
    loadPost(postId);
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

    private void loadPost (final String postId) {
        post = postRepository.getPostById(postId);

        // @TODO: Why would this be null?
        if (post == null) {
            return;
        }

        Title postTile = post.getTitle();
        Date postDate = post.getDate();
        Content postContent = post.getContent();

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(postTile.getRendered());

        TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
        titleTextView.setText(postTile.getRendered());

        String dayString = android.text.format.DateFormat.format("MMMM dd, yyyy", postDate.getTime()).toString();
        TextView secondaryTextView = (TextView) findViewById(R.id.secondaryTextView);
        secondaryTextView.setText(dayString);

        TextView descriptionTextView = (TextView) findViewById(R.id.description);
        if (Build.VERSION.SDK_INT > 24) {
            descriptionTextView.setText(Html.fromHtml(postContent.getRendered(), Html.FROM_HTML_MODE_COMPACT));
        } else {
          //noinspection deprecation
          descriptionTextView.setText(Html.fromHtml(postContent.getRendered()));
        }


        scoreText = (TextView) findViewById(R.id.scoreTextView);
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


      Button playButton = (Button) findViewById(R.id.playButton);
      playButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          playClick(post);
        }
      });
    }

    private void playClick (Post post) {
      if (post.getMp3() == null || post.getMp3().isEmpty()) {
        return;
      }

      // Download if not downloaded
      File file = new MP3FileManager().getFileFromUrl(post.getMp3(), this.getApplicationContext());
      if (!file.exists()) {
        // declare the dialog as a member field of your activity
        ProgressDialog mProgressDialog;

        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("A message");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

        // execute this when the downloader must be fired
        final DownloadTask downloadTask = new DownloadTask(this, mProgressDialog);
        downloadTask.execute(post.getMp3());

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
          @Override public void onCancel(DialogInterface dialog) {
            downloadTask.cancel(true);
          }
        });
      }

      String source = post.getMp3();
      String id = String.valueOf(source.hashCode());

      MusicProvider mMusicProvider = MusicProvider.getInstance();
      MediaMetadataCompat item = mMusicProvider.getMusic(id);

      if (item == null) {
        item = new MediaMetadataCompat.Builder()
          .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
          .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, source)
          .putString(MediaMetadataCompat.METADATA_KEY_TITLE, post.getTitle().getRendered())
          .build();

        mMusicProvider.updateMusic(id, item);
      }

      MediaBrowserCompat.MediaItem bItem =
        new MediaBrowserCompat.MediaItem(item.getDescription(),
          MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);

      boolean isPlaying = id.equals(getPlayingMediaId());
      onMediaItemSelected(bItem, isPlaying);
    }
}
