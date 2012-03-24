package com.collabnet.svnedge.discovery.client.android.discover;

import static com.collabnet.svnedge.discovery.client.android.discover.DiscoverActivity.TAG;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;

import com.collabnet.svnedge.discovery.SvnEdgeServerInfo;
import com.collabnet.svnedge.discovery.client.android.R;
import com.collabnet.svnedge.discovery.mdns.SvnEdgeCsvnServiceKey;

/**
 * The observer for when the user clicks on the name of a svnedge server running.
 * 
 * @author Marcello de Sales (marcello.desales@gmail.com)
 * 
 */
public class SvnEdgeServersListLongClickListener implements OnItemLongClickListener {

    /**
     * The current list of svnedge servers found.
     */
    private List<SvnEdgeServerInfo> csvnServersFound;
    /**
     * The main activity.
     */
    private Activity mainActivity;
    /**
     * The alert to be displayed.
     */
    private AlertDialog alert;

    public SvnEdgeServersListLongClickListener(Activity main, List<SvnEdgeServerInfo> serversFound) {
        this.mainActivity = main;
        this.csvnServersFound = serversFound;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> l, View v, int pos, long id) {

        Log.d(TAG, "Clicked on item at position " + pos);
        final SvnEdgeServerInfo selectedServer = this.csvnServersFound.get(pos);
        String tfPath = selectedServer.getPropertyValue(SvnEdgeCsvnServiceKey.TEAMFORGE_PATH);
        boolean serverConverted = tfPath.equals("");

        AlertDialog.Builder builder = new AlertDialog.Builder(this.mainActivity);
        builder.setTitle("Subversion Server: " + selectedServer.getHostAddress());
        if (serverConverted) {
            builder.setIcon(R.drawable.icon_teamforge);
        } else {
            builder.setIcon(R.drawable.subversion);
        }
        Log.d(TAG, "Will shot the options");

        ServerOptionsAdapter ad = new ServerOptionsAdapter(mainActivity, serverConverted);
        builder.setAdapter(ad, new ServerOptions(selectedServer));

        AlertDialog alert = builder.create();
        alert.show();
        this.alert = alert;

        Log.d(TAG, "Opening the " + selectedServer.getUrl() + " in the browser...");

        return false;
    }

    /**
     * @return The alert dialog constructed by this observer.
     */
    public AlertDialog getAlert() {
        return this.alert;
    }

    /**
     * The server options to be displayed.
     * 
     * @author Marcello de Sales (marcello.desales@gmail.com)
     */
    private class ServerOptions implements DialogInterface.OnClickListener {

        /**
         * The selected server.
         */
        private SvnEdgeServerInfo selectedServer;

        public ServerOptions(SvnEdgeServerInfo server) {
            this.selectedServer = server;
        }

        private String getTeamForgeWizardAction() {
            return this.selectedServer.getPropertyValue(SvnEdgeCsvnServiceKey.TEAMFORGE_PATH);
        }

        public void onClick(DialogInterface dialog, int pos) {
            String url = selectedServer.getUrl().toString();
            if (!selectedServer.isManagedByTeamForge()) {
                if (pos == ServerOptionsAdapter.ITEM_INTEGRATION) {
                    openIntegrationURL(url);

                } else if (pos == ServerOptionsAdapter.ITEM_OPEN_URL) {
                    openServerURL(url);

                } else if (pos == ServerOptionsAdapter.ITEM_OPEN_VIEWVC) {
                    openViewVcURL(url);

                } else if (pos == ServerOptionsAdapter.ITEM_EMAIL_INFO) {
                    sendEmailToCsvnServerAdmin(url);
                }

            } else { // server converted
                if (pos == ServerOptionsAdapter.ITEM_INTEGRATION) {
                    openIntegrationURL(url);

                } else if (pos == ServerOptionsAdapter.ITEM_OPEN_URL) {
                    openViewVcURL(url);

                } else if (pos == ServerOptionsAdapter.ITEM_OPEN_VIEWVC) {
                    sendEmailToCsvnServerAdmin(url);
                }
            }
        }

        /**
         * Opens the server main page UI with the device's browser.
         * 
         * @param url is the svnedge server UI to the web console.
         */
        private void openServerURL(String url) {
            // open the browser with the URL from the server.
            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            mainActivity.startActivity(myIntent);
        }

        /**
         * Opens the viewvc page of the URL.
         * 
         * @param url is the URL of the view vc for the repositories.
         */
        private void openViewVcURL(String url) {
            // open the browser with the viewVC URL.
            Pattern p = Pattern.compile(":([0-9]+)/");
            Matcher m = p.matcher(url);
            if (m.find()) {
                String consolePort = m.group(1);
                String viewVcPort = "18080";
                url = url.replace(consolePort, viewVcPort);
            }
            int index = url.lastIndexOf("/");
            url = url.substring(0, index) + "/viewvc";
            openServerURL(url);
        }

        /**
         * Opens the TeamForge page integration page for this server.
         * 
         * @param url is the url of the server.
         */
        private void openIntegrationURL(String url) {
            String conversion = url + this.getTeamForgeWizardAction();
            openServerURL(conversion);
        }
    }

    /**
     * Opens the email client with the pre-formatter email about the discovery of the server.
     * 
     * @param url is the URL of the svnedge server selected.
     */
    private void sendEmailToCsvnServerAdmin(String url) {

        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

        emailIntent.setType("plain/text");

        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "" });
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Found SvnEdge Server running on " + url);
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "I found a CollabNet Subversion Edge server running "
                + "at '" + url + "', using the CollabNet " + "Discovery - Android Client.");

        this.mainActivity.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }
}
