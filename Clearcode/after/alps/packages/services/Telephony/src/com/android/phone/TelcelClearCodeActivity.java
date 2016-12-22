package com.android.phone;

import java.io.File;
import java.io.FileWriter;
import android.widget.Toast;
import android.net.Uri;
import android.content.Intent;
import android.content.Context;
import android.util.Log;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import android.os.Environment;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.PluginState;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class TelcelClearCodeActivity extends Activity
{
  private final String TAG = "TelcelClearCodeActivity";

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    	super.onCreate(savedInstanceState);
	Log.v(TAG, "onCreate");
	AlertDialog alert = new AlertDialog.Builder(this)
            .setTitle("")
            .setMessage("SIN SUBSCRIPCION AL SERVICIO -33-")
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
  		finish();
                    	}
		})
            .setCancelable(false)
            .show();
  }  
}

