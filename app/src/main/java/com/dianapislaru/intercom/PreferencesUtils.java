package com.dianapislaru.intercom;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PreferencesUtils {

    private static final String KEY_CONTACTS = "CONTACT_";
    private static final int CONTACTS_LIMIT = 30;

    public static void saveStringValue(Context context, String key, String value) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(key, value);
        editor.commit();
    }

    public static void saveIntValue(Context context, String key, int value) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putInt(key, value);
        editor.commit();
    }

    public static String getStringValue(Context context, String key, String defaultValue) {
        SharedPreferences preferences = getPreferences(context);
        return preferences.getString(key, defaultValue);
    }

    public static int getIntValue(Context context, String key, int defaultValue) {
        SharedPreferences preferences = getPreferences(context);
        return preferences.getInt(key, defaultValue);
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.edit();

    }

    private static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static Contact getContact(Context context, int buttonIndex) {
        SharedPreferences preferences = getPreferences(context);
        Gson gson = new Gson();
        String json = preferences.getString(KEY_CONTACTS+buttonIndex, "");
        if(json.equals("")) {
            return null;
        }
        return gson.fromJson(json, Contact.class);
    }

    public static void updateContactList(Context context, ArrayList<Contact> contacts) {
        SharedPreferences.Editor editor = getEditor(context);

        Gson gson = new Gson();
        int size = 0;
        if(contacts != null) {
            size = contacts.size();
        }

        for(int i = 1; i <= CONTACTS_LIMIT; ++i) {
            if(i <= size) {
                editor.putString(KEY_CONTACTS+i, gson.toJson(contacts.get(i-1)));
            } else {
                editor.putString(KEY_CONTACTS+i, "");
            }
        }
        editor.commit();
    }

    public static ArrayList<Contact> getContactList(Context context) {
        SharedPreferences preferences = getPreferences(context);
        Gson gson = new Gson();
        String json;

        ArrayList<Contact> list = new ArrayList<>();

        for(int i = 1; i <= CONTACTS_LIMIT; ++i) {
            json = preferences.getString(KEY_CONTACTS+i, "");
            if(json.equals("")) {
                break;
            }
            list.add(gson.fromJson(json, Contact.class));
        }

        return list;
    }
}
