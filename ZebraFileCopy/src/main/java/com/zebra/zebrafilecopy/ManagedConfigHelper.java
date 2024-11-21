package com.zebra.zebrafilecopy;

import android.content.Context;
import android.content.RestrictionsManager;
import android.os.Bundle;
import android.util.Log;

import com.zebra.criticalpermissionshelper.CriticalPermissionsHelper;
import com.zebra.criticalpermissionshelper.EPermissionType;
import com.zebra.criticalpermissionshelper.IResultCallbacks;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import androidx.enterprise.feedback.KeyedAppState;
import androidx.enterprise.feedback.KeyedAppStatesReporter;

import static com.zebra.zebrafilecopy.Constants.DEFAULT_DESTINATION_FILE;
import static com.zebra.zebrafilecopy.Constants.DEFAULT_NUMERICAL_CHMOD;
import static com.zebra.zebrafilecopy.Constants.DEFAULT_SOURCE_FILE;
import static com.zebra.zebrafilecopy.Constants.DEFAULT_UNIX_CHMOD;
import static com.zebra.zebrafilecopy.Constants.KEY_DESTINATION_FILE;
import static com.zebra.zebrafilecopy.Constants.KEY_NUMERICAL_CHMOD;
import static com.zebra.zebrafilecopy.Constants.KEY_SOURCE_FILE;
import static com.zebra.zebrafilecopy.Constants.KEY_UNIX_CHMOD;

