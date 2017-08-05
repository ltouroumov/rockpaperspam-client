package com.tricktrap.rps.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

/**
 * @author ldavid
 * @created 8/5/17
 */
public class ContactLoader {

    private static final String LOG = ContactLoader.class.getSimpleName();

    private static final String[] LIST_PROJECTION = {
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.LOOKUP_KEY,
        ContactsContract.Contacts.DISPLAY_NAME,
    };

    private static final String[] RAW_PROJECTION = {
        ContactsContract.RawContacts._ID,
        ContactsContract.RawContacts.CONTACT_ID,
        ContactsContract.RawContacts.ACCOUNT_TYPE,
        ContactsContract.RawContacts.ACCOUNT_NAME,
    };

    private static final String[] DETAIL_PROJECTION = {
        ContactsContract.Contacts.Data._ID,
        ContactsContract.Contacts.Data.RAW_CONTACT_ID,
        ContactsContract.Contacts.Data.MIMETYPE,
        ContactsContract.Contacts.Data.DATA1,
        ContactsContract.Contacts.Data.DATA2,
        ContactsContract.Contacts.Data.DATA3,
        ContactsContract.Contacts.Data.DATA4
    };
    private static final String DETAIL_SELECTION = ContactsContract.Contacts.Data.MIMETYPE + " IN (?, ?, ?, ?)";

    private static final String[] DETAIL_SELECTION_ARGS = {
        "vnd.android.cursor.item/name",
        "vnd.android.cursor.item/phone_v2",
        "vnd.android.cursor.item/email_v2",
        "vnd.android.cursor.item/vnd.com.whatsapp.profile"
    };

    private static final String RAW_SELECTION = ContactsContract.RawContacts.CONTACT_ID + " = ?";

    public Contact loadProfile(Context context) {
        Cursor cursor = context.getContentResolver()
            .query(
                ContactsContract.Profile.CONTENT_URI,
                LIST_PROJECTION,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME
            );

        Contact contact = new Contact();

        if (cursor.moveToNext()) {
            // Get the _ID value
            long contactId = cursor.getLong(0);
            // Get the selected LOOKUP KEY
            String contactKey = cursor.getString(1);
            String displayName = cursor.getString(2);

            contact.setId(contactId);
            contact.setKey(contactKey);
            contact.setName(displayName);

            Log.d(LOG, String.format("Contact: %d, %s, %s", contactId, contactKey, displayName));
            contact.getRawContacts().add(new Contact.Raw(contactId, "com.android.profile", "Profile"));

            loadRawContacts(context, contact);
            loadContactDetail(context, contact);
        }
        cursor.close();

        return contact;
    }

    public ContactCollection loadFriends(Context context) {
        Cursor cursor = context.getContentResolver()
            .query(
                ContactsContract.Contacts.CONTENT_URI,
                LIST_PROJECTION,
                ContactsContract.Contacts.HAS_PHONE_NUMBER + " = 1",
                null,
                ContactsContract.Contacts.DISPLAY_NAME
            );

        ContactCollection contacts = new ContactCollection();

        while (cursor.moveToNext()) {
            // Get the _ID value
            long contactId = cursor.getLong(0);
            // Get the selected LOOKUP KEY
            String contactKey = cursor.getString(1);
            String displayName = cursor.getString(2);

            Contact contact = new Contact();
            contact.setId(contactId);
            contact.setKey(contactKey);
            contact.setName(displayName);

            // Log.d(LOG, String.format("Contact: %d, %s, %s", contactId, contactKey, displayName));

            loadRawContacts(context, contact);
            loadContactDetail(context, contact);

            if (contact.getName() != null) {
                contacts.add(contact);
            }
        }

        cursor.close();

        return contacts;
    }

    private void loadRawContacts(Context context, Contact contactData) {
        if (contactData.getId() == 0) {
            Log.e(LOG, "Can't load raw contacts for ID 0");
            return;
        }

        String contactSearchId = Long.toString(contactData.getId());
        Cursor cursor = context.getContentResolver()
            .query(
                ContactsContract.RawContacts.CONTENT_URI,
                RAW_PROJECTION,
                RAW_SELECTION,
                new String[] { contactSearchId },
                null
            );

        while (cursor.moveToNext()) {
            long _id = cursor.getLong(0);
            long contactId = cursor.getLong(1);
            String accountType = cursor.getString(2);
            String accountName = cursor.getString(3);

            if (accountType == null && accountName == null) {
                accountType = "local";
                accountName = "Local";
            }

            // Log.d(LOG, String.format("RawContact: %d, %d, %s, %s", _id, contactId, accountName, accountType));
            contactData.getRawContacts().add(new Contact.Raw(_id, accountType, accountName));
        }

        cursor.close();
    }

    private void loadContactDetail(Context context, Contact contactData) {
        if (contactData.getUri() == null) {
            Log.e(LOG, String.format("Can't load contacts details for ID %d, uri is null", contactData.getId()));
            return;
        }

        Uri contactUri = contactData.getUri();
        Uri cursorUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Data.CONTENT_DIRECTORY);
        // Starts the query
        Cursor cursor = context.getContentResolver()
            .query(
                cursorUri,
                DETAIL_PROJECTION,
                DETAIL_SELECTION,
                DETAIL_SELECTION_ARGS,
                null
            );

        while (cursor.moveToNext()) {
            long id = cursor.getLong(0);
            long rawContactId = cursor.getLong(1);
            String mimeType = cursor.getString(2);
            String data1 = cursor.getString(3);
            String data2 = cursor.getString(4);
            String data3 = cursor.getString(5);
            String data4 = cursor.getString(6);

            // Log.d(LOG, String.format("ContactInfo: %d, %d, %s, %s, %s, %s, %s", id, rawContactId, mimeType, data1, data2, data3, data4));

            switch (mimeType) {
                case "vnd.android.cursor.item/name":
                    if (data1 != null) {
                        Contact.Data name = new Contact.Data(Contact.DataType.NAME, data1);
                        contactData.getRawContact(rawContactId).getData().add(name);
                    }
                    break;
                case "vnd.android.cursor.item/phone_v2":
                    if (data4 != null) {
                        Contact.Data phone = new Contact.Data(Contact.DataType.PHONE, data4);
                        contactData.getRawContact(rawContactId).getData().add(phone);
                    }
                    break;
                case "vnd.android.cursor.item/email_v2":
                    if (data1 != null) {
                        Contact.Data email = new Contact.Data(Contact.DataType.EMAIL, data1);
                        contactData.getRawContact(rawContactId).getData().add(email);
                    }
                    break;
                case "vnd.android.cursor.item/vnd.com.whatsapp.profile":
                    if (data1 != null) {
                        Contact.Data whatsapp = new Contact.Data(Contact.DataType.WHATSAPP, data1);
                        contactData.getRawContact(rawContactId).getData().add(whatsapp);
                    }
                    break;
            }
        }

        cursor.close();
    }


}
