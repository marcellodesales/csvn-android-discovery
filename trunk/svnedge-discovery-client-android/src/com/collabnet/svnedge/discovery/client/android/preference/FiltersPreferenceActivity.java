package com.collabnet.svnedge.discovery.client.android.preference;

import com.collabnet.svnedge.discovery.client.android.R;
import com.collabnet.svnedge.discovery.client.android.R.xml;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class FiltersPreferenceActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_filters);
    }
}
