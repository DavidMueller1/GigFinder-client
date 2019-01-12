package com.example.david.gigfinder;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.david.gigfinder.adapters.SectionsPageAdapter;
import com.example.david.gigfinder.tools.ColorTools;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "APPLOG - MainActivity";

    public static String idToken;
    public static int userId;

    SharedPreferences sharedPreferences;

    private SectionsPageAdapter sectionsPageAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(getString(R.string.shared_prefs), Context.MODE_PRIVATE);

        String user = "none";
        /*if (getIntent().hasExtra("user")){
            user = getIntent().getExtras().getString("user");
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("user", user);
            editor.apply();
        } else {*/
            user = sharedPreferences.getString("user", "none");
        // }

        /*
        try {
            user = getIntent().getStringExtra("user");

            SharedPreferences.Editor editor = sharedPreferences.edit();
            //editor.clear();
            editor.putString("user", user);
            editor.commit();
        }
        catch (NullPointerException e) {
            user = sharedPreferences.getString("user", "none");
        }
        */
        if(user == null || user.equals("none")) {
            // TODO get user from server
            Toast.makeText(getApplicationContext(),"Using default user",Toast.LENGTH_SHORT).show();
            user = "host";
        }


        idToken = getIntent().getExtras().getString("idToken");

        mViewPager = findViewById(R.id.viewpager);
        mViewPager.setOffscreenPageLimit(1);
        setupViewPager(mViewPager, user);

        final TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        if(user.equals("artist")) {
            int[] icons = {R.drawable.ic_baseline_search_24px, R.drawable.ic_baseline_star , R.drawable.ic_guitar, R.drawable.ic_baseline_chat_bubble, R.drawable.ic_baseline_user};
            int tabColor = getResources().getColor(R.color.orange);
            for(int i = 0; i < 5; i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                tab.setIcon(icons[i]);
                tab.getIcon().setColorFilter(tabColor, PorterDuff.Mode.SRC_IN);
            }

            tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if(tab == tabLayout.getTabAt(4)) {
                        int userColor = sharedPreferences.getInt("userColor", getResources().getColor(R.color.black));
                        int statusBarColor = ColorTools.getSecondaryColor(userColor);
                        getWindow().setStatusBarColor(statusBarColor);

                        for(int i = 0; i < 5; i++) {
                            TabLayout.Tab t = tabLayout.getTabAt(i);
                            t.getIcon().setColorFilter(userColor, PorterDuff.Mode.SRC_IN);
                            tabLayout.setTabTextColors(userColor, userColor);
                            tabLayout.setSelectedTabIndicatorColor(userColor);
                        }
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    if(tab == tabLayout.getTabAt(4)) {
                        getWindow().setStatusBarColor(getResources().getColor(R.color.darkOrange));

                        int defaultColor = getResources().getColor(R.color.orange);
                        for(int i = 0; i < 5; i++) {
                            TabLayout.Tab t = tabLayout.getTabAt(i);
                            t.getIcon().setColorFilter(defaultColor,  PorterDuff.Mode.SRC_IN);
                            tabLayout.setTabTextColors(defaultColor, defaultColor);
                            tabLayout.setSelectedTabIndicatorColor(defaultColor);
                        }
                    }
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

            tabLayout.getTabAt(0).select();
        }
        else {
            int[] icons = {R.drawable.ic_baseline_search_24px, R.drawable.ic_baseline_event , R.drawable.ic_baseline_chat_bubble, R.drawable.ic_baseline_user};
            int tabColor = getResources().getColor(R.color.orange);
            for(int i = 0; i < 4; i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                tab.setIcon(icons[i]);
                tab.getIcon().setColorFilter(tabColor, PorterDuff.Mode.SRC_IN);
            }

            tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if(tab == tabLayout.getTabAt(3)) {
                        int userColor = sharedPreferences.getInt("userColor", getResources().getColor(R.color.orange));
                        int statusBarColor = ColorTools.getSecondaryColor(userColor);
                        getWindow().setStatusBarColor(statusBarColor);

                        for(int i = 0; i < 4; i++) {
                            TabLayout.Tab t = tabLayout.getTabAt(i);
                            t.getIcon().setColorFilter(userColor, PorterDuff.Mode.SRC_IN);
                            tabLayout.setTabTextColors(userColor, userColor);
                            tabLayout.setSelectedTabIndicatorColor(userColor);
                        }
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    if(tab == tabLayout.getTabAt(3)) {
                        getWindow().setStatusBarColor(getResources().getColor(R.color.darkOrange));

                        int defaultColor = getResources().getColor(R.color.orange);
                        for(int i = 0; i < 4; i++) {
                            TabLayout.Tab t = tabLayout.getTabAt(i);
                            t.getIcon().setColorFilter(defaultColor,  PorterDuff.Mode.SRC_IN);
                            tabLayout.setTabTextColors(defaultColor, defaultColor);
                            tabLayout.setSelectedTabIndicatorColor(defaultColor);
                        }
                    }
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

            tabLayout.getTabAt(0).select();
        }
    }

    /**
     * Adds the Fragments which will be selectable by the Tabs and their Titles to a SectionsPageAdapter and passes it to the ViewPager
     */
    private void setupViewPager(ViewPager viewPager, String user) {
        Bundle args = new Bundle();
        args.putString("idToken", idToken);
        sectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());

        if(user.equals("artist")) {

            ExploreFragment exploreFragment = new ExploreFragment();
            FavoritesFragment favoritesFragment = new FavoritesFragment();
            GigsFragment gigsFragment = new GigsFragment();
            ChatFragment chatFragment = new ChatFragment();
            ArtistProfileFragment artistProfileFragment = new ArtistProfileFragment();

            exploreFragment.setArguments(args);
            favoritesFragment.setArguments(args);
            artistProfileFragment.setArguments(args);
            gigsFragment.setArguments(args);
            chatFragment.setArguments(args);

            sectionsPageAdapter.addFragment(exploreFragment, getString(R.string.nav_explore));
            sectionsPageAdapter.addFragment(favoritesFragment, getString(R.string.nav_favorites));
            sectionsPageAdapter.addFragment(gigsFragment, getString(R.string.nav_gigs));
            sectionsPageAdapter.addFragment(chatFragment, getString(R.string.nav_chat));
            sectionsPageAdapter.addFragment(artistProfileFragment, getString(R.string.nav_profile));
        }
        else {
            ExploreFragment exploreFragment = new ExploreFragment();
            GigsFragment gigsFragment = new GigsFragment();
            ChatFragment chatFragment = new ChatFragment();
            HostProfileFragment hostProfileFragment = new HostProfileFragment();

            exploreFragment.setArguments(args);
            hostProfileFragment.setArguments(args);
            gigsFragment.setArguments(args);
            chatFragment.setArguments(args);

            sectionsPageAdapter.addFragment(exploreFragment, getString(R.string.nav_explore));
            sectionsPageAdapter.addFragment(gigsFragment, getString(R.string.nav_events));
            sectionsPageAdapter.addFragment(chatFragment, getString(R.string.nav_chat));
            sectionsPageAdapter.addFragment(hostProfileFragment, getString(R.string.nav_profile));
        }

        viewPager.setAdapter(sectionsPageAdapter);
    }


}
