package com.example.david.gigfinder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

public class ExploreFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "APPLOG - ExploreFragment";

    String idToken;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Boolean mLocationPermissionsGranted = false;
    private static final float DEFAULT_ZOOM = 12;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_explore, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        idToken = getArguments().getString("idToken");
        if(idToken.equals("offline")) {
            //offline mode
            offlineMode();
        }else{
            //online mode
            getLocationPermission();
            GetEvents getEvents = new GetEvents();
            getEvents.execute();
        }
        super.onActivityCreated(savedInstanceState);
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
        try {
            JSONArray jsonArray = new JSONArray(events);
            for(int i=0; i<jsonArray.length(); i++){
                dropMarker(jsonArray.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void dropMarker(JSONObject event){
        try {
                LatLng testMarker = new LatLng(event.getDouble("latitude"), event.getDouble("longitude"));//TODO Get right Location
                Marker myMarker = mMap.addMarker(new MarkerOptions()
                        .position(testMarker)
                        .title(event.getString("title"))
                        .snippet(event.getString("description")));
                myMarker.setTag(event);
            } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    /**
     *
     */
    class GetEvents extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/events?location=" + 48.150960 + "," + 11.580820 + "&radius=10000.0"); //TODO: latlng
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Authorization", idToken); //TODO idToken
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
        protected void onPostExecute(String result){
            Log.d(TAG, "EVENTS: " + result);
            showEvents(result);
        }
    }
}
