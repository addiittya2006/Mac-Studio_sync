package se.popcorn_time.base.analytics;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import se.popcorn_time.base.utils.Logger;

public class Analytics {

    private static final Analytics INSTANCE = new Analytics();

    private GoogleAnalytics googleAnalytics;
    private Tracker tracker;

    private Analytics() {

    }

    public static void init(Context context, String trackerId, boolean dryRun) {
        if (TextUtils.isEmpty(trackerId)) {
            Logger.error("Analytics: Initialization error. Tracker id is empty.");
            return;
        }

        INSTANCE.googleAnalytics = GoogleAnalytics.getInstance(context);
        INSTANCE.googleAnalytics.setDryRun(dryRun);
        INSTANCE.googleAnalytics.setLocalDispatchPeriod(1800);

        INSTANCE.tracker = INSTANCE.googleAnalytics.newTracker(trackerId);
        INSTANCE.tracker.setAnonymizeIp(dryRun);
        INSTANCE.tracker.enableExceptionReporting(true);
        INSTANCE.tracker.enableAutoActivityTracking(true);
        INSTANCE.tracker.enableAdvertisingIdCollection(false);
        Logger.debug("Analytics: Initialization completed");
    }

    @Nullable
    public static Tracker getTracker() {
        return INSTANCE.tracker;
    }
}