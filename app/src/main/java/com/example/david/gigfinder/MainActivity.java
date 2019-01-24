package com.example.david.gigfinder;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.david.gigfinder.adapters.SectionsPageAdapter;
import com.example.david.gigfinder.tools.ColorTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "APPLOG - MainActivity";

    public static String idToken;
    public static int userId;
    private String user;

    private SharedPreferences sharedPreferences;

    private SectionsPageAdapter sectionsPageAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(getString(R.string.shared_prefs), Context.MODE_PRIVATE);
        idToken = getIntent().getExtras().getString("idToken");

        user = sharedPreferences.getString("user", "none");

        if(idToken.equals("offline")){
            initGui();
        }else {
            if (user.equals("none")) {
                GetUser getUser = new GetUser();
                getUser.execute();
            } else {
                checkSharedPrefs(user);
            }
        }
    }

    /**
     * Checks if the SharedPrefs have the UserProfile, Genres, SocialMedia
     * @param user
     */
    private void checkSharedPrefs(String user){

        //Check if UserProfile is missing
        if(sharedPreferences.getString("userProfile", "x").equals("x")){
            if(user.equals("artist")) {
                GetArtist getArtist = new GetArtist();
                getArtist.execute();
            }else{
                GetHost getHost = new GetHost();
                getHost.execute();
            }
        }else{
            initGui();
        }

        //Check if Genres is missing
        if(sharedPreferences.getString("genres", "x").equals("x")){
            GetGenres getGenres = new GetGenres();
            getGenres.execute();
        }

        //Check if Social Media is missing
        if (sharedPreferences.getString("social medias", "x").equals("x")){
            GetSocialMedias getSocialMedias = new GetSocialMedias();
            getSocialMedias.execute();
        }
    }

    /**
     * Initalizes the Gui
     */
    private void initGui(){

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
            EventsFragment eventsFragment = new EventsFragment();
            ChatFragment chatFragment = new ChatFragment();
            HostProfileFragment hostProfileFragment = new HostProfileFragment();

            exploreFragment.setArguments(args);
            hostProfileFragment.setArguments(args);
            eventsFragment.setArguments(args);
            chatFragment.setArguments(args);

            sectionsPageAdapter.addFragment(exploreFragment, getString(R.string.nav_explore));
            sectionsPageAdapter.addFragment(eventsFragment, getString(R.string.nav_events));
            sectionsPageAdapter.addFragment(chatFragment, getString(R.string.nav_chat));
            sectionsPageAdapter.addFragment(hostProfileFragment, getString(R.string.nav_profile));
        }

        viewPager.setAdapter(sectionsPageAdapter);
    }

    /**
     * Gets artist object from Server and stores it in SharedPrefs
     */
    class GetArtist extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/artists");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Authorization", idToken);
                urlConnection.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return response.toString();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "USER PROFILE: " + result);
            sharedPreferences.edit().putString("userProfile", result).apply();
            initGui();
        }
    }

    /**
     * Gets host object from Server and stores it in SharedPrefs
     */
    class GetHost extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/hosts");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Authorization", idToken);
                urlConnection.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return response.toString();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "USER PROFILE: " + result);
            sharedPreferences.edit().putString("userProfile", result).apply();
            initGui();
        }
    }

    /**
     * Gets genres from Server and stores them in SharedPrefs
     */
    class GetUser extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/user");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Authorization", idToken);
                urlConnection.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return response.toString();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "GetUser: " + result);
            try {
                JSONObject userJson = new JSONObject(result);
                if(userJson.get("artist").equals(null)){
                    sharedPreferences.edit().putString("user", "host").apply();
                    Log.d(TAG, "GetUser: " + "host");
                    user = "host";
                }else{
                    sharedPreferences.edit().putString("user", "artist").apply();
                    Log.d(TAG, "GetUser: " + "artist");
                    user = "artist";
                }
                checkSharedPrefs(user);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets genres from Server and stores them in SharedPrefs
     */
    class GetGenres extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/genres");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Authorization", idToken);
                urlConnection.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return response.toString();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "GENRES: " + result);
            sharedPreferences.edit().putString("genres", result).apply();
        }
    }

    /**
     * Gets social medias from Server and stores them in SharedPrefs
     */
    class GetSocialMedias extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/socialmedias");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Authorization", idToken);
                urlConnection.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return response.toString();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "Social Medias: " + result);

            SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.shared_prefs), MODE_PRIVATE).edit();
            editor.putString("social medias", result);
            editor.apply();
        }
    }
}
