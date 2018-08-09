package com.dianapislaru.intercom;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    FloatingActionButton addButton;
    ListView contactsListView;
    ContactsListAdapter contactsAdapter;
    ArrayList<Contact> contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        addButton = findViewById(R.id.add_button);
        contactsListView = findViewById(R.id.contacts_list);

        contacts = PreferencesUtils.getContactList(this);

        if(contacts == null) {
            contacts = new ArrayList<>();
        }

        contactsAdapter = new ContactsListAdapter(this, contacts);
        contactsListView.setAdapter(contactsAdapter);


        contactsListView.setOnItemClickListener((adapterView, view, i, l) -> {
            showContactDialog(i);
        });

        addButton.setOnClickListener(view -> {
            Contact contact = PhoneContactsUtils.getFirstContact(this);
            if(contact == null) {
                Toast.makeText(this, getResources().getString(R.string.no_contacts), Toast.LENGTH_LONG).show();
                return;
            }
            showContactDialog(-1);
        });

    }

    public void showContactDialog(int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle(getResources().getString(R.string.contacts));

        String[] items = PhoneContactsUtils.getNames(this);

        if(items == null) {
            Toast.makeText(this, getResources().getString(R.string.no_contacts), Toast.LENGTH_LONG).show();
            return;
        }

        int checkedIndex = 0;
        if(index != -1) {
            String selectedName = contacts.get(index).getName();
            for(int i = 0; i < items.length; ++i) {
                if(selectedName.equals(items[i])) {
                    checkedIndex = i;
                }
            }
        }


        //list of items
        builder.setSingleChoiceItems(items, checkedIndex,
                (dialog, which) -> {
                    // item selected logic
                });

        String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText,
                (dialog, which) -> {
                    // positive button logic
                    int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    Contact correspondingContact = PhoneContactsUtils.getContactFromName(SettingsActivity.this, items[selectedPosition]);
                    if(correspondingContact == null) {
                        return;
                    }
                    if(index >= contacts.size() || index == -1) {
                        contacts.add(correspondingContact);
                    } else {
                        contacts.set(index, correspondingContact);
                    }
                    PreferencesUtils.updateContactList(SettingsActivity.this, contacts);
                    contactsAdapter.notifyDataSetChanged();
                });

        String negativeText = getString(android.R.string.cancel);
        builder.setNegativeButton(negativeText,
                (dialog, which) -> {
                    // negative button logic
                });

        if(index != -1) {
            String deleteText = getResources().getString(R.string.delete);
            builder.setNeutralButton(deleteText,
                    (dialogInterface, i) -> {
                        contacts.remove(index);
                        PreferencesUtils.updateContactList(SettingsActivity.this, contacts);
                        contactsAdapter.notifyDataSetChanged();
                    });
        }


        AlertDialog dialog = builder.create();
        dialog.show();
    }



}
