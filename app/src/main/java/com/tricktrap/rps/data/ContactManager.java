package com.tricktrap.rps.data;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import com.tricktrap.rps.MyFirebaseMessagingService;

import java.util.ArrayList;

/**
 * @author ldavid
 * @created 8/5/17
 */
public class ContactManager {

    private static final String LOG = ContactManager.class.getSimpleName();

    private ContactService service;

    ContactManager(ContactService service) {
        this.service = service;
    }

    public void duplicate(Context context, long contactId, String contactKey, String target) {

        Log.d(LOG, String.format("Duping Contact %d/%s", contactId, contactKey));
        Contact friend = service.getFriends().find(contact -> contact.getId() == contactId && contact.getKey().equals(contactKey));
        if (friend == null) {
            Log.e(LOG, "Contact not found");
            return;
        }

        Log.i(LOG, String.format("Found Contact %s", friend.getName()));

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        int rawContactInsertIndex = ops.size();

        ops.add(ContentProviderOperation
            .newInsert(ContactsContract.RawContacts.CONTENT_URI)
            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
            .build());

        //Display name/Contact name
        ops.add(ContentProviderOperation
            .newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactInsertIndex)
            .withValue(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, " " + friend.getName())
            .build());

        //Phone Number
        ops.add(ContentProviderOperation
            .newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
            .withValue(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, target)
            .withValue(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, "1").build());


        saveOps(context, ops);

    }

    public void create(Context context, Contact.Raw payload) {

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        int rawContactInsertIndex = ops.size();

        ops.add(ContentProviderOperation
            .newInsert(ContactsContract.RawContacts.CONTENT_URI)
            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
            .build());

        for (Contact.Data datum : payload.getData()) {
            switch (datum.getType()) {
                case NAME:
                    //Display name/Contact name
                    ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, datum.getValue())
                        .build());
                    break;
                case PHONE:
                    //Phone Number
                    ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, datum.getValue())
                        .withValue(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, "1").build());
                    break;
            }
        }

        saveOps(context, ops);
    }

    public void delete(Context context, long contactId, String contactKey) {

        Uri contactUri = ContactsContract.Contacts.getLookupUri(contactId, contactKey);

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        ops.add(ContentProviderOperation.newDelete(contactUri).build());

        saveOps(context, ops);

    }

    private void saveOps(Context context, ArrayList<ContentProviderOperation> ops) {
        try {
            ContentProviderResult[] res = context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);

            Log.i(LOG, "Ops saved");
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            Log.e(LOG, "Failed to save ops", ex);
        }
    }
}
