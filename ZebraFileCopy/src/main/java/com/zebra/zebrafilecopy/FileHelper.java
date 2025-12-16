package com.zebra.zebrafilecopy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.DocumentsContract;
import android.util.Log;

import com.zebra.criticalpermissionshelper.CriticalPermissionsHelper;
import com.zebra.criticalpermissionshelper.EPermissionType;
import com.zebra.criticalpermissionshelper.IResultCallbacks;
import com.zebra.criticalpermissionshelper.ProfileManagerCommand;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;

import static android.content.Context.STORAGE_SERVICE;
import static android.provider.DocumentsContract.EXTRA_INITIAL_URI;
import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;
import static androidx.core.content.ContextCompat.getSystemService;

public class FileHelper {
    private final static String EXTERNAL_STORAGE_PROVIDER_AUTHORITY = "com.android.externalstorage.documents";


    @RequiresApi(Build.VERSION_CODES.Q)
    private static void askPermission(Context context, String targetFilePath) {
        File targetFile = new File(targetFilePath);
        String targetPath = targetFile.getParent();
        StorageManager storageManager = (StorageManager) getSystemService(context, StorageManager.class);
        Intent intent = storageManager.getPrimaryStorageVolume().createOpenDocumentTreeIntent();

        Uri uri = intent.getParcelableExtra("android.provider.extra.INITIAL_URI", Uri.class);
        if (uri != null) {
            String scheme = uri.toString();
            scheme = scheme.replace("/root/", "/document/");
            scheme += "%3A" + targetPath;
            uri = Uri.parse(scheme);
            intent.putExtra("android.provider.extra.INITIAL_URI", uri);
        }
        context.startActivity(intent);
    }



    public static void checkFolderPermissions(Context context, String path)
    {
        if(path.contains("Android/data") || path.contains("android/data"))
        {
            checkPermissions(context, path);
        }
    }

