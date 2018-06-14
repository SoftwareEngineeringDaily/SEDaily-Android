package com.koalatea.thehollidayinn.softwareengineeringdaily.podcast;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.koalatea.thehollidayinn.softwareengineeringdaily.MainActivity;
import com.koalatea.thehollidayinn.softwareengineeringdaily.PlaybackControllerActivity;
import com.koalatea.thehollidayinn.softwareengineeringdaily.R;
import com.koalatea.thehollidayinn.softwareengineeringdaily.app.SEDApp;
import com.koalatea.thehollidayinn.softwareengineeringdaily.audio.MusicProvider;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.AppDatabase;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Bookmark;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Post;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.APIInterface;
import com.koalatea.thehollidayinn.softwareengineeringdaily.repositories.PodcastDownloadsRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.repositories.PostRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.repositories.UserRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.BookmarkDao;
import com.koalatea.thehollidayinn.softwareengineeringdaily.downloads.MP3FileManager;
import com.koalatea.thehollidayinn.softwareengineeringdaily.util.AlertUtil;
import com.koalatea.thehollidayinn.softwareengineeringdaily.util.ReactiveUtil;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.ohoussein.playpause.PlayPauseView;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class PodcastDetailActivity extends PlaybackControllerActivity {
  private PostRepository postRepository;
  private UserRepository userRepository;
  private DisposableObserver myDisposableObserver;

  @BindView(R.id.scoreTextView)
  TextView scoreText;

  @BindView(R.id.deleteButton)
  Button deleteButton;

  @BindView(R.id.playButton)
  PlayPauseView playButton;

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
  private MenuItem downloadItem;
  private MenuItem bookmarkItem;
  private Boolean bookmarked = false;
  private PodcastDownloadsRepository podcastDownloadsRepository;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_podcast_detail);
    ButterKnife.bind(this);

    podcastDownloadsRepository = PodcastDownloadsRepository.getInstance();
    setUp();

    if (toolbar != null) {
      toolbar.setTitle("");
      setSupportActionBar(toolbar);
      getSupportActionBar().setDisplayShowTitleEnabled(false); // @TODO: This doesn't seem to work
    }

