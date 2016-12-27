package org.apache.cordova.pdfpluginmanager;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Button;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PdfPluginManager extends CordovaPlugin {

  private int READ_EXTERNAL = 0;

  public static final String READ = Manifest.permission.READ_EXTERNAL_STORAGE;
  public static final String WRITE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

  public static CallbackContext callbackContext;

  public static PluginResult result;

  public static List<BtnObject> btnsList = new ArrayList<BtnObject>();

  @Override public boolean execute(String action, JSONArray args, CallbackContext callbackContext)
          throws JSONException {
    PdfPluginManager.callbackContext = callbackContext;
    String url = "";
    String title = "";
    JSONArray btnsArray = null;

    if ("openPdf".equals(action)) {
      try {
        title = args.getString(0);
        url = args.getString(1);
        btnsArray = args.getJSONArray(2);
        btnsList.clear();

        //Make sure we don't have more than 3 buttons
        if(btnsArray.length()>3){
          callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, ""));
          return true;
        }

        for (int j = 0; j < btnsArray.length(); j++) {
          JSONObject jsonobject = btnsArray.getJSONObject(j);
          int id = jsonobject.getInt("id");
          String name = jsonobject.getString("name");
          boolean isDefault = jsonobject.getBoolean("isDefault");

          BtnObject button = new BtnObject(id,name,isDefault);
          btnsList.add(button);
        }

      } catch (Exception e) {
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, ""));
      }
      try {
        this.openPdf(url, title);
      } catch (Exception e) {
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Failed to open pdf"));
      }
      return true;
    }
    if ("closePdf".equals(action)) {
      try {
        PdfUtils.deleteCachedPdfs();
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, ""));
      } catch (ActivityNotFoundException e) {
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, ""));
      }
      return true;
    }
    return false;  // Returning false results in a "MethodNotFound" error.
  }

  private void openPdf(String url, String title) {
    if (!PermissionHelper.hasPermission(this, READ)) {
      PermissionHelper.requestPermissions(this, READ_EXTERNAL, new String[] {READ, WRITE});
    } else {
      Intent intent = new Intent(cordova.getActivity(), PdfActivity.class);
      intent.putExtra("file", url);
      intent.putExtra("title", title);
      cordova.getActivity().startActivity(intent);
    }
  }
}