    public static void checkPermissions(Context context, String path)
    {
        if(path.contains("Android/data") || path.contains("android/data"))
        {
            Uri uri;
            Uri treeUri;

            File targetPath = new File(path);
            String targetFolder = targetPath.getParent();
            if(targetFolder.contains("/sdcard/"))
            {
                targetFolder = targetFolder.replace("/sdcard/", "");
            }

            if(targetFolder.startsWith("Android"))
            {
                targetFolder = "primary:" + targetFolder;
            }

            uri = DocumentsContract.buildDocumentUri(
                    EXTERNAL_STORAGE_PROVIDER_AUTHORITY,
                    targetFolder
            );
            treeUri = DocumentsContract.buildTreeDocumentUri(
                    EXTERNAL_STORAGE_PROVIDER_AUTHORITY,
                    targetFolder
            );
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if(checkIfGotAccess(context, treeUri))
                    return;
                else
                {
                    openDirectory(context, uri);
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private static void openDirectory(Context context, Uri uri) {
        Intent intent =
                getPrimaryVolume(context).createOpenDocumentTreeIntent()
                        .putExtra(EXTRA_INITIAL_URI, uri);
        context.startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static StorageVolume getPrimaryVolume(Context context) {
        StorageManager sm = (StorageManager) getSystemService(context, StorageManager.class);
        return sm.getPrimaryStorageVolume();
    }


    private static Boolean checkIfGotAccess(Context context, Uri treeUri) {
        List<UriPermission> permissionList = context.getContentResolver().getPersistedUriPermissions();
        for (int i = 0; i < permissionList.size(); i++) {
            UriPermission it = permissionList.get(i);
            if (it.getUri().equals(treeUri) && it.isReadPermission())
                return true;
        }
        return false;
    }

    public static void copyFile(String srcPath, String destPath, boolean useMx, Context context) throws IOException {
        if(useMx)
        {
            CountDownLatch latch = new CountDownLatch(1);
            copyWithProfileManager(context, srcPath, destPath, new IResultCallbacks() {
                @Override
                public void onSuccess(String message, String resultXML) {
                    Log.d(Constants.TAG, message);
                    latch.countDown();
                }

                @Override
                public void onError(String message, String resultXML) {
                    Log.e(Constants.TAG, message);
                    Log.e(Constants.TAG, resultXML);
                    latch.countDown();
                }

                @Override
                public void onDebugStatus(String message) {
                    Log.v(Constants.TAG, message);
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                Log.e(Constants.TAG, "Exception in copyfile while trying to await countdown latch.");
                if(latch.getCount() > 0)
                {
                    latch.countDown();
                }
            }
        }
        else {

            File sourceFile = new File(srcPath);
            File destinationFile = new File(destPath + "_temporary");

            if (!sourceFile.exists()) {
                throw new IOException("Source file not found: " + srcPath);
            }

            if (!destinationFile.exists()) {
                destinationFile.createNewFile();
            }

            try (FileInputStream fis = new FileInputStream(sourceFile);
                 FileOutputStream fos = new FileOutputStream(destinationFile)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
            }
            if (!destinationFile.exists()) {
                throw new IOException("Error, file : " + destPath + " does not exist after copy.");
            } else {
                File destinationRealName = new File(destPath);
                destinationFile.renameTo(destinationRealName);
            }
        }
    }

    public static void copySingleFileWithChmod(int nChmod, int nChmod_octal, String sDestination, String sSource, boolean useMx, Context context) {
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
            FileHelper.copyFile(sSource, sDestination, useMx, context);
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


    public static void copyFolder(String srcPath, String destPath, int chMod, int chModOctal, boolean useMx, Context context) throws IOException {
        File sourceDir = new File(srcPath);
        File destDir = new File(destPath);

        if (!sourceDir.exists()) {
            throw new IOException("Source folder not found: " + srcPath);
        }

        if (!sourceDir.isDirectory()) {
            throw new IOException("Source path is not a directory: " + srcPath);
        }

        // We do not create the folder, we let the MX FileMgr creating it for us
        if(useMx == false) {
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
        }

        // Apply chmod to destination folder to make it browsable
        if (useMx == false && chMod != -1) {
            try {
                setChmod(destPath, chModOctal);
                Log.d(Constants.TAG, "Chmod applied to folder: " + destPath);
            } catch (Exception e) {
                Log.e(Constants.TAG, "Exception while applying chmod to folder: " + destPath + ", Exception: " + e.getMessage());
            }
        }

        File[] files = sourceDir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            String destFilePath = destPath + File.separator + file.getName();

            if (file.isDirectory()) {
                // Recursive call will create subfolder and apply chmod to it
                copyFolder(file.getAbsolutePath(), destFilePath, chMod, chModOctal, useMx, context);
            } else {
                copyFile(file.getAbsolutePath(), destFilePath, useMx, context);
                // Apply chmod to copied file
                if (useMx == false && chMod != -1) {
                    try {
                        setChmod(destFilePath, chMod);
                        Log.d(Constants.TAG, "Chmod applied to file: " + destFilePath);
                    } catch (Exception e) {
                        Log.e(Constants.TAG, "Exception while applying chmod to file: " + destFilePath + ", Exception: " + e.getMessage());
                    }
                }
            }
        }
    }

    public static void setChmod(String path, int mode) throws Exception {
        Class<?> libcore = Class.forName("libcore.io.Libcore");
        Field field = libcore.getDeclaredField("os");
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        Object os = field.get(field);
        Method chmod = os.getClass().getMethod("chmod", String.class, int.class);
        chmod.invoke(os, path, mode);
    }

    public static int getPermissions(String path) throws Exception {
        // Execute the stat command to get the file permissions
        Process process = Runtime.getRuntime().exec("stat -c %a " + path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String output = reader.readLine();
        process.waitFor();
        reader.close();

        // Convert the string output to an integer
        if (output != null) {
            return Integer.parseInt(output);
        } else {
            throw new Exception("Unable to retrieve permissions for file: " + path);
        }
    }

    public static int convertToOctal(String permission) {
        if (permission.length() != 10) {
            throw new IllegalArgumentException("Invalid permission format");
        }

        int[] permissionBits = new int[3];
        int specialBits = 0;

        for (int i = 1; i < 10; i++) {
            char c = permission.charAt(i);
            int index = (i - 1) / 3;
            switch (c) {
                case 'r':
                    permissionBits[index] += 4;
                    break;
                case 'w':
                    permissionBits[index] += 2;
                    break;
                case 'x':
                    permissionBits[index] += 1;
                    break;
                case 's':
                    permissionBits[index] += 1;
                    if (i < 4) { // Setuid bit
                        specialBits += 04000;
                    } else if (i < 7) { // Setgid bit
                        specialBits += 02000;
                    }
                    break;
                case 't':
                    permissionBits[index] += 1;
                    if (i >= 7) { // Sticky bit
                        specialBits += 01000;
                    }
                    break;
                case '-':
                    break;
                default:
                    throw new IllegalArgumentException("Invalid permission character: " + c);
            }
        }

        return specialBits + (permissionBits[0] * 64) + (permissionBits[1] * 8) + permissionBits[2];
    }

    public static String convertPermissionToOctalString(String permission) throws IllegalArgumentException{
        if (permission == null || permission.length() != 10) {
            throw new IllegalArgumentException("Permission string must be 10 characters long");
        }

        int mode = 0;

        // Special permissions (SUID, SGID, Sticky bit)
        if (permission.charAt(3) == 's') mode |= 04000;
        if (permission.charAt(6) == 's') mode |= 02000;
        if (permission.charAt(9) == 't') mode |= 01000;

        // Owner permissions
        if (permission.charAt(1) == 'r') mode |= 00400;
        if (permission.charAt(2) == 'w') mode |= 00200;
        if (permission.charAt(3) == 'x' || permission.charAt(3) == 's') mode |= 00100;

        // Group permissions
        if (permission.charAt(4) == 'r') mode |= 00040;
        if (permission.charAt(5) == 'w') mode |= 00020;
        if (permission.charAt(6) == 'x' || permission.charAt(6) == 's') mode |= 00010;

        // Others permissions
        if (permission.charAt(7) == 'r') mode |= 00004;
        if (permission.charAt(8) == 'w') mode |= 00002;
        if (permission.charAt(9) == 'x' || permission.charAt(9) == 't') mode |= 00001;

        // Convert to octal string with leading zeros
        return String.format("%04o", mode);
    }

    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            PackageManager packageManager = context.getPackageManager();
            String packageName = context.getPackageName();
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

    private static void copyWithProfileManager(Context context, String sourceFile, String destinationFile, IResultCallbacks callbackInterface) {
        String profileName = "FileCopy-1";
        String profileData = "";
        try {
            profileData = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                    "<characteristic type=\"Profile\">\n" +
                    "<parm name=\"ProfileName\" value=\"" + profileName + "\"/>\n" +
                    "  <characteristic version=\"11.3\" type=\"FileMgr\">\n" +
                    "    <parm name=\"FileAction\" value=\"1\" />\n" +
                    "    <characteristic type=\"file-details\">\n" +
                    "      <parm name=\"TargetAccessMethod\" value=\"2\" />\n" +
                    "      <parm name=\"TargetPathAndFileName\" value=\"" + destinationFile + "\" />\n" +
                    "      <parm name=\"IfDuplicate\" value=\"1\" />\n" +
                    "      <parm name=\"SourceAccessMethod\" value=\"2\" />\n" +
                    "      <parm name=\"SourcePathAndFileName\" value=\"" + sourceFile + "\" />\n" +
                    "    </characteristic>\n" +
                    "  </characteristic>\n" +
                    "</characteristic>\n";

            ProfileManagerCommand profileManagerCommand = new ProfileManagerCommand(context);
            profileManagerCommand.execute(profileData, profileName, callbackInterface);
            //}
        } catch (Exception e) {
            e.printStackTrace();
            if (callbackInterface != null) {
                callbackInterface.onError("Error on profile: " + profileName + "\nError:" + e.getLocalizedMessage() + "\nProfileData:" + profileData, "");
            }
        }
    }
}
