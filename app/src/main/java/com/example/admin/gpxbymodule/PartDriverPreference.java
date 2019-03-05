package com.example.admin.gpxbymodule;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

public class PartDriverPreference extends PreferenceActivity {
    public static final int RESULT_CODE_THEME_UPDATED = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        String themeName = pref.getString("theme", "Default(Red)");
        if (themeName.equals("Default(Red)")) {
            setTheme(R.style.AppTheme);
        } else if (themeName.equals("Light Blue")) {
            setTheme(R.style.customtheme);
        }
        super.onCreate(savedInstanceState);
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setPreferenceScreen(null);
        addPreferencesFromResource(R.xml.partdriverpreference);
        setContentView(R.layout.preferencelayout);
        findPreference("theme").setOnPreferenceChangeListener(
                new PartDriverPreference.RefreshActivityOnPreferenceChangeListener(RESULT_CODE_THEME_UPDATED));

        final Preference contact = findPreference("feedback");
        contact.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(Intent.ACTION_SENDTO);
                i.setType("message/rfc822");
                i.setData(Uri.parse("mailto:info@microbizone.com"));
                i.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
                i.putExtra(Intent.EXTRA_TEXT   , "");
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    String txt = "There are no email clients installed.";
                    customToast(txt);
                }
                return true;
            }
        });

    }

    private class RefreshActivityOnPreferenceChangeListener implements Preference.OnPreferenceChangeListener {
        private final int resultCode;
        public RefreshActivityOnPreferenceChangeListener(int resultCode) {
            this.resultCode = resultCode;
        }

        @Override
        public boolean onPreferenceChange(Preference p, Object newValue) {
            setResult(resultCode);
            return true;
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    private void customToast(String txt){
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(
                getApplicationContext().LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.toast, null);
        TextView t = (TextView)view.findViewById(R.id.toasttxt);
        t.setText(txt);
        toast.setView(view);
        toast.setGravity(Gravity.TOP | Gravity.RIGHT, 15, 50);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.enterright);
        view.startAnimation(animation);
        toast.show();
    }
}
