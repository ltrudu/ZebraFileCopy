********************************
Zebra File Copy
********************************

Documentation
Copy paste what you need.
********************************

## Use intent to copy files.


```cmd
adb shell am broadcast -a com.zebra.zebrafilecopy.copyfile --es source "/sdcard/Documents/MotoRDP.xml" --es destination "/enterprise/usr/MotoRDP.xml"
```


## Use intent to copy files and change its permission using a numerical chmod. 

chmod should be a numerical value 0XXX with the octal representation of permissions.

Recommended chmod is 0666.


```cmd
adb shell am broadcast -a com.zebra.zebrafilecopy.copyfile --es source "/sdcard/Documents/MotoRDP.xml" --es destination "/enterprise/usr/MotoRDP.xml" --es chmod "0666"
```


## Use intent to copy files and change its permission using a unix notation chmod.

chmodstring should be a text value of 10 characters begining whith - with this structure -XXXXXXXXX where X can be rwxst depending on the position.

Read linux documentation for more information.

Recommended chmodunix is -rw-rw-rw-.


```cmd
adb shell am broadcast -a com.zebra.zebrafilecopy.copyfile --es source "/sdcard/Documents/MotoRDP.xml" --es destination "/enterprise/usr/MotoRDP.xml" --es chmodunix "-rw-rw-rw-"
```


Go to the site: https://chmod-calculator.app/ to calculate the CHMOD.


## Use intent to copy files with MX FileMgr.

```cmd
adb shell am broadcast -a com.zebra.zebrafilecopy.copyfile --es source "/sdcard/Documents/MotoRDP.xml" --es destination "/enterprise/usr/MotoRDP.xml" --es usemx "true"
```

## Use intent to deploy Enteprise Browser configuration folder with MX FileMgr.

```cmd
adb shell am broadcast -a com.zebra.zebrafilecopy.copyfile --es source "/sdcard/Documents/enterprisebrowser" --es destination "/enterprise/device/enterprisebrowser" --es usemx "true"
```


********************************


Or use the following XML to import a StageNow Profile:

```xml
<wap-provisioningdoc>
  <characteristic version="10.5" type="Intent">
    <parm name="Action" value="Broadcast" />
    <parm name="ActionName" value="com.zebra.zebrafilecopy.copyfile" />
    <parm name="Package" value="com.zebra.zebrafilecopy.ext" />
    <parm name="Class" value="com.zebra.zebrafilecopy.CopyBroadcastReceiver" />
    <parm name="Category" value="android.intent.category.DEFAULT" />
    <characteristic type="Extra">
      <parm name="ExtraType" value="string" />
      <parm name="ExtraName" value="source" />
      <parm name="ExtraValue" value="/sdcard/Documents/MotoRDP.xml" />
    </characteristic>
    <characteristic type="Extra1">
      <parm name="Extra1Type" value="string" />
      <parm name="Extra1Name" value="destination" />
      <parm name="Extra1Value" value="/enterprise/usr/MotoRDP.xml" />
    </characteristic>
    <characteristic type="Extra2">
      <parm name="Extra2Type" value="string" />
      <parm name="Extra2Name" value="chmodunix" />
      <parm name="Extra2Value" value="-rw-rw-rw-" />
    </characteristic>
  </characteristic>
</wap-provisioningdoc>
```

## Additional StageNow profiles are available in the Stagenow folder of this repository

********************************

Managed Configuration

********************************

To be able to process managed configuration settings and updates, the app need to be launched to register to the updates events.

Or use the following XML to import a StageNow Profile if you want to start the application :

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
