package com.hypodiabetic.happplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Spinner;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;

import io.realm.RealmResults;
import layout.AdapterRealmList;

/**
 * Created by Tim on 02/01/2017.
 * Shared Utility Functions
 */

public class Utilities {

    private final static String TAG = "Utilities";

    public static String displayAge(Date timestamp) {
        int minutesAgo = (int) Math.floor(getDiffInMins(timestamp, new Date()));
        switch (minutesAgo){
            case 0:
                return MainApp.getInstance().getString(R.string.time_just_now);
            case 1:
                return minutesAgo + " " + MainApp.getInstance().getString(R.string.time_min_ago);
            default:
                return minutesAgo + " " + MainApp.getInstance().getString(R.string.time_mins_ago);
        }
    }

    public static String displayInsulin(Double value, int decPoints){
        return round(value,decPoints) + "u";
    }

    public static String displayPosNeg(Double value){
        if (value > 0 ){
            return "+";
        } else if (value < 0){
            return "-";
        } else {
            return "";
        }
    }

    public static double getDiffInMins(Date timestampFrom, Date timestampTo) {
        return (timestampTo.getTime() - timestampFrom.getTime()) /(1000*60);
    }


    public static Double round(Double value, int decPoints){
        if (value == null || value.isInfinite() || value.isNaN()) return 0D;
        DecimalFormat df;
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');

        switch (decPoints){
            case 1:
                if (getPrecisionRounding()){
                    df = new DecimalFormat("##0.00", otherSymbols);
                } else {
                    df = new DecimalFormat("##0.0", otherSymbols);
                }
                break;
            case 2:
                df = new DecimalFormat("##0.00", otherSymbols);
                break;
            case 3:
                df = new DecimalFormat("##0.000", otherSymbols);
                break;
            default:
                df = new DecimalFormat("##0.0000", otherSymbols);
        }
        return Double.parseDouble(df.format(value));
    }

    public static Double stringToDouble(String string){
        //Used to support locations where , is used as decimal separator
        if (string == null) {
            Log.e(TAG, "stringToDouble Null value!");
            return 0.0;
        }
        if (string.equals("")) {
            Log.e(TAG, "stringToDouble Empty value!");
            return 0.0;
        }

        String valueWithDot = string.replaceAll(",",".");

        try {
            if(valueWithDot.equals("")) {
                return 0.0;
            } else {
                return Double.valueOf(valueWithDot);
            }
        } catch (NumberFormatException e2)  {
            // This happens if we're trying (say) to parse a string that isn't a number, as though it were a number!
            // If this happens, it should only be due to application logic problems.
            // In this case, the safest thing to do is return 0, having first fired-off a log warning.
            Log.e(TAG, "Warning: Value is not a number" + string);
            //Crashlytics.logException(e2);
            return 0.0;
        }
    }


    //returns if precision_rounding is enabled or not
    public static Boolean getPrecisionRounding(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainApp.getInstance());
        return prefs.getBoolean("sys_precision_rounding", false);
    }

    //returns the location of an item in a spinner
    public static int getIndex(Spinner spinner, String myString){
        int index = -1;
        for (int i=0;i<spinner.getCount();i++){
            //Log.e("TEST", "getIndex: " + spinner.getItemAtPosition(i).toString());
            if (spinner.getItemAtPosition(i).toString().equals(myString)){
                index = i;
            }
        }
        return index;
    }

    public static Date getDateHoursAgo(int hours){
        return new Date(new Date().getTime() - ((60000 * 60 * hours)));
    }
}
