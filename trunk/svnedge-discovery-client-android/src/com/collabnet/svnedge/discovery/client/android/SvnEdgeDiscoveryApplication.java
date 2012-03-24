package com.collabnet.svnedge.discovery.client.android;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.collabnet.svnedge.discovery.SvnEdgeBonjourClient;
import com.collabnet.svnedge.discovery.SvnEdgeServerInfo;
import com.collabnet.svnedge.discovery.SvnEdgeServersListener;
import com.collabnet.svnedge.discovery.client.android.discover.DiscoverActivity;
import com.collabnet.svnedge.discovery.mdns.SvnEdgeCsvnServiceKey;
import com.collabnet.svnedge.discovery.mdns.SvnEdgeServiceType;

/**
 * This is the main application implementation.
 * 
 * @author Marcello de Sales (marcello.desales@gmail.com)
 * 
 */
public class SvnEdgeDiscoveryApplication extends Application implements SvnEdgeServersListener {

    /**
     * Called when the activity is first created. 
     **/
    public static final String TAG = "SvnEdgeDiscovery";
    /**
     * The message code to signal when an SvnEdge server is found running.
     */
    public static final int MESSAGE_SERVER_IS_RUNNING = 1;
    /**
     * The message code to signal when an SvnEdge server has stopped.
     */
    public static final int MESSAGE_SERVER_STOPPED = 2;
    /**
     * The list of csvn servers received.
     */
    private List<SvnEdgeServerInfo> csvnServersFound;
    /**
     * The instance of the csvn discovery client.
     */
    private SvnEdgeBonjourClient csvnClient;
    /**
     * The ip address as an integer value.
     */
    private int ipAddress;
    /**
     * To allow the app receive multicast network packets.
     */
    private MulticastLock multicastLock;
    /**
     * The notification manager reference.
     */
    public NotificationManager notificationManager;
    /**
     * The current notification.
     */
    private Notification notification;
    /**
     * The ID for the notifications.
     */
    private int clientNotificationCount = 0;

    // Intents
    private PendingIntent mainIntent;

    /**
     * Builds a new application.
     */
    public SvnEdgeDiscoveryApplication() {
        this.csvnServersFound = new ArrayList<SvnEdgeServerInfo>();
    }

    /**
     * @return the current list of servers found by the discovery API.
     */
    public synchronized List<SvnEdgeServerInfo> getFoundServers() {
        return this.csvnServersFound;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // init notificationManager
        this.notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        this.notification = new Notification(R.drawable.discovery_api, "SVNEdge Discovery", System.currentTimeMillis());
        this.mainIntent = PendingIntent.getActivity(this, 0, new Intent(this, StartUpEulaActivity.class), 0);
    }

    @Override
    public void onTerminate() {
        Log.d(TAG, "Calling onTerminate()");
        // Remove all notifications
        this.notificationManager.cancelAll();

        this.stopDiscovery();
        super.onTerminate();
    }

    @Override
    public void csvnServerIsRunning(SvnEdgeServerInfo runningServer) {
        Log.d(TAG, "Found server running: " + runningServer);
        synchronized (this.csvnServersFound) {
            this.csvnServersFound.add(runningServer);
            Log.d(TAG, "Refreshing view after server found running: " + this.csvnServersFound.size());
            this.broadcastServerStartedMessage(runningServer);
        }
    }

    @Override
    public void csvnServerStopped(SvnEdgeServerInfo stoppedServer) {
        Log.d(TAG, "Server Stopped: " + stoppedServer);

        // TODO: verify if the server is in the cache before.
        SvnEdgeServerInfo removed = null;
        synchronized (this.csvnServersFound) {
            if (this.csvnServersFound.size() == 1) {
                removed = this.csvnServersFound.remove(0);
            } else {
                List<SvnEdgeServerInfo> updatedList = new ArrayList<SvnEdgeServerInfo>();
                for (SvnEdgeServerInfo existingServer : this.csvnServersFound) {
                    if (!existingServer.getServiceName().equals(stoppedServer.getServiceName())) {
                        updatedList.add(existingServer);
                    } else {
                        removed = existingServer;
                    }
                }
                this.csvnServersFound = updatedList;
            }
            this.broadcastServerShutdownMessage(removed);

        }
    }

    /**
     * Broadcasts an Android message upon the discovery of a new server running (or captured from the mDNS as running).
     * @param foundServer is the information about the svnedge server found.
     */
    private void broadcastServerStartedMessage(SvnEdgeServerInfo foundServer) {
        Message msg = Message.obtain();
        msg.what = MESSAGE_SERVER_IS_RUNNING;

        Bundle bundle = new Bundle();
        bundle.putString("index", foundServer.getServiceName());
        bundle.putString("url", foundServer.getUrl().toString());
        bundle.putString("hostAddress", foundServer.getHostAddress());
        boolean converted = foundServer.getPropertyValue(SvnEdgeCsvnServiceKey.TEAMFORGE_PATH).equals("");
        bundle.putBoolean("converted", converted);

        msg.setData(bundle);
        DiscoverActivity.currentInstance.viewUpdateHandler.sendMessage(msg);
    }

