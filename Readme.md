*Please be aware that this library / application / sample is provided as a community project without any guarantee of support*

All content under this repository's root folder is subject to the [Development Tool License Agreement](https://github.com/ZebraDevs/AISuite_Android_Samples/blob/main/Zebra%20Development%20Tool%20License.pdf). By accessing, using, or distributing any part of this content, you agree to comply with the terms of the Development Tool License Agreement.


********************************
# *Zebra File Copy*
********************************

# Documentation:

• 1: Setup.

• 2: How to setup Zebra ERDP

• 3: How to setup Enterprise Browser

• 4: ADB: Copy Paste what you need.

• 5: Managed Configuration

********************************

## StageNow profiles samples available at the following link [StageNowProfiles](https://github.com/ltrudu/ZebraFileCopy/tree/master/StageNow)

********************************
## 1: Setup

To setup the ZebraFileCopy, you have to install the latest apk in the [Releases](https://github.com/ltrudu/ZebraFileCopy/releases) page of this repository.

Once installed you'll need to start it at least once to have the application silently aquire its permissions and register the necessary broadcast receivers.

This step is mandatory.

You can run the app with:

• Adb with the following command:

```cmd
 adb shell am start -n com.zebra.zebrafilecopy.ext/com.zebra.zebrafilecopy.SplashActivity
```

• Your favorire EMM with the following XML: [StartZebraFileCopy](https://github.com/ltrudu/ZebraFileCopy/blob/master/StageNow/StartZebraFileCopy.xml)

• Or scan the following barcode on StageNow:

### PDF417
<img width="432" height="131" alt="image" src="https://github.com/user-attachments/assets/0b5b169b-1d2e-4a32-9a75-8a4f0cd8e1c1" />

### Javascript JS PDF417
<img width="364" height="151" alt="image" src="https://github.com/user-attachments/assets/6c5e5613-7af8-48eb-a4d1-786a0df45f27" />

********************************
## Setup ERDP

To setup ERDP, first install the ZebraFileCopy and launch it once as explained in this documentation.

Then you'll have to upload your MotoRDP.xml file to the Documents folder as Documents/MotoRDP.xml

You can use the following methods to setup ERDP:

• Adb with the following command:

```cmd
 adb shell am broadcast -a com.zebra.zebrafilecopy.copyfile --es source "/sdcard/Documents/MotoRDP.xml" --es destination "/enterprise/usr/MotoRDP.xml" --es chmod "0666"
```

• Your favorire EMM with the following XML: [ZebraFCSetupERDP](https://github.com/ltrudu/ZebraFileCopy/blob/master/StageNow/ZebraFCSetupERDP.xml)

• Or scan the following barcode on StageNow:

### PDF417
<img width="300" height="379" alt="image" src="https://github.com/user-attachments/assets/bfdbaf73-af37-400f-9f89-846421ceb841" />

### Javascript JS PDF417
<img width="296" height="344" alt="image" src="https://github.com/user-attachments/assets/0256ceae-fb6f-4f60-9b2c-cc2d845fd4b3" />

********************************
## Setup Enterprise Browser

To setup Enterprise Browser, first install the ZebraFileCopy and launch it once as explained in this documentation.

Then you'll have to copy all your files in the following folder: Documents/enterprisebrowser

This folder will contain the Config.xml file and all other files that you need for your setup.

We are going to use the copy folder feature of ZebraFileCopy so all files and folders inside Documents/enterprisebrowser will be copied recursively to the Enterprise Browser configuration folder.

All existing files will be overwriten and this folder will be used in priority by Enterprise Browser, since we will use the enterprise partition, the settings will persist accross reboot and enterprise reset (or reset from settings).

If you need to delete the folder, you can try the experimental DeleteBroadcast receiver (WIP, not actually documented, check the code for more information).

To setup Enteprise Browser you can use the following methods:

• Adb with the following command:

```cmd
adb shell am broadcast -a com.zebra.zebrafilecopy.copyfile --es source "/sdcard/Documents/enterprisebrowser" --es destination "/enterprise/device/enterprisebrowser" --es usemx "true"
```

• Your favorire EMM with the following XML: [ZebraFileCopy_EnterpriseBrowser](https://github.com/ltrudu/ZebraFileCopy/blob/master/StageNow/ZebraFileCopy_EnterpriseBrowser.xml)

• Or scan the following barcode on StageNow:

### PDF417
<img width="367" height="279" alt="image" src="https://github.com/user-attachments/assets/2d3f6dd2-86e4-4a43-8ecb-760afe8d3f77" />


### Javascript JS PDF417
<img width="374" height="235" alt="image" src="https://github.com/user-attachments/assets/6860bbc6-5cf5-4d0e-a0eb-453f33f2d095" />


********************************
## Copy Paste what you need

### Use intent to copy files.


```cmd
adb shell am broadcast -a com.zebra.zebrafilecopy.copyfile --es source "/sdcard/Documents/MotoRDP.xml" --es destination "/enterprise/usr/MotoRDP.xml"
```


### Use intent to copy files and change its permission using a numerical chmod. 

chmod should be a numerical value 0XXX with the octal representation of permissions.

Recommended chmod is 0666.


```cmd
adb shell am broadcast -a com.zebra.zebrafilecopy.copyfile --es source "/sdcard/Documents/MotoRDP.xml" --es destination "/enterprise/usr/MotoRDP.xml" --es chmod "0666"
```


### Use intent to copy files and change its permission using a unix notation chmod.

chmodstring should be a text value of 10 characters begining whith - with this structure -XXXXXXXXX where X can be rwxst depending on the position.

Read linux documentation for more information.

Recommended chmodunix is -rw-rw-rw-.


```cmd
adb shell am broadcast -a com.zebra.zebrafilecopy.copyfile --es source "/sdcard/Documents/MotoRDP.xml" --es destination "/enterprise/usr/MotoRDP.xml" --es chmodunix "-rw-rw-rw-"
```


Go to the site: https://chmod-calculator.app/ to calculate the CHMOD.


### Use intent to copy files with MX FileMgr.

```cmd
adb shell am broadcast -a com.zebra.zebrafilecopy.copyfile --es source "/sdcard/Documents/MotoRDP.xml" --es destination "/enterprise/usr/MotoRDP.xml" --es usemx "true"
```

### Use intent to deploy Enteprise Browser configuration folder with MX FileMgr.

```cmd
adb shell am broadcast -a com.zebra.zebrafilecopy.copyfile --es source "/sdcard/Documents/enterprisebrowser" --es destination "/enterprise/device/enterprisebrowser" --es usemx "true"
```

********************************

Managed Configuration

********************************

To be able to process managed configuration settings and updates, the app need to be launched to register to the updates events.

You can do it with your favorite EMM:

- Action: android.intent.action.MAIN
- Category: android.intent.category.LAUNCHER
- Package Name: com.zebra.zebrafilecopy.ext
- Activity : com.zebra.zebrafilecopy.ext/com.zebra.zebrafilecopy.SplashActivity

Or use the following XML with OEMConfig passthrough if you want to start the application using MX :

```xml
<wap-provisioningdoc>
  <characteristic version="10.5" type="Intent">
    <parm name="Action" value="StartActivity" />
    <parm name="ActionName" value="android.intent.action.MAIN" />
    <parm name="Package" value="com.zebra.zebrafilecopy.ext" />
    <parm name="Class" value="com.zebra.zebrafilecopy.SplashActivity" />
  </characteristic>
</wap-provisioningdoc>
```

*********************************************************