//    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    mService = SEDApp.component().kibblService();
    userRepository = UserRepository.getInstance(this);

    Intent intent = getIntent(); // gets the previously created intent
    String postId = intent.getStringExtra("POST_ID");

    postRepository = PostRepository.getInstance();
    loadPost(postId);

    setUpDownloadObserver();
  }

  private void setUpPlayButtonState(String title) {
    PodcastSessionStateManager podcastSessionStateManager = PodcastSessionStateManager.getInstance();
    PlaybackStateCompat playbackStateCompat = podcastSessionStateManager.getLastPlaybackState();
    String currentPLayingTitle = podcastSessionStateManager.getCurrentTitle();
    boolean isSameMedia = currentPLayingTitle.equals(title);

    if (playbackStateCompat != null && playbackStateCompat.getState() == PlaybackStateCompat.STATE_PLAYING && isSameMedia) {
      playButton.toggle();
    }
  }

  private void checkForBookMarks() {
    if (post == null) {
      return;
    }

    String postId = post.get_id();

    AppDatabase db = AppDatabase.getDatabase();

    Observable.just(db)
      .subscribeOn(Schedulers.io())
      .subscribe(bookmarkdb -> {
        BookmarkDao bookmarkDao = bookmarkdb.bookmarkDao();
        Bookmark bookmark = bookmarkDao.loadById(postId);

        if (bookmark != null) {  // the post has been bookmarked before
          if (bookmarkItem != null) {
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                markBookmarked();
              }
            });
          }
          bookmarked = true;
        }
      });
  }

  private void markBookmarked () {
    IconicsDrawable bookmarkIcon = new IconicsDrawable(this)
            .icon(GoogleMaterial.Icon.gmd_bookmark)
            .color(getResources().getColor(R.color.accent))
            .sizeDp(24);
    bookmarkItem.setIcon(bookmarkIcon);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.podcast_detail_menu, menu);

    downloadItem = menu.findItem(R.id.menu_item_download);
    checkDownloadState();

    bookmarkItem = menu.findItem(R.id.menu_item_bookmark);
    if (bookmarkItem != null) {
      IconicsDrawable bookmarkIcon = new IconicsDrawable(this)
              .icon(GoogleMaterial.Icon.gmd_bookmark)
              .color(getResources().getColor(R.color.white))
              .sizeDp(24);
      bookmarkItem.setIcon(bookmarkIcon);
    }
    checkForBookMarks();

    // Share button
    IconicsDrawable share = new IconicsDrawable(this)
            .icon(GoogleMaterial.Icon.gmd_share)
            .color(getResources().getColor(R.color.white))
            .sizeDp(24);
    menu.findItem(R.id.menu_item_share).setIcon(share);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch(item.getItemId()) {
      case R.id.menu_item_download:
        downloadMp3();
        break;
      case R.id.menu_item_share:
        startShareIntent();
        break;
      case R.id.menu_item_bookmark:
        onClickBookmarkButton();
        break;
    }

    return super.onOptionsItemSelected(item);
  }

  /*
   * Start a share intent
   */
  public void startShareIntent() {
    if (post == null) return;
    String shareContent = "Check out this episode of Software Engineering Daily: ";
    shareContent += post.getLink();

    Intent shareIntent = ShareCompat.IntentBuilder.from(this)
            .setText(shareContent)
            .setType("text/plain")
            .setChooserTitle("Share Podcast")
            .createChooserIntent();

    startActivity(shareIntent);
  }

  @Override
  public void onStop() {
    super.onStop();
    if (myDisposableObserver != null) {
      myDisposableObserver.dispose();
    }
  }

  private void loadPost (final String postId) {
    post = postRepository.getPostById(postId);

    if (post == null) {
      Intent intent = new Intent(this, MainActivity.class);
      startActivity(intent);
      return;
    }

    String title = post.getTitle().getRendered();

    setUpPlayButtonState(title);

    titleTextView.setText(title);

    String webPostContent = post.getContent().getRendered();

    webPostContent = webPostContent.replaceAll("<audio class=\"wp-audio-shortcode\".*</audio>", "");
    webPostContent = webPostContent.replaceAll("<p class=\"powerpress_links powerpress_links_mp3\">.*Download</a></p>", "");

    descriptionWebView.loadData(webPostContent, "text/html", "UTF-8");

    String dayString = android.text.format.DateFormat.format("MMMM dd, yyyy", post.getDate().getTime()).toString();
    secondaryTextView.setText(dayString);

    scoreText.setText(String.valueOf(post.getScore()));
    setVoteButtonStates();

    checkDownloadState();
    checkForBookMarks();
  }

  private void checkDownloadState () {
    if (post == null) {
      return;
    }

    if (!podcastDownloadsRepository.isPodcastDownloaded(post)) {
      setUpNotDownloadedState();
    } else {
      setUpDownloadedState();
    }

    if (podcastDownloadsRepository.isDownloading(post.get_id())) {
      setUpDownloadedState();
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
      AlertUtil.displayMessage(this, "You must login to vote");
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
            .subscribe(ReactiveUtil.getEmptyObservable());
  }

  @OnClick(R.id.down_button)
  public void downVotePost () {
    if (post == null) return;

    if (userRepository.getToken().isEmpty()) {
      AlertUtil.displayMessage(this,"You must login to vote");
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
      .subscribe(ReactiveUtil.getEmptyObservable());
  }

  /* Downloads */

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

  private void setUpDownloadObserver() {
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
    podcastDownloadsRepository
            .getDownloadChanges()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(myDisposableObserver);
  }

  public void setUpDownloadedState() {
    if (downloadItem != null) {
      IconicsDrawable removeIcon = new IconicsDrawable(this)
              .icon(GoogleMaterial.Icon.gmd_cancel)
              .color(Color.WHITE)
              .sizeDp(24);
      downloadItem.setIcon(removeIcon);
    }

    deleteButton.setVisibility(View.VISIBLE);
  }

  public void setUpNotDownloadedState() {
    if (downloadItem != null) {
      IconicsDrawable download = new IconicsDrawable(this)
              .icon(GoogleMaterial.Icon.gmd_cloud_download)
              .color(Color.WHITE)
              .sizeDp(24);
      downloadItem.setIcon(download);
    }

    deleteButton.setVisibility(View.INVISIBLE);
  }

  private boolean hasValidMp3() {
    if (post == null || post.getMp3() == null || post.getMp3().isEmpty()) {
      return false;
    }

    return true;
  }

  @OnClick(R.id.playButton)
  public void playClick () {
    if (!hasValidMp3()) return;
    playButton.toggle();
    playMedia();
  }

  public void downloadMp3 () {
    if (!hasValidMp3()) return;
    if (downloadItem == null) return;

    if (podcastDownloadsRepository.isDownloading(post.get_id())) {
      setUpNotDownloadedState();
      podcastDownloadsRepository.cancelDownload(post);
    } else {
      setUpDownloadedState();
      podcastDownloadsRepository.downloadPostMP3(post);
    }
  }

  public void playMedia () {
    if (!hasValidMp3()) return;

    String source = post.getMp3();
    String id = String.valueOf(source.hashCode());

    String mediaUri = source;
    if (podcastDownloadsRepository.isPodcastDownloaded(post)) {
      MP3FileManager mp3FileManager = new MP3FileManager();
      Context context = SEDApp.component.context();
      String filename = mp3FileManager.getFileNameFromUrl(post.getMp3());
      File file = new File(mp3FileManager.getRootDirPath(context), filename);
      mediaUri = file.getAbsolutePath();
    }

    MusicProvider mMusicProvider = MusicProvider.getInstance();
    MediaMetadataCompat item; // = mMusicProvider.getMusic(id);

//    if (item == null) {
    // @TODO: I'm pretty sure we can buld these somwhere else. The provider could read from the repository
    item = new MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, mediaUri)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, post.getTitle().getRendered())
            .build();

//    mMusicProvider.updateMusic(id, item);
//    }

    MediaBrowserCompat.MediaItem bItem =
            new MediaBrowserCompat.MediaItem(item.getDescription(),
                    MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);

    mMusicProvider.updateMusic(bItem.getMediaId(), item);
    
    PodcastSessionStateManager podcastSessionStateManager = PodcastSessionStateManager.getInstance();
    String currentPLayingTitle = podcastSessionStateManager.getCurrentTitle();
    boolean isSameMedia = currentPLayingTitle.equals(post.getTitle().getRendered());

    onMediaItemSelected(bItem, isSameMedia);
  }

  private void handleDownloadStateChange (String state) {
    if (post == null) return;
    Boolean downloaded = podcastDownloadsRepository.isPodcastDownloaded(post);
    if (downloaded) {
      setUpDownloadedState();
    } else {
      setUpNotDownloadedState();
    }
  }

  public void onClickBookmarkButton() {
    if (post == null) return;

    if(userRepository.getToken().isEmpty()) {
      AlertUtil.displayMessage(this, "You must login to bookmark");
      return;
    }

    if (bookmarked) {
      removeBookmark(post);
    } else {
      addBookmark(post);
    }
  }

  private void addBookmark(Post post) {
    if (post == null) return;

    bookmarked = true;
    if (bookmarkItem != null) {
      IconicsDrawable bookmarkIcon = new IconicsDrawable(this)
              .icon(GoogleMaterial.Icon.gmd_bookmark)
              .color(getResources().getColor(R.color.accent))
              .sizeDp(24);
      bookmarkItem.setIcon(bookmarkIcon);
    }

    mService.addBookmark(post.get_id())
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ReactiveUtil.getEmptyObservable());

    AppDatabase db = AppDatabase.getDatabase();

    Observable.just(db)
      .subscribeOn(Schedulers.io())
      .subscribe(bookmarkdb -> {
        BookmarkDao bookmarkDao = db.bookmarkDao();
        Bookmark bookmarkFound = bookmarkDao.loadById(post.get_id());

        if (bookmarkFound != null) return;

        Bookmark bookmark = new Bookmark(post);
        bookmarkDao.insertOne(bookmark);
      });

  }

  private void removeBookmark(Post post) {
    if (post == null) return;

    bookmarked = false;
    if (bookmarkItem != null) {
      IconicsDrawable bookmarkIcon = new IconicsDrawable(this)
              .icon(GoogleMaterial.Icon.gmd_bookmark)
              .color(getResources().getColor(R.color.white))
              .sizeDp(24);
      bookmarkItem.setIcon(bookmarkIcon);
    }

    AppDatabase db = AppDatabase.getDatabase();

    Observable.just(db)
      .subscribeOn(Schedulers.io())
      .subscribe(bookmarkdb -> {
        BookmarkDao bookmarkDao = db.bookmarkDao();
        Bookmark bookmark = bookmarkDao.loadById(post.get_id());
        if (bookmark != null) bookmarkDao.delete(bookmark);
      });

    mService.removeBookmark(post.get_id())
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ReactiveUtil.getEmptyObservable());
  }
}
