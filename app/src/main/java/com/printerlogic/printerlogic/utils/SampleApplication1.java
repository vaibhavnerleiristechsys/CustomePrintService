package com.printerlogic.printerlogic.utils;

import android.app.Application;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.multidex.MultiDex;
import androidx.navigation.fragment.NavHostFragment;

import com.datadog.android.BuildConfig;
import com.datadog.android.Datadog;
import com.datadog.android.core.configuration.Configuration;
import com.datadog.android.core.configuration.Credentials;
import com.datadog.android.privacy.TrackingConsent;
import com.datadog.android.rum.tracking.ComponentPredicate;
import com.datadog.android.rum.tracking.FragmentViewTrackingStrategy;
import com.datadog.android.log.Logger;

import java.util.Arrays;
import java.util.List;

public class SampleApplication1 extends Application {
    private Logger logger;

    private final List<String> tracedHosts = Arrays.asList("datadoghq.com",
            "127.0.0.1");

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializeDatadog();
    }

    private void initializeDatadog() {
        Datadog.initialize(
                this,
                createDatadogCredentials(),
                createDatadogConfiguration(),
                TrackingConsent.GRANTED
        );
        Datadog.setVerbosity(Log.VERBOSE);


        initializeLogger();
       
    }

    private Credentials createDatadogCredentials() {
        return new Credentials(
                "pub44227d305d57f155395314dc2415aff2",
                BuildConfig.BUILD_TYPE,
                "admin",
                "com.example.customeprintservice",
                "VasionPrint"
        );
    }

    private Configuration createDatadogConfiguration() {

        final Configuration.Builder configBuilder = new Configuration.Builder(
                true,
                true,
                true,
                true
        ).setFirstPartyHosts(tracedHosts)
                .useViewTrackingStrategy(
                        new FragmentViewTrackingStrategy(true,
                                new ComponentPredicate<Fragment>() {
                                    @Override
                                    public boolean accept(Fragment component) {
                                        return !NavHostFragment.class.isAssignableFrom(
                                                component.getClass());
                                    }

                                    @Override
                                    public String getViewName(Fragment component) {
                                        return component.getClass().getSimpleName();
                                    }
                                })
                )
                .trackInteractions();



        return configBuilder.build();
    }

    private void initializeLogger() {
        // Initialise Logger
        logger = new Logger.Builder()
                .setLogcatLogsEnabled(true)
                .setNetworkInfoEnabled(true)
                .setLoggerName("Application")
                .build();
        logger.v("Created a logger");

    }

    public Logger getLogger() {
        return logger;
    }

    public static SampleApplication1 fromContext(Context context) {
        return (SampleApplication1) context.getApplicationContext();
    }

}
