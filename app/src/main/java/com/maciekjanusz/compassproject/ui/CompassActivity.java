package com.maciekjanusz.compassproject.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.maciekjanusz.compassproject.R;
import com.maciekjanusz.compassproject.input.LocationInputDialogFragment;
import com.maciekjanusz.compassproject.navigation.NavigationService;
import com.maciekjanusz.compassproject.preferences.PreferencesActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

import static com.maciekjanusz.compassproject.preferences.AppPreferences.SHOW_WIDGET_INFO_PREF;

public class CompassActivity extends AppCompatActivity implements
        LocationInputDialogFragment.Callbacks {

    private static final int REQUEST_PLACE_PICKER = 999;
    private static final int REQUEST_RESOLVE_ERROR = 998;
    private static final String TAG = "CompassActivity";

    @Bind(R.id.snackbar_layout) CoordinatorLayout snackbarCoordinator;
    @Bind(R.id.toolbar)         Toolbar toolbar;

    private boolean resolvingError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        // Check for sensors availability
        PackageManager packageManager = getPackageManager();
        boolean sensorsAvailable = (packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS) &&
                packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER));

        if(sensorsAvailable) {
            // if sensors are available, proceed with adding compassFragment
            if (findViewById(R.id.fragment_container) != null) {
                if (savedInstanceState != null) {
                    return;
                }
                // Create a new Fragment to be placed in the activity layout
                CompassFragment compassFragment = new CompassFragment();
                // Add the fragment to the 'fragment_container' FrameLayout
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, compassFragment).commit();
            }
        } else {
            // show dialog with finish() on dismiss
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.sensors_unavailable_error));
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            });
            builder.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PLACE_PICKER) {
            // This result is from the PlacePicker dialog.

            if (resultCode == Activity.RESULT_OK) {
                // retrieve latitude and longitude from result data
                final Place place = PlacePicker.getPlace(data, this);
                LatLng latLng = place.getLatLng();
                Log.i(TAG, "Lat: " + latLng.latitude + " Lon: " + latLng.longitude);

                onLocationPicked(latLng);
            }
        } else if (requestCode == REQUEST_RESOLVE_ERROR) {
            // This result is from the google play services error resolution intent

            resolvingError = false;
            if (resultCode == RESULT_OK) {
                Snackbar.make(snackbarCoordinator, R.string.resolution_successful,
                        Snackbar.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * This event fires when google play services client fails to connect.
     *
     * param connectionResult {@link ConnectionResult} from
     * {@link com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener#onConnectionFailed(ConnectionResult)}
     */
    @SuppressWarnings("unused")
    public void onEvent(@NonNull ConnectionResult connectionResult) {
        // stop navigation service
        stopService(new Intent(this, NavigationService.class));

        if(resolvingError) {
            // if already resolving, return
            return;
        }
        if(connectionResult.hasResolution()) {
            try {
                resolvingError = true;
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                Snackbar.make(snackbarCoordinator, R.string.resolution_failed,
                        Snackbar.LENGTH_LONG).show();
            }
        } else {
            showErrorDialog(connectionResult.getErrorCode());
            resolvingError = true;
        }
    }

    @Override
    public void onLocationPicked(LatLng latLng) {
        // location has been picked -> start navigation service
        Intent serviceIntent = new Intent(this, NavigationService.class);
        serviceIntent.putExtra(NavigationService.EXTRA_DESTINATION, latLng);
        startService(serviceIntent);
    }

    @Override
    public void onInvalidLocationPicked() {
        Snackbar.make(snackbarCoordinator, R.string.location_invalid, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onPlacePickerChosen() {
        startPlacePicker();
    }

    private void startPlacePicker() {
        try {
            Intent intent = new PlacePicker.IntentBuilder().build(this);
            startActivityForResult(intent, CompassActivity.REQUEST_PLACE_PICKER);
            // if something goes wrong, proceed with error resolution:
        } catch (GooglePlayServicesRepairableException e) {
            showErrorDialog(e.getConnectionStatusCode());
        } catch (GooglePlayServicesNotAvailableException e) {
            showErrorDialog(e.errorCode);
        }
    }

    private void showErrorDialog(int errorCode) {
        Dialog dialog = GoogleApiAvailability.getInstance()
                .getErrorDialog(this, errorCode, REQUEST_RESOLVE_ERROR);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                resolvingError = false;
            }
        });
        dialog.show();
    }

    public void showWidgetInfoSnackbar() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean showWidgetInfo = sharedPreferences.getBoolean(SHOW_WIDGET_INFO_PREF, true);

        if(showWidgetInfo) {
            Snackbar.make(snackbarCoordinator,
                    R.string.widget_info, Snackbar.LENGTH_LONG)
                    .setAction(R.string.do_not_show_again, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sharedPreferences
                                    .edit()
                                    .putBoolean(SHOW_WIDGET_INFO_PREF, false)
                                    .apply();
                        }
                    }).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_compass_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, PreferencesActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
