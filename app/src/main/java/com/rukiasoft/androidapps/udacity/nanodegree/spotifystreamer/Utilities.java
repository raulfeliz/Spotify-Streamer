package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * Created by Raúl Feliz on 2014 for the Udacity Nanodegree.
 */
class Utilities {
    static private Toast toast = null;

    /**
     * Show Toas. If a Toast is shown, replaces the text instead of creating a new one. In case of multiple Toast
     * in a row, only one with several texts will be shown, instead of a lot of them sequentially;
     * @param context
     * @param text
     */
    public static void showToast(final Context context, final String text) {
        //only show if context is an instance of Activity
        if (!(context instanceof Activity)) {
            return;
        }
        try {
            if (toast != null) {
                if (toast.getView().isShown()) {
                    toast.setText(text);
                    toast.show();
                    return;
                }
            }
            toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
            toast.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Force to hide keyboard if showing
     * @param activity
     */
    public static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

