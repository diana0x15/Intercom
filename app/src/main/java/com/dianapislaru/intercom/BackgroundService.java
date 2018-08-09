package com.dianapislaru.intercom;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static com.dianapislaru.intercom.SerialUtils.*;

public class BackgroundService extends NotificationListenerService {

    private static final String TAG = BackgroundService.class.getSimpleName();
    private static final int ONGOING_NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "default";

    private Handler connectionHandler;
    private Notification notification;
    public static ExecutorService threadExecutor;

    public static Camera mCamera;
    public static SurfaceHolder sHolder;
    public static Camera.Parameters parameters;

    public static Context context;

    Runnable taskRunnable = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "STATUS CHECK");
            connectToSerial(BackgroundService.this);
            // TODO send serial ACK
            connectionHandler.postDelayed(taskRunnable, Preferences.TIME_INTERVAL);
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        connectionHandler = new Handler();
        threadExecutor = Executors.newSingleThreadExecutor();
        createNotification();
        startForeground(ONGOING_NOTIFICATION_ID, notification);
        startRepeatingTask();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    private void startRepeatingTask() {
        taskRunnable.run();
    }

    void stopRepeatingTask() {
        connectionHandler.removeCallbacks(taskRunnable);
    }

    private void createNotification() {

        String title = getResources().getString(R.string.notification_title);
        String text = getResources().getString(R.string.notification_content);
        int icon = R.drawable.notification;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSmallIcon(icon).getNotification();
        } else {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }

            notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSmallIcon(icon)
                    .build();
        }
    }

    @Override
    public void onDestroy() {
        stopRepeatingTask();
        stopIoManager();
        super.onDestroy();
    }


    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        CharSequence text = sbn.getNotification().tickerText;
        Log.i(TAG, "NOTIFICATION - " + text);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager == null) {
            return;
        }
        try {
            if (text != null && text.toString().contains("Message from")) {
                SerialUtils.sendMessage(SerialUtils.OPEN_GATE_MESSAGE_HEX);
                cancelAllNotifications();
                startCamera();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }


    // CAMERA


    public static void startCamera() {

        try {
            mCamera = Camera.open(0);
            mCamera.setPreviewTexture(new SurfaceTexture(10));
            Camera.Parameters params = mCamera.getParameters();
            params.setPreviewSize(2448, 3264);
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            params.setPictureFormat(ImageFormat.JPEG);
            mCamera.setParameters(params);
            mCamera.startPreview();
            mCamera.takePicture(null, null, mCall);
        } catch (Exception e1) {
            Log.e(TAG, e1.getMessage());
        }
    }

    public static Camera.PictureCallback mCall = new Camera.PictureCallback() {

        public void onPictureTaken(final byte[] data, Camera camera) {
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(System.currentTimeMillis());
            String timeStamp = DateFormat.format("dd-MM-yyyy hh:mm:ss", cal).toString();
            timeStamp += ".jpg";

            File dir = new File(Environment.getExternalStorageDirectory(),
                    context.getResources().getString(R.string.app_name));

            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir.getAbsolutePath(), timeStamp);

            try {
                if (!file.exists())
                    file.createNewFile();

                FileOutputStream out = new FileOutputStream(file);
                out.write(data);
                out.flush();
                out.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            stopCamera();
        }
    };

    public static void stopCamera() {
        if (null == mCamera)
            return;
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

}
