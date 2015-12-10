package com.qt.app.common;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by QTTeam on 11/13/2015.
 */
public class BGPlugin extends CordovaPlugin {

    public static final String ACTION_ADD_BGSERVICE = "addBGService";
    private Uri QUERY_URI = ContactsContract.Contacts.CONTENT_URI;
    private String CONTACT_ID = ContactsContract.Contacts._ID;
    private String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
    private Uri EMAIL_CONTENT_URI = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
    private String EMAIL_CONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
    private String EMAIL_DATA = ContactsContract.CommonDataKinds.Email.DATA;
    private String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
    private String PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
    private Uri PHONE_CONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
    private String PHONE_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
    private Uri CONTENT_DATA_URI = ContactsContract.Data.CONTENT_URI;
    private ContentResolver contentResolver;


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        Log.d("QTBGPlugin", "inside execute");

        try {
            if (ACTION_ADD_BGSERVICE.equals(action)) {

                contentResolver = this.cordova.getActivity().getApplicationContext().getContentResolver();
                Log.d("QTBGPlugin", "inside execute");


                JSONArray contactList = new JSONArray();
                String[] projection = new String[]{CONTACT_ID, DISPLAY_NAME, HAS_PHONE_NUMBER};
                Cursor cursor = contentResolver.query(QUERY_URI, projection, null, null,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC");

                while (cursor.moveToNext()) {
                    contactList.put(getContact(cursor));
                }
                callbackContext.success(contactList);
                cursor.close();
                return true;
            }

            callbackContext.error("not_found");
            return false;
        }
        catch(Exception err) {
            Log.d("QTBGPlugin", "inside catch");
            callbackContext.error("Error in BGPlugin" + err);
            return false;
        }

    }

    private JSONObject getContact(Cursor cursor) {
        String contactId = cursor.getString(cursor.getColumnIndex(CONTACT_ID));
        String name = (cursor.getString(cursor.getColumnIndex(DISPLAY_NAME)));

        String phones = getPhone(cursor, contactId);
        String qtFlag = getQTFlag(cursor, contactId);

        JSONObject contact = new JSONObject();
        try {
            contact.put("contactid", contactId);
            contact.put("name", name);
            contact.put("phones", phones);
            contact.put("qtFlag", qtFlag);
        }
        catch(Exception err) {
            Log.d("QTBGPlugin", "inside getContact catch" + err);
        }
        Log.d("QTBGPlugin", "getContact : "+contact.toString());
        return contact;
    }

    private String getPhone(Cursor cursor, String contactId) {
        String phoneNumbers = "";
        int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));
        if (hasPhoneNumber > 0) {
            Cursor phoneCursor = contentResolver.query(PHONE_CONTENT_URI, null, PHONE_CONTACT_ID + " = ?", new String[]{contactId}, null);
            while (phoneCursor.moveToNext()) {
                String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(PHONE_NUMBER));
                phoneNumbers += phoneNumber + ",";
            }
            phoneNumbers = phoneNumbers.substring(0,phoneNumbers.length());
            phoneCursor.close();
        }
        return phoneNumbers;
    }

    private String getQTFlag(Cursor cursor, String contactId) {
        Cursor qtCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + "='vnd.android.cursor.item/QT'", new String[]{contactId}, null);
        if(qtCursor.getCount()>0)
            return "1";
        return "0";
    }



}
