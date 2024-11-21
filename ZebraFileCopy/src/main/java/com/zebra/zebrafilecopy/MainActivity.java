package com.zebra.zebrafilecopy;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import static com.zebra.zebrafilecopy.FileHelper.copyFile;
import static com.zebra.zebrafilecopy.FileHelper.setChmod;

public class MainActivity extends AppCompatActivity {
    private TextView et_results;
    private ScrollView sv_results;
    private String mResults = "";
    private static boolean mOptmizeRefresh = true;
    private Handler mScrollDownHandler = null;
    private Runnable mScrollDownRunnable = null;

    public static MainActivity mMainActivity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        et_results = (TextView)findViewById(R.id.et_results);
        sv_results = (ScrollView)findViewById(R.id.sv_results);

        //copyMotoRDP();

    }

    private String getAppVersionName() {
        String versionName = "";
        try {
            PackageManager packageManager = getPackageManager();
            String packageName = getPackageName();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            versionName = packageInfo.versionName;
            int versionCode = packageInfo.versionCode;
             Log.d("AppVersion", "Version Name: " + versionName);
            Log.d("AppVersion", "Version Code: " + versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    private void displayDocumentation() {
        addLineToResults("********************************\n");
        addLineToResults("Zebra File Copy " + getAppVersionName() +"\n");
        addLineToResults("********************************\n");
        addLineToResults("Documentation");
        addLineToResults("Copy paste what you need.");
        addLineToResults("********************************\n");
        addLineToResults("Use intent to copy files.\n\n");
        addLineToResults("adb shell am broadcast -a com.zebra.zebrafilecopy.copyfile -n com.zebra.zebrafilecopy/com.zebra.zebrafilecopy.CopyBroadcastReceiver --es source \"/sdcard/Documents/MotoRDP.xml\" --es destination \"/enterprise/usr/MotoRDP.xml\"\n\n");
        addLineToResults("adb shell am broadcast -a com.zebra.zebrafilecopy.copyfile -n com.zebra.zebrafilecopy/com.zebra.zebrafilecopy.CopyBroadcastReceiver --es source \"/sdcard/Documents/MotoRDP.xml\" --es destination \"/enterprise/usr/MotoRDP.xml\"--es chmod \"0666\"\n\n");
        addLineToResults("chmod should be a numerical value 0XXX with the octal representation of permissions.");
        addLineToResults("recommended chmod is 0666.\n\n");
        addLineToResults("adb shell am broadcast -a com.zebra.zebrafilecopy.copyfile -n com.zebra.zebrafilecopy/com.zebra.zebrafilecopy.CopyBroadcastReceiver --es source \"/sdcard/Documents/MotoRDP.xml\" --es destination \"/enterprise/usr/MotoRDP.xml\" --es chmodunix \"-rw-rw-rw-\"\n\n");
        addLineToResults("chmodstring should be a text value of 10 characters begining whith - with this structure -XXXXXXXXX where X can be rwxst depending on the position.\nRead linux documentation for more information.");
        addLineToResults("recommended chmodstring is -rw-rw-rw-.\n");
        addLineToResults("Go to the site: https://chmod-calculator.app/ to calculate the CHMOD.\n\n");
        addLineToResults("********************************\n\n");
        addLineToResults("Or use the following XML to import a StageNow Profile:\n\n");
        addLineToResults("<wap-provisioningdoc>\n" +
                "  <characteristic version=\"10.5\" type=\"Intent\">\n" +
                "    <parm name=\"Action\" value=\"Broadcast\" />\n" +
                "    <parm name=\"ActionName\" value=\"com.zebra.zebrafilecopy.copyfile\" />\n" +
                "    <parm name=\"Package\" value=\"com.zebra.zebrafilecopy\" />\n" +
                "    <parm name=\"Class\" value=\".CopyBroadcastReceiver\" />\n" +
                "    <parm name=\"Category\" value=\"android.intent.category.DEFAULT\" />\n" +
                "    <characteristic type=\"Extra\">\n" +
                "      <parm name=\"ExtraType\" value=\"string\" />\n" +
                "      <parm name=\"ExtraName\" value=\"source\" />\n" +
                "      <parm name=\"ExtraValue\" value=\"/sdcard/Documents/MotoRDP.xml\" />\n" +
                "    </characteristic>\n" +
                "    <characteristic type=\"Extra1\">\n" +
                "      <parm name=\"Extra1Type\" value=\"string\" />\n" +
                "      <parm name=\"Extra1Name\" value=\"destination\" />\n" +
                "      <parm name=\"Extra1Value\" value=\"/enterprise/usr/MotoRDP.xml\" />\n" +
                "    </characteristic>\n" +
                "    <characteristic type=\"Extra2\">\n" +
                "      <parm name=\"Extra2Type\" value=\"string\" />\n" +
                "      <parm name=\"Extra2Name\" value=\"chmodstring\" />\n" +
                "      <parm name=\"Extra2Value\" value=\"-rw-rw-rw-\" />\n" +
                "    </characteristic>\n" +
                "  </characteristic>\n" +
                "</wap-provisioningdoc>");
    }

    private void copyMotoRDP() {
        try {
            String nChmod_octalString = FileHelper.convertPermissionToOctalString("-rw-rw-rw-");
            int nChmod_octal = Integer.parseInt(nChmod_octalString, 8);
            int nChmod = Integer.parseInt(nChmod_octalString);
            Log.d(Constants.TAG, "Copying file: /sdcard/Documents/MotoRDP.xml to /enterprise/usr/MotoRDP.xml");
            copyFile("/sdcard/Documents/MotoRDP.xml", "/enterprise/usr/MotoRDP.xml");
            Log.d(Constants.TAG, "File: /sdcard/Documents/MotoRDP.xml copied successfully to /enterprise/usr/MotoRDP.xml");
            setChmod("/enterprise/usr/MotoRDP.xml", nChmod_octal);
            int newChmod = 0;
            newChmod = FileHelper.getPermissions("/enterprise/usr/MotoRDP.xml");
            if(newChmod != nChmod)
            {
                Log.e(Constants.TAG, "Error, chmod not set on file:" + "/enterprise/usr/MotoRDP.xml" + "\nChmod expected: 0777\nChmod found: " + newChmod);
            }
            else
            {
                Log.d(Constants.TAG, "Chmod set to 0777 on file /enterprise/usr/MotoRDP.xml");
            }
            finish();
        } catch (Exception e) {
            Log.e(Constants.TAG, "Exception :" + e.getMessage());
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mScrollDownHandler = new Handler(Looper.getMainLooper());
        displayDocumentation();
        mMainActivity = this;
        // Check if we have a managed configuration, if yes, process it
        ManagedConfigHelper.ProcessManagedConfiguration(this);
    }

    @Override
    protected void onPause() {
        mMainActivity = null;
        if(mScrollDownRunnable != null)
        {
            mScrollDownHandler.removeCallbacks(mScrollDownRunnable);
            mScrollDownRunnable = null;
            mScrollDownHandler = null;
        }
        super.onPause();
    }

    public void addLineToResults(final String lineToAdd)
    {
        mResults += lineToAdd + "\n";
        updateAndScrollDownTextView();
    }

    private void updateAndScrollDownTextView()
    {
        if(mOptmizeRefresh)
        {
            if(mScrollDownRunnable == null)
            {
                mScrollDownRunnable = new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                et_results.setText(mResults);
                                sv_results.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        sv_results.fullScroll(ScrollView.FOCUS_DOWN);
                                    }
                                });
                            }
                        });
                    }
                };
            }
            else
            {
                // A new line has been added while we were waiting to scroll down
                // reset handler to repost it....
                mScrollDownHandler.removeCallbacks(mScrollDownRunnable);
            }
            mScrollDownHandler.postDelayed(mScrollDownRunnable, 300);
        }
        else
        {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    et_results.setText(mResults);
                    sv_results.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        }

    }

}