public class ManagedConfigHelper {
    public static void ProcessManagedConfiguration(Context context) {
        try {
            // Handle the configuration change
            logInfo("AppRestrictionsChangeReceiver", "Processing Managed configuration if available.");

            // Retrieve the new managed configurations
            RestrictionsManager myRestrictionsMgr =
                    (RestrictionsManager) context.getSystemService(Context.RESTRICTIONS_SERVICE);
            // Get the application restrictions bundle
            Bundle managedConfig = null;

            if (myRestrictionsMgr != null) {
                managedConfig = myRestrictionsMgr.getApplicationRestrictions();
                logInfo("Managed config found:", managedConfig.toString());
            }

            if (managedConfig != null) {
                String sourceFilePath = managedConfig.getString(KEY_SOURCE_FILE, DEFAULT_SOURCE_FILE);
                String destinationFilePath = managedConfig.getString(KEY_DESTINATION_FILE, DEFAULT_DESTINATION_FILE);
                int optionalNumericalCHMOD = managedConfig.getInt(KEY_NUMERICAL_CHMOD, DEFAULT_NUMERICAL_CHMOD);
                String optionalUnixStyleCHMOD = managedConfig.getString(KEY_UNIX_CHMOD, DEFAULT_UNIX_CHMOD);

                logInfo("AppRestrictionsChangeReceiver", "Source File: " + sourceFilePath);
                logInfo("AppRestrictionsChangeReceiver", "Destination File: " + destinationFilePath);
                logInfo("AppRestrictionsChangeReceiver", "Optional Numerical CHMOD: " + optionalNumericalCHMOD);
                logInfo("AppRestrictionsChangeReceiver", "Optional Unix Style CHMOD: " + optionalUnixStyleCHMOD);

                final String resultInfo = "Copying file from:\n" + sourceFilePath + "\nto:\n" + destinationFilePath + ((optionalNumericalCHMOD != -1 || optionalUnixStyleCHMOD != null) ? "\nwith chmod: " + ((optionalNumericalCHMOD != -1 ? optionalNumericalCHMOD : optionalUnixStyleCHMOD)) : "") + "\n";

                if (sourceFilePath == null || sourceFilePath.isEmpty()) {
                    sendFeedback(context, "Error", "Source file is null or empty");
                    return;
                }

                if (destinationFilePath == null || destinationFilePath.isEmpty()) {
                    sendFeedback(context, "Error", "Destination file is null or empty");
                    return;
                }

                CriticalPermissionsHelper.grantPermission(context, EPermissionType.MANAGE_EXTERNAL_STORAGE, new IResultCallbacks() {
                    @Override
                    public void onSuccess(String message, String resultXML) {
                        String resultMessage = resultInfo;
                        resultMessage += "Manage external storage permission granted.\n";
                        File sourceFile = new File(sourceFilePath);
                        if (sourceFile.exists() == false) {
                            sendFeedback(context, "Error", "Source file not found:" + sourceFilePath);
                            return;
                        }

                        File destinationFile = new File(destinationFilePath);
                        File destinationDir = new File(destinationFile.getParent());
                        if (destinationDir.exists() == false) {
                            destinationDir.mkdirs();
                        }

                        File destFile = new File(destinationFilePath);
                        if (destFile.exists()) {
                            destFile.delete();
                        }

                        resultMessage += "Copying file from:" + sourceFilePath + " to destination:" + destinationFilePath + "\n";
                        try {
                            FileHelper.copyFile(sourceFilePath, destinationFilePath);
                        } catch (IOException e) {
                            sendFeedback(context, "Error", "Exception while copying source:" + sourceFilePath + " to destination:" + destinationFilePath + "\nException:" + e.getMessage());
                            return;
                        }

                        File destFileCopied = new File(destinationFilePath);
                        if (destFileCopied.exists()) {
                            resultMessage += "File copied with success to:" + destinationFilePath + "\n";
                        } else {
                            sendFeedback(context, "Error", "Unkown error, file: " + destinationFilePath + " not found, please contact your administrator.");
                            return;
                        }

                        int nChmod = -1;
                        int nChmod_octal = -1;
                        if (optionalNumericalCHMOD != -1) {
                            nChmod = optionalNumericalCHMOD;
                            // Ugly octal transformation :(
                            nChmod_octal = Integer.parseInt(String.valueOf(optionalNumericalCHMOD), 8);
                        } else if (optionalUnixStyleCHMOD != null && optionalUnixStyleCHMOD.isEmpty() == false) {
                            if (optionalUnixStyleCHMOD.length() != 10) {
                                sendFeedback(context, "Error", "Could not apply unix style chmod. String must be 10 characters long starting with -");
                                return;
                            }
                            String sOctalChmod = FileHelper.convertPermissionToOctalString(optionalUnixStyleCHMOD);
                            nChmod = Integer.parseInt(sOctalChmod);
                            nChmod_octal = Integer.parseInt(sOctalChmod, 8);
                        }

                        if (nChmod != -1) {
                            int oldChmod = 0;
                            try {
                                oldChmod = FileHelper.getPermissions(destinationFilePath);
                            } catch (Exception e) {
                                sendFeedback(context, "Error", "Exception while trying to get old chmod.\nException:" + e.getMessage());
                                return;
                            }
                            resultMessage += "Found old chmod:" + String.valueOf(oldChmod) + "\n";
                            resultMessage += "Applying CHMOD:" + String.valueOf(nChmod) + "\n";
                            try {
                                FileHelper.setChmod(destinationFilePath, nChmod_octal);
                            } catch (Exception e) {
                                sendFeedback(context, "Error", "Exception while applying CHMOD:" + nChmod + "\nException:" + e.getMessage());
                                return;
                            }
                            int newChmod = -1;
                            try {
                                newChmod = FileHelper.getPermissions(destinationFilePath);
                            } catch (Exception e) {
                                sendFeedback(context, "Error", "Exception while trying to get new chmod.\nException:" + e.getMessage());
                            }
                            if (newChmod == nChmod) {
                                resultMessage += "Chmod applied with success to:" + destinationFilePath + "\n";
                            } else {
                                sendFeedback(context, "Error", "Error while applying chmod:" + String.valueOf(nChmod) + " found chmod:" + String.valueOf(newChmod));
                                return;
                            }
                        }
                        resultMessage += "Managed configuration applied with success !!!";
                        sendFeedback(context, "Success", resultMessage);
                    }

                    @Override
                    public void onError(String message, String resultXML) {
                        logError(Constants.TAG, "Critical permission helper error:" + message);
                        logError(Constants.TAG, resultXML);
                        sendFeedback(context, "Error", "Critical permission helper error:" + message);
                    }

                    @Override
                    public void onDebugStatus(String message) {
                        logInfo(Constants.TAG, "Critical permission helper debug:" + message);
                    }
                });
            } else {
                sendFeedback(context, "Error", "No managed configurations available.");
            }
        }
        catch(Exception e)
        {
            sendFeedback(context, "Exception", e.getMessage());
        }
    }

    private static void sendFeedback(Context context, String key, String message) {
        if(MainActivity.mMainActivity != null)
        {
            MainActivity.mMainActivity.addLineToResults(key + ":" + message);
        }
        Log.d(Constants.TAG, key + ":" + message);
        KeyedAppStatesReporter reporter = KeyedAppStatesReporter.create(context);
        Collection states = new HashSet<>();
        int severity = KeyedAppState.SEVERITY_INFO;
        if(key.equalsIgnoreCase("Error"))
            severity = KeyedAppState.SEVERITY_ERROR;
        states.add(KeyedAppState.builder()
                .setKey(Constants.TAG)
                .setSeverity(severity)
                .setMessage(key)
                .setData(message)
                .build());
        reporter.setStatesImmediate(states);
    }

    private static void logInfo(String tag, String message)
    {
        Log.d(tag,  message);
        if(MainActivity.mMainActivity != null)
        {
            MainActivity.mMainActivity.addLineToResults( "Debug: " + message);
        }
    }

    private static void logError(String tag, String message) {
        Log.e(tag, message);
        if (MainActivity.mMainActivity != null) {
            MainActivity.mMainActivity.addLineToResults("Error: " + message);
        }
    }
}
