package com.example.david.gigfinder;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "APPLOG - MainActivity";

    private SectionsPageAdapter sectionsPageAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "App starting...");

        mViewPager = findViewById(R.id.viewpager);
        setupViewPager(mViewPager);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.getTabAt(4).select(); // TODO select the relevant Tab

        //tabLayout.getTabAt(0).setIcon(R.drawable.ic_music_gig);
    }

    /**
     * Adds the Fragments which will be selectable by the Tabs and their Titles to a SectionsPageAdapter and passes it to the ViewPager
     */
    private void setupViewPager(ViewPager viewPager) {
        sectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());
        sectionsPageAdapter.addFragment(new ExploreFragment(), getString(R.string.nav_explore));
        sectionsPageAdapter.addFragment(new FavoritesFragment(), getString(R.string.nav_favorites));
        sectionsPageAdapter.addFragment(new GigsFragment(), getString(R.string.nav_gigs));
        sectionsPageAdapter.addFragment(new ChatFragment(), getString(R.string.nav_chat));
        sectionsPageAdapter.addFragment(new ProfileFragment(), getString(R.string.nav_profile));
        viewPager.setAdapter(sectionsPageAdapter);
    }


}