    /**
     * Broadcasts an Android message upon the discovery of a running server being stopped (captured from the mDNS).
     * @param serverStopped is the information about the svnedge server that stopped running.
     */
    private void broadcastServerShutdownMessage(SvnEdgeServerInfo serverStopped) {
        Message msg = Message.obtain();
        msg.what = MESSAGE_SERVER_STOPPED;

        Bundle bundle = new Bundle();
        bundle.putString("hostAddress", serverStopped.getHostname());
        msg.setData(bundle);
        DiscoverActivity.currentInstance.viewUpdateHandler.sendMessage(msg);
    }

    /**
     * Updates the different intent with the discovery activity.
     * @param eventMessage is the event sent.
     * @param serverInfo is the svnedge server information.
     * 
     * ATTENTION: SEE THE HANDLER IMPLEMENTATION. DiscoveryActivity.viewUpdateHandler
     */
    private void sendServerEventNotification(int eventMessage, SvnEdgeServerInfo serverInfo) {
        switch (eventMessage) {
        case MESSAGE_SERVER_IS_RUNNING:
            this.mainIntent = PendingIntent.getActivity(this, 0, new Intent(this, DiscoverActivity.class), 0);
            this.notification = new Notification(R.drawable.discovery_api, "SVNEdge Server Running",
                    System.currentTimeMillis());
            this.notification.flags = Notification.FLAG_AUTO_CANCEL;
            this.notification.setLatestEventInfo(this, "Server '" + serverInfo.getHostAddress() + "'",
                    "Found SvnEdge server is at '" + serverInfo.getHostAddress() + "'", this.mainIntent);

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            if (prefs.getBoolean("prefVibrateOnServerStarted", true)) {
                this.notification.vibrate = new long[] { 0, 100 };
            }

            break;

        default:
            break;
        }

        this.notificationManager.notify(-1, this.notification);
    }

    /**
     * Initializes the Wifi connectivity if necessary. It takes the user to the Wifi setup intent.
     */
    public void initWifiNetworkConnectivity() {
        Log.d(TAG, "Trying to get the Wifi Manager: ");
        WifiManager wifiMan = (WifiManager) this.getSystemService(WIFI_SERVICE);
        if (wifiMan.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
            Log.d(TAG, "The WIFI state is different: " + wifiMan.getWifiState());
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("There is no WIFI connected. Would you like to configure one?").setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent wirelessSettingsIntent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                            startActivity(wirelessSettingsIntent);
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
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

    /**
     * @return the InetAddress instance from the Wifi connectivity.
     */
    private InetAddress getIpAddressFromWifi() {
        byte[] byteaddr = new byte[] { (byte) (this.ipAddress & 0xff), (byte) (this.ipAddress >> 8 & 0xff),
                (byte) (this.ipAddress >> 16 & 0xff), (byte) (this.ipAddress >> 24 & 0xff) };
        try {
            InetAddress convertedAddress = InetAddress.getByAddress(byteaddr);
            return convertedAddress;
        } catch (UnknownHostException e) {
            Log.e(TAG, "Error occurred with the host...", e);
            return null;
        }
    }

    /**
     * @return The IP address from the Wifi.
     */
    public String getWifiIpAddress() {
        InetAddress addr = this.getIpAddressFromWifi();
        return addr.getHostAddress();
    }

    /**
     * Acquires the Multicast Lock from the Android kernel.
     */
    private void acquireMulticastLock() {
        Log.d(TAG, "Trying to create a multicast lock");
        WifiManager wifiMan = (WifiManager) this.getSystemService(WIFI_SERVICE);
        this.multicastLock = wifiMan.createMulticastLock(TAG);
        this.multicastLock.setReferenceCounted(true);
        Log.d(TAG, "The lock is not held... Trying to acquire...");
        this.multicastLock.acquire();
        Log.d(TAG, "Acquired..." + this.multicastLock);
    }

    /**
     * Show the start notification when the discovery is enabled.
     */
    private void showStartNotification() {
        this.notification.flags = Notification.FLAG_ONGOING_EVENT;
        this.notification.setLatestEventInfo(this, "CollabNet SVNEdge Discovery", "The discovery client is running...",
                this.mainIntent);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (prefs.getBoolean("prefVibrateOnStartDiscovery", true)) {
            this.notification.vibrate = new long[] { 100, 200, 100, 200 };
        }
        this.notificationManager.notify(-1, this.notification);
    }

    /**
     * Starts the discovery API for a given listener.
     * 
     * @param listener
     *            is the listener interested in the events about Subversion Edge servers.
     */
    public void startDiscovery() {
        try {
            // Acquires the multicast lock from the kernel.
            this.acquireMulticastLock();

            this.csvnClient = SvnEdgeBonjourClient.makeInstance(
                    this.getIpAddressFromWifi(), "csvndroid",
                    SvnEdgeServiceType.CSVN);
            Log.d(TAG, "Created the client..." + this.csvnClient);
            this.csvnClient.addServersListener(this);
            Log.d(TAG, "Started the discovery client");

            showStartNotification();

        } catch (IOException e) {
            Log.e(TAG, "Did not initilize discovery client", e);
        }
    }

    /**
     * Stops the discovery service and releases the services.
     */
    public void stopDiscovery() {
        // remove all the notifications in the status bar.
        this.notificationManager.cancelAll();
        // stop the discovery client
        try {
            this.csvnClient.stop();

        } catch (IOException e) {
            Log.e(TAG, "Error closing the jnDNS client: ", e);
            e.printStackTrace();
        }
        // release the multicast lock in the kernel.
        this.multicastLock.release();
    }

}
