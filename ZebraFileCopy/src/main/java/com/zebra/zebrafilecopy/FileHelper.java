package com.zebra.zebrafilecopy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.DocumentsContract;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

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

    public static void copyFile(String srcPath, String destPath) throws IOException {

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
        }
        else {
            File destinationRealName = new File(destPath);
            destinationFile.renameTo(destinationRealName);
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
}
