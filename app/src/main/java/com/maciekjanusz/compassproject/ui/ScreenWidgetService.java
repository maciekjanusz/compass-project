package com.maciekjanusz.compassproject.ui;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.maciekjanusz.compassproject.sensor.Compass;
import com.maciekjanusz.compassproject.navigation.NavigationBundle;
import com.maciekjanusz.compassproject.navigation.ServiceMessage;
import com.maciekjanusz.compassproject.navigation.ServiceState;
import com.maciekjanusz.draglayout.DragLayout;

import de.greenrobot.event.EventBus;

import static com.maciekjanusz.compassproject.preferences.AppPreferences.WIDGET_SERVICE_RUNNING;
import static com.maciekjanusz.compassproject.util.CompassMath.adjustBearing;

public class ScreenWidgetService extends Service implements DragLayout.DragListener, Compass.CompassListener {

    private static final int SIZE_DP = 96;
    private static final int WIDGET_SCALE_LINES = 72;

    private WindowManager.LayoutParams params;
    private WindowManager windowManager;
    private DragLayout dragLayout;
    private CompassView compassView;
    private Compass compass;

    private float currentRoll;
    private int rotation;
    private boolean started;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        compass = new Compass(this, this);

        initParams();
        initViews();
    }

    private void initViews() {
        int sizePx = dpToPx(SIZE_DP);

        windowManager = ((WindowManager) getApplicationContext().
                getSystemService(Context.WINDOW_SERVICE));
        rotation = windowManager.getDefaultDisplay().getRotation();
        // init compass view
        compassView = new CompassView(this);
        compassView.setCompassEnabled(true);
        compassView.setHasBackground(true);
        compassView.setStrokeWidth(0);
        compassView.setScaleLines(WIDGET_SCALE_LINES);
        compassView.setLayoutParams(new LinearLayout.LayoutParams(sizePx, sizePx));

        // init dragLayout & add compass view
        dragLayout = new DragLayout(this);
        dragLayout.addView(compassView);
        dragLayout.setDragListener(this);
        dragLayout.setInitialPosition(params.x, params.y);
        dragLayout.setScale(-0.25f);

        dragLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent activityIntent = new Intent(ScreenWidgetService.this, CompassActivity.class);
                activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(activityIntent);
            }
        });
    }

    private void initParams() {
        int paramFlags = WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            paramFlags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        }

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                paramFlags,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!started) {
            compass.start();
            // check navigation service status
            EventBus.getDefault().register(this);
            EventBus.getDefault().post(ServiceMessage.REQUEST_NAVIGATION_SERVICE_STATE);

            // display compass on screen
            displayOverlayView(params);

            saveRunningPref(true);
            started = true;
        }
        return START_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // update screen rotation
        rotation = windowManager.getDefaultDisplay().getRotation();
    }

    @Override
    public void onDestroy() {
        compass.stop();
        removeOverlayView();
        EventBus.getDefault().unregister(this);
        saveRunningPref(false);
        started = false;
        super.onDestroy();
    }

    private void saveRunningPref(boolean running) {
        PreferenceManager
                .getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(WIDGET_SERVICE_RUNNING, running)
                .commit();
    }

    private void displayOverlayView(WindowManager.LayoutParams params) {
        if(dragLayout != null && !(dragLayout.isShown())) {
            windowManager.addView(dragLayout, params);
        }
    }

    private void removeOverlayView() {
        if(dragLayout != null && dragLayout.isShown()) {
            windowManager.removeView(dragLayout);
        }
    }

    @Override
    public void onDragFinished(float x, float y) {
        updateViewPosition(x, y);
    }

    @Override
    public void onDrag(float x, float y) {
        updateViewPosition(x, y);
    }

    @Override
    public void onDragStarted(float x, float y) {
        updateViewPosition(x, y);
    }

    private void updateViewPosition(float x, float y) {
        params.x = (int) x;
        params.y = (int) y;
        windowManager.updateViewLayout(dragLayout, params);
    }

    @Override
    public void onCompassStateChanged(float bearing, float pitch, float roll) {
        currentRoll = roll;
        bearing = adjustBearing(bearing, roll, rotation);
        compassView.setCompassBearing(-bearing);
        compassView.invalidate();
    }

    @SuppressWarnings("unused")
    public void onEvent(NavigationBundle navigationBundle) {
        float bearing = navigationBundle.getBearing();
        bearing = adjustBearing(bearing, currentRoll, 0);
        compassView.setNavigationBearing(bearing);
        compassView.setNavigationEnabled(true);
    }

    @SuppressWarnings("unused")
    public void onEvent(@NonNull ServiceState serviceState) {
        switch(serviceState) {
            case NAVIGATION_RUNNING:
                // nothing
                break;
            case NAVIGATION_STOPPED:
                compassView.setNavigationEnabled(false);
                break;
        }
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
