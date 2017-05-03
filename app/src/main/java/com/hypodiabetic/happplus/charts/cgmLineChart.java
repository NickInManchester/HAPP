package com.hypodiabetic.happplus.charts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.hypodiabetic.happplus.Events.SGVEvent;
import com.hypodiabetic.happplus.Intents;
import com.hypodiabetic.happplus.MainApp;
import com.hypodiabetic.happplus.R;
import com.hypodiabetic.happplus.UtilitiesDisplay;
import com.hypodiabetic.happplus.UtilitiesTime;
import com.hypodiabetic.happplus.plugins.PluginManager;
import com.hypodiabetic.happplus.plugins.devices.CGMDevice;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by Tim on 31/01/2017.
 * CGM Readings Line chart with APS Predicted BG(s)
 */

public class cgmLineChart extends AbstractFragmentLineChart {

    private BroadcastReceiver mCGMNewCGMReading;
    private final static String TAG   =   "cgmLineChart";

    //Create a new instance of this Fragment
    public static cgmLineChart newInstance(Integer numHours, String title, String summary, String yAxisLDesc, @ColorInt int lineColour) {
        cgmLineChart fragment = new cgmLineChart();

        Bundle args = new Bundle();
        args.putInt(ARG_NUM_HOURS, numHours);
        args.putInt(ARG_LINE_COLOUR, lineColour);
        args.putString(ARG_YAXIS_DESC, yAxisLDesc);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_SUMMARY, summary);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart(){
        super.onStart();

        //CGM Readings Line Chart
        CGMDevice deviceCGM = (CGMDevice) PluginManager.getPluginByClass(CGMDevice.class);
        LineChart cgmLineChart  =   this.getChart();

        if (deviceCGM != null) {
            if (deviceCGM.getIsLoaded() && cgmLineChart != null) {
                //DataSet
                List<SGVEvent> cgmReadings = deviceCGM.getReadingsSince(UtilitiesTime.getDateHoursAgo(new Date(), 8));
                //cgmReadings = cgmReadings.sort("timestamp", Sort.ASCENDING); // TODO: 03/05/2017 still needs sorting?
                List<Entry> entries = new ArrayList<>();
                for (SGVEvent sgvEvent : cgmReadings) {
                    entries.add(new Entry((float) sgvEvent.getTimeStamp().getTime(), sgvEvent.getSGV().floatValue()));
                }
                LineDataSet cgmReadingsDataSet = new LineDataSet(entries, "label");

                //yAxis
                YAxis yAxisL = cgmLineChart.getAxisLeft();
                yAxisL.setValueFormatter(new IAxisValueFormatter() {
                    CGMDevice deviceCGM = (CGMDevice) PluginManager.getPluginByClass(CGMDevice.class);

                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        return UtilitiesDisplay.sgv((double) value, false, false, deviceCGM.getPref(CGMDevice.PREF_BG_UNITS).getStringValue());
                    }
                });
                yAxisL.setAxisMaximum(200);
                yAxisL.setAxisMinimum(20);
                LimitLine cgmReadingsMaxLine = new LimitLine(150);
                cgmReadingsMaxLine.setLineColor(ContextCompat.getColor(getContext(), R.color.colorCGMMaxLine));
                yAxisL.addLimitLine(cgmReadingsMaxLine);
                LimitLine cgmReadingsMinLine = new LimitLine(50);
                cgmReadingsMinLine.setLineColor(ContextCompat.getColor(getContext(), R.color.colorCGMMinLine));
                yAxisL.addLimitLine(cgmReadingsMinLine);

                this.renderChart(cgmReadingsDataSet);
            }
        } else {
            Log.d(TAG, "onStart: could not find Device CGM");
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        registerReceivers();
    }

    @Override
    public void onPause(){
        super.onPause();
        if (mCGMNewCGMReading != null)  LocalBroadcastManager.getInstance(MainApp.getInstance()).unregisterReceiver(mCGMNewCGMReading);
    }

    private void registerReceivers() {
        mCGMNewCGMReading = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refreshChart();
            }
        };
        LocalBroadcastManager.getInstance(MainApp.getInstance()).registerReceiver(mCGMNewCGMReading, new IntentFilter(Intents.newLocalEvent.NEW_LOCAL_EVENT_SGV));
    }

}
