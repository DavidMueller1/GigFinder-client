package com.example.david.gigfinder;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;

import com.example.david.gigfinder.tools.Utils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ExploreFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "APPLOG - ExploreFragment";

    SharedPreferences sharedPreferences;
    String idToken;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Boolean mLocationPermissionsGranted = false;
    private static final float DEFAULT_ZOOM = 9;

    private String user;
    private int userId;

    private String[] genreStrings;
    private JSONArray genresFromServer;
    private ArrayList<String> myGenres;
    private boolean[] checkedGenres;

    private ArrayList<Marker> markers = new ArrayList<Marker>();

    private boolean showOldEvents = false;
    private boolean onlyEventsByFavs = false;
    private boolean onlyMyEvents = false;

    private ImageButton filterBtn;
    private PopupWindow popupWindow;

    private boolean fragmentLoaded = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_explore, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        sharedPreferences = getContext().getSharedPreferences(getString(R.string.shared_prefs), Context.MODE_PRIVATE);
        idToken = getArguments().getString("idToken");
        filterBtn = getView().findViewById(R.id.filter_btn);
        user = sharedPreferences.getString("user", "");
        userId = sharedPreferences.getInt("userId", 1);

        if(idToken.equals("offline")) {
            //offline mode
            offlineMode();
        }else{
            //online mode
            showGenres();
            getLocationPermission();
            GetEvents getEvents = new GetEvents();
            getEvents.execute();
            initMenu();
            filterBtn.setOnClickListener(filterOnClick());
        }
        super.onActivityCreated(savedInstanceState);

        reloadAnimation(true);
        fragmentLoaded = true;
    }

    private void initMenu() {

        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_filter_layout, null);

        popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(false);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                popupWindow.setFocusable(false);
            }
        });

        Switch expiredEventsSwitch = popupView.findViewById(R.id.expiredEventsSwitch);
        Switch eventsBySwitch = popupView.findViewById(R.id.eventsByFavsSwitch);
        Switch myEventsSwitch = popupView.findViewById(R.id.eventsByMeSwitch);
        TextView filterByGenreTxt = popupView.findViewById(R.id.filterByGenreTxt);

        expiredEventsSwitch.setChecked(showOldEvents);
        expiredEventsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    showOldEvents = true;
                    GetEvents getEvents = new GetEvents();
                    getEvents.execute();
                }else{
                    showOldEvents = false;
                    GetEvents getEvents = new GetEvents();
                    getEvents.execute();
                }
            }
        });

        if(user.equals("artist")) {

            myEventsSwitch.setVisibility(View.GONE);

            eventsBySwitch.setChecked(onlyEventsByFavs);
            eventsBySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        onlyEventsByFavs = true;
                        GetEvents getEvents = new GetEvents();
                        getEvents.execute();
                    } else {
                        onlyEventsByFavs = false;
                        GetEvents getEvents = new GetEvents();
                        getEvents.execute();
                    }
                }
            });

        }else {

            eventsBySwitch.setVisibility(View.GONE);

            myEventsSwitch.setChecked(onlyEventsByFavs);
            myEventsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        onlyMyEvents = true;
                        GetEvents getEvents = new GetEvents();
                        getEvents.execute();
                    } else {
                        onlyMyEvents = false;
                        GetEvents getEvents = new GetEvents();
                        getEvents.execute();
                    }
                }
            });

        }
        filterByGenreTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterByGenres();
            }
        });
    }

    private void showGenres(){
        myGenres = new ArrayList<String>();
        try {
            genresFromServer = new JSONArray(sharedPreferences.getString("genres", ""));
            genreStrings = new String[genresFromServer.length()];
            checkedGenres = new boolean[genreStrings.length];
            for(int i=0; i<genresFromServer.length(); i++){
                genreStrings[i] = genresFromServer.getJSONObject(i).getString("value");
                checkedGenres[i] = true;
                myGenres.add(genresFromServer.getJSONObject(i).getString("value"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Map Ready!");
        mMap = googleMap;

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                JSONObject event = (JSONObject) marker.getTag();
                Intent intent = new Intent(getActivity(), EventProfileActivity.class);
                intent.putExtra("idToken", idToken);
                intent.putExtra("Event", event.toString());
                Log.d(TAG, event.toString());
                startActivity(intent);
            }
        });

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getLocationPermission();
            return;
        }
        mMap.setMyLocationEnabled(true);

        if (mLocationPermissionsGranted) {
            Log.d(TAG, "Getting device location!");
            getDeviceLocation();
        }
    }

    private View.OnClickListener filterOnClick(){
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!popupWindow.isFocusable()) {
                    popupWindow.setFocusable(true);
                    popupWindow.showAsDropDown(v);
                }else {
                    popupWindow.dismiss();
                }
            }
        };
        return onClickListener;
    }

    //TODO
    private void filterByGenres() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext());
        mBuilder.setTitle(getString(R.string.filtern_by_genre_title));

        final ArrayList<String> selectedGenres = myGenres;

        mBuilder.setMultiChoiceItems(genreStrings, checkedGenres, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                //unchecking
                if (isChecked) {
                    selectedGenres.add(genreStrings[which].toString());
                } else {
                    selectedGenres.remove(genreStrings[which].toString());
                }
            }
        });

        mBuilder.setPositiveButton(getString(R.string.registration_genre_picker_positive), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myGenres = selectedGenres;
                GetEvents getEvents = new GetEvents();
                getEvents.execute();
                popupWindow.dismiss();
            }
        });

        /*
        mBuilder.setNeutralButton("Alle Genres", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for(int i=0; i<checkedGenres.length; i++){
                    checkedGenres[i] = true;
                    //((AlertDialog) dialog).getListView().setItemChecked(i, true);
                }
            }
        });


        mBuilder.setNegativeButton(getString(R.string.registration_genre_picker_negative), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        */

        AlertDialog dialog = mBuilder.create();
        dialog.show();
    }

    /**
     * Gets the location of the device and moves the camera
     */
    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        try{
            if(mLocationPermissionsGranted){
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful() && task.getResult() != null){
                            Log.d(TAG, "Found location!");
                            Location currentLocation = (Location) task.getResult();
                            Log.d(TAG, String.valueOf(currentLocation.getLatitude()));
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);

                        }
                        else{
                            Log.d(TAG, "Could not find location!");
                        }
                    }
                });
            }
        }catch(SecurityException e){
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Moves the map do a location
     * @param latLng
     * @param zoom
     */
    private void moveCamera(LatLng latLng, float zoom){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    /**
     * Checks the permission for Locations and External Storage
     */
    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(getActivity(), permissions, 1234);
            }
        } else {
            ActivityCompat.requestPermissions(getActivity(), permissions, 1234);
        }
    }

    @Override
    /**
     * Checks if the permission is granded, if true calls initMap()
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;

        switch (requestCode){
            case 1234:{
                if(grantResults.length>0){
                    for(int i=0; i<grantResults.length; i++){
                        if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionsGranted = true;
                    initMap();
                }
            }
        }
    }

    /**
     * Initalizes the Map
     */
    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(ExploreFragment.this);
    }

    /**
     * Checks internet connection and starts offline mode
     */
    private void offlineMode(){
        if(isNetworkAvailable()){
            //Go back to Login
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        }else{
            //Show Popup
        }
    }

    /**
     * Checks if Network is Available
     * @return True if there is an Internet Connection
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showEvents(String events){
        for(Marker i : markers){
            i.remove();
        }
        markers.clear();
        try {
            JSONArray jsonArray = new JSONArray(events);
            for(int i=0; i<jsonArray.length(); i++){
                dropMarker(jsonArray.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void filterEvents(String events){
        String favs = sharedPreferences.getString("favorites", "null");
        try {
            JSONArray jsonArray = new JSONArray(events);
            JSONArray newArray = new JSONArray();
            for(int i=0; i<jsonArray.length(); i++){
                if(!showOldEvents){
                    String end = jsonArray.getJSONObject(i).getString("end");
                    if(Utils.convertStringToDate(end).before(Calendar.getInstance().getTime())){
                        continue;
                    }
                }
                if (user.equals("artist") && onlyEventsByFavs){
                    int id = jsonArray.getJSONObject(i).getInt("hostId");
                    if(!Utils.isUserInFavorites(id, favs)){
                        continue;
                    }
                }
                if(user.equals("host") && onlyMyEvents){
                    int id = jsonArray.getJSONObject(i).getInt("hostId");
                    if(id != userId){
                        continue;
                    }
                }
                if(!eventFitsGenres(jsonArray.getJSONObject(i).getJSONArray("eventGenres"))){
                    continue;
                }
                newArray.put(jsonArray.getJSONObject(i));
            }
            Log.d(TAG, "!!! NEW EVENTS:" + newArray.toString());
            showEvents(newArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean eventFitsGenres(JSONArray eventGenres){
        String genres = sharedPreferences.getString("genres", "");
        for(int i=0; i<eventGenres.length(); i++){
            try {
                String thisGenres = Utils.genreIdToString(eventGenres.getJSONObject(i).getInt("genreId"), genresFromServer.toString());
                if(myGenres.contains(thisGenres)){
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void dropMarker(JSONObject event){
        try {
                LatLng testMarker = new LatLng(event.getDouble("latitude"), event.getDouble("longitude"));//TODO Get right Location
                Marker myMarker = mMap.addMarker(new MarkerOptions()
                        .position(testMarker)
                        .title(event.getString("title"))
                        .snippet(event.getString("description")));
                myMarker.setTag(event);
                markers.add(myMarker);
            } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    private void reloadAnimation(boolean isReloading) {
        if(isReloading) {
            RotateAnimation rotateAnimation = new RotateAnimation(0, -360, Animation.RELATIVE_TO_SELF, 0.565f, Animation.RELATIVE_TO_SELF, 0.53f);
            rotateAnimation.setRepeatCount(Animation.INFINITE);
            rotateAnimation.setDuration(1000);
            rotateAnimation.setInterpolator(new LinearInterpolator());
            getView().findViewById(R.id.explore_reload).startAnimation(rotateAnimation);
        }
        else {
            getView().findViewById(R.id.explore_reload).clearAnimation();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (fragmentLoaded && isVisibleToUser && sharedPreferences.getBoolean("reloadExplore", false)) {
            GetEvents getEvents = new GetEvents();
            getEvents.execute();
        }
    }

    /**
     *
     */
    private class GetEvents extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/events?location=" + 48.150960 + "," + 11.580820 + "&radius=10000.0"); //TODO: latlng
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Authorization", idToken);
                urlConnection.setRequestMethod("GET");
                urlConnection.setUseCaches(false);

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
        protected void onPostExecute(String result){
            Log.d(TAG, "EVENTS: " + result);
            filterEvents(result);
        }
    }
}
