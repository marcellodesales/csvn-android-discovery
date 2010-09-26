package com.collabnet.svnedge.discovery.client.android.discover;

import static com.collabnet.svnedge.discovery.client.android.discover.DiscoverActivity.TAG;

import java.util.ArrayList;

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

public class SvnEdgeServersListLongClickListener implements OnItemLongClickListener {

    private ArrayList<SvnEdgeServerInfo> csvnServersFound;
    private Activity mainActivity;
    
    public SvnEdgeServersListLongClickListener(Activity main, ArrayList<SvnEdgeServerInfo> serversFound) {
        this.mainActivity = main;
        this.csvnServersFound = serversFound;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> l, View v, int pos, long id) {

        Log.d(TAG, "Clicked on item at position " + pos);
        final SvnEdgeServerInfo selectedServer = this.csvnServersFound.get(pos);

//            final CharSequence[] items = {"Open SvnEdge URL...", "Email SvnEdge Admin..."};
        final CharSequence[] items = {"Open console URL...", "Open ViewVC"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mainActivity);
        builder.setTitle("SvnEdge Server: " + selectedServer.getHostAddress());
        Log.d(TAG, "Will shot the options");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int pos) {
//                    if (pos == 0) {
                // open the browser with the URL from the server.
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                        selectedServer.getUrl().toString()));
                mainActivity.startActivity(myIntent);
//                    } else {
//                        sendEmailToCsvnServerAdmin(selectedServer);
//                    }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();

        Log.d(TAG, "Opening the " + selectedServer.getUrl() + 
                " in the browser...");

        return false;
    }

    private void sendEmailToCsvnServerAdmin(SvnEdgeServerInfo csvnServer) {

        final Intent emailIntent =
                new Intent(android.content.Intent.ACTION_SEND);

        emailIntent.setType("plain/text");

//        String adminName = csvnServer.getPropertyValue(
//                SvnEdgeCsvnServiceKey.ADMIN_NAME);
//        String adminEmail =  adminName + "<" + csvnServer.getPropertyValue(
//                SvnEdgeCsvnServiceKey.ADMIN_EMAIL) + ">";
        String adminName = null;
        String adminEmail =  null;
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                new String[] { adminEmail });
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                "Regarding the SvnEdge Server on " + 
                csvnServer.getUrl().toString());
        emailIntent
                .putExtra(android.content.Intent.EXTRA_TEXT,
                        "Hi " + adminName + ",\n\n " +
                        "I found a CollabNet Subversion Edge server running " +
                        "at '" + csvnServer.getUrl() + "', which is under " +
                        "your administration, using the CollabNet Android " +
                        "Discovery Client. \n\nThanks, Marcello de Sales.");

        this.mainActivity.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }
}
