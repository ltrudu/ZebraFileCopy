package com.zebra.zebrafilecopy;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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

import static com.zebra.zebrafilecopy.Constants.APPLICATION_RESTRICTIONS_CHANGED;
import static com.zebra.zebrafilecopy.Constants.DEFAULT_DESTINATION_FILE;
import static com.zebra.zebrafilecopy.Constants.DEFAULT_NUMERICAL_CHMOD;
import static com.zebra.zebrafilecopy.Constants.DEFAULT_SOURCE_FILE;
import static com.zebra.zebrafilecopy.Constants.DEFAULT_UNIX_CHMOD;
import static com.zebra.zebrafilecopy.Constants.KEY_DESTINATION_FILE;
import static com.zebra.zebrafilecopy.Constants.KEY_NUMERICAL_CHMOD;
import static com.zebra.zebrafilecopy.Constants.KEY_SOURCE_FILE;
import static com.zebra.zebrafilecopy.Constants.KEY_UNIX_CHMOD;
import static com.zebra.zebrafilecopy.ManagedConfigHelper.ProcessManagedConfiguration;

public class AppRestrictionsChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (APPLICATION_RESTRICTIONS_CHANGED.equals(intent.getAction()))
        {
            ProcessManagedConfiguration(context);
        }
    }
    }
