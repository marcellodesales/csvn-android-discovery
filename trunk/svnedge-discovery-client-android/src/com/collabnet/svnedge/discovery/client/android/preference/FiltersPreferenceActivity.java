package com.collabnet.svnedge.discovery.client.android.preference;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.collabnet.svnedge.discovery.client.android.R;

/**
 * The filters preference activity loaded from the xml configuration.
 * 
 * @author Marcello de Sales (marcello.desales@gmail.com)
 * 
 */
public class FiltersPreferenceActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_filters);
    }
}
