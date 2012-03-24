package com.collabnet.svnedge.discovery.client.android.discover;

import static com.collabnet.svnedge.discovery.client.android.discover.DiscoverActivity.TAG;

import java.util.List;

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
import com.collabnet.svnedge.discovery.mdns.SvnEdgeCsvnServiceKey;

/**
 * The adapter that displays the list of Subversion Edge servers.
 * 
 * @author Marcello de Sales (marcello.desales@gmail.com)
 * 
 */
public class SvnEdgeServerInfoListAdapter extends ArrayAdapter<SvnEdgeServerInfo> {

    /**
     * The main activity.
     */
    private Activity mainActivity;
    /**
     * The list of servers found.
     */
    private List<SvnEdgeServerInfo> serversFound;

    /**
     * Creates a new adapter with the given parameters for the UI.
     * 
     * @param context the context.
     * @param textViewResourceId the resource UI for the view.
     * @param items the list of current servers found.
     * @param parent
     */
    public SvnEdgeServerInfoListAdapter(Context context, int textViewResourceId, List<SvnEdgeServerInfo> items,
            Activity parent) {
        super(context, textViewResourceId, items);
        Log.d(TAG, "Initializing the adapter with " + items.size() + " servers");
        this.serversFound = items;
        this.mainActivity = parent;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        Log.d(TAG, "Updating UI for server at position " + position);
        SvnEdgeServerInfo serverInfo = this.serversFound.get(position);
        Log.d(TAG, "Putting in the UI: " + serverInfo);
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            boolean svnServerConverted = serverInfo.getPropertyValue(SvnEdgeCsvnServiceKey.TEAMFORGE_PATH).equals("");
            int rowId = svnServerConverted ? R.layout.row_svn_mode_teamforge : R.layout.row_svn_mode_standalone;
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
            }
        }
        return v;
    }
}