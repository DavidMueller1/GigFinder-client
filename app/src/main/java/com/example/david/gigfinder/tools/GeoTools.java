package com.example.david.gigfinder.tools;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class GeoTools {
    private static final String TAG = "GeoTools";

    public static String getAddressFromLatLng(Context context, LatLng pos) {
        // Region convert coordinates to address
        String addressString = "Keine Addresse gefunden";

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(pos.latitude, pos.longitude, 1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            Log.d(TAG, "Network error");

        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            Log.d(TAG, "Coordinate error");

        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            Log.d(TAG, "No Address found");
        }
        else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using getAddressLine
            for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }

            addressString = address.getAddressLine(0);
        }
        //endregion

        return addressString;
    }
}
