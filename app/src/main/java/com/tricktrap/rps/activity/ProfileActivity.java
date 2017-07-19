package com.tricktrap.rps.activity;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.tricktrap.rps.R;
import com.tricktrap.rps.data.ContactService;

import java.util.ArrayList;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = ProfileActivity.class.getSimpleName();

    private EditText name;
    private EditText phone;
    private Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        saveBtn = (Button) findViewById(R.id.save);
        saveBtn.setOnClickListener(this::doSave);

        name = (EditText) findViewById(R.id.name);
        phone = (EditText) findViewById(R.id.phone);
        phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
    }

    private void doSave(View view) {

        saveBtn.setEnabled(false);

        String displayName = name.getText().toString();
        String phoneNumber = phone.getText().toString();

        String country = Locale.getDefault().getCountry();
        phoneNumber = PhoneNumberUtils.formatNumberToE164(phoneNumber, country);

        Log.d(TAG, String.format("Saving Contact %s %s (%s)", displayName, phoneNumber, country));

        writeToContacts(displayName, phoneNumber);

        ContactService.getInstance()
                .loadAsync(this, false)
                .whenComplete(service -> {
                    Intent intent = new Intent(this, RegisterActivity.class);
                    startActivity(intent);
                });
    }

    private void writeToContacts(String displayName, String phoneNumber) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        int rawContactInsertIndex = ops.size();

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Profile.CONTENT_RAW_CONTACTS_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());

        //Display name/Contact name
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
                .build());

        //Phone Number
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                .withValue(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, "1").build());


        try {
            ContentProviderResult[] res = getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);

            Log.i(TAG, "Contact Saved");
            for (ContentProviderResult re : res) {
                Log.d(TAG, re.uri.toString());
            }
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            Log.e(TAG, "Failed saving contact", ex);
        }
    }
}
