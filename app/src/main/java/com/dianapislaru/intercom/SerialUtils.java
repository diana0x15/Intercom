package com.dianapislaru.intercom;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Looper;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.List;

public class SerialUtils {

    private static final String TAG = SerialUtils.class.getSimpleName();
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final int BAUD_RATE = 19200;
    private static final int DATA_BITS = 8;
    private static final int WRITE_TIMEOUT_MILLIS = 1;
    public static final String ALIVE_MESSAGE_HEX = "AA";
    public static final String OPEN_GATE_MESSAGE_HEX = "FD";

    private static UsbDeviceConnection usbDeviceConnection;
    public static UsbSerialPort sPort;
    private static SerialInputOutputManager serialIoManager;

    private static long lastMessageTime;
    private static Context context;

    private static SerialInputOutputManager.Listener messageListener
            = new SerialInputOutputManager.Listener() {

        @Override
        public void onNewData(byte[] data) {
            long time = System.currentTimeMillis();
            if (time > lastMessageTime + 200) {
                lastMessageTime = time;
                processReceivedData(data);
            }
        }

        @Override
        public void onRunError(Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Runner stopped: " + e.getMessage());
        }
    };



    public static void processReceivedData(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02X", b));
        }
        String message = sb.toString();
        Log.i(TAG, "READ: " + message);
        if(context != null) {
            try {
                int buttonIndex = Integer.parseInt(message);
                BackgroundService.startCamera();
                WhatsappUtils.startVideoCall(context, buttonIndex);
            } catch (NumberFormatException e) {

            }
        }
    }

    public static void sendMessage(String message) {
        if (sPort == null || usbDeviceConnection == null) {
            return;
        }

        int len = message.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(message.charAt(i), 16) << 4)
                    + Character.digit(message.charAt(i+1), 16));
        }
        try {
            sPort.write(data, WRITE_TIMEOUT_MILLIS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void connectToSerial(Context c) {
        if(sPort != null) {
            return;
        }

        context = c;

        // Find all available drivers from attached devices.
        UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        if(manager == null) {
            return;
        }

        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }

        // Open a connection to the first available driver.
        UsbDeviceConnection connection = manager.openDevice(availableDrivers.get(0).getDevice());
        if (connection == null) {
            return;
        }

        sPort = availableDrivers.get(0).getPorts().get(0);

        usbDeviceConnection = manager.openDevice(sPort.getDriver().getDevice());
        if (usbDeviceConnection == null) {
            return;
        }


        try {
            sPort.open(usbDeviceConnection);
            sPort.setParameters(BAUD_RATE, DATA_BITS, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        } catch (IOException e) {
            try {
                sPort.close();
            } catch (IOException e2) {
                // Ignore.
            }
            sPort = null;
            return;
        }


        startIoManager();
    }

    public static void stopIoManager() {
        if(sPort != null) {
            try {
                sPort.close();
                sPort = null;
            } catch (IOException e) {

            }
        }

        if(usbDeviceConnection != null) {
            usbDeviceConnection.close();
        }

        if (serialIoManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            serialIoManager.stop();
            serialIoManager = null;
        }
    }

    public static void startIoManager() {
        if (sPort != null) {
            Log.i(TAG, "Starting io manager ..");
            serialIoManager = new SerialInputOutputManager(sPort, messageListener);
            BackgroundService.threadExecutor.submit(serialIoManager);
        }
    }

}
