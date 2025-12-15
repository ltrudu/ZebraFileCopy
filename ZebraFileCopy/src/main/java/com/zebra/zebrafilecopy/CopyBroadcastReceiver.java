package com.zebra.zebrafilecopy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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
        Log.d(Constants.TAG, "CopyBroadcastReceiver::onReceive");

        // Ensure that we have the right permissions all the time
        // because we don't know if the user will have launched the app from the launcher
        // or directly from a broadcast intent


        String sSource = intent.getExtras().getString(Constants.EXTRA_CONFIGURATION_SOURCE, null);
        String sDestination = intent.getExtras().getString(Constants.EXTRA_CONFIGURATION_DESTINATION, null);
        String sChmod = intent.getExtras().getString(Constants.EXTRA_CONFIGURATION_CHMOD, null);
        String sChmodString = intent.getExtras().getString(Constants.EXTRA_CONFIGURATION_CHMODSTRING, null);
        if(sSource == null)
        {
            Log.e(Constants.TAG, "You must specify a source path as an argument with --es [source]");
            return;
        }

        if(sDestination == null)
        {
            Log.e(Constants.TAG, "You must specify a destination path as an argument with --es [destination]");
            return;
        }

        CriticalPermissionsHelper.grantPermission(context, EPermissionType.MANAGE_EXTERNAL_STORAGE, new IResultCallbacks() {
                    @Override
                    public void onSuccess(String message, String resultXML) {
                        Log.d(Constants.TAG, "Manage external storage permission granted.");
                        File sourceFile = new File(sSource);
                        if(sourceFile.exists() == false)
                        {
                            Log.e(Constants.TAG, "Source file not found:" + sSource);
                            return;
                        }

                        File destinationFile = new File(sDestination);
                        File destinationDir = new File(destinationFile.getParent());
                        if(destinationDir.exists() == false)
                        {
                            destinationDir.mkdirs();
                        }

                        File destFile = new File(sDestination);
                        if(destFile.exists())
                        {
                            destFile.delete();
                        }

                        Log.d(Constants.TAG, "Copying file from:" + sSource + " to destination:" + sDestination);
                        try {
                            FileHelper.checkFolderPermissions(context, sDestination);
                            FileHelper.copyFile(sSource, sDestination);
                        } catch (IOException e) {
                            Log.e(Constants.TAG, "Exception while copying source:" + sSource + " to destination:" + sDestination + "\nException:" + e.getMessage());
                            return;
                        }

                        File destFileCopied = new File(sDestination);
                        if(destFileCopied.exists())
                        {
                            Log.d(Constants.TAG, "File copied with success to:" + sDestination);
                        }
                        else
                        {
                            Log.e(Constants.TAG, "Unkown error, file not found, please contact your administrator.");
                            return;
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

                        if(nChmod != -1)
                        {
                            int oldChmod = 0;
                            try {
                                oldChmod = FileHelper.getPermissions(sDestination);
                            } catch (Exception e) {
                                Log.e(Constants.TAG, "Exception while trying to get old chmod.\nException:" + e.getMessage());
                                return;
                            }
                            Log.d(Constants.TAG, "Found old chmod:" + String.valueOf(oldChmod));
                            Log.d(Constants.TAG, "Applying CHMOD:" + String.valueOf(nChmod));
                            try {
                                FileHelper.setChmod(sDestination, nChmod_octal);
                            } catch (Exception e) {
                                Log.e(Constants.TAG, "Exception while applying CHMOD:" + nChmod + "\nException:" + e.getMessage());
                            }
                            int newChmod = -1;
                            try {
                                newChmod = FileHelper.getPermissions(sDestination);
                            } catch (Exception e) {
                                Log.e(Constants.TAG, "Exception while trying to get new chmod.\nException:" + e.getMessage());
                            }
                            if(newChmod == nChmod) {
                                Log.d(Constants.TAG, "Chmod applied with success to:" + sDestination);
                            }
                            else
                            {
                                Log.d(Constants.TAG, "Error while applying chmod:" + String.valueOf(nChmod) + " found chmod:" + String.valueOf(newChmod));
                            }
                        }

                    }

                    @Override
                    public void onError(String message, String resultXML) {
                        Log.e(Constants.TAG, "Critical permission helper error:" + message);
                        Log.e(Constants.TAG, resultXML);
                    }

                    @Override
                    public void onDebugStatus(String message) {
                        Log.v(Constants.TAG, "Critical permission helper debug:" + message);
                    }
                });
    }
}
