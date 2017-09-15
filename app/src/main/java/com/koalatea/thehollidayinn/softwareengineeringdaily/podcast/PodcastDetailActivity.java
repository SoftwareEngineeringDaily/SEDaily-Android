package com.koalatea.thehollidayinn.softwareengineeringdaily.podcast;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.koalatea.thehollidayinn.softwareengineeringdaily.R;
import com.koalatea.thehollidayinn.softwareengineeringdaily.audio.MediaPlayer;
import com.koalatea.thehollidayinn.softwareengineeringdaily.audio.MusicProvider;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Post;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.APIInterface;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.ApiUtils;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.PostRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.UserRepository;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PodcastDetailActivity extends AppCompatActivity {
    private PostRepository postRepository;
    private UserRepository userRepository;
    private TextView scoreText;
    private Post post;
    private APIInterface mService;
    private MediaPlayer mediaPlayer;
    private String postId;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcast_detail);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mService = ApiUtils.getKibbleService(this);
        userRepository = UserRepository.getInstance(this);

        Intent intent = getIntent(); // gets the previously created intent
        postId = intent.getStringExtra("POST_ID");

        mediaPlayer = MediaPlayer.getInstance(this);

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

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(post.title.rendered);

        TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
        titleTextView.setText(post.title.rendered);

        String dayString = android.text.format.DateFormat.format("MMMM dd, yyyy", post.date.getTime()).toString();
        TextView secondaryTextView = (TextView) findViewById(R.id.secondayTextView);
        secondaryTextView.setText(dayString);

        TextView descriptionTextView = (TextView) findViewById(R.id.description);
        if (Build.VERSION.SDK_INT > 24) {
            descriptionTextView.setText(Html.fromHtml(post.content.rendered, Html.FROM_HTML_MODE_COMPACT));
        } else {
            descriptionTextView.setText(Html.fromHtml(post.content.rendered));
        }


        scoreText = (TextView) findViewById(R.id.scoreTextView);
        scoreText.setText(String.valueOf(post.score));

        final ImageView upButton = (ImageView) findViewById(R.id.up_button);
        final ImageView downButton = (ImageView) findViewById(R.id.down_button);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (post.upvoted != null && post.upvoted) {
                upButton.getDrawable().setTint(getResources().getColor(R.color.colorPrimary));
            }
        }

        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (userRepository.getToken().isEmpty()) {
                    displayMessage("You must login to vote");
                    return;
                }

                String direction = "";

                if (post.upvoted != null && post.upvoted) {
                    direction = "up";
                    post.score -= 1;
                    post.upvoted = false;
                    post.downvoted = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        upButton.getDrawable().setTint(getResources().getColor(R.color.button_grey));
                        downButton.getDrawable().setTint(getResources().getColor(R.color.button_grey));
                    }
                } else {
                    direction = "down";
                    post.score += 1;

                    if (post.downvoted) {
                        post.score += 1;
                    }

                    post.upvoted = true;
                    post.downvoted = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        upButton.getDrawable().setTint(getResources().getColor(R.color.colorPrimary));
                        downButton.getDrawable().setTint(getResources().getColor(R.color.button_grey));
                    }
                }

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, postId);
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, direction);
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "VOTE");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                scoreText.setText(String.valueOf(post.score));

                mService.upVote(post._id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Void>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.v("keithtest", e.toString());
                        }

                        @Override
                        public void onNext(Void posts) {

                        }
                    });
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (post.downvoted != null && post.downvoted) {
                downButton.getDrawable().setTint(getResources().getColor(R.color.colorPrimary));
            }
        }

        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userRepository.getToken().isEmpty()) {
                    displayMessage("You must login to vote");
                    return;
                }

                if (post.downvoted != null && post.downvoted) {
                    post.score += 1;
                    post.downvoted = false;
                    post.upvoted = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        downButton.getDrawable().setTint(getResources().getColor(R.color.button_grey));
                        upButton.getDrawable().setTint(getResources().getColor(R.color.button_grey));
                    }
                } else {
                    post.score -= 1;

                    if (post.upvoted) {
                        post.score -= 1;
                    }

                    post.downvoted = true;
                    post.upvoted = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        downButton.getDrawable().setTint(getResources().getColor(R.color.colorPrimary));
                        upButton.getDrawable().setTint(getResources().getColor(R.color.button_grey));
                    }
                }

                scoreText.setText(String.valueOf(post.score));

                mService.downVote(post._id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Void>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.v("keithtest", e.toString());
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
                if (post.mp3 == null || post.mp3.isEmpty()) {
                    return;
                }

                String source = post.mp3;
                String id = String.valueOf(source.hashCode());

                MusicProvider mMusicProvider = MusicProvider.getInstance();
                MediaMetadataCompat item = mMusicProvider.getMusic(id);

                if (item == null) {
                    item = new MediaMetadataCompat.Builder()
                            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, source)
//                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
//                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
//                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
//                        .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
//                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, iconUrl)
//                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
//                        .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, trackNumber)
//                        .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, totalTrackCount)
                            .build();


                    mMusicProvider.updateMusic(id, item);
                }

                MediaBrowserCompat.MediaItem bItem =
                        new MediaBrowserCompat.MediaItem(item.getDescription(),
                                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);

                boolean isPlaying = id.equals(mediaPlayer.getPlayingMediaId());
                mediaPlayer.onMediaItemSelected(bItem, isPlaying);
            }
        });
    }
}
