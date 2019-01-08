package com.example.david.gigfinder.tools;

import android.graphics.Color;
import android.util.Log;

public abstract class ColorTools {
    private static final String TAG = "ColorTools";
    /**
     * Checks whether a background color is light enough to use black font
     * @param color
     * @return is the color a bright
     */
    public static int isBrightColor(int color) {
        int[] rgb = {Color.red(color), Color.green(color), Color.blue(color)};
        Log.d(TAG, rgb[0] + "");
        // formula for the brightness (returns a value between 0 and 255)
        int brightness = (int) Math.sqrt(rgb[0] * rgb[0] * .299 + rgb[1] * rgb[1] * .587 + rgb[2] * rgb[2] * .114);

        if (brightness >= 110) {
            return Color.BLACK;
        }

        return Color.WHITE;
    }
}
