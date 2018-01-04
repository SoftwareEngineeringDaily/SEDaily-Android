package com.koalatea.thehollidayinn.softwareengineeringdaily;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.koalatea.thehollidayinn.softwareengineeringdaily.auth.LoginRegisterActivity;
import com.koalatea.thehollidayinn.softwareengineeringdaily.repositories.FilterRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.repositories.UserRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.podcast.PodListFragment;
import com.koalatea.thehollidayinn.softwareengineeringdaily.podcast.RecentPodcastFragment;
import com.koalatea.thehollidayinn.softwareengineeringdaily.subscription.SubscriptionActivity;

import timber.log.Timber;

public class MainActivity extends PlaybackControllerActivity
    implements SearchView.OnQueryTextListener {
    private UserRepository userRepository;
    private FilterRepository filterRepository;

    private RecentPodcastFragment firstFragment;
    private PodListFragment secondPage;
    private PodListFragment thirdPage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setUp();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        userRepository = UserRepository.getInstance(this);
        filterRepository = FilterRepository.getInstance();

        setUpBottomNavigation();

        showInitialPage();
    }

    private void showInitialPage () {
        if (firstFragment == null) {
            firstFragment = RecentPodcastFragment.newInstance();
        }

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
                if (secondPage == null) {
                    secondPage = PodListFragment.newInstance("Greatest Hits", "");
                }
                this.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, secondPage)
                        .commit();
                break;
            case R.id.action_music:
                if (thirdPage == null) {
                    thirdPage = PodListFragment.newInstance("Just For You", "");
                }
                this.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, thirdPage)
                        .commit();
                break;
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (userRepository.getToken() != null && !userRepository.getToken().isEmpty()) {
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
        } else if (id == R.id.action_subscribe) {
            Timber.v("keithtest-action_subscribe");
            startActivity(new Intent(this, SubscriptionActivity.class));
        }
        //else if (id == R.id.opensource) {
            //Intent intent = new Intent(this, OssLicensesMenuActivity.class);
            //String title = getString(R.string.open_source_info);
            //intent.putExtra("title", title);
            //startActivity(intent);
        //}

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        firstFragment.goHome();
        filterRepository.setSearch(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return true;
    }
}
