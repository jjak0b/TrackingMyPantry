package com.jjak0b.android.trackingmypantry.ui.settings;

import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;

import com.dr1009.app.chronodialogpreference.ChronoPreferenceFragment;
import com.jjak0b.android.trackingmypantry.R;

import nl.invissvenska.numberpickerpreference.NumberDialogPreference;
import nl.invissvenska.numberpickerpreference.NumberPickerPreferenceDialogFragment;

public class SettingsFragment extends ChronoPreferenceFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof NumberDialogPreference) {
            NumberDialogPreference dialogPreference = (NumberDialogPreference) preference;
            DialogFragment dialogFragment = NumberPickerPreferenceDialogFragment
                    .newInstance(
                            dialogPreference.getKey(),
                            dialogPreference.getMinValue(),
                            dialogPreference.getMaxValue(),
                            dialogPreference.getStepValue(),
                            dialogPreference.getUnitText()
                    );
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getParentFragmentManager(), DIALOG_FRAGMENT_TAG);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }
}