package fr.ralmn.wakemeup.activities;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.TextView;

import fr.ralmn.wakemeup.R;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            ((TextView) findViewById(R.id.aboutVersion)).setText(pInfo.versionName);
            ((TextView) findViewById(R.id.buildName)).setText(String.valueOf(pInfo.versionCode));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Linkify.addLinks(((TextView) findViewById(R.id.aboutRalmn)), Linkify.ALL);

    }
}
