********************************
Zebra File Copy
********************************

Documentation
Copy paste what you need.
********************************

Use intent to copy files.


adb shell am broadcast -a com.zebra.zebrafilecopy.copyfile -n com.zebra.zebrafilecopy/com.zebra.zebrafilecopy.CopyBroadcastReceiver --es source "/sdcard/Documents/MotoRDP.xml" --es destination "/enterprise/usr/MotoRDP.xml"


adb shell am broadcast -a com.zebra.zebrafilecopy.copyfile -n com.zebra.zebrafilecopy/com.zebra.zebrafilecopy.CopyBroadcastReceiver --es source "/sdcard/Documents/MotoRDP.xml" --es destination "/enterprise/usr/MotoRDP.xml" --es chmod "0666"


chmod should be a numerical value 0XXX with the octal representation of permissions.
recommended chmod is 0666.


adb shell am broadcast -a com.zebra.zebrafilecopy.copyfile -n com.zebra.zebrafilecopy/com.zebra.zebrafilecopy.CopyBroadcastReceiver --es source "/sdcard/Documents/MotoRDP.xml" --es destination "/enterprise/usr/MotoRDP.xml" --es chmodunix "-rw-rw-rw-"


chmodstring should be a text value of 10 characters begining whith - with this structure -XXXXXXXXX where X can be rwxst depending on the position.
Read linux documentation for more information.
recommended chmodunix is -rw-rw-rw-.

Go to the site: https://chmod-calculator.app/ to calculate the CHMOD.

********************************


Or use the following XML to import a StageNow Profile:


<wap-provisioningdoc>
  <characteristic version="10.5" type="Intent">
    <parm name="Action" value="Broadcast" />
    <parm name="ActionName" value="com.zebra.zebrafilecopy.copyfile" />
    <parm name="Package" value="com.zebra.zebrafilecopy" />
    <parm name="Class" value=".CopyBroadcastReceiver" />
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
