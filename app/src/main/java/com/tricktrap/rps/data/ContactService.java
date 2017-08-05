package com.tricktrap.rps.data;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Data;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import com.tricktrap.rps.MyFirebaseMessagingService;
import com.tricktrap.rps.utils.async.AsyncFuture;

import java.util.ArrayList;

/**
 * @author ldavid
 * @created 3/3/17
 */
public class ContactService {

    private static final String LOG = ContactService.class.getSimpleName();

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
    private ContactLoader loader;
    private ContactManager manager;

    private ContactService() {
        loader = new ContactLoader();
        manager = new ContactManager(this);
    }

    public ContactManager getManager() {
        return manager;
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
                profile = loader.loadProfile(context);
                friends = loader.loadFriends(context);
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

    public void sync(Context context) {
        Log.d(LOG, "Syncing Contacts");

        ApiProxy.getInstance().syncContacts(this, context);
    }

}
