package com.koalatea.thehollidayinn.softwareengineeringdaily;

import android.app.Fragment;
import android.app.SearchManager;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.koalatea.thehollidayinn.softwareengineeringdaily.app.SEDApp;
import com.koalatea.thehollidayinn.softwareengineeringdaily.auth.LoginRegisterActivity;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.SubscriptionResponse;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.UserResponse;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.APIInterface;
import com.koalatea.thehollidayinn.softwareengineeringdaily.notifications.NotificationActivity;
import com.koalatea.thehollidayinn.softwareengineeringdaily.podcast.TopRecListFragment;
import com.koalatea.thehollidayinn.softwareengineeringdaily.repositories.FilterRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.repositories.UserRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.latest.RecentPodcastFragment;
import com.koalatea.thehollidayinn.softwareengineeringdaily.subscription.SubscriptionActivity;
import com.koalatea.thehollidayinn.softwareengineeringdaily.util.AlertUtil;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import hotchemi.android.rate.AppRate;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import com.koalatea.thehollidayinn.softwareengineeringdaily.data.AppDatabase;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.BookmarkDao;
import com.koalatea.thehollidayinn.softwareengineeringdaily.podcast.PodListFragment;

public class MainActivity extends PlaybackControllerActivity
    implements SearchView.OnQueryTextListener {
    private UserRepository userRepository;
    private FilterRepository filterRepository;

    private RecentPodcastFragment firstFragment;
    private TopRecListFragment secondPage;
    private TopRecListFragment thirdPage;
    private PodListFragment bookmarksFragment;

    private Menu menu;
    private Drawer drawer;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private SecondaryDrawerItem subscribeItem;
    private SecondaryDrawerItem loginItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setUp();

        userRepository = SEDApp.component.userRepository();
        filterRepository = FilterRepository.getInstance();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tabLayout = findViewById(R.id.tabs);
        setUpDrawer(toolbar);

        showInitialPage();
        setUpReviewWatch();
    }

    private void setUpReviewWatch() {
        AppRate.with(this)
            .setInstallDays(1) // default 10, 0 means install day.
            .setLaunchTimes(3) // default 10
            .setRemindInterval(2) // default 1
            .setShowLaterButton(true) // default true
            .setDebug(false) // default false
            .monitor();

        // Show a dialog if meets conditions
        AppRate.showRateDialogIfMeetsConditions(this);
    }

    private void loadMe () {
        APIInterface apiInterface = SEDApp.component.kibblService();
        apiInterface.me()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new DisposableObserver<UserResponse>() {
                @Override
                public void onNext(UserResponse userResponse) {
                    displaySubscribedView(userResponse);
                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onComplete() {

                }
            });
    }

    private void displaySubscribedView (UserResponse userResponse) {
        SubscriptionResponse subscriptionResponse = userResponse.getSubscription();
        if (subscriptionResponse != null && subscriptionResponse.active) {
            subscribeItem.getName().setText("View Subscription");
            userRepository.setHasPremium(true);
        }
    }

    private void showInitialPage () {
        if (firstFragment == null) {
            firstFragment = RecentPodcastFragment.newInstance();
        }

        toolbar.setTitle("Latest");

        this.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, firstFragment)
                .commit();
    }

    public void setUpDrawer(Toolbar toolbar) {
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withIcon(GoogleMaterial.Icon.gmd_mic).withName(R.string.latest);
        SecondaryDrawerItem item2 = new SecondaryDrawerItem().withIdentifier(2).withIcon(GoogleMaterial.Icon.gmd_assessment).withName(R.string.greatest_hits);
        SecondaryDrawerItem item3 = new SecondaryDrawerItem().withIdentifier(3).withIcon(GoogleMaterial.Icon.gmd_show_chart).withName(R.string.just_for_you);

        loginItem = new SecondaryDrawerItem().withIdentifier(4).withIcon(GoogleMaterial.Icon.gmd_perm_identity).withName(R.string.login);
        subscribeItem = new SecondaryDrawerItem().withIdentifier(5).withIcon(GoogleMaterial.Icon.gmd_monetization_on).withName(R.string.subscribe);

        SecondaryDrawerItem bookmarkItem = new SecondaryDrawerItem().withIdentifier(6).withIcon(GoogleMaterial.Icon.gmd_bookmark).withName(R.string.bookmarks);
        SecondaryDrawerItem downloadItem = new SecondaryDrawerItem()
                .withIdentifier(8)
                .withIcon(GoogleMaterial.Icon.gmd_file_download)
                .withName(R.string.downloads);
        SecondaryDrawerItem notificationItem = new SecondaryDrawerItem()
                .withIdentifier(7)
                .withIcon(GoogleMaterial.Icon.gmd_notifications)
                .withName("Notifications");

        AccountHeaderBuilder accountHeaderBuilder = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.color.accent);

        if (!userRepository.getToken().isEmpty()) {
            loginItem.getName().setText(getString(R.string.logout));
            accountHeaderBuilder.addProfiles(
                new ProfileDrawerItem()
                    .withName("Logged In")
//                        .withEmail("mikepenz@gmail.com")
                    .withIcon(getResources()
                    .getDrawable(R.drawable.sedaily_logo))
            );
        } else {
            accountHeaderBuilder.addProfiles(
                new ProfileDrawerItem()
                    .withName("LoggedOut")
                    .withIcon(getResources()
                        .getDrawable(R.drawable.sedaily_logo))
            );
        }

        AccountHeader headerResult = accountHeaderBuilder
