package uk.co.rushallolympicgirls.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.onesignal.Continue;
import com.onesignal.OneSignal;
import com.onesignal.user.subscriptions.IPushSubscriptionObserver;
import com.onesignal.user.subscriptions.PushSubscriptionChangedState;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/** Single integration boundary for all OneSignal SDK operations. */
public final class OneSignalManager {
    private static final String APP_ID = "351b0852-3cfa-44c2-8e7f-0d41b3a8d32d";
    private static final OneSignalManager INSTANCE = new OneSignalManager();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean dialogShown = new AtomicBoolean(false);
    // OneSignal keeps weak observer references, so retain this for the app lifetime.
    private IPushSubscriptionObserver subscriptionObserver;

    private OneSignalManager() {}

    public static OneSignalManager getInstance() { return INSTANCE; }

    public void initialize(Context context) {
        if (!initialized.compareAndSet(false, true)) return;
        Context appContext = context.getApplicationContext();
        executor.execute(() -> OneSignal.initWithContext(appContext, APP_ID));
    }

    public void setupPushSubscriptionObserver(Activity activity) {
        subscriptionObserver = new IPushSubscriptionObserver() {
            @Override public void onPushSubscriptionChange(PushSubscriptionChangedState state) {
                maybeShowReadyDialog(activity, state.getCurrent().getId());
            }
        };
        OneSignal.getUser().getPushSubscription().addObserver(subscriptionObserver);
        maybeShowReadyDialog(activity, OneSignal.getUser().getPushSubscription().getId());
    }

    private boolean isServerSubscription(String id) {
        return id != null && !id.isEmpty() && !id.startsWith("local-");
    }

    private void maybeShowReadyDialog(Activity activity, String subscriptionId) {
        if (!isServerSubscription(subscriptionId) || !dialogShown.compareAndSet(false, true)) return;
        new Handler(Looper.getMainLooper()).post(() -> {
            if (activity.isFinishing() || activity.isDestroyed()) return;
            new AlertDialog.Builder(activity)
                .setTitle("Stay updated with the team")
                .setMessage("Receive fixtures, training changes and important Rushall Olympic Girls club updates.")
                .setPositiveButton("Enable notifications", (dialog, which) -> requestPermission())
                .setNegativeButton("Not now", null)
                .show();
        });
    }

    public void requestPermission() {
        OneSignal.getNotifications().requestPermission(true, Continue.none());
    }

    public void login(String externalId) { OneSignal.login(externalId); }
    public void logout() { OneSignal.logout(); }
    public void setTeam(String team) { OneSignal.getUser().addTag("team", team); }
    public void setRole(String role) { OneSignal.getUser().addTag("role", role); }
}
