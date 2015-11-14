package com.qt.app.common;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.IntentService;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;


import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by SAMSUNG on 11/13/2015.
 */
public class BGService extends IntentService {

    public BGService() {
        super("BGService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        Log.d("QTBGPlugin", "inside onHandleIntent");
        //TODO do something useful
        /*GET OR CREATE ACCOUNT*/
        AccountManager manager = AccountManager.get(getApplicationContext());
        Account[] account_list = manager.getAccountsByType(intent.getStringExtra("accountName"));
        Account account = null;
        if(account_list == null) {
            Log.d("QTBGPlugin", "inside account_list");
            account = new Account("QtUser", intent.getStringExtra("accountName"));
            manager.addAccountExplicitly(account, intent.getStringExtra("pwd"), null);
        }
        else {
            account = account_list[0];
        }

        Log.d("QTBGPlugin", "b4 query");

        /*FETCH CONTACTS*/
        Cursor mCursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, // URI
                null,                 // The columns to return for each row
                null,          // Data for first contact
                null,              // first contact
                null                   // The sort order for the returned rows
        );

        if(mCursor!=null && mCursor.getCount()>0){
            mCursor.moveToFirst();
            //ITERATE THROUGH PHONE CONTACTS
            for(int i=0;i<mCursor.getCount();i++) {
                String id = mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                String cNumber = "";
                String nameContact = "";
                String hasPhone = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                if (hasPhone.equalsIgnoreCase("1")) {
                    Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);

                    while (phones.moveToNext())
                        cNumber += phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) + "|";

                    phones.close();

                    nameContact = mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                    Toast.makeText(getApplicationContext(), nameContact + " " + cNumber, Toast.LENGTH_SHORT).show();

                    /*SEND AJAX CALL TO RETREIVECONTACT.PHP*/
                    RetrieveContacts rc = new RetrieveContacts();
                    rc.setPhoneNumbers(cNumber);
                    rc.setDisplayName(nameContact);

                    Log.d("QTBGPlugin", "b4 rc.execute");

                    rc.execute(intent.getStringExtra("serverURL"));


                }

            }

        }

        //return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }


    private class RetrieveContacts extends AsyncTask <String, String, String>{
        protected String phoneNumbers = "";
        protected String displayName = "";
        protected void setPhoneNumbers(String arg){
            phoneNumbers = arg;
        }

        protected void setDisplayName(String arg){
            displayName = arg;
        }

        @Override
        protected String doInBackground(String ... furl){
            try {
                URL urlObj = new URL(furl[0]);
                HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                conn.setRequestMethod("POST");

                // read the response
                System.out.println("Response Code: " + conn.getResponseCode());
                InputStream in = new BufferedInputStream(conn.getInputStream());
                //String response = IOUtils.toString(in, "UTF-8");
                System.out.println(in.toString());
                return in.toString();
            }
            catch(Exception err){
                return "error";
            }

        }

        @Override
        protected void onPostExecute(String result) {
            if(!result.equals("NOTFOUND")) {


                Intent insertIntent = new Intent(ContactsContract.Intents.Insert.ACTION);
                insertIntent.setType(ContactsContract.RawContacts.CONTENT_TYPE);

                insertIntent.putExtra(ContactsContract.Intents.Insert.NAME, displayName);

                // Defines an array list to contain the ContentValues objects for each row
                ArrayList<ContentValues> contactData = new ArrayList<ContentValues>();

                // Sets up the row as a ContentValues object
                ContentValues rawContactRow = new ContentValues();

                // Adds the account type and name to the row
                rawContactRow.put(ContactsContract.RawContacts.ACCOUNT_TYPE, "Qt");
                rawContactRow.put(ContactsContract.RawContacts.ACCOUNT_NAME, "QtUser");

                // Adds the row to the array
                contactData.add(rawContactRow);
                // Sets up the row as a ContentValues object
                ContentValues phoneRow = new ContentValues();

                // Specifies the MIME type for this data row (all data rows must be marked by their type)
                phoneRow.put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                );

                // Adds the phone number and its type to the row
                phoneRow.put(ContactsContract.CommonDataKinds.Phone.NUMBER, result.toString());

                // Adds the row to the array
                contactData.add(phoneRow);

				/*
				 * Adds the array to the intent's extras. It must be a parcelable object in order to
				 * travel between processes. The device's contacts app expects its key to be
				 * Intents.Insert.DATA
				 */
                insertIntent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, contactData);

                // Send out the intent to start the device's contacts app in its add contact activity.
                startActivity(insertIntent);

                //super.onPostExecute(result);
            }
        }

    }

}
