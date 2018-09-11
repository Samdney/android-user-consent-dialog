package com.myexample.userconsentdialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ConsentDialog.NoticeDialogListener {


    private AdView mAdView;
    private Bundle extras;
    private AdRequest request;

    private boolean pause = false;
    private SharedPreferences settings;
    private ConsentDialog consentDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set consent dialog settings
        settings = getSharedPreferences("com.myexample.userconsentdialog", MODE_PRIVATE);

        // Toolbar, menu and navigation
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Ad banner
        // TODO: Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");

        // TODO: Sample-Test-Id: ca-app-pub-3940256099942544/6300978111
        this.mAdView = findViewById(R.id.adView);
        this.mAdView.setVisibility(View.VISIBLE);

        initPrivacySettings();

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
            }
        });
    }

    private void initPrivacySettings(){
        if (settings.getBoolean("isFirstRun", true)) {
            extras = new Bundle();
            extras.putBoolean("tag_for_under_age_of_consent", true);
            extras.putString("max_ad_content_rating", "G");
            extras.putString("npa", "1");

            request = new AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                    .build();

            this.mAdView.loadAd(request);

            setFirebaseAnalyticsOffSettings(true);
        } else {
            if(!settings.getBoolean("adsNonPersonalized", true)){
                setAdsNonPerSettings(false);
            } else {
                setAdsNonPerSettings(true);
            }

            if(!settings.getBoolean("offFirebaseAnalytics", true)){
                setFirebaseAnalyticsOffSettings(false);
            } else {
                setFirebaseAnalyticsOffSettings(true);
            }
        }
    }

    // Event listener for internet connection
    private BroadcastReceiver networkStateReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = manager.getActiveNetworkInfo();
            if(ni != null) {
                initPrivacySettings();
            }
        }
    };

    @Override
    public void onPause(){
        unregisterReceiver(networkStateReceiver);
        super.onPause();
        pause = true;
    }

    @Override
    public void onResume(){
        super.onResume();
        registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        pause = false;
    }

    @Override
    public void onStart() {
        super.onStart();
        // On first start, at all
        if (settings.getBoolean("isFirstRun", true) && (pause == false)) {
            consentDialog = new ConsentDialog(this, settings);
            consentDialog.showConsentDialog();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_manage) {
            consentDialog = new ConsentDialog(this, settings);
            consentDialog.showConsentDialog();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void onAdsRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_Ads_option01:
                if (checked)
                    setAdsNonPerSettings(false);
                break;
            case R.id.radio_Ads_option02:
                if (checked)
                    setAdsNonPerSettings(true);
                break;
        }
    }

    private void setAdsNonPerSettings(boolean nonPersonalized){
        extras = new Bundle();
        extras.putBoolean("tag_for_under_age_of_consent", true);
        extras.putString("max_ad_content_rating", "G");

        if(nonPersonalized == false){
            settings.edit().putBoolean("adsNonPersonalized", false).apply();
        } else {
            extras.putString("npa", "1");
            settings.edit().putBoolean("adsNonPersonalized", true).apply();
        }

        request = new AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                .build();

        this.mAdView.loadAd(request);
    }

    public void onFirebaseAnalyticsRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_FirebaseAnalytics_option01:
                if (checked)
                    setFirebaseAnalyticsOffSettings(false);
                break;
            case R.id.radio_FirebaseAnalytics_option02:
                if (checked)
                    setFirebaseAnalyticsOffSettings(true);
                break;
        }
    }

    private void setFirebaseAnalyticsOffSettings(boolean offFirebaseAnalytics){
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(!offFirebaseAnalytics);   // Needs: WAKE_LOCK
        settings.edit().putBoolean("offFirebaseAnalytics", offFirebaseAnalytics).apply();
    }

    @Override
    public void onConsentDialogNegativeClick() {
        if (settings.getBoolean("isFirstRun", true)) {
            finish();
        }
    }

    @Override
    public void onConsentDialogPositiveClick() {
        onAdsRadioButtonClicked(consentDialog.getCheckedAdsRadioButtonView());
        onFirebaseAnalyticsRadioButtonClicked(consentDialog.getCheckedFirebaseAnalyticsRadioButtonView());
        settings.edit().putBoolean("isFirstRun", false).apply();
    }
}
