/*
 * Copyright (C) 2007 The Android Open Source Project
 * Copyright (C) 2013 Micronet. micronet.co.il
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;


import static android.provider.Settings.System.SHUTDOWN_TIMEOUT;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceGroup;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;


/**
 * Gesture lock pattern settings.
 */

public class IgnitionSettings extends SettingsPreferenceFragment implements
    Preference.OnPreferenceChangeListener, Indexable {
	
	private static final String TAG = "IgnitionSettings";
	/** If there is no setting in the provider, use this. */
	private static final int FALLBACK_SHUTDOWN_TIMEOUT_VALUE = -1;
    private static final String KEY_SHUTDOWN_TIMEOUT = "ignition_timeout";

    private ShutdownTimeoutPreference mShutdownTimeoutPreference;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ContentResolver resolver = getActivity().getContentResolver();
		
        addPreferencesFromResource(R.xml.ignition_settings);

		mShutdownTimeoutPreference = (ShutdownTimeoutPreference) findPreference(KEY_SHUTDOWN_TIMEOUT);
        long currentTimeout = Settings.System.getLong(resolver, SHUTDOWN_TIMEOUT,
                FALLBACK_SHUTDOWN_TIMEOUT_VALUE);
        long timeoutValue = (0 > currentTimeout) ? -1 : ((currentTimeout == Integer.MAX_VALUE) ? -1 : currentTimeout);
        mShutdownTimeoutPreference.setValue(String.valueOf(timeoutValue));
        mShutdownTimeoutPreference.setOnPreferenceChangeListener(this);
        disableUnusableTimeouts(mShutdownTimeoutPreference);
        updateTimeoutPreferenceDescription(timeoutValue);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
	}
    private void disableUnusableTimeouts(ListPreference timeoutPreference) {
        final DevicePolicyManager dpm =
                (DevicePolicyManager) getActivity().getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        final long maxTimeout = dpm != null ? dpm.getMaximumTimeToLock(null) : 0;
        if (maxTimeout == 0) {
            return; // policy not enforced
        }
        final CharSequence[] entries = timeoutPreference.getEntries();
        final CharSequence[] values = timeoutPreference.getEntryValues();
        ArrayList<CharSequence> revisedEntries = new ArrayList<CharSequence>();
        ArrayList<CharSequence> revisedValues = new ArrayList<CharSequence>();
        for (int i = 0; i < values.length; i++) {
            long timeout = Long.parseLong(values[i].toString());
            if (timeout <= maxTimeout) {
                revisedEntries.add(entries[i]);
                revisedValues.add(values[i]);
            }
        }
        if (revisedEntries.size() != entries.length || revisedValues.size() != values.length) {
            final int userPreference = Integer.parseInt(timeoutPreference.getValue());
            timeoutPreference.setEntries(
                    revisedEntries.toArray(new CharSequence[revisedEntries.size()]));
            timeoutPreference.setEntryValues(
                    revisedValues.toArray(new CharSequence[revisedValues.size()]));
            if (userPreference <= maxTimeout) {
                timeoutPreference.setValue(String.valueOf(userPreference));
            } else if (revisedValues.size() > 0
                    && Long.parseLong(revisedValues.get(revisedValues.size() - 1).toString())
                    == maxTimeout) {
                // If the last one happens to be the same as the max timeout, select that
                timeoutPreference.setValue(String.valueOf(maxTimeout));
            } else {
                // There will be no highlighted selection since nothing in the list matches
                // maxTimeout. The user can still select anything less than maxTimeout.
                // TODO: maybe append maxTimeout to the list and mark selected.
            }
        }
        timeoutPreference.setEnabled(revisedEntries.size() > 0);
    }

	

    private void updateTimeoutPreferenceDescription(long currentTimeout) {
        ListPreference preference = mShutdownTimeoutPreference;
        String summary;
        if (currentTimeout == -1) {
            summary = preference.getContext().getString(R.string.ignition_timeout_zero_summary);
        } else if (currentTimeout < -1) {
            // Unsupported value
            summary = "";
        } else {
            final CharSequence[] entries = preference.getEntries();
            final CharSequence[] values = preference.getEntryValues();
                int best = 0;
                for (int i = 0; i < values.length; i++) {
                    long timeout = Long.parseLong(values[i].toString());
                if ((currentTimeout >= timeout)&&(timeout > 0)) {
                        best = i;
                    }
                }
                summary = preference.getContext().getString(R.string.ignition_timeout_summary,
                        entries[best]);
        }
        preference.setSummary(summary);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        if (KEY_SHUTDOWN_TIMEOUT.equals(key)) {
            int value = Integer.parseInt((String) objValue);
            int oldvalue = Integer.parseInt(((ListPreference)preference).getValue());
            if (value != oldvalue) {
                Log.d(TAG, "update shutdown timeout from "+ oldvalue + " to " + value);
                int timeoutValue = ( -1 == value) ? Integer.MAX_VALUE : value;
                try {
                    Settings.System.putInt(getContentResolver(), SHUTDOWN_TIMEOUT, timeoutValue);
                    updateTimeoutPreferenceDescription(value);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "could not persist shutdown timeout setting", e);
                }
            }
        }
        return true;
    }
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.ignition_settings;
                    result.add(sir);

                    return result;
                }
    	};
    
}

