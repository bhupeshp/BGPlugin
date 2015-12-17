package com.qt.app.common;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.R;

/**
 * Created by QTTeam on 11/13/2015.
 */
public class BGPlugin extends CordovaPlugin {

    public static final String ACTION_ADD_BGSERVICE = "addBGService";
    public static final String ACTION_GET_OWNERNAME = "getOwnerName";
    public static final String ACTION_GET_NOTIFICATION = "getPersistentNotification";
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
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

        Log.d("QTBGPlugin", "inside execute");

        try {
            if (ACTION_ADD_BGSERVICE.equals(action)) {
                cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        contentResolver = cordova.getActivity().getApplicationContext().getContentResolver();
                        Log.d("QTBGPlugin", "inside execute");


                        JSONArray contactList = new JSONArray();
                        String[] projection = new String[]{CONTACT_ID, DISPLAY_NAME, HAS_PHONE_NUMBER};
                        Cursor cursor = contentResolver.query(QUERY_URI, projection, null, null,
                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

                        while (cursor.moveToNext()) {
                            int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));
                            if (hasPhoneNumber > 0)
                                contactList.put(getContact(cursor));
                        }
                        callbackContext.success(contactList);
                        cursor.close();

                    }
                });


                return true;
            }

            if (ACTION_GET_OWNERNAME.equals(action)) {
                contentResolver = cordova.getActivity().getApplicationContext().getContentResolver();
                Cursor c = contentResolver.query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);
                int count = c.getCount();
                String[] columnNames = c.getColumnNames();
                boolean b = c.moveToFirst();
                int position = c.getPosition();
                JSONArray ownerInfoList = new JSONArray();
                if (count == 1 && position == 0) {
                    for (int j = 0; j < columnNames.length; j++) {
                        String columnName = columnNames[j];
                        String columnValue = c.getString(c.getColumnIndex(columnName));
                        JSONObject ownerInfo = new JSONObject();
                        ownerInfo.put(columnName,columnValue);
                        ownerInfoList.put(ownerInfo);
                    }
                }
                c.close();
                callbackContext.success(ownerInfoList);

                return true;
            }

            if(ACTION_GET_NOTIFICATION.equals(action)){
                // Use NotificationCompat.Builder to set up our notification.
                NotificationCompat.Builder builder = new NotificationCompat.Builder(cordova.getActivity().getApplicationContext());

                //icon appears in device notification bar and right hand corner of notification
                builder.setSmallIcon(cordova.getActivity().getApplicationContext().getApplicationInfo().icon);

                // This intent is fired when notification is clicked
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://"));
                Intent intentScan = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://"));
                Intent intentVCard = new Intent(Intent.ACTION_VIEW, Uri.parse("http://facebook.com/"));
                Intent intentExit = new Intent(Intent.ACTION_VIEW, Uri.parse("http://youtube.com/"));

                PendingIntent pendingIntent = PendingIntent.getActivity(cordova.getActivity().getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                PendingIntent pendingIntentScan = PendingIntent.getActivity(cordova.getActivity().getApplicationContext(), 0, intentScan, PendingIntent.FLAG_UPDATE_CURRENT);

                PendingIntent pendingIntentVCard = PendingIntent.getActivity(cordova.getActivity().getApplicationContext(), 0, intentVCard, PendingIntent.FLAG_UPDATE_CURRENT);

                PendingIntent pendingIntentExit = PendingIntent.getActivity(cordova.getActivity().getApplicationContext(), 0, intentExit, PendingIntent.FLAG_UPDATE_CURRENT);

                // Set the intent that will fire when the user taps the notification.
                builder.setContentIntent(pendingIntent);

                // Large icon appears on the left of the notification
                builder.setLargeIcon(BitmapFactory.decodeResource(cordova.getActivity().getApplicationContext().getResources(), cordova.getActivity().getApplicationContext().getApplicationInfo().icon));

                // Content title, which appears in large type at the top of the notification
                builder.setContentTitle("Notifications Title");

                // Content text, which appears in smaller text below the title
                builder.setContentText("Your notification content here.");

                builder.setAutoCancel(false);

                // The subtext, which appears under the text on newer devices.
                // This will show-up in the devices with Android 4.2 and above only
                builder.setSubText("Tap to view documentation about notifications.");

                builder.addAction(R.drawable.ic_menu_camera, "SCAN", pendingIntentScan);
                builder.addAction(R.drawable.ic_menu_share, "Qt Card", pendingIntentVCard);
                builder.addAction(R.drawable.ic_menu_close_clear_cancel, "Exit", pendingIntentExit);

                NotificationManager notificationManager = (NotificationManager) cordova.getActivity().getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

                // Will display the notification in the notification bar
                notificationManager.notify(0, builder.build());

                callbackContext.success();
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
