package com.collabnet.svnedge.discovery.client.android.discover;

import static com.collabnet.svnedge.discovery.client.android.discover.DiscoverActivity.TAG;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.collabnet.svnedge.discovery.SvnEdgeServerInfo;
import com.collabnet.svnedge.discovery.client.android.R;
import com.collabnet.svnedge.discovery.client.android.R.id;
import com.collabnet.svnedge.discovery.client.android.R.layout;
import com.collabnet.svnedge.discovery.mdns.SvnEdgeCsvnServiceKey;

public class SvnEdgeServerInfoAdapter extends ArrayAdapter<SvnEdgeServerInfo> {

    private Activity mainActivity;
    private ArrayList<SvnEdgeServerInfo> serversFound;

    public SvnEdgeServerInfoAdapter(Context context,
            int textViewResourceId, ArrayList<SvnEdgeServerInfo> items, Activity parent) {
        super(context, textViewResourceId, items);
        Log.d(TAG, "Initializing the adapter with " + items.size() + " servers") ;
        this.serversFound = items;
        this.mainActivity = parent;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        Log.d(TAG, "Updating UI for server at position " + position) ;
        SvnEdgeServerInfo serverInfo = this.serversFound.get(position);
        Log.d(TAG, "Putting in the UI: " + serverInfo) ;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)this.mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            int rowId = serverInfo.getPropertyValue(SvnEdgeCsvnServiceKey.TEAMFORGE_PATH).equals("") ?
                        R.layout.row_svn_mode_teamforge : R.layout.row_svn_mode_standalone;
            v = vi.inflate(rowId, null);
        }
        if (serverInfo != null) {
            TextView tt = (TextView) v.findViewById(R.id.toptext);
            TextView bt = (TextView) v.findViewById(R.id.bottomtext);
            if (tt != null) {
                tt.setText("Service Name: " + serverInfo.getServiceName());
            }
            if (bt != null) {
                bt.setText("URL: " + serverInfo.getUrl().toString());
//                bt.setText("Contact: " +
//                           o.getPropertyValue(SvnEdgeCsvnServiceKey.ADMIN_NAME) +
//                           " <" +
//                           o.getPropertyValue(SvnEdgeCsvnServiceKey.ADMIN_EMAIL) +
//                           ">");
            }
        }
        return v;
    }
}