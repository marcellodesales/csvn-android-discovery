package com.collabnet.svnedge.discovery.client.android.discover;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.collabnet.svnedge.discovery.SvnEdgeServerInfo;
import com.collabnet.svnedge.discovery.client.android.R;
import com.collabnet.svnedge.discovery.client.android.SvnEdgeDiscoveryApplication;
import com.collabnet.svnedge.discovery.client.android.preference.FiltersPreferenceActivity;
import com.collabnet.svnedge.discovery.client.android.preference.SettingsPreferenceActivity;
import com.collabnet.svnedge.discovery.client.util.CloseActivityAction;
import com.collabnet.svnedge.discovery.mdns.SvnEdgeServiceType;

public class DiscoverActivity extends Activity {

    /**
     * The TAG used for logging.
     */
    public static final String TAG = "SvnEdgeDiscovery";
    /**
     * The main application reference.
     */
    private SvnEdgeDiscoveryApplication app;
    /**
     * The adapter of the list of SvnEdge servers found.
     */
    private SvnEdgeServerInfoListAdapter csvnServersFoundAdapter;
    /**
     * The current IP number.
     */
    private TextView currentIp;
    /**
     * The search dialog that appears when starting the discovery API.
     */
    private ProgressDialog initialSearchDialog;
    /**
     * The switch to start/stop the discovery.
     */
    private boolean started;
    
    public static DiscoverActivity currentInstance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentInstance = this;

        this.app = ((SvnEdgeDiscoveryApplication)getApplicationContext());

        ArrayList<SvnEdgeServerInfo> foundServers = (ArrayList<SvnEdgeServerInfo>)getLastNonConfigurationInstance();
        foundServers = foundServers != null ? foundServers: this.app.getFoundServers();

        setContentView(R.layout.main);

        this.app.initWifiNetworkConnectivity();
        this.initUiComponents(foundServers);

        this.csvnServersFoundAdapter =
                new SvnEdgeServerInfoListAdapter(this, R.layout.row_svn_mode_standalone,
                        foundServers, this);

        ListView svnEdgeServersList =
                (ListView) this.findViewById(R.id.listViewServersFound);
        svnEdgeServersList.setAdapter(this.csvnServersFoundAdapter);

        Log.d(TAG, "Finished loading... ");
    }

    /**
     * Initializes the UI components onCreate() execution, with the given 
     * list of servers. 
     * @param foundServers is the initial number of servers found. This is the
     * case of when the device changes perspective.
     */
    private void initUiComponents(ArrayList<SvnEdgeServerInfo> foundServers) {
        Log.d(TAG, "Initializing the components...");
        this.currentIp = (TextView) this.findViewById(R.id.textCurrentIp);

        Log.d(TAG, "Getting the IP address");
        // initialize IP address
        String wifiIp = this.app.getWifiIpAddress();
        if (wifiIp != null) {
            Log.d(TAG, "Wifi Connected. IP Address = " + wifiIp);
            this.currentIp.setText(wifiIp);
        } else {
            Log.d(TAG, "Wifi Disconnected.");
            this.currentIp.setText("Wifi Not Connected.");
        }

        ListView svnEdgeServersList = (ListView) this.findViewById(R.id.listViewServersFound);
        svnEdgeServersList.setOnItemLongClickListener(new SvnEdgeServersListLongClickListener(
                        this, foundServers));

        Log.d(TAG, "Getting services");
    }

    /**
     * Shows an alert message with 
     * @param text is the text to be displayed.
     */
    private void showLongToastMessage(CharSequence text) {
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(getApplicationContext(), text, duration);
        toast.show();
    }

    /**
     * Cleans the list of found servers, showing the given message.
     * @param actionMessage
     */
    private void clearSvnEdgeServersList() {
        ListView svnEdgeServersList =
            (ListView) this.findViewById(R.id.listViewServersFound);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                this, android.R.layout.simple_list_item_1, new String[] {});
        svnEdgeServersList.setAdapter(adapter);
    }

    /**
     * Runnable used to update the list of found servers on a different thread.
     */
    private Runnable returnRes = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Running the updater of the adapter 'returnRes'" + 
                    app.getFoundServers().size());
            csvnServersFoundAdapter.notifyDataSetChanged();
        }
    };

    public Handler viewUpdateHandler = new Handler() {

        public void handleMessage(final Message msg) {

            switch(msg.what) {
            case SvnEdgeDiscoveryApplication.MESSAGE_SERVER_IS_RUNNING:

                Log.d(TAG, "Found server running. ");
                runOnUiThread(returnRes);
                break;

            case SvnEdgeDiscoveryApplication.MESSAGE_SERVER_STOPPED :

                if (app.getFoundServers().size() == 0) {

                    Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                clearSvnEdgeServersList();
                            }
                        };
                        runOnUiThread(r);

                } else {
                    Log.d(TAG, "Refreshing view after server stopped: " + 
                            app.getFoundServers().size());
                    runOnUiThread(returnRes);
                }

                showLongToastMessage("The server '" + 
                        msg.getData().getString("hostAddress") + 
                        "' has been turned off.");
                break;
            }
        }
    };

    @Override
    public Object onRetainNonConfigurationInstance() {
        return this.app.getFoundServers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO In case there will be more menus, use the context menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent optionIntent = null;
        switch (item.getItemId()) {
        case R.id.menu_item_discover:
            // clicked on start the service
            if (!this.started) {
                Log.d(TAG, "Selected Service Type: " + SvnEdgeServiceType.CSVN);
                this.initialSearchDialog = ProgressDialog.show(this, "",
                        "Starting discovery service", true);

                this.app.startDiscovery();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        initialSearchDialog.dismiss();
                    }
                }, 3000);

                this.started = true;

            } else {
                this.started = false;
                // clicked on stop the service
                Log.d(TAG, "Turning off service: ");
                clearSvnEdgeServersList();
                showLongToastMessage("The service has been turned off...");
            }
            break;

        case R.id.menu_item_wifi_settings:
            Log.d(TAG, "Opening the wifi settings");
            Intent wirelessSettingsIntent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
            startActivity(wirelessSettingsIntent);
            break;

        case R.id.menu_item_filters:
            Log.d(TAG, "Opening the filter settings");
            optionIntent = new Intent(this, FiltersPreferenceActivity.class);
            startActivity(optionIntent);
            break;

        case R.id.menu_item_settings:
            Log.d(TAG, "Opening the preferences settings");
            optionIntent = new Intent(this, SettingsPreferenceActivity.class);
            startActivity(optionIntent);
            break;

        case R.id.menu_item_about:
            Log.d(TAG, "Opening the about screen");
            CharSequence text = "This option hasn't been implemented...";
            int duration = Toast.LENGTH_LONG;
            Toast toast =
                    Toast.makeText(getApplicationContext(), text, duration);
            toast.show();
            break;

        case R.id.menu_item_exit:
            Log.d(TAG, "Opening the closing option");
            String msg = "Are you sure you want to close the CollabNet SvnEdge Discovery client?";
            CloseActivityAction.confirm(DiscoverActivity.this, msg, "Yes", "Cancel");
            break;
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // TODO change the name of the first menu item...
        return super.onPrepareOptionsMenu(menu);
    }
}
