package com.collabnet.svnedge.discovery.client.android.discover;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import com.collabnet.svnedge.discovery.client.android.R;

/**
 * Adapter showing the options to be performed in a SvnEdge Server found.
 */
public class ServerOptionsAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;

    private final ArrayList<ListItem> mItems = new ArrayList<ListItem>();

    public static final int ITEM_INTEGRATION = 0;
    public static final int ITEM_OPEN_URL = 1;
    public static final int ITEM_OPEN_VIEWVC = 2;
    public static final int ITEM_EMAIL_INFO = 3;

    /**
     * Specific item in our list.
     */
    public class ListItem {
        public final CharSequence text;
        public final Drawable image;
        public final int actionTag;

        public ListItem(Resources res, int textResourceId, int imageResourceId, int actionTag) {
            text = res.getString(textResourceId);
            if (imageResourceId != -1) {
                image = res.getDrawable(imageResourceId);
            } else {
                image = null;
            }
            this.actionTag = actionTag;
        }
    }

    public ServerOptionsAdapter(Activity launcher, boolean converted) {
        super();

        mInflater = (LayoutInflater) launcher.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Create default actions
        Resources res = launcher.getResources();

        if (!converted) {
            mItems.add(new ListItem(res, R.string.server_option_teamforge,
                R.drawable.icon_teamforge, ITEM_INTEGRATION));
        }

        mItems.add(new ListItem(res, R.string.server_option_open_url,
                R.drawable.web_browser, ITEM_OPEN_URL));

        mItems.add(new ListItem(res, R.string.server_option_open_viewvc,
                R.drawable.viewvc, ITEM_OPEN_VIEWVC));

        mItems.add(new ListItem(res, R.string.server_option_email_info,
                R.drawable.email_link, ITEM_EMAIL_INFO));
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ListItem item = (ListItem) getItem(position);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.alert_server_options, parent, false);
        }

        TextView textView = (TextView) convertView;
        textView.setTag(item);
        textView.setText(item.text);
        textView.setCompoundDrawablesWithIntrinsicBounds(item.image, null, null, null);

        return convertView;
    }

    public int getCount() {
        return mItems.size();
    }

    public Object getItem(int position) {
        return mItems.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

}