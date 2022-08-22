package com.HuimangTech.iShare;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.Date;
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class SettingsActivity extends AppCompatActivity implements Preference.OnPreferenceClickListener {

    SharedPreferences.OnSharedPreferenceChangeListener onChangeListener;
    Preference.OnPreferenceClickListener onClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        onChangeListener = (sharedPreferences, key) -> {
            switch (key) {

                case "theme": {
                    switch (sharedPreferences.getString(key, "system")) {
                        case "dark":
                            AppCompatDelegate.setDefaultNightMode(
                                    AppCompatDelegate.MODE_NIGHT_YES);
                            break;
                        case "light":
                            AppCompatDelegate.setDefaultNightMode(
                                    AppCompatDelegate.MODE_NIGHT_NO);
                            break;
                        case "system":
                            AppCompatDelegate.setDefaultNightMode(
                                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                            break;
                    }
                }
                break;
                case "feedback": {
                    Thread t = new Thread(() -> {
                        final String toEmail = "loffy@live.com"; // support person in charge
                        sendEmail(toEmail, "iShare Bug Report", sharedPreferences.getString(key, "Null message"));
                        sharedPreferences.edit().putString(key, "").apply();
                    });
                    t.start();
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(onChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(onChangeListener);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Log.d("tag", "onPreferenceClick: " + preference.getKey());

        switch (preference.getKey()) {
            case "theme":
            case "feedback": {
            }
            break;
        }
        return false;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }

    public static void sendEmail(String toEmail, String subject, String body) {
        try {
            final String fromEmail = "loffy@live.com"; //requires valid gmail id
            final String password = "TyVHG3Bg0tMkEPIr"; // correct password for gmail id
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp-relay.sendinblue.com"); //SMTP Host
            props.put("mail.smtp.port", "587"); //TLS Port
            props.put("mail.smtp.auth", "true"); //enable authentication
            props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS

            Authenticator auth = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fromEmail, password);
                }
            };

            Session session = Session.getInstance(props, auth);
            MimeMessage msg = new MimeMessage(session);

            //set message headers
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");

            msg.setFrom(new InternetAddress("app@iShare.com", "iShare Support"));

            msg.setReplyTo(InternetAddress.parse("no_reply@example.com", false));

            msg.setSubject(subject, "UTF-8");

            msg.setText(body, "UTF-8");

            msg.setSentDate(new Date());

            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
            Transport.send(msg);
            Log.d("TAG", "sendEmail: Feedback mail was sent");
        } catch (Exception e) {
            Log.d("tag", "sendEmail: " + e);
            ;
        }
    }
}