package com.collabnet.svnedge.discovery.client.android.preference;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.collabnet.svnedge.discovery.client.android.R;

/**
 * The settings activity loadded from the xml configuration.
 * 
 * @author Marcello de Sales (marcello.desales@gmail.com)
 * 
 */
public class SettingsPreferenceActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_settings);
    }
}
