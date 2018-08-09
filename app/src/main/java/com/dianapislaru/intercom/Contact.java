package com.dianapislaru.intercom;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class Contact {

    private String name;
    private long id;

    public Contact(String name, long id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public static ArrayList<Contact> fromJsonList(List<String> jsonList) {

        if(jsonList == null) {
            return null;
        }

        Gson gson = new Gson();
        ArrayList<Contact> contacts = new ArrayList<>();
        for(String json : jsonList) {
            contacts.add(gson.fromJson(json, Contact.class));
        }
        return contacts;
    }

    public static List<String> toJsonList(List<Contact> contactList) {

        if(contactList == null) {
            return null;
        }

        Gson gson = new Gson();
        ArrayList<String> jsonList = new ArrayList<>();
        for(Contact c : contactList) {
            jsonList.add(gson.toJson(c));
        }
        return jsonList;
    }
}
