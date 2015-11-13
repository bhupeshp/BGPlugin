package com.qt.app.common;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

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

        try {
            if (ACTION_ADD_BGSERVICE.equals(action)) {
                JSONObject arg_object = args.getJSONObject(0);
                String accountName = arg_object.getString("accountName");
                String pwd = arg_object.getString("accountPwd");
                String serverURL = arg_object.getString("serverURL");

                Context context=this.cordova.getActivity().getApplicationContext();
                // use this to start and trigger a service
                Intent i= new Intent(context, BGService.class);
                // potentially add data to the intent
                i.putExtra("accountName", accountName);
                i.putExtra("pwd",pwd);
                i.putExtra("serverURL",serverURL);
                context.startService(i);
                return true;
            }
            callbackContext.error("Invalid action");
            return false;
        }
        catch(Exception err) {
            Toast.makeText(this.cordova.getActivity().getApplicationContext(), err.toString(), Toast.LENGTH_SHORT).show();
            return false;
        }

    }
}