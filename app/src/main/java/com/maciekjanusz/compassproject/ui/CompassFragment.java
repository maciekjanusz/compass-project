package com.maciekjanusz.compassproject.ui;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.maciekjanusz.compassproject.R;
import com.maciekjanusz.compassproject.input.LocationInputDialogFragment;
import com.maciekjanusz.compassproject.navigation.NavigationBundle;
import com.maciekjanusz.compassproject.navigation.NavigationService;
import com.maciekjanusz.compassproject.navigation.ServiceMessage;
import com.maciekjanusz.compassproject.navigation.ServiceState;
import com.maciekjanusz.compassproject.sensor.Compass;
import com.maciekjanusz.compassproject.util.ValueFormatter;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

import static com.maciekjanusz.compassproject.preferences.AppPreferences.isWidgetServiceRunning;
import static com.maciekjanusz.compassproject.util.CompassMath.adjustBearing;
import static com.maciekjanusz.compassproject.util.CompassMath.calculateTimeToReach;

public class CompassFragment extends Fragment implements Compass.CompassListener {

    private static final String LOCATION_INPUT_DIALOG_TAG = "location_input_dialog";

    @Bind(R.id.compass_view)                CompassView compassView;
    @Bind(R.id.action_widget_switch)        SwitchCompat widgetSwitch;
    @Bind(R.id.pick_place_button)           FloatingActionButton pickPlaceButton;
    @Bind(R.id.pitch_view)                  TextView pitchView;
    @Bind(R.id.roll_view)                   TextView rollView;
    @Bind(R.id.navigation_status_layout)    LinearLayout navigationStatusLayout;
    @Bind(R.id.destination_text_view)       TextView destinationTextView;
    @Bind(R.id.speed_text_view)             TextView speedTextView;
    @Bind(R.id.distance_text_view)          TextView distanceTextView;
    @Bind(R.id.estimated_time_text_view)    TextView estimatedTimeTextView;

    private Compass compass;
    private ValueFormatter valueFormatter;
    private final LocationInputDialogFragment locationInputDialogFragment =
            new LocationInputDialogFragment();

    private ServiceState currentServiceState = ServiceState.NAVIGATION_STOPPED;
    private int deviceScreenRotation;
    private int currentPitch;
    private int currentRoll;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        compass = new Compass(getContext(), this);
        valueFormatter = new ValueFormatter(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_compass, container, false);
        ButterKnife.bind(this, rootView);
        // views bound -> references are now valid
        initCompassView();
        initWidgetSwitch();

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // cleanup
        ButterKnife.unbind(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // update parameters
        valueFormatter.loadPrefs(getContext());
        updateRotation();

        compass.start();

        // register to eventBus and request nav service state
        EventBus eventBus = EventBus.getDefault();
        eventBus.register(this);
        eventBus.post(ServiceMessage.REQUEST_NAVIGATION_SERVICE_STATE);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        compass.stop();
        super.onPause();
    }

    private void initCompassView() {
        compassView.setCompassEnabled(true);
        compassView.setCardinalsEnabled(true);
        compassView.setHasBackground(true);
    }

    private void initWidgetSwitch() {
        widgetSwitch.setChecked(isWidgetServiceRunning(getContext()));

        widgetSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent serviceIntent = new Intent(getContext(), ScreenWidgetService.class);
                if (isChecked) {
                    getContext().startService(serviceIntent);
                    ((CompassActivity) getActivity()).showWidgetInfoSnackbar();
                } else {
                    getContext().stopService(serviceIntent);
                }
            }
        });
    }

    @Override
    public void onCompassStateChanged(float bearing, float pitch, float roll) {
        updateCompass(bearing, roll);
        updatePitchAndRoll(pitch, roll);
    }

    private void updateRotation() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        deviceScreenRotation = display.getRotation();

        /*
            TODO: there is a problem:
            if screen is rotated from landscape to opposite landscape (or from portrait to
            upside down portrait), reconfiguration is not triggered, and compass will show
            bearings as adjusted for previous screen rotation. Currently I don't know
            how to detect these screen rotation changes.
         */
    }

    @SuppressWarnings("unused")
    public void onEvent(@NonNull NavigationBundle navigationBundle) {
        Location location = navigationBundle.getLocation();
        float distance = navigationBundle.getDistance();
        float speed = location.getSpeed();

        // navBundle received -> show navigation status and heading
        navigationStatusLayout.setVisibility(View.VISIBLE);
        compassView.setNavigationEnabled(true);

        // set bearing
        float bearing = navigationBundle.getBearing();
        bearing = adjustBearing(bearing, currentRoll, 0);
        compassView.setNavigationBearing(bearing);


        // update navigation status
        LatLng destination = navigationBundle.getDestination();
        distanceTextView.setText(valueFormatter.formatDistance(distance));
        destinationTextView.setText(valueFormatter.formatCoordinates(
                destination.latitude, destination.longitude));
        speedTextView.setText(valueFormatter.formatSpeed(speed));

        // try to calculate estimated reach time only if speed > 0
        if(speed == 0) {
            estimatedTimeTextView.setText(getString(R.string.infinity));
        } else {
            long estimatedTime = calculateTimeToReach(speed, distance);
            estimatedTimeTextView.setText(valueFormatter.formatDuration(estimatedTime));
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(@NonNull ServiceState serviceState) {
        currentServiceState = serviceState;
        switch(serviceState) {
            case NAVIGATION_RUNNING:
                changeFloatingButton(true);
                break;
            case NAVIGATION_STOPPED:
                compassView.setNavigationEnabled(false);
                changeFloatingButton(false);
                navigationStatusLayout.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void changeFloatingButton(boolean navigationServiceRunning) {
        if (navigationServiceRunning) {
            pickPlaceButton.setImageResource(R.drawable.ic_cancel_white_48dp);
        } else {
            pickPlaceButton.setImageResource(R.drawable.ic_navigation_white_48dp);
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.pick_place_button)
    public void onFloatingButtonClick() {
        switch (currentServiceState) {
            case NAVIGATION_RUNNING:
                getContext().stopService(new Intent(getContext(), NavigationService.class));
                break;
            case NAVIGATION_STOPPED:
                if(!locationInputDialogFragment.isAdded()) {
                    locationInputDialogFragment.show(getActivity().getSupportFragmentManager(), LOCATION_INPUT_DIALOG_TAG);
                }
                break;
        }
    }

    private void updatePitchAndRoll(float pitchF, float rollF) {
        // cast to int so they can be compared with current
        int pitch = (int) pitchF;
        int roll = (int) rollF;

        /*
            Update views ONLY if values has changed, using valueFormatter,
            - or watch the sawtooth on memory monitor...
         */

        if(pitch != currentPitch) {
            currentPitch = pitch;
            pitchView.setText(valueFormatter.formatDegreeValue(pitch));
        }

        if(roll != currentRoll) {
            currentRoll = roll;
            rollView.setText(valueFormatter.formatDegreeValue(roll));
        }
    }

    private void updateCompass(float bearing, float roll) {
        // check if view is not null (samsung bug)
        if(compassView != null) {
            // ensure that the value is correct with current roll & deviceScreenRotation
            bearing = adjustBearing(bearing, roll, deviceScreenRotation);
            // update compass view
            compassView.setCompassBearing(-bearing);
        }
    }
}
