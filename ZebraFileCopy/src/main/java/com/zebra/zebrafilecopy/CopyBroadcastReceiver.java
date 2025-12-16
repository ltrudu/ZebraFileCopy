package com.zebra.zebrafilecopy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.zebra.criticalpermissionshelper.CriticalPermissionsHelper;
import com.zebra.criticalpermissionshelper.EPermissionType;
import com.zebra.criticalpermissionshelper.IResultCallbacks;

import java.io.File;
import java.io.IOException;

// adb shell am broadcast -a com.zebra.zebrafilecopy.copyfile -n com.zebra.zebrafilecopy/com.zebra.zebrafilecopy.CopyBroadcastReceiver --es source "/sdcard/Documents/MotoRDP.xml" --es destination "/enterprise/usr/MotoRDP.xml" [--es chmod "0666"]
// adb shell am broadcast -a com.zebra.zebrafilecopy.copyfile -n com.zebra.zebrafilecopy/com.zebra.zebrafilecopy.CopyBroadcastReceiver --es source "/sdcard/Documents/MotoRDP.xml" --es destination "/enterprise/usr/MotoRDP.xml" [--es chmodunix "-rw-rw-rw-"]

public class CopyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.d(Constants.TAG, "CopyBroadcastReceiver::onReceive");

        // Ensure that we have the right permissions all the time
        // because we don't know if the user will have launched the app from the launcher
        // or directly from a broadcast intent


        String sSource = intent.getExtras().getString(Constants.EXTRA_CONFIGURATION_SOURCE, null);
        String sDestination = intent.getExtras().getString(Constants.EXTRA_CONFIGURATION_DESTINATION, null);
        String sChmod = intent.getExtras().getString(Constants.EXTRA_CONFIGURATION_CHMOD, null);
        String sChmodString = intent.getExtras().getString(Constants.EXTRA_CONFIGURATION_CHMODSTRING, null);
        String sUseMX = intent.getExtras().getString(Constants.EXTRA_CONFIGURATION_USE_MX, "false");

        if(sSource == null)
        {
            LogUtils.e(Constants.TAG, "You must specify a source path as an argument with --es [source]");
            return;
        }

        if(sDestination == null)
        {
            LogUtils.e(Constants.TAG, "You must specify a destination path as an argument with --es [destination]");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean useMX = false;
                if(sUseMX != null)
                {
                    useMX = sUseMX.equalsIgnoreCase("true");
                }

                int nChmod = -1;
                int nChmod_octal = -1;
                if(sChmod != null)
                {
                    nChmod = Integer.parseInt(sChmod);
                    nChmod_octal = Integer.parseInt(sChmod, 8);
                }
                else if(sChmodString != null)
                {
                    String sOctalChmod = FileHelper.convertPermissionToOctalString(sChmodString);
                    nChmod = Integer.parseInt(sOctalChmod);
                    nChmod_octal = Integer.parseInt(sOctalChmod, 8);
                }

                File sourceFile = new File(sSource);
                if(sourceFile.exists() == false)
                {
                    LogUtils.e(Constants.TAG, "Source file not found:" + sSource);
                    return;
                }

                if(sourceFile.isDirectory() == false) {
                    LogUtils.reportOnlyInfoAndErrorsToMainActivity = true;
                    FileHelper.copySingleFileWithChmod(nChmod, nChmod_octal, sDestination, sSource, useMX, context);
                    LogUtils.reportOnlyInfoAndErrorsToMainActivity = false;
                }
                else
                {
                    try {
                        LogUtils.reportOnlyInfoAndErrorsToMainActivity = true;
                        FileHelper.copyFolder(sSource, sDestination, nChmod, nChmod_octal, useMX, context);
                        LogUtils.reportOnlyInfoAndErrorsToMainActivity = false;
                    } catch (IOException e) {
                        LogUtils.e(Constants.TAG, "Copy folder errpr:" + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }).start();
   }

    }
