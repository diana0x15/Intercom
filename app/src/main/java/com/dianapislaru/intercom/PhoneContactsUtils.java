package com.dianapislaru.intercom;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

public class PhoneContactsUtils {

    private static final String videoMimeType = "vnd.android.cursor.item/vnd.com.whatsapp.video.call";

    public static List<Contact> getAllContacts(Context context) {
        return fetchContactsWithWhatsapp(context);
    }

    public static String[] getNames(Context context) {
        List<Contact> contacts = getAllContacts(context);
        if(contacts == null) {
            return null;
        }
        String[] names = new String[contacts.size()];
        int i = 0;
        for(Contact c : contacts) {
            names[i++] = c.getName();
        }
        return names;
    }

    public static Contact getContactFromName(Context context, String name) {
        List<Contact> contacts = getAllContacts(context);
        for(Contact c : contacts) {
            if(c.getName().equals(name)) {
                return c;
            }
        }
        return null;
    }

    public static Contact getFirstContact(Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;

        Contact firstContact = null;

        try {
            cursor = resolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    null, null, null,
                    ContactsContract.Contacts.DISPLAY_NAME);

            if(cursor == null) {
                return null;
            }

            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(ContactsContract.Data._ID));
                String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                String mimeType = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE));

                if (displayName != null && mimeType.equals(videoMimeType)) {
                    firstContact = new Contact(displayName, id);
                    break;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return firstContact;
    }

    // Find the Whatsapp ID of contact with given name
    private static List<Contact> fetchContactsWithWhatsapp(Context context) {

        List<Contact> contacts = new ArrayList<>();

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;

        try {
            cursor = resolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    null, null, null,
                    ContactsContract.Contacts.DISPLAY_NAME);

            if(cursor == null) {
                return null;
            }

            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(ContactsContract.Data._ID));
                String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                String mimeType = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE));

                if (displayName != null && mimeType.equals(videoMimeType)) {
                    contacts.add(new Contact(displayName, id));
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }


        return contacts;
    }

}
