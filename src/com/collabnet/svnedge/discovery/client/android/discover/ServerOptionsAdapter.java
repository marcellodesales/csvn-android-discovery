package com.collabnet.svnedge.discovery.client.android.discover;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.collabnet.svnedge.discovery.client.android.R;

/**
 * Adapter showing the options to be performed in a SvnEdge Server found.
 * 
 * @author Marcello de Sales (marcello.desales@gmail.com)
 */
public class ServerOptionsAdapter extends BaseAdapter {

    /**
     * The inflater for the items to be displayed in the UI.
     */
    private final LayoutInflater mInflater;
    /**
     * The items to be displayed.
     */
    private final List<ListItem> mItems = new ArrayList<ListItem>();
    /**
     * The integration option.
     */
    public static final int ITEM_INTEGRATION = 0;
    /**
     * The open URL option.
     */
    public static final int ITEM_OPEN_URL = 1;
    /**
     * The open VIEW VC Option.
     */
    public static final int ITEM_OPEN_VIEWVC = 2;
    /**
     * The email option.
     */
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
            image = imageResourceId != -1 ? res.getDrawable(imageResourceId) : null;
            this.actionTag = actionTag;
        }
    }

    /**
     * When the user clicks on a server on the list, this adapter shows the list of options available.
     * 
     * @param launcher it is the activity that launched it.
     * @param converted if the server has been converted to a teamforge mode.
     */
    public ServerOptionsAdapter(Activity launcher, boolean converted) {
        super();

        mInflater = (LayoutInflater) launcher.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Create default actions
        Resources res = launcher.getResources();

        if (!converted) {
            mItems.add(new ListItem(res, R.string.server_option_teamforge, R.drawable.icon_teamforge, ITEM_INTEGRATION));
        }

        mItems.add(new ListItem(res, R.string.server_option_open_url, R.drawable.web_browser, ITEM_OPEN_URL));
        mItems.add(new ListItem(res, R.string.server_option_open_viewvc, R.drawable.viewvc, ITEM_OPEN_VIEWVC));
        mItems.add(new ListItem(res, R.string.server_option_email_info, R.drawable.email_link, ITEM_EMAIL_INFO));
    }

    @Override
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

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}