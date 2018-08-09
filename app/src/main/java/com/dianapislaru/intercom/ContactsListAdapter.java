package com.dianapislaru.intercom;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class ContactsListAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<Contact> contacts;
    private static LayoutInflater inflater=null;

    public ContactsListAdapter(Activity activity, ArrayList<Contact> contacts) {
        this.activity = activity;
        this.contacts = contacts;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return contacts.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.list_row, null);

        TextView contactTitle = vi.findViewById(R.id.list_row_title);
        TextView contactName = vi.findViewById(R.id.list_row_subtitle);

        contactTitle.setText(activity.getResources().getString(R.string.contact_list_item_title, position+1));
        contactName.setText(contacts.get(position).getName());
        return vi;
    }
}
