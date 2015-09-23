package com.maciekjanusz.compassproject.preferences;


import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.maciekjanusz.compassproject.R;

import java.util.ArrayList;
import java.util.List;

public class AppPreferencesFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        // everything below is for setting current pref value as Preference summary

        Preference unitSystemPreference = findPreference(getString(R.string.length_unit_list_preference_key));
        Preference coordinatesInputPreference = findPreference(getString(R.string.coordinates_input_mode_preference_key));

        List<Preference> preferences = new ArrayList<>();
        preferences.add(unitSystemPreference);
        preferences.add(coordinatesInputPreference);

        for(Preference preference : preferences) {
            if(preference instanceof ListPreference) {
                preference.setSummary(((ListPreference) preference).getEntry());
                preference.setOnPreferenceChangeListener(this);
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(preference instanceof ListPreference) {
            ListPreference listPref = ((ListPreference) preference);

            CharSequence[] entryValues = listPref.getEntryValues();
            for (int i = 0, entryValuesLength = entryValues.length; i < entryValuesLength; i++) {
                CharSequence str = entryValues[i];
                if (str.equals(newValue)) {
                    preference.setSummary(listPref.getEntries()[i]);
                }
            }
        }
        return true;
    }
}
