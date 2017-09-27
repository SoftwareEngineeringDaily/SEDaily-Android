package com.koalatea.thehollidayinn.softwareengineeringdaily;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.koalatea.thehollidayinn.softwareengineeringdaily.audio.MusicService;
import com.koalatea.thehollidayinn.softwareengineeringdaily.auth.LoginRegisterActivity;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.FilterRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.UserRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.podcast.PodListFragment;
import com.koalatea.thehollidayinn.softwareengineeringdaily.podcast.RecentPodcastFragment;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private UserRepository userRepository;
    private RecentPodcastFragment firstFragment;
    private MediaBrowserCompat mMediaBrowser;
    private FilterRepository filterRepository;

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallbacks =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {

                    // Get the token for the MediaSession
                    MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();

                    // Create a MediaControllerCompat
                    MediaControllerCompat mediaController =
                            null;
                    try {
                        mediaController = new MediaControllerCompat(MainActivity.this, token);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    // Save the controller
                    MediaControllerCompat.setMediaController(MainActivity.this, mediaController);

                    // Finish building the UI
                    buildTransportControls();
                }

                @Override
                public void onConnectionSuspended() {
                    // The Service has crashed. Disable transport controls until it automatically reconnects
                }

                @Override
                public void onConnectionFailed() {
                    // The Service has refused our connection
                }
            };
    MediaControllerCompat.Callback controllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {}

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {}
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userRepository = UserRepository.getInstance(this);
        filterRepository = FilterRepository.getInstance();

        mMediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, MusicService.class),
                mConnectionCallbacks,
                null); // optional Bundle

        setUpBottomNavigation();
        showInitialPage();
    }

    private void showInitialPage () {
        firstFragment = RecentPodcastFragment.newInstance();
        this.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, firstFragment)
                .commit();
    }

    private void setUpBottomNavigation() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        return navigationItemSelected(item);
                    }
                });
    }

    private Boolean navigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favorites:
                showInitialPage();
                break;
            case R.id.action_schedules:
                PodListFragment second = PodListFragment.newInstance("Greatest Hits", "");
                this.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, second)
                        .commit();
                break;
            case R.id.action_music:
                PodListFragment third = PodListFragment.newInstance("Just For You", "");
                this.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, third)
                        .commit();
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (!userRepository.getToken().isEmpty()) {
            menu.findItem(R.id.action_toggle_login_register).setVisible(false);
            menu.findItem(R.id.action_logout).setVisible(true);
        } else {
            menu.findItem(R.id.action_toggle_login_register).setVisible(true);
            menu.findItem(R.id.action_logout).setVisible(false);
        }

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(true);

        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.search),
            new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    filterRepository.setSearch("");
                    return true;
                }

                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    return true;
                }
            });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_toggle_login_register) {
            Intent intent = new Intent(this, LoginRegisterActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_logout) {
            userRepository.setToken("");
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        mMediaBrowser.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        // (see "stay in sync with the MediaSession")
        if (MediaControllerCompat.getMediaController(this) != null) {
            MediaControllerCompat.getMediaController(this).unregisterCallback(controllerCallback);
        }
        mMediaBrowser.disconnect();
    }

    void buildTransportControls()
    {
        // Grab the view for the play/pause button
        ImageView mPlayPause = (ImageView) findViewById(R.id.logo);

        // @TODO: Move this to fragment player when we add it
//        mPlayPause.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Since this is a play/pause button, you'll need to test the current state
//                // and choose the action accordingly
//
//                int pbState = MediaControllerCompat.getMediaController(MainActivity.this).getPlaybackState().getState();
//                if (pbState == PlaybackStateCompat.STATE_PLAYING) {
//                    MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().pause();
//                } else {
//                    MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().play();
//                }
//            }
//        });

        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(MainActivity.this);

        // Display the initial state
        MediaMetadataCompat metadata = mediaController.getMetadata();
        PlaybackStateCompat pbState = mediaController.getPlaybackState();

        // Register a Callback to stay in sync
        mediaController.registerCallback(controllerCallback);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        filterRepository.setSearch(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return true;
    }
}
