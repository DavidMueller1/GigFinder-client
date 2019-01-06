package com.example.david.gigfinder;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.david.gigfinder.adapters.SectionsPageAdapter;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "APPLOG - MainActivity";

    private SectionsPageAdapter sectionsPageAdapter;
    private ViewPager mViewPager;
    private String idToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        idToken = getIntent().getExtras().getString("idToken");

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
        Bundle args = new Bundle();
        args.putString("idToken", idToken);
        sectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());
        ProfileFragment profileFragment = new ProfileFragment();
        profileFragment.setArguments(args);
        ExploreFragment exploreFragment = new ExploreFragment();
        exploreFragment.setArguments(args);
        sectionsPageAdapter.addFragment(exploreFragment, getString(R.string.nav_explore));
        sectionsPageAdapter.addFragment(new FavoritesFragment(), getString(R.string.nav_favorites));
        sectionsPageAdapter.addFragment(new GigsFragment(), getString(R.string.nav_gigs));
        sectionsPageAdapter.addFragment(new ChatFragment(), getString(R.string.nav_chat));
        sectionsPageAdapter.addFragment(profileFragment, getString(R.string.nav_profile));
        viewPager.setAdapter(sectionsPageAdapter);
    }


}
