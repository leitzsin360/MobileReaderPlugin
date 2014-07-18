MobileReaderPlugin
==============================================

PhoneGap(3.3+) plugin to access the Mobile reader device on Android. 

Install
========================================
Assuming the PhoneGap CLI is installed, from the command line run:

cordova plugin add https://github.com/leitzsin360/MobileReaderPlugin

add following uses-permission in file AndroidManifest.xml:

<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />

Usage:
========================================
plugin object name:window.mobilereader.

call method usage:
	window.mobilereader.<funname>(scallback(retDataJsonObject),ecallback(object),options);
	
paramters and arguments define:
	callback:success callback,function
	ecallback:error callback,,function
	options:json array,nullable
	retDataJsonObject fields:
		1.firmwareversion:firmware version,string type
		2.userName:string
		3.creditCardNumber:card number,string type
		4.result:error code ,0=OK,-1=failed,number
		5.errorMsg:error message,string type
		6.expiration:string type
		7.data:string type
		8.KSN:string type
		9.encryptionMode:encrypt mode, string type	

for example: 
call read function to read data from mobile reader,js code is :
	window.mobilereader.read(scallback(retDataJsonObject),ecallback(object),options);


Function List
========================================


1.Function read
========================================
Command to request swiping card
param:
scallback(jsonobject):return jsondata
options=[]


2.Function standby
========================================
Command to enter standby mode
param:
options=[]


3.Function exit
========================================
Command to exit standby mode
param:
options=[]


4.Function worklock
========================================
Command to set the Work mode of device and Lock device
param:
options=[{data:"workmode(2d)+lockstate(2d)"}]
	workmode:
	　　　　　　　　00 --  Passive Mode.
	　　　　　　　　01 --  Active Mode.
	lockstate:
	　　　　　　　　00  --  Locked
	　　　　　　　　01  --  Unlocked


5.Function reset
========================================
Command to to restore to default settings and unlock the device
param:
options=[{data:"oldkey(32d)"}]
	oldkey:oldkeys, length  >=32 .

6.Function timeout
========================================
Command to set timeout to enter Standby mode and sleep mode
param:
options=[{data:"<StandbyTime>"}]
	StandbyTime:　　uint: minute.
　　　　　　　　00 		--  don’t time out
　　　　　　　　01～255 	--  scope of time to enter Standby mode (1 min - 255min).


7.Function test
========================================
Command to test communication link
param:
options=[{data:"<testdata>"}]
	testdata:　test data ,lenght > 60.


7.Function writekey
========================================
Command to write Key to device
param:
options=[{"oldKey":"<oldKey>",
		"oldKsn":"<oldKsn>",
		"oldKeyManage":"<oldKeyManage>",
		"newKey":"<newKey>",
		"newKsn":"<newKsn>",
		"newKeyManage":"<newKeyManage>"
		}]
	
	<oldKey>:old key;
	<oldKsn>:old ksn,length>=16;
	<oldKeyManage>:old key manage type,values is "FixedKey" or "Dukpt";
	<newKey>:new key,length>=32;
	<newKsn>:new ksn,length>=16;
	<newKeyManage>:new key manage type,values is "FixedKey" or "Dukpt";












read data from mobile reader





Support
========================================
support email:health@sin360.net


