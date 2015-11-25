package com.qt.app.common;


import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by SAMSUNG on 11/13/2015.
 */
public class BGPlugin extends CordovaPlugin {

    public static final String ACTION_ADD_BGSERVICE = "addBGService";


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        Log.d("QTBGPlugin", "inside execute");

        try {
            if (ACTION_ADD_BGSERVICE.equals(action)) {
                Log.d("QTBGPlugin", "inside execute");
                JSONObject arg_object = args.getJSONObject(0);
                String contactId = arg_object.getString("contactId");

                Context context=this.cordova.getActivity().getApplicationContext();
                Cursor qCursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + "='vnd.android.cursor.item/QT'", new String[]{contactId}, null);

                if(qCursor!=null && qCursor.getCount()>0){
                    qCursor.close();
                    callbackContext.success();
                    return true;
                }
                qCursor.close();
                callbackContext.error("not_found");
                return false;
            }
            callbackContext.error("Invalid_action");
            return false;
        }
        catch(Exception err) {
            Log.d("QTBGPlugin", "inside catch");
            callbackContext.error("Error in BGPlugin" + err);
            return false;
        }

    }

}
