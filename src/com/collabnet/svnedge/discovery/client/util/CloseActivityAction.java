package com.collabnet.svnedge.discovery.client.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

/**
 * Closes the activity.
 * 
 * @author Marcello de Sales (marcello.desales@gmail.com)
 * 
 */
public class CloseActivityAction {

    public static void confirm(final Activity activity, String message, String positiveButton, String negativeButton) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message).setCancelable(false)
                .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        activity.setResult(Activity.RESULT_CANCELED);
                        activity.finish();
                    }
                }).setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
