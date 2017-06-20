package com.securingapps.rps.data;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Data;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import com.securingapps.rps.utils.async.AsyncFuture;

import java.util.ArrayList;

/**
 * @author ldavid
 * @created 3/3/17
 */
public class ContactService {

    private static final String LOG = ContactService.class.getSimpleName();

    private static final String[] LIST_PROJECTION = {
            Contacts._ID,
            Contacts.LOOKUP_KEY,
            Contacts.DISPLAY_NAME,
    };

    private static final String[] RAW_PROJECTION = {
            RawContacts._ID,
            RawContacts.CONTACT_ID,
            RawContacts.ACCOUNT_TYPE,
            RawContacts.ACCOUNT_NAME,
    };

    private static final String[] DETAIL_PROJECTION = {
            Data._ID,
            Data.RAW_CONTACT_ID,
            Data.MIMETYPE,
            Data.DATA1,
            Data.DATA2,
            Data.DATA3,
            Data.DATA4
    };
    private static final String DETAIL_SELECTION = Data.MIMETYPE + " IN (?, ?, ?, ?)";

    private static final String[] DETAIL_SELECTION_ARGS = {
            "vnd.android.cursor.item/name",
            "vnd.android.cursor.item/phone_v2",
            "vnd.android.cursor.item/email_v2",
            "vnd.android.cursor.item/vnd.com.whatsapp.profile"
    };

    private static final String RAW_SELECTION = RawContacts.CONTACT_ID + " = ?";

    private static ContactService _instance;

    public static ContactService getInstance() {
        if (_instance == null) {
            _instance = new ContactService();
        }

        return _instance;
    }

    private boolean loaded = false;
    private Contact profile = null;
    private ContactCollection friends = new ContactCollection();

    private ContactService() {
    }

    public Contact getProfile() {
        return profile;
    }

    public ContactCollection getFriends() {
        return friends;
    }

    public AsyncFuture<ContactService> loadAsync(final Context context) {
        return loadAsync(context, false);
    }

    public AsyncFuture<ContactService> loadAsync(final Context context, final boolean sync) {
        AsyncFuture<ContactService> future = AsyncFuture.supplyAsync(() -> {
            if (!loaded) {
                ContactService.this.load(context);
            }
            return ContactService.this;
        });
        future = future.whenCompleteAsync(service -> {
            if (sync) ContactService.this.sync(context);
        });
        return future;
    }

    public AsyncFuture<Void> syncAsync(final Context context) {
        return AsyncFuture.runAsync(() -> ContactService.this.sync(context));
    }

    public AsyncFuture<Contact> dupeAsync(Context context, long contactId, String contactKey, String target) {
        return AsyncFuture.supplyAsync(() -> ContactService.this.dupe(context, contactId, contactKey, target));
    }

    private void sync(Context context) {
        Log.d(LOG, "Syncing Contacts");

        ApiProxy.getInstance().syncContacts(this, context);
    }

    private Contact dupe(Context context, long contactId, String contactKey, String target) {
        if (!loaded) load(context);

        Log.d(LOG, String.format("Duping Contact %d/%s", contactId, contactKey));
        Contact friend = friends.find(contact -> contact.getId() == contactId && contact.getKey().equals(contactKey));
        if (friend == null) {
            Log.e(LOG, "Contact not found");
            return null;
        }

        Log.i(LOG, String.format("Found Contact %s", friend.getName()));

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        int rawContactInsertIndex = ops.size();

        ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_TYPE, null)
                .withValue(RawContacts.ACCOUNT_NAME, null).build());

        //Display name/Contact name
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                .withValue(StructuredName.DISPLAY_NAME, " " + friend.getName())
                .build());

        //Phone Number
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                .withValue(Phone.NUMBER, target)
                .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                .withValue(Phone.TYPE, "1").build());


        try {
            ContentProviderResult[] res = context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);

            Log.i(LOG, "Contact Dupe'd");
            for (ContentProviderResult re : res) {
                Log.d(LOG, re.uri.toString());
            }

            load(context);
            // sync(context);
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            Log.e(LOG, "Failed saving contact", ex);
        }

        return null;
    }

    private void load(Context context) {
        Log.d(LOG, "Loading Contacts");
        this.profile = loadProfile(context);
        this.friends = loadFriends(context);
    }

    private Contact loadProfile(Context context) {
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

    private ContactCollection loadFriends(Context context) {
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
