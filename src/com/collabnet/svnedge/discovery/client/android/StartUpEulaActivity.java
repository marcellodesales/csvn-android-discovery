package com.collabnet.svnedge.discovery.client.android;

import com.collabnet.svnedge.discovery.client.android.discover.DiscoverActivity;
import com.collabnet.svnedge.discovery.client.util.CloseActivityAction;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ViewFlipper;

/**
 * Show a series of wizard-like steps to the user, which might include an EULA, program credits, and helpful hints.
 * 
 * @author Marcello de Sales (marcello.desales@gmail.com)
 */
public class StartUpEulaActivity extends Activity {

    /**
     * In-order list of wizard steps to present to user. These are layout resource ids.
     */
    public final static int[] STEPS = new int[] { R.layout.wiz_eula };
    /**
     * The view flipper
     */
    protected ViewFlipper flipper = null;
    /**
     * The buttons
     */
    protected Button next, prev;

    private void closeWizardOpenMainActivity() {
        StartUpEulaActivity.this.setResult(Activity.RESULT_OK);
        StartUpEulaActivity.this.finish();

        Intent mainScreenIntent = new Intent(this, DiscoverActivity.class);
        startActivity(mainScreenIntent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (!prefs.getBoolean("prefCheckBoxShowEula", false)) {
            closeWizardOpenMainActivity();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_wizard);

        this.flipper = (ViewFlipper) this.findViewById(R.id.wizard_flipper);

        // inflate the layouts for each step
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int layout : STEPS) {
            View step = inflater.inflate(layout, this.flipper, false);
            this.flipper.addView(step);
        }

        next = (Button) this.findViewById(R.id.action_next);
        next.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (isLastDisplayed()) {
                    // user walked past end of wizard, so return okay
                    closeWizardOpenMainActivity();

                    // final CheckBox checkBox = (CheckBox) findViewById(R.id.showeula);
                    // SharedPreferences.Editor editor = prefs.edit();
                    // editor.putBoolean("prefCheckBoxShowEula", checkBox.isChecked());
                    // editor.commit();

                } else {
                    // show next step and update buttons
                    flipper.showNext();
                    updateButtons();
                }
            }
        });

        prev = (Button) this.findViewById(R.id.action_prev);
        prev.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (isFirstDisplayed()) {
                    // user walked past beginning of wizard, so return that they
                    // canceled

                    String msg = "Are you sure you want to close the CollabNet SvnEdge Discovery client?";
                    CloseActivityAction.confirm(StartUpEulaActivity.this, msg, "Yes", "No");

                } else {
                    // show previous step and update buttons
                    flipper.showPrevious();
                    updateButtons();
                }
            }
        });

        this.updateButtons();
    }

    /**
     * @return if the first button is being displayed.
     */
    protected boolean isFirstDisplayed() {
        return (flipper.getDisplayedChild() == 0);
    }

    /**
     * @return if the last button is displayed.
     */
    protected boolean isLastDisplayed() {
        return (flipper.getDisplayedChild() == flipper.getChildCount() - 1);
    }

    /**
     * Show the buttons upon user's action.
     */
    protected void updateButtons() {
        boolean eula = (flipper.getDisplayedChild() == 0);

        next.setText(eula ? "Agree" : "Next");
        prev.setText(eula ? "Close" : "Back");

    }

}
