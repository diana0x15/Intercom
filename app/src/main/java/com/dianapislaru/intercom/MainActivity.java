package com.dianapislaru.intercom;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.UsbSerialDriver;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Button refreshButton;
    private TextView connectionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkNotificationAccessEnabled();

        Intent serviceIntent = new Intent(MainActivity.this, BackgroundService.class);
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);

        Button settingsButton = findViewById(R.id.activity_main_settings_button);
        refreshButton = findViewById(R.id.activity_main_refresh_button);
        connectionTextView  = findViewById(R.id.activity_main_connection_text);

        refreshButton.setOnClickListener(view -> {
            stopService(serviceIntent);
            startService(serviceIntent);
            setConnectionText();
        });

        settingsButton.setOnClickListener(view -> {
            startActivity(settingsIntent);
        });

        startService(serviceIntent);
    }

    private void setConnectionText() {
        String text;
        if(SerialUtils.sPort == null) {
            text = getResources().getString(R.string.not_connected);
        } else {
            UsbSerialDriver driver = SerialUtils.sPort.getDriver();
            if(driver == null) {
                text = getResources().getString(R.string.not_connected);
            }else {
                text = String.format(getResources().getString(R.string.connected_to_device), driver.getDevice().getDeviceName());
            }
        }
        connectionTextView.setText(text);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if(manager == null) {
            return false;
        }
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void checkNotificationAccessEnabled() {
        String enabledAppList = Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners");
        if (enabledAppList == null || !enabledAppList.contains(getPackageName())) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            } else {
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
            }
        }
    }
}
