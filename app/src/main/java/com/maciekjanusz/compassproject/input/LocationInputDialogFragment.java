package com.maciekjanusz.compassproject.input;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.maciekjanusz.compassproject.R;

public class LocationInputDialogFragment extends DialogFragment {

    private Callbacks callback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            callback = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement LocationInputDialogFragment.Callbacks");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View contentView = new LocationInputViewFactory().getLocationInputView(getContext());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.pick_place)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            LatLng latLng = ((LocationInputView) contentView).getLatLng();
                            callback.onLocationPicked(latLng);
                        } catch (InvalidLocationException e) {
                            callback.onInvalidLocationPicked();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                })
                .setNeutralButton(R.string.pick_on_map, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.onPlacePickerChosen();
                    }
                });
        builder.setView(contentView);

        return builder.create();
    }


    public interface Callbacks {

        void onLocationPicked(LatLng latLng);

        void onInvalidLocationPicked();

        void onPlacePickerChosen();
    }
}