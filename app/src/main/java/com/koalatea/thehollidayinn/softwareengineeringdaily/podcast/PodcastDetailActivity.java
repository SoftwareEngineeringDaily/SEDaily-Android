package com.koalatea.thehollidayinn.softwareengineeringdaily.podcast;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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

import com.koalatea.thehollidayinn.softwareengineeringdaily.PlaybackControllerActivity;
import com.koalatea.thehollidayinn.softwareengineeringdaily.R;
import com.koalatea.thehollidayinn.softwareengineeringdaily.app.SEDApp;
import com.koalatea.thehollidayinn.softwareengineeringdaily.audio.MusicProvider;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Post;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.APIInterface;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.ApiUtils;
import com.koalatea.thehollidayinn.softwareengineeringdaily.repositories.PodcastDownloadsRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.repositories.PostRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.repositories.UserRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.downloads.MP3FileManager;
import java.io.File;

import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class PodcastDetailActivity extends PlaybackControllerActivity {
  private static String TAG = "PodcastDetail";
  private PostRepository postRepository;
  private UserRepository userRepository;
  private DisposableObserver myDisposableObserver;

  @BindView(R.id.scoreTextView)
  TextView scoreText;

  @BindView(R.id.deleteButton)
  Button deleteButton;

  @BindView(R.id.playButton)
  Button playButton;

  @BindView(R.id.toolbar)
  Toolbar toolbar;

  @BindView(R.id.up_button)
  ImageView upButton;

  @BindView(R.id.down_button)
  ImageView downButton;

  @BindView(R.id.titleTextView)
  TextView titleTextView;
  @BindView(R.id.secondaryTextView)
  TextView secondaryTextView;
  @BindView(R.id.description)
  WebView descriptionWebView;

  private Post post;
  private APIInterface mService;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_podcast_detail);
    ButterKnife.bind(this);

    setUp();

    if (toolbar != null) {
      toolbar.setTitle("");
      setSupportActionBar(toolbar);
      getSupportActionBar().setDisplayShowTitleEnabled(false); // @TODO: This doesn't seem to work
    }


    mService = SEDApp.component().kibblService();
    userRepository = UserRepository.getInstance(this);

    Intent intent = getIntent(); // gets the previously created intent
    String postId = intent.getStringExtra("POST_ID");

    postRepository = PostRepository.getInstance();
    loadPost(postId);

    myDisposableObserver = new DisposableObserver<String>() {
      @Override
      public void onNext(String state) {
        handleDownloadStateChange(state);
      }

      @Override
      public void onComplete() { }

      @Override
      public void onError(Throwable e) { }
    };
    PodcastDownloadsRepository podcastDownloadsRepository = PodcastDownloadsRepository.getInstance();
    podcastDownloadsRepository
            .getDownloadChanges()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(myDisposableObserver);
  }

  @Override
  public void onStop() {
    super.onStop();
    if (myDisposableObserver != null) {
      myDisposableObserver.dispose();
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
    if (post == null) {
      return;
    }

    titleTextView.setText(post.getTitle().getRendered());
    descriptionWebView.loadData(post.getContent().getRendered(), "text/html", "UTF-8");

    String dayString = android.text.format.DateFormat.format("MMMM dd, yyyy", post.getDate().getTime()).toString();
    secondaryTextView.setText(dayString);

    scoreText.setText(String.valueOf(post.getScore()));
    setVoteButtonStates();

    if (!PodcastDownloadsRepository.getInstance().isPodcastDownloaded(post)) {
      setUpNotDownloadedState();
    }

    if (PodcastDownloadsRepository.getInstance().isDownloading(post.get_id())) {
      playButton.setText(R.string.downloading);
      deleteButton.setVisibility(View.INVISIBLE);
    }
  }

  private void setVoteButtonStates () {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      if (post.getUpvoted() != null && post.getUpvoted()) {
        upButton.getDrawable().setTint(ContextCompat.getColor(this, R.color.colorAccent));
      }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      if (post.getDownvoted() != null && post.getDownvoted()) {
        downButton.getDrawable().setTint(ContextCompat.getColor(this, R.color.colorAccent));
      }
    }
  }

  @OnClick(R.id.up_button)
  public void upvotePost () {
    if (post == null) return;

    if (userRepository.getToken().isEmpty()) {
      displayMessage("You must login to vote");
      return;
    }

    Integer newScore = post.getScore();
    if (post.getUpvoted() != null && post.getUpvoted()) {
      newScore -= 1;
      post.setUpvoted(false);
      post.setDownvoted(false);
    } else {
      newScore += 1;
      if (post.getDownvoted() != null && post.getDownvoted()) {
        newScore += 1;
      }
      post.setUpvoted(true);
      post.setDownvoted(false);
    }

    SEDApp.component().analyticsFacade().trackUpVote(post.getId());
    setVoteButtonStates();
    scoreText.setText(String.valueOf(newScore));

    mService.upVote(post.get_id())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new DisposableObserver<Void>() {
              @Override
              public void onComplete() {}
              @Override
              public void onError(Throwable e) {
                Log.v(TAG, e.toString());
              }
              @Override
              public void onNext(Void posts) {}
            });
  }

  @OnClick(R.id.down_button)
  public void downVotePost () {
    if (post == null) return;

    if (userRepository.getToken().isEmpty()) {
      displayMessage("You must login to vote");
      return;
    }

    Integer newScore = post.getScore();
    if (post.getDownvoted() != null && post.getDownvoted()) {
      newScore += 1;
      post.setDownvoted(false);
      post.setUpvoted(false);
    } else {
      newScore -= 1;
      if (post.getUpvoted()) {
        newScore -= 1;
      }
      post.setDownvoted(true);
      post.setUpvoted(false);
    }

    SEDApp.component().analyticsFacade().trackDownVote(post.getId());
    setVoteButtonStates();
    scoreText.setText(String.valueOf(newScore));

    mService.downVote(post.get_id())
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(new DisposableObserver<Void>() {
        @Override
        public void onComplete() {}

        @Override
        public void onError(Throwable e) {
          Log.v(TAG, e.toString());
        }

        @Override
        public void onNext(Void posts) {}
      });
  }

  @OnClick(R.id.deleteButton)
  public void confirmRemoveLocalDownload() {
    if (post == null) return;
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

  @OnClick(R.id.playButton)
  public void playClick () {
    if (post == null || post.getMp3() == null || post.getMp3().isEmpty()) {
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

    File file = new MP3FileManager().getFileFromUrl(post.getMp3(), SEDApp.component().context());

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

  private void handleDownloadStateChange (String state) {
    if (post == null) return;
    Boolean downloaded = PodcastDownloadsRepository.getInstance().isPodcastDownloaded(post);
    if (downloaded) {
      setUpDownloadedState();
    } else {
      setUpNotDownloadedState();
    }
  }
}
