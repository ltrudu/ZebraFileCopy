package com.zebra.zebrafilecopy;

public class Constants {
    public static final String TAG = "ZebraFileCopy";
    public static final String EXTRA_CONFIGURATION_SOURCE = "source";
    public static final String EXTRA_CONFIGURATION_DESTINATION = "destination";
    public static final String EXTRA_CONFIGURATION_CHMOD = "chmod";
    public static final String EXTRA_CONFIGURATION_CHMODSTRING = "chmodunix";
    public static final String EXTRA_CONFIGURATION_USE_MX = "usemx";

    /** Manage configuration **/
    public static final String APPLICATION_RESTRICTIONS_CHANGED = "android.intent.action.APPLICATION_RESTRICTIONS_CHANGED";
    public static final String KEY_SOURCE_FILE = "SourceFile";
    public static final String KEY_DESTINATION_FILE = "DestinationFile";
    public static final String KEY_NUMERICAL_CHMOD = "OptionalNumericalCHMOD";
    public static final String KEY_UNIX_CHMOD = "OptionalUnixStyleCHMOD";
    public static final String DEFAULT_SOURCE_FILE = null;
    public static final String DEFAULT_DESTINATION_FILE = null;
    public static final int    DEFAULT_NUMERICAL_CHMOD = -1;
    public static final String DEFAULT_UNIX_CHMOD = null;

}
