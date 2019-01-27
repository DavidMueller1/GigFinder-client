package com.example.david.gigfinder.tools;

import android.graphics.Color;
import android.util.Log;

public abstract class ColorTools {
    private static final String TAG = "ColorTools";

    private static final int MIN_BRIGHTNESS = 175;
    private static final int MIN_SUPER_BRIGHTNESS = 225;
    private static final float DELTA_BRIGHTNESS_FOR_SECONDARY = 0.15f;


    /**
     * Checks whether a background color is light enough to use black font
     * @param color
     * @return is the color a bright
     */
    public static int isBrightColor(int color) {
        if (isBrightColorBool(color)) {
            return Color.BLACK;
        }
        return Color.WHITE;
    }

    public static boolean isBrightColorBool(int color) {
        int[] rgb = {Color.red(color), Color.green(color), Color.blue(color)};
        Log.d(TAG, rgb[0] + "");
        // formula for the brightness (returns a value between 0 and 255)
        int brightness = (int) Math.sqrt(rgb[0] * rgb[0] * .299 + rgb[1] * rgb[1] * .587 + rgb[2] * rgb[2] * .114);

        if (brightness >= MIN_BRIGHTNESS) {
            return true;
        }

        return false;
    }

    public static boolean isSuperBrightColorBool(int color) {
        int[] rgb = {Color.red(color), Color.green(color), Color.blue(color)};
        Log.d(TAG, rgb[0] + "");
        // formula for the brightness (returns a value between 0 and 255)
        int brightness = (int) Math.sqrt(rgb[0] * rgb[0] * .299 + rgb[1] * rgb[1] * .587 + rgb[2] * rgb[2] * .114);

        if (brightness >= MIN_SUPER_BRIGHTNESS) {
            return true;
        }

        return false;
    }

    /**
     * calculates the secondary color which is slightly darker or brighter depending on the input color brightness
     * @param color the primary color
     * @return the secondary color
     */
    public static int getSecondaryColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        if(hsv[2] < DELTA_BRIGHTNESS_FOR_SECONDARY) {
            hsv[2] += DELTA_BRIGHTNESS_FOR_SECONDARY;
        }
        else {
            hsv[2] -= DELTA_BRIGHTNESS_FOR_SECONDARY;
        }

        return Color.HSVToColor(hsv);
    }
}
