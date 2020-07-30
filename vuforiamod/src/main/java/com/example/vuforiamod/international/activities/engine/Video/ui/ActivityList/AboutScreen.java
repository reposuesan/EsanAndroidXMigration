/*===============================================================================
Copyright (c) 2019 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.example.vuforiamod.international.activities.engine.Video.ui.ActivityList;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import com.example.vuforiamod.R;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class AboutScreen extends Activity
{
    private static final String LOGTAG = "AboutScreen";
    
    private WebView mAboutWebText;
    private String mClassToLaunch;
    private String mClassToLaunchPackage;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.about_screen);
        
        Bundle extras = getIntent().getExtras();

        mAboutWebText = findViewById(R.id.about_html_text);

        AboutWebViewClient aboutWebClient = new AboutWebViewClient();
        mAboutWebText.setWebViewClient(aboutWebClient);

        if (extras != null)
        {
            String webText = extras.getString("ABOUT_TEXT");
            mClassToLaunchPackage = getPackageName();
            mClassToLaunch = mClassToLaunchPackage + "." + extras.getString("ACTIVITY_TO_LAUNCH");

            StringBuilder aboutText = new StringBuilder();

            if (webText != null)
            {
                try
                {
                    InputStream is = getAssets().open(webText);
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(is));
                    String line;

                    while ((line = reader.readLine()) != null)
                    {
                        aboutText.append(line);
                    }
                } catch (IOException e)
                {
                    Log.e(LOGTAG, "About html loading failed");
                }

            }

            mAboutWebText.loadData(aboutText.toString(), "text/html", "UTF-8");
            TextView aboutTextTitle = findViewById(R.id.about_text_title);
            aboutTextTitle.setText(extras.getString("ABOUT_TEXT_TITLE"));
        }

        Button startButton = findViewById(R.id.button_start);
        startButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startARActivity();
            }
        });
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        mAboutWebText.destroy();
        mAboutWebText = null;
    }


    // Starts the chosen activity
    private void startARActivity()
    {
        Intent i = new Intent();
        //i.setClassName(mClassToLaunchPackage, mClassToLaunch);
        i.setClassName("appostgrado.esan.edu.pe",  "com.example.vuforiamod.international.activities.engine.Video.app.VideoPlayback.VideoPlayback");
        startActivity(i);
    }
    
    
    /*@Override
    public void onClick(View v)
    {
        switch (v.getTag())
        {
            case R.id.button_start:
                startARActivity();
                break;
        }
    }*/
    
    private class AboutWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }
    }
}
