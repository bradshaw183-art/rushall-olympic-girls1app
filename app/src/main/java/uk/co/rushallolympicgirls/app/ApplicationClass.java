package uk.co.rushallolympicgirls.app;

import android.app.Application;

/** Initializes notifications before any Activity or notification deep link opens. */
public class ApplicationClass extends Application {
    @Override public void onCreate() {
        super.onCreate();
        OneSignalManager.getInstance().initialize(this);
    }
}
