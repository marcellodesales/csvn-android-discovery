package com.collabnet.svnedge.discovery.client.android.discover;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.collabnet.svnedge.discovery.SvnEdgeBonjourClient;
import com.collabnet.svnedge.discovery.SvnEdgeServerInfo;
import com.collabnet.svnedge.discovery.SvnEdgeServersListener;
import com.collabnet.svnedge.discovery.client.android.R;
import com.collabnet.svnedge.discovery.client.android.preference.FiltersPreferenceActivity;
import com.collabnet.svnedge.discovery.client.android.preference.SettingsPreferenceActivity;
import com.collabnet.svnedge.discovery.client.util.CloseActivityAction;
import com.collabnet.svnedge.discovery.mdns.SvnEdgeServiceType;

public class DiscoverActivity extends Activity implements
        SvnEdgeServersListener { //, OnItemSelectedListener {

    private ArrayList<SvnEdgeServerInfo> csvnServersFound;
    private SvnEdgeServerInfoListAdapter csvnServersFoundAdapter;

    private SvnEdgeBonjourClient csvnClient;

    /** Called when the activity is first created. */
    public static final String TAG = "SvnEdgeDiscovery";

    TextView currentIp;

    ProgressDialog initialSearchDialog;
    /**
     * To allow the app receive multicast network packets.
     */
    MulticastLock multicastLock;
    /**
     * The ip address as an integer value.
     */
    private int ipAddress;

    private Runnable returnRes = new Runnable() {

        @Override
        public void run() {
            Log.d(TAG, "Running the updater of the adapter 'returnRes'" + csvnServersFound.size());
            if (csvnServersFound != null && csvnServersFound.size() > 0) {
                csvnServersFoundAdapter.notifyDataSetChanged();
            }
        }
    };

    private void initNetwork() {
        Log.d(TAG, "Trying to get the Wifi Manager: ");
        WifiManager wifiMan = (WifiManager) this.getSystemService(WIFI_SERVICE);
        if (wifiMan.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
            Log.d(TAG, "The WIFI state is different: " + wifiMan.getWifiState());
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("There is no WIFI connected. Would you like to configure one?")
                   .setCancelable(false)
                   .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           Intent wirelessSettingsIntent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                           startActivity(wirelessSettingsIntent);
                       }
                   })
                   .setNegativeButton("No", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                       }
                   });
            AlertDialog alert = builder.create();
            alert.show();

        } else {
            WifiInfo wifiinfo = wifiMan.getConnectionInfo();
            this.ipAddress = wifiinfo.getIpAddress();
        }
    }

    private InetAddress getInetAddressFromWifi() {
        byte[] byteaddr =
                new byte[] { (byte) (this.ipAddress & 0xff),
                        (byte) (this.ipAddress >> 8 & 0xff),
                        (byte) (this.ipAddress >> 16 & 0xff),
                        (byte) (this.ipAddress >> 24 & 0xff) };
        try {
            InetAddress convertedAddress = InetAddress.getByAddress(byteaddr);
            if (convertedAddress != null) {
                return convertedAddress;
            } else {
                return InetAddress.getLocalHost();
            }
        } catch (UnknownHostException e) {
            Log.e(TAG, "Error occurred with the host...", e);
        }
        return null;
    }

    private String getWifiIpAddress() {
        InetAddress addr = getInetAddressFromWifi();
        return addr.getHostAddress();
    }

    private void initUiComponents() {
        Log.d(TAG, "Initializing the components...");
        this.currentIp = (TextView) this.findViewById(R.id.textCurrentIp);

        Log.d(TAG, "Getting the IP address");
        // initialize IP address
        String wifiIp = this.getWifiIpAddress();
        if (wifiIp != null) {
            Log.d(TAG, "Wifi Connected. IP Address = " + wifiIp);
            this.currentIp.setText(wifiIp);
        } else {
            Log.d(TAG, "Wifi Disconnected.");
            this.currentIp.setText("Wifi Not Connected.");
        }

        Log.d(TAG, "Getting services");
//        // initialize the services types spinner.
//        CharSequence[] serviceTypes = new String[2];
//        serviceTypes[0] = "Off";
//        serviceTypes[1] = "Start Discovery";
//        Spinner spinnerServiceTypes =
//                (Spinner) this.findViewById(R.id.spinnerServiceTypes);
//        ArrayAdapter<CharSequence> adapter =
//                new ArrayAdapter<CharSequence>(this,
//                        android.R.layout.simple_spinner_item, serviceTypes);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerServiceTypes.setAdapter(adapter);
//        spinnerServiceTypes.setOnItemSelectedListener(this);

        ListView svnEdgeServersList = (ListView) this.findViewById(R.id.listViewServersFound);
        svnEdgeServersList.setOnItemLongClickListener(new SvnEdgeServersListLongClickListener(
                        this, this.csvnServersFound));
    }

    private void clearSvnEdgeServersList(String actionMessage) {
        ListView svnEdgeServersList =
            (ListView) this.findViewById(R.id.listViewServersFound);
        ArrayAdapter<CharSequence> adapter =
                new ArrayAdapter<CharSequence>(this,
                        android.R.layout.simple_list_item_1,
                        new String[] {});
        svnEdgeServersList.setAdapter(adapter);

        CharSequence text = actionMessage;
        int duration = Toast.LENGTH_LONG;
        Toast toast =
                Toast.makeText(getApplicationContext(), text, duration);
        toast.show();
    }

