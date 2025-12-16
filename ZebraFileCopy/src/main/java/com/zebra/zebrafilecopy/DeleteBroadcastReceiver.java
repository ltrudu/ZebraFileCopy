package com.zebra.zebrafilecopy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.File;
import java.io.IOException;

// adb shell am broadcast -a com.zebra.zebrafilecopy.copyfile -n com.zebra.zebrafilecopy/com.zebra.zebrafilecopy.CopyBroadcastReceiver --es source "/sdcard/Documents/MotoRDP.xml" --es destination "/enterprise/usr/MotoRDP.xml" [--es chmod "0666"]
// adb shell am broadcast -a com.zebra.zebrafilecopy.copyfile -n com.zebra.zebrafilecopy/com.zebra.zebrafilecopy.CopyBroadcastReceiver --es source "/sdcard/Documents/MotoRDP.xml" --es destination "/enterprise/usr/MotoRDP.xml" [--es chmodunix "-rw-rw-rw-"]

public class DeleteBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.d(Constants.TAG, "CopyBroadcastReceiver::onReceive");

        // Ensure that we have the right permissions all the time
        // because we don't know if the user will have launched the app from the launcher
        // or directly from a broadcast intent


        String sFile = intent.getExtras().getString(Constants.EXTRA_CONFIGURATION_FILE, null);
        String sUseMX = intent.getExtras().getString(Constants.EXTRA_CONFIGURATION_USE_MX, "false");

        if(sFile == null)
        {
            LogUtils.e(Constants.TAG, "You must specify a file to delete as an argument with --es [file]");
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

                File sourceFile = new File(sFile);
                if(sourceFile.exists() == false)
                {
                    LogUtils.e(Constants.TAG, "Source file not found:" + sFile);
                    return;
                }
                LogUtils.reportOnlyInfoAndErrorsToMainActivity = true;
                try {
                    FileHelper.deleteFileOrFolder(sFile, useMX, context);
                } catch (IOException e) {
                    LogUtils.e(Constants.TAG, "Exception while deleting file or folder: " + sFile);
                }
                LogUtils.reportOnlyInfoAndErrorsToMainActivity = false;
            }
        }).start();
   }

    }
