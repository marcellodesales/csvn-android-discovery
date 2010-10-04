package com.collabnet.svnedge.discovery.client.android;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import com.collabnet.svnedge.discovery.SvnEdgeBonjourClient;
import com.collabnet.svnedge.discovery.SvnEdgeServerInfo;
import com.collabnet.svnedge.discovery.SvnEdgeServersListener;
import com.collabnet.svnedge.discovery.client.android.discover.DiscoverActivity;
import com.collabnet.svnedge.discovery.mdns.SvnEdgeCsvnServiceKey;
import com.collabnet.svnedge.discovery.mdns.SvnEdgeServiceType;

public class SvnEdgeDiscoveryApplication extends Application implements 
        SvnEdgeServersListener {

    /** Called when the activity is first created. */
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
    private ArrayList<SvnEdgeServerInfo> csvnServersFound;
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
     * Builds a new application.
     */
    public SvnEdgeDiscoveryApplication() {
        this.csvnServersFound = new ArrayList<SvnEdgeServerInfo>();

    }

    /**
     * @return the current list of servers found by the discovery API.
     */
    public synchronized ArrayList<SvnEdgeServerInfo> getFoundServers() {
        return this.csvnServersFound;
    }

    @Override
    public void csvnServerIsRunning(SvnEdgeServerInfo runningServer) {
        Log.d(TAG, "Found server running: " + runningServer);
        synchronized (this.csvnServersFound) {
            this.csvnServersFound.add(runningServer);
            Log.d(TAG, "Refreshing view after server found running: " + 
                    this.csvnServersFound.size());
            this.broadcastServerStartedMessage(runningServer);
        }
    }

    @Override
    public void csvnServerStopped(SvnEdgeServerInfo stoppedServer) {
        Log.d(TAG, "Server Stopped: " + stoppedServer);

        //TODO: verify if the server is in the cache before.
        SvnEdgeServerInfo removed = null;
        synchronized (this.csvnServersFound) {
            if (this.csvnServersFound.size() == 1) {
                removed = this.csvnServersFound.remove(0);
            } else {
                ArrayList<SvnEdgeServerInfo> updatedList = new ArrayList<SvnEdgeServerInfo>();
                for (SvnEdgeServerInfo existingServer : this.csvnServersFound) {
                    if (!existingServer.getServiceName().equals(
                            stoppedServer.getServiceName())) {
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

    private void broadcastServerStartedMessage(SvnEdgeServerInfo foundServer) {
        Message msg = Message.obtain();
        msg.what = MESSAGE_SERVER_IS_RUNNING;

        Bundle bundle = new Bundle();
        bundle.putString("index", foundServer.getServiceName());
        bundle.putString("url", foundServer.getUrl().toString());
        bundle.putString("hostAddress", foundServer.getHostAddress());
        boolean converted = foundServer.getPropertyValue(
                SvnEdgeCsvnServiceKey.TEAMFORGE_PATH).equals("");
        bundle.putBoolean("converted", converted);

        msg.setData(bundle);
        DiscoverActivity.currentInstance.viewUpdateHandler.sendMessage(msg);
    }

    private void broadcastServerShutdownMessage(SvnEdgeServerInfo serverStopped) {
        Message msg = Message.obtain();
        msg.what = MESSAGE_SERVER_STOPPED;

        Bundle bundle = new Bundle();
        bundle.putString("hostAddress", serverStopped.getHostname());
        msg.setData(bundle);
        DiscoverActivity.currentInstance.viewUpdateHandler.sendMessage(msg);
    }

    /**
     * Initializes the Wifi connectivity if necessary. It takes the user to the
     * Wifi setup intent.
     */
    public void initWifiNetworkConnectivity() {
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

    /**
     * @return the InetAddress instance from the Wifi connectivity.
     */
    private InetAddress getIpAddressFromWifi() {
        byte[] byteaddr =
                new byte[] { (byte) (this.ipAddress & 0xff),
                        (byte) (this.ipAddress >> 8 & 0xff),
                        (byte) (this.ipAddress >> 16 & 0xff),
                        (byte) (this.ipAddress >> 24 & 0xff) };
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
     * Starts the discovery API for a given listener.
     * @param listener is the listener interested in the events about 
     * Subversion Edge servers.
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

        } catch (IOException e) {
            Log.e(TAG, "Did not initilize discovery client", e);
        }
    }

    /**
     * Stops the discovery service and releases the services.
     */
    public void stopDiscovery() {
        this.csvnClient.stop();

        this.multicastLock.release();
        this.csvnClient = null;
    }

    @Override
    public void onTerminate() {
        this.stopDiscovery();
        super.onTerminate();
    }

}