//    @Override
//    public void onNothingSelected(AdapterView<?> arg0) {
//        Log.d(TAG, "Doing nothing since nothing was selected");
//    }

    @Override
    public void csvnServerIsRunning(SvnEdgeServerInfo runningServer) {
        Log.d(TAG, "Found server running: " + runningServer);
        this.csvnServersFound.add(runningServer);

        Log.d(TAG, "Refreshing view after server found running: " + this.csvnServersFound.size());
        runOnUiThread(returnRes);
    }

    @Override
    public void csvnServerStopped(SvnEdgeServerInfo stoppedServer) {
        Log.d(TAG, "Server stopped: " + stoppedServer);
        synchronized (this.csvnServersFound) {
            if (this.csvnServersFound.size() == 1) {
                final SvnEdgeServerInfo svnSrvInf = this.csvnServersFound.remove(0);
                if (svnSrvInf.getServiceName().equals(stoppedServer.getServiceName())) {
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            clearSvnEdgeServersList("The server '" + svnSrvInf.getHostAddress() + 
                                    "' has been turned off.");
                        }
                    };
                    runOnUiThread(r);
                    return;
                }
            }
        }
        ArrayList<SvnEdgeServerInfo> updatedList =
                new ArrayList<SvnEdgeServerInfo>();
        for (SvnEdgeServerInfo existingServer : this.csvnServersFound) {
            if (!existingServer.getServiceName().equals(
                    stoppedServer.getServiceName())) {
                updatedList.add(existingServer);
            }
        }
        this.csvnServersFound = updatedList;

        Log.d(TAG, "Refreshing view after server stopped: " + this.csvnServersFound.size());
        runOnUiThread(returnRes);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return this.csvnServersFound;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<SvnEdgeServerInfo> saved = (ArrayList<SvnEdgeServerInfo>)getLastNonConfigurationInstance();
        this.csvnServersFound = saved != null ? saved : new ArrayList<SvnEdgeServerInfo>();

        setContentView(R.layout.main);

        initNetwork();
        initUiComponents();

        this.csvnServersFoundAdapter =
                new SvnEdgeServerInfoListAdapter(this, R.layout.row_svn_mode_standalone,
                        this.csvnServersFound, this);

        ListView svnEdgeServersList =
                (ListView) this.findViewById(R.id.listViewServersFound);
        svnEdgeServersList.setAdapter(this.csvnServersFoundAdapter);

        Log.d(TAG, "Finished loading: ");
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
            if (csvnClient == null) {
                Log.d(TAG, "Selected Service Type: " + SvnEdgeServiceType.CSVN);
                this.initialSearchDialog = ProgressDialog.show(this, "",
                        "Starting discovery service", true);

                Log.d(TAG, "Trying to create a multicast lock");
                WifiManager wifiMan = (WifiManager) this.getSystemService(WIFI_SERVICE);
                this.multicastLock = wifiMan.createMulticastLock(TAG);
                this.multicastLock.setReferenceCounted(true);
                Log.d(TAG, "The lock is not held... Trying to acquire...");
                this.multicastLock.acquire();
                Log.d(TAG, "Acquired..." + this.multicastLock);

                try {
                    this.csvnClient = SvnEdgeBonjourClient.makeInstance(
                            this.getInetAddressFromWifi(), "csvndroid",
                            SvnEdgeServiceType.CSVN);
                    Log.d(TAG, "Created the client..." + this.csvnClient);
                    this.csvnClient.addServersListener(this);
                    Log.d(TAG, "Started the discovery client");

                } catch (IOException e) {
                    Log.e(TAG, "Did not initilize discovery client", e);
                }

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        initialSearchDialog.dismiss();
                    }
                }, 3000);

            } else {
                // clicked on stop the service

                Log.d(TAG, "Turning off service: ");
                clearSvnEdgeServersList("The service has been turned off...");
                csvnClient.stop();

                this.multicastLock.release();
                this.csvnClient = null;
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
