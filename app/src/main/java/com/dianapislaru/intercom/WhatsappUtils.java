package com.dianapislaru.intercom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.util.Log;

public class WhatsappUtils {

    private static final String TAG = WhatsappUtils.class.getSimpleName();

    private static final String whatsapp_package = "com.whatsapp";
    private static final String whatsapp_uri = "content://com.android.contacts/data/";
    private static final String videoMimeType = "vnd.android.cursor.item/vnd.com.whatsapp.video.call";
    private static final String voiceMimeType = "vnd.android.cursor.item/vnd.com.whatsapp.voip.call";

    public static void startVideoCall(Context context, int buttonIndex) {
        Contact contact = PreferencesUtils.getContact(context, buttonIndex);

        if (contact == null) {
            return;
        }

        long whatsappId = contact.getId();

        Log.i(TAG, "CALLING: " +  "# " + buttonIndex + "  " + contact.getName() + " - " + contact.getId());


        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        String uriString = whatsapp_uri + whatsappId;
        intent.setDataAndType(Uri.parse(uriString), videoMimeType);
        intent.setPackage(whatsapp_package);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