//                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
//                    @Override
//                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
//                        return false;
//                    }
//                })
                .build();

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        item1,
                        item2,
                        item3,
                        bookmarkItem,
                        downloadItem,
                        notificationItem,
                        new DividerDrawerItem(),
                        loginItem,
                        subscribeItem
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        navigationItemSelected(drawerItem);
                        drawer.closeDrawer();
                        return false;
                    }
                })
                .build();
    }

    private Boolean navigationItemSelected(@NonNull IDrawerItem item) {
        switch ((int) item.getIdentifier()) {
            case 1:
                tabLayout.setVisibility(View.VISIBLE);
                showInitialPage();
                break;
            case 2:
                if (secondPage == null) {
                    secondPage = TopRecListFragment.newInstance("Greatest Hits", "");
                }

                toolbar.setTitle("Greatest Hits");
                tabLayout.setVisibility(View.GONE);

                this.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, secondPage)
                        .commit();
                break;
            case 3:
                if (thirdPage == null) {
                    thirdPage = TopRecListFragment.newInstance("Just For You", "");
                }

                toolbar.setTitle("Just For You");
                tabLayout.setVisibility(View.GONE);

                this.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, thirdPage)
                        .commit();
                break;
            case 4:
                if (!userRepository.getToken().isEmpty()) {
                    logout();
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(this, LoginRegisterActivity.class);
                    startActivity(intent);
                }
                break;
            case 5:
                startActivity(new Intent(this, SubscriptionActivity.class));
                break;
            case 6:
                if (userRepository.getToken().isEmpty()) {
                    AlertUtil.displayMessage(this, "You must login to use this feature");
                    break;
                }

                toolbar.setTitle("Bookmarks");
                tabLayout.setVisibility(View.GONE);

                if (bookmarksFragment == null) {
                    bookmarksFragment = PodListFragment.newInstance("Bookmarks", "");
                }

                this.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, bookmarksFragment)
                        .commit();
                break;
            case 7:
                startActivity(new Intent(this, NotificationActivity.class));
                break;
            case 8:
                toolbar.setTitle("Bookmarks");
                tabLayout.setVisibility(View.GONE);

                PodListFragment fragment = PodListFragment.newInstance("Downloads", "");

                this.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
                break;
        }

        return true;
    }

    private void logout() {
        userRepository.setToken("");
        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "sed-db").build();
        Observable.just(db)
            .subscribeOn(Schedulers.io())
            .subscribe(bookmarkdb -> {
                BookmarkDao bookmarkDao = db.bookmarkDao();
                bookmarkDao.deleteAll();
            });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;

        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        MenuItem searchItem = menu.findItem(R.id.search);
        IconicsDrawable searchIcon = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_search)
                .color(Color.WHITE)
                .sizeDp(20);
        searchItem.setIcon(searchIcon);

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

        // Do this after the menu is loaded
        loadMe();

        return true;
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
