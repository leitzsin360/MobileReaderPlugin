package com.lke.plugin;



import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Build;
import android.text.format.Time;
import android.util.Log;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import com.gdseed.mobilereader.MobileReader;
import com.gdseed.mobilereader.MobileReader.ReaderStatus;

public class MobileReaderPlugin extends CordovaPlugin  {

    private static final String LOG_TAG = "com.lke.Plugin.MobileReaderPlugin";
	private static final String Algorithm = "DESede/ECB/NOPADDING";
	private final static String modelMS62x = "MS62x";
	private final static String modelMS22x = "MS22x";
	
	private android.content.Context myContext;
	
	private String curModel = new String("MS62x");
	private String phoneModel = new String();
	private String phoneSysCode = new String();
	private String phoneCountry = new String();
	private String phoneManufacturer = new String();
	private boolean playSounds = true;
	
	
	//1) Command to set timeout to enter Standby mode and sleep mode
	private static final String CMDSTANDBY="standby";
	private static final String CMDEXITSTANDBY="exit";

	//2) Command to write Key
	private static final String CMDWRITE="writekey";

	//3) Command to to restore to default settings and unlock the device
	private static final String CMDRESET="reset";

	//4) Command to test communication link
	private static final String CMDTEST="test";

	//5) Command to set the Work mode of device and Lock device 
	private static final String CMDWORK="worklock";
	private static final String CMDLOCK="lock";
	private static final String CMDUNLOCK="unlock";

	//6) Command to request swiping card
	private static final String CMDREAD="read";	
	private static final String CMDTIMEOUT="timeout";
	
	//ERROR CODE
	private static final String ERROK="0";
	private static final String ERRFailed="-1";
	private static final String ERRUNKNOWNFORMAT="100";
	private static final String ERRPARSEFAILED="-1";
	//private static final String ERRUNKNOWNFORMAT="100";
	//private static final String ERRUNKNOWNFORMAT="100";
	//private static final String ERRUNKNOWNFORMAT="100";
	
	private MobileReader mobileReader;
	
    private CallbackContext callbackContext;
    
    private JSONObject lastReadData;
	
    String action;
    JSONArray args;
    /**
     * Executes the request.
     *
     * This method is called from the WebView thread. To do a non-trivial amount of work, use:
     *     cordova.getThreadPool().execute(runnable);
     *
     * To run on the UI thread, use:
     *     cordova.getActivity().runOnUiThread(runnable);
     *
     * @param action          The action to execute.
     * @param args            The exec() arguments.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return                Whether the action was valid.
     *
     */
    @Override
    public boolean execute(String myaction, JSONArray myargs, CallbackContext mycallbackContext) {
        this.callbackContext = mycallbackContext;
        this.action = myaction;
        this.args = myargs;
        log("JSONArray:"+args.toString());
        if (!mobileReader.deviceIsAvailable()){
        	callbackContext.error("mobile reader is not ready");
        	return false;
        }
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
        
		        JSONObject ret = MobileReaderPlugin.this.newResultObject();
		        if(action.equals(CMDRESET)) {
		        	reset(args,ret);
		        }else if(action.equals(CMDSTANDBY)){
		        	enterStandby(ret);
		        	//callbackContext.success(ret);
		        }else if(action.equals(CMDEXITSTANDBY)){
		        	exitStandby(ret);
		        	//callbackContext.success(ret);
		        }else if(action.equals(CMDTEST)){
		        	test(args,ret);
		        	//callbackContext.success(ret);
		        }else if(action.equals(CMDWORK)){
		        	worklock(args,ret);
		        	//callbackContext.success(ret);
		        }else if(action.equals(CMDWRITE)){
		        	writeKey(args,ret);
		        	//callbackContext.success(ret);
		        //}else if(action.equals(CMDLOCK)){
		        //	SendCmd(cmdLockSystem);
		        //	callbackContext.success(ret);
		        //}else if(action.equals(CMDUNLOCK)){
		        //	open();
		        //	callbackContext.success(ret);
		        }else if(action.equals(CMDREAD)){
		        	//callbackContext.success(ret);
		        	//this.doReadData();
		        	open();
		        }else if(action.equals(CMDTIMEOUT)){
		        	JSONObject obj = args.optJSONObject(0);
		        	if (obj != null) {
		        		try{
			        		int timeout = obj.optInt(FDATA);
			        		setTimerout(timeout);
			        		callbackContext.success(ret);
			        		//return true;
		        		}catch(Exception ex)
		        		{
		        			log(ex.getMessage());
		        			callbackContext.error("settimeout failed");
		        			//return false;
		        		}
		        	}
		        	//return false;
		        }else{
		        	callbackContext.error("Unknown action:"+action);
		        	//return false;
		        }
            }
        });
        return true;
    } 
    
    /**
     * Constructor.
     */
    public MobileReaderPlugin() {
		phoneSysCode = Build.VERSION.RELEASE;
		phoneModel = Build.MODEL;
		phoneManufacturer = Build.MANUFACTURER;
	   	this.InitMobileReader();
    }
    
    private MobileReader getMobileReader() {
     	this.InitMobileReader();
		return mobileReader;
	}


	/*init reader*/
    public boolean InitMobileReader()
    {
    	if(mobileReader == null)
    	{
    		if(myContext== null){
    			myContext = cordova.getActivity();
    		}
    		mobileReader = new MobileReader(myContext);
    		mobileReader.setOnDataListener(new MobileReader.CallInterface() {
    			@Override
    			public void call(ReaderStatus state) {
    				//int tmp = state.ordinal();
    				//ReaderStatus state = ReaderStatus.values()[tmp];
    				switch(state)
    				{
    					case BEGIN_RECEIVE:
    						sendStatus("begin receive",false);
    						break;
    					case TIMER_OUT:
    						sendStatus("time out",true);
    						break;
    					case END_RECEIVE:
    						sendStatus("end receive",false);
    						break;
    					case DEVICE_PLUGIN:
    						sendStatus("device plugin",false);
    						break;
    					case DEVICE_PLUGOUT:
    						sendStatus("device plugout",true);
    						close();
    						break;
    					case DECODE_OK:
    						sendStatus("decode ok",false);
    						doReadData();
    						break;
    					case DECODE_ERROR:
    						sendStatus("decode error",true);
    						break;
    					case RECEIVE_ERROR:
    						sendStatus("receive error",true);
    						break;
    					case BUF_OVERFLOW:
    						sendStatus("buffer overflow",true);
    						break;
    				}
    			}
    		});    		
    		log("mobile reader inited");
    	}
    	return true;
    }
    
    
    public void sendStatus(String status,boolean isError)
    {
    	log(status);
    	//this.onMessage(status,null);
    	if(isError && this.callbackContext != null)
    		this.callbackContext.error(status);
    }
    
    
    public boolean doReadData()
    {
		try {
			log("doReadData");
	    	lastReadData = readdata();
			this.callbackContext.success(lastReadData);
			return true;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		finally{
			//this.mobileReader.close();
		}
    }
    
	public int writecmd(String cmd) {
		byte tmp[] = new byte[256];
		int cnt = StringToHex(cmd, tmp);
		mobileReader.write(tmp, cnt);
		byte rawData[] = new byte[1024];
		int len = mobileReader.read(rawData);
		int ret = len>0?(int)rawData[0]:-1;
		if(0x77==ret) ret = (int)rawData[1];
		log("writecmd --> " + cmd);
		return ret;
	}
	
	
	public int writecmd(String cmd,JSONObject retObj)
	{
		int ret = writecmd(cmd);
		callbackResult(retObj,ret,"");
		return ret;
	}
	
	static final String cmdEnterStandBy="0F";
	public int enterStandby(JSONObject retObj)
	{
		return writecmd(cmdEnterStandBy,retObj);
	}
	static final String cmdExitStandBy="0C";
	public int exitStandby(JSONObject retObj)
	{
		return writecmd(cmdExitStandBy,retObj);
	}
    
	static final String cmdTest="01";
	public int test(JSONArray args,JSONObject retObj)
	{
		String data = "";
		if(args != null && args.length()>0){
			JSONObject obj = args.optJSONObject(0);
			data = obj.optString(FDATA);
		}
		if("".equals(data) || data.length()<=60)
		{
			callbackResult(retObj,-1,"test data length must > 60");
			return -1;
		}
		String cmd = cmdTest+data;
		
		byte tmp[] = new byte[256];
		int cnt = StringToHex(cmd, tmp);
		mobileReader.write(tmp, cnt);
		byte rawData[] = new byte[1024];
		int len = mobileReader.read(rawData);
		int ret = len>0?(int)rawData[0]:-1;
		if(0x77==ret) ret = (int)rawData[1];
		log("test --> " + cmd);
		
		callbackResult(retObj,ret,"");
		
		return ret;
	}
    
	
	static final String cmdWorkLock="05";
	public int worklock(JSONArray args,JSONObject retObj)
	{
		String data="";
		if(args != null && args.length()>0){
			JSONObject obj = args.optJSONObject(0);
			data = obj.optString(FDATA);
		}
		if(data.length()==0){
			data="0001";
		}
		String cmd = cmdWorkLock+data;
		
		return writecmd(cmd,retObj);
	}
	
	///write key
	public int writeKey(String oldKey,String oldKsn,String oldKeyManage,String newKey,String newKsn,String newKeyManage,JSONObject retObj)
	{
		int ret=-1;
		String retCode=ERROK;
		String errMsg = "";
	
		if (!mobileReader.deviceIsAvailable()) return ret;
		
		if (oldKsn.length() < 14) {
			errMsg = "oldKsn length <14";
			callbackResult(retObj,ret,errMsg);			
			return ret;
		}	
		
		if (DUKPT.equals(oldKeyManage)) {
			byte tmpOldKsn[] = new byte[10];
			byte baseOldKey[] = new byte[16];
			byte oldInitKey[] = new byte[16];
			StringToHex(oldKsn, tmpOldKsn);
			StringToHex(oldKey, baseOldKey);
			generateInitKeyByBdk(baseOldKey, tmpOldKsn, oldInitKey);
			log("Cmd -->" + "oldInitKey" + oldInitKey);
			oldKey = HexToString(oldInitKey, oldInitKey.length, 100);
		} else if (FIXED_KEY.equals(oldKeyManage)){
			
		} else {
			errMsg = "Unknown oldKeyManage";
			callbackResult(retObj,ret,errMsg);			
			return ret;
		}
		
		cmdRefactory = headRefactory + oldKey;				
		if (newKey.length() < 32) {
			errMsg = "newKen length <32";
			callbackResult(retObj,ret,errMsg);			
			return ret;
		}
		
		log("Cmd -->" + newKey);
		
		if (newKsn.length() < 14) {
			errMsg =("New KSN length less 14 Characters!!");
			callbackResult(retObj,ret,errMsg);			
			return ret;
		}
		
		String keyManagent = new String();
		if (DUKPT.equals(newKeyManage)) {
			keyManagent = "fe";
			byte tmpKsn[] = new byte[10];
			byte baseKey[] = new byte[16];
			byte initKey[] = new byte[16];
			StringToHex(newKsn, tmpKsn);
			StringToHex(newKey, baseKey);
			generateInitKeyByBdk(baseKey, tmpKsn, initKey);
			newKey = HexToString(initKey, initKey.length, 100);					
		} else if (FIXED_KEY.equals(newKeyManage)){
			keyManagent = "00";
		} else {
			errMsg = "Unknown oldKeyManage";
			callbackResult(retObj,ret,errMsg);			
			return ret;
		}
		cmdKey = cmdMainKey + "10" + keyManagent + "0300" + newKey;
		cmdKsnAndDeviceInfo = "060016" + newKsn + "00" + "00000000" + "00000000" + "0015aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa000000000000";
		log("Key->" + cmdKey);
		log("KSN DEV -> " + cmdKsnAndDeviceInfo);
		messageHandler(MessageType.InjectBegin);		
		return ret;
	}
	
	final String FOLDKEY="oldKey";
	final String FOLDKSN="oldKsn";
	final String FOLDKEYMANAGE="oldKeyManage";
	final String FNEWKEY="newKey";
	final String FNEWKSN="newKsn";
	final String FNEWKEYMANAGE="newKeyManage";
	///write key
	public int writeKey(JSONArray args,JSONObject retObj)
	{
		int ret=-1;
		String oldKey, oldKsn, oldKeyManage, newKey, newKsn, newKeyManage;
		if(args == null || args.length()== 0 || args.optJSONObject(0) == null){
			callbackResult(retObj,ret,"missing key data");		
			return -1;
		}
		JSONObject dataobj = args.optJSONObject(0);
		
		oldKey = getResultField(dataobj,FOLDKEY);
		oldKsn = getResultField(dataobj,FOLDKSN);
		oldKeyManage = getResultField(dataobj,FOLDKEY);
		newKey = getResultField(dataobj,FNEWKEY);
		newKsn = getResultField(dataobj,FNEWKSN);
		newKeyManage = getResultField(dataobj,FNEWKEYMANAGE);
		
		return writeKey( oldKey, oldKsn, oldKeyManage, newKey, newKsn, newKeyManage, retObj);
	}
	
	static final String cmdReset="03";
	public int reset(JSONArray args,JSONObject retObj)
	{
		String data="";
		if(args != null && args.length()>0){
			JSONObject obj = args.optJSONObject(0);
			data = obj.optString(FDATA);
		}
		String cmd = cmdReset+data;
		return writecmd(cmd,retObj);
	}
    
    public JSONObject  readdata() throws JSONException
    {
    	byte rawData[] = new byte[1024];
    	int trackCount[] = new int[1];
    	trackCount[0] = 0;
    	JSONObject ret = newResultObject();
		int len = mobileReader.read(rawData);
		int recode = 0;
		
		if (len < 1) {
			//if (len < 1) {
			//	mobileReader.writeRecoderToFile(getCurrentFileName() + "_null.raw");
			//} else {
			//	mobileReader.writeRecoderToFile(getCurrentFileName() + "_crc.raw");
			//}
			recode = 100;
			ret.put(FERRMSG,"No Data!");
		} else {
			String display = new String();
			recode = (int)rawData[0];
			if (0x07 == rawData[0] || 0x50 == rawData[0]
					|| 0x48 == rawData[0] || 0x08 == rawData[0]
					|| 0x49 == rawData[0]) {
				
				ret.put(FRESULT,rawData[0]);
				ret.put(FERRMSG,"Please update the lastest hardware!");
			} else if (0x60 == rawData[0]) {
				ret = Lib12_ParseTrack(rawData, trackCount);
				if (trackCount[0] < 2) {
					//outMsg.setTextColor(Color.RED);
				} else {
					//outMsg.setTextColor(Color.WHITE);
				}
				//ret.put(FDATA,display);
			} else {
				//outMsg.setTextColor(Color.RED);
				display = "Unknown Format !!";
				mobileReader.writeRecoderToFile(getCurrentFileName() + "_unknown.raw");
				ret.put(FERRMSG,display);
				ret.put(FRESULT,ERRUNKNOWNFORMAT);
			}

			if ("0" != ret.get(FRESULT)) {
				//outMsg.setTextColor(Color.RED);
				//outMsg.setText("Parse Data Error");
				display = "Parse Data Error";
				ret.put(FERRMSG,display);
			} else {
				//outMsg.setText(display);
				ret.put(FDATA,display);
				mobileReader.writeRecoderToFile("ok.raw");
			}
		}

		//mobileReader.close();
		ret.put(FRESULT,recode);
	
		return ret;
    }
    
    /*release reader*/
    public boolean ReleaseMobileReader()
    {
    	if(this.getMobileReader() != null) {
    		this.getMobileReader().close();
    		//this.getMobileReader().releaseRecorderDevice();
    	} 
    	
    	return true;
    }
    
    public boolean deviceIsAvailable()
    {
    	return this.getMobileReader().deviceIsAvailable();
    }
    
    public void setOnDataListener(com.gdseed.mobilereader.MobileReader.CallInterface arg0)
    {
    	this.getMobileReader().setOnDataListener(arg0);
    }
    /*get version string*/
    public String getVersion()
    {
    	return this.getMobileReader().getVersion();
    }
    
    public boolean open() // equivalent to public Boolean open(true).
    {
    	return open(true);
    }
    
    //open the mobile reader, you can control play sounds. If the device is MS2xx playSound=true,
    //if the device is MS6xx playSound=false
    public boolean open(boolean playSounds)
    {
    	return this.getMobileReader().open(playSounds);
    }

    public void close()  // close the mobile reader.
    {
    	this.getMobileReader().close();
    }

 
    public void writeRecoderToFile()// for debug.
    {
    	//this.getMobileReader().writeRecoderToFile();
    	//this.mobileReader.
    }
    
    public void setTimerout(int timeout)
    {
    	this.getMobileReader().setTimerout(timeout);
    }
    
    
   

    
    
    @Override
    public void onResume(boolean multitasking)
    {
        this.InitMobileReader();
        super.onResume(multitasking);
    }
    
    @Override
    public void onPause(boolean multitasking)
    {
    	this.ReleaseMobileReader();
    	super.onPause(multitasking);
    }
    
    @Override
    public void onReset()
    {
       	this.ReleaseMobileReader();
    	this.InitMobileReader();
    	//this.getMobileReader().onReset();
    	super.onReset();
    }
    
    @Override
    public void onDestroy()
    {
    	this.ReleaseMobileReader();
    	super.onDestroy();
    }
    
    
	private JSONObject Lib12_ParseTrack(byte input[], int track[]) throws JSONException {
		int panLength = 0;
		int index = 0;
		String version = new String();
		String encryptMode = new String();
		String first6Pan = new String();
		String last4Pan = new String();
		String expiryDate = new String();
		String userName = new String();
		String ksn = new String();
		String encrypedData = new String();
		//String ret = new String();
		String xxx = new String();
		String trackInfo = new String();
		byte byEncrypedData[];
		String decrypedData = new String();
		int tmp = 0;
		
		JSONObject retObj = newResultObject();

		index = 1;
		version += (char) ('0' + input[index]);
		version += '.';
		//version += (char) ('0' + input[++index]);
		version += String.format("%02d", input[++index]);
		

		tmp = input[++index];
		if (curModel.equals(modelMS22x)) {
			if (1 == tmp) {
				encryptMode = "fixed key";
			} else if (2 == tmp){
				encryptMode = "dukpt";
			} else {
				encryptMode = "unknown";
			}
			
		} else if (curModel.equals(modelMS62x)){
			if (0 == tmp) {
				encryptMode = "fixed key";
			} else if (1 == tmp) {
				encryptMode = "diperse I";
			} else if (0xFE == (int)(tmp&0xff)){
				encryptMode = "dukpt";
			} else {
				encryptMode = "unknown";
			}
		}

		panLength = input[++index];
		// xxx
		for (int i = 0; i < panLength - 10; i++) {
			xxx += "x";
		}

		// fist 4 pan
		index++;
		for (int i = 0; i < 6; i++) {
			tmp = input[(i >> 1) + index] & 0xff;
			tmp = i % 2 == 0 ? tmp >> 4 : tmp & 0x0f;
			tmp = tmp > 9 ? tmp - 10 + 'A' : tmp + '0';
			first6Pan += (char) tmp;
		}

		// last 4 pan
		index += 3;
		for (int i = 0; i < 4; i++) {
			tmp = input[(i >> 1) + index] & 0xff;
			tmp = i % 2 == 0 ? tmp >> 4 : tmp & 0x0f;
			tmp = tmp > 9 ? tmp - 10 + 'A' : tmp + '0';
			last4Pan += (char) tmp;
		}

		// expiry data
		index += 2;
		for (int i = 0; i < 4; i++) {
			tmp = input[(i >> 1) + index] & 0xff;
			tmp = i % 2 == 0 ? tmp >> 4 : tmp & 0x0f;
			tmp = tmp > 9 ? tmp - 10 + 'A' : tmp + '0';
			expiryDate += (char) tmp;
		}

		// User name
		index += 2;
		for (int i = 0; i < 26; i++) {
			userName += (char) input[i + index];
		}

		// ksn
		index += 26;
		for (int i = 0; i < 20; i++) {
			tmp = input[(i >> 1) + index] & 0xff;
			tmp = i % 2 == 0 ? tmp >>> 4 : tmp & 0x0f;
			tmp = tmp > 9 ? tmp - 10 + 'A' : tmp + '0';
			ksn += (char) tmp;
		}

		// encrypted data
		index += 10;
		for (int i = 0; i < 160; i++) {
			if (0 != i && 0 == (i % 32))
				encrypedData += '\n';
			tmp = input[(i >> 1) + index] & 0xff;
			tmp = i % 2 == 0 ? tmp >> 4 : tmp & 0x0f;
			tmp = tmp > 9 ? tmp - 10 + 'a' : tmp + '0';
			encrypedData += (char) tmp;
		}
		
		byEncrypedData = new byte[80];
		for (int i = 0; i < 80; i++) {
			byEncrypedData[i] = input[index + i];
		}

		index += 80;
		byte tmpTrackInfo = input[index];
		int trackCount = 0;
		for (int i = 0; i < 3; i++) {
			byte info = (byte) (tmpTrackInfo & (0x01 << i));
			if (0 != info) {
				trackInfo += (char) ('1' + i);
				trackCount++;
			}
		}
		
		if (null != track) {
			track[0] = trackCount;
		}

//		ret = ("Firmware Version:" + version + "\n" + "Encryption Mode:"
//				+ encryptMode + "\n" + "Track Info:" + trackInfo + "\n"
//				+ "PAN:" + first6Pan + xxx + last4Pan + "\n" + "Expiry Date:"
//				+ expiryDate + "\n" + "User Name:" + userName + "\n" + "KSN:"
//				+ ksn + "\n" + "Encrypted Data:" + "\n" + encrypedData + "\n");
	
//		if (true) {
//			return ret;
//		}
		byte[] tmp_key = {0x01, 0x23, 0x45, 0x67, (byte)0x89, (byte)0xab, (byte)0xcd, (byte)0xef, (byte)0xfe, (byte)0xdc, (byte)0xba, (byte)0x98, 0x76, 0x54, 0x32, 0x10};
		for(int i = 0; i < byEncrypedData.length; i++) {
			if (0 != i && 0 == (i % 16)) {
				System.out.format("\n");
			}
			
			System.out.format("%02x ", byEncrypedData[i]);
		}
		byte[] srcBytes = decryptMode(tmp_key, byEncrypedData);
		for(int i = 0; i < srcBytes.length; i++) {
			if (0 != i && 0 == (i % 16)) {
				System.out.format("\n");
			}
			
			System.out.format("%02x ", srcBytes[i]);
		}
		
		for (int i = 0; i < 160; i++) {
			if (0 != i && 0 == (i % 32))
				decrypedData += '\n';
			tmp = srcBytes[(i >> 1)] & 0xff;
			tmp = i % 2 == 0 ? tmp >> 4 : tmp & 0x0f;
			tmp = tmp > 9 ? tmp - 10 + 'a' : tmp + '0';
			decrypedData += (char) tmp;
		}
		
		// 取pan
		int panIndex = 1;
		String realPan = new String();
		for(int i = 0; i < panLength; i++) {
			tmp = srcBytes[(i + panIndex) >> 1];
			if (0 != (i % 2)) {
				tmp >>= 4;
			}
			
			tmp &= 0x0f;
			realPan += tmp;
		}
		
		
//		ret += decrypedData;
//		ret += "\nPan:" + realPan;

		retObj.put(FVERSION,version);
		retObj.put(FENCRYPT,encryptMode);
		//retObj.put(FPAN,realPan);
		retObj.put(FEXPIR,expiryDate);
		retObj.put(FUSERNAME,userName);
		retObj.put(FKSN,ksn);
		retObj.put(FENCRYPTDATA,encrypedData);	
		retObj.put(FDATA,decrypedData);
		retObj.put(FTRACK,trackInfo);
		retObj.put(FCARDNUM,realPan);
		return retObj;//return ret;
		
	}

	public static byte[] decryptMode(byte[] keybyte, byte[] src) {
		try {
			SecretKey deskey = new SecretKeySpec(keybyte, Algorithm);
			Cipher c1 = Cipher.getInstance(Algorithm);
			c1.init(Cipher.DECRYPT_MODE, deskey);
			return c1.doFinal(src);
		} catch (java.security.NoSuchAlgorithmException e1) {
			Log.e("aa", e1.getMessage());
		} catch (javax.crypto.NoSuchPaddingException e2) {
			Log.e("aa", e2.getMessage());
		} catch (java.lang.Exception e3) {
			Log.e("aa", e3.getMessage());
		}
		
		return null;
	}
	    
	private String getCurrentFileName() {
		Time t = new Time();
		t.setToNow(); // 取得系统时间。
		int year = t.year % 100;
		int month = t.month + 1;//月份0～11
		int day = t.monthDay;
		int hour = t.hour; // 0-23
		int minute = t.minute;
		int second = t.second;
		String date = new String();
		//String.format("%02d-%02d-%02d_%02d:%02d:%02d", year, month, day, hour, minute, second);
		date = String.format("%02d%02d%02d_%02d%02d%02d", year, month, day, hour, minute, second);
		return phoneManufacturer + phoneModel + "_" + phoneSysCode + "_" + phoneCountry + "_" + mobileReader.getVersion().toString() + "_" + date ;
	}
	
	private static void log(String str) {
		Log.d(LOG_TAG, str + '\n');
	}	
	
	/*****/
	public JSONObject newResultObject()
	{
		JSONObject obj = new JSONObject();
		try {		
			obj.put(FVERSION,"");
			obj.put(FUSERNAME,"");
			//obj.put(FPAN,"");
			obj.put(FCARDNUM,"");
			obj.put(FEXPIR,"");
			obj.put(FRESULT,ERROK);
			obj.put(FERRMSG,"");
			obj.put(FDATA,"");
			obj.put(FKSN,"");
			obj.put(FENCRYPT,"");
			obj.put(FENCRYPTDATA,"");
			obj.put(FTRACK,"");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj;
	}
	
	static String FVERSION="firmwareversion";
	static String FUSERNAME="userName";
	static String FPAN="PAN";
	static String FCARDNUM="creditCardNumber";
	static String FRESULT="result";
	static String FERRMSG="errorMsg";
	static String FEXPIR="expiration";
	static String FDATA="data";
	static String FKSN="KSN";
	static String FENCRYPT="encryptionMode";
	static String FENCRYPTDATA="encryptedData";
	static String FTRACK="track";
	//static String EXPIR="Expiration";
	static String FTIMEOUT="timeout";
	
	
	private void generateInitKeyByBdk(byte baseKey[], byte ksn[], byte[] initKey)
	{
		System.arraycopy(ksn, 0, initKey, 0, 8);
		byte leftInitKey[] = new byte[8];
		System.arraycopy(initKey, 0, leftInitKey, 0, 8);
		byte tmp[] = tripleDes(baseKey, leftInitKey);
		System.arraycopy(tmp, 0, initKey, 0, 8);
		
		baseKey[0]		^= (byte)0xc0;	
		baseKey[1]		^= (byte)0xc0;	
		baseKey[2]		^= (byte)0xc0;	
		baseKey[3]		^= (byte)0xc0;
		baseKey[0 + 8]	^= (byte)0xc0;	
		baseKey[1 + 8]	^= (byte)0xc0;	
		baseKey[2 + 8]	^= (byte)0xc0;	
		baseKey[3 + 8]	^= (byte)0xc0;
		
		System.arraycopy(ksn, 0, initKey, 8, 8);
		byte rightInitKey[] = new byte[8];
		System.arraycopy(initKey, 8, rightInitKey, 0, 8);
		tmp = tripleDes(baseKey, rightInitKey);
		System.arraycopy(tmp, 0, initKey, 8, 8);
	}

	//private static final String Algorithm = "DESede/ECB/NOPADDING";
	public static byte[] tripleDes(byte[] keybyte, byte[] src) {
		try {
			SecretKey deskey = new SecretKeySpec(keybyte, Algorithm);
			Cipher c1 = Cipher.getInstance(Algorithm);
			c1.init(Cipher.ENCRYPT_MODE, deskey);
			return c1.doFinal(src);
		} catch (java.security.NoSuchAlgorithmException e1) {
			Log.e("aa", e1.getMessage());
		} catch (javax.crypto.NoSuchPaddingException e2) {
			Log.e("aa", e2.getMessage());
		} catch (java.lang.Exception e3) {
			Log.e("aa", e3.getMessage());
		}
		
		return null;
	}

	
	private String ByteToString(byte in) {
		String ret = String.format("%02x", in);
		return ret;
	}

	private String HexToString(byte input[], int len, int lineByte) {
		String ret = new String();
		for (int i = 0; i < len; i++) {
			if ((i % lineByte == 0) && i != 0) {
				ret += '\n';
			}

			ret += ByteToString(input[i]);
		}

		return ret;
	}
	
	private int GetHexChar(char c) {
		switch(c) {
		case '0':case '1':case '2':case '3':case '4':
		case '5':case '6':case '7':case '8':case '9':
			return c - '0';
		case 'a':case 'b':case 'c':case 'd':case 'e':case 'f':
			return c - 'a' + 10;
		case 'A':case 'B':case 'C':case 'D':case 'E':case 'F':
			return c - 'A' + 10;
		default:
			return -1;
		}
	}
	
	private int StringToHex(String str, byte[] dst) {
		int count = 0;
		int tmp = 0;
		for(int i = 0; i < str.length(); i++) {
			tmp = GetHexChar(str.charAt(i));
			if (tmp < 0) continue;
			if (count < dst.length * 2) {
				if (0 == (count % 2)) {
					dst[count >> 1] = (byte)(tmp * 16);
				} else {
					dst[count >> 1] += (byte)tmp;
				}
				
				count++;
			} else {
				return -1;
			}
		}
		
		return count >> 1;
	}

	private int injectStep = 0;
	private int repeatCount = 0;
	private int maxRepeatCount = 3;
	//private Timer timer = null;
	//private TimerTask timerTask = null;
	private int timerOut = 1500;
	private String title = new String();

	
	private void SendCmd(String cmd) {
		byte tmp[] = new byte[256];
		log("Cmd --> " + cmd);
		int ret = StringToHex(cmd, tmp);
		mobileReader.write(tmp, ret);
	}
	
	private void nextStep(int step) {
		log("next Step --> " + (step + 1));
		//stopTimer();
		open();
		int maxStep = 4;
		title = "Step --> " + (step + 1) + "/" + maxStep + '\n';
		log(title);
		switch(step) {
		case 0:
			SendCmd(cmdRefactory);
			//startTimer(timerOut);
			break;
		case 1:
			SendCmd(cmdKey);
			//startTimer(timerOut);
			break;
		case 2:
			SendCmd(cmdKsnAndDeviceInfo);
			//startTimer(timerOut);
			break;
		case 3:
			SendCmd(cmdLockSystem);
			//startTimer(timerOut);
			break;
		case 4:
			messageHandler(MessageType.InjectOk);
			return;
		default:
			messageHandler(MessageType.InjectFail);
			return;
		}
		
		setOutput("send.....");
	}
	
	private void setOutput(String str) {
		//lblOutput.setText(title + str);
	}

	
	private void messageHandler(MessageType msg) {
		log(msg.toString());
		if (MessageType.InjectBegin == msg) {
			injectStep = 0;
			repeatCount = 0;
			nextStep(injectStep);
		} else if (MessageType.ReturnFail == msg) {
			if (repeatCount < maxRepeatCount) {
				repeatCount++;
				nextStep(injectStep);
			} else {
				messageHandler(MessageType.InjectFail);
			}
		} else if (MessageType.ReturnOk == msg) {
			nextStep(++injectStep);
		} else if (MessageType.TimeOut == msg) {
			if (repeatCount < maxRepeatCount) {
				repeatCount++;
				nextStep(injectStep);
			} else {
				messageHandler(MessageType.InjectFail);
			}
		} else if (MessageType.InjectFail == msg) {
//			stopTimer();
			close();
			setOutput("Inject Failed!!!");
//			btnKeyInject.setEnabled(true);
		} else if (MessageType.InjectOk == msg) {
//			stopTimer();
			close();
//			title = "";
			setOutput("Inject Ok!!!!");
//			btnKeyInject.setEnabled(true);
		}
	}
	
	
	private final static String FIXED_KEY = "FixedKey";
	private final static String DUKPT = "Dukpt";
	
	// cmd
	private final static String headRefactory = new String("03");
	private static String cmdRefactory ;
	private final static String cmdLockSystem = new String("050100");
	private final static String cmdMainKey = new String("02100001000123456789abcdeffedcba9876543210");
	private String cmdKey = new String();
	private String cmdKsnAndDeviceInfo = new String();
	private static enum MessageType {
		InjectBegin, TimeOut, ReturnOk, ReturnFail, InjectFail, InjectOk
	}

	
	private final String errMsg[]=new String[]{
		"EEPROM can’t be written correctly"
		,"memory overflow"
		,"EEPROM is not enough"
		,"the index of key has been occupied"
		,"unknown error"
		,"data of magnetic track can’t be decoded correctly"
		,"key reading error"
		,"the key can’t be used for encryption"
		,"the key don’t support disperse algorithm"
		,"the dukpt count overflow"
		,"the device did not receive right command"
		,"unknown command"
		,"random number is null"
		,"the command did not unrealized"
		,"the command format is error"
		,"reserved"
		,"the device is unlocked"
		,"the old key that is encrypted track data is wrong"
		,"reserved"
		,"Disperse flag cannot support host mode"
		,"the device is locked"		
	};

	public String getErrMsg(int errcode,String defaultMsg){
		if(errcode>=0 && errcode<errMsg.length) return errMsg[errcode];
		else if(errcode == -1 ){
			if("".equals(defaultMsg)) return "exec failed";
			else return defaultMsg;
		}else{
			return "";
		}
	}

	
	public void setResultField(JSONObject retObj,String name,String str){
		try{
			if(retObj != null){
				retObj.put(name,str);
			}
		}catch(JSONException ex){
			log(ex.getMessage());
		}
	}
	

	public void setResultField(JSONObject retObj,String name,int num){
		try{
			if(retObj != null){
				retObj.put(name,String.valueOf(num));
			}
		}catch(JSONException ex){
			log(ex.getMessage());
		}
	}	

	public void callbackResult(JSONObject retObj,int errCode,String defaultMsg){
		setResultField(retObj,FRESULT,errCode);
		if(errCode == 0x90)
		{
			callbackContext.success(retObj);
		}else{
			setResultField(retObj,FERRMSG,getErrMsg(errCode,defaultMsg));
			callbackContext.error(retObj);
		}
	}		
	/*
	public void callbackResult(JSONObject retObj,int errCode){
		setResultField(retObj,FRESULT,errCode);
		if(errCode == 0x90)
		{
			callbackContext.success(retObj);
		}else{
			setResultField(retObj,FERRMSG,getErrMsg(errCode));
			callbackContext.error(retObj);
		}
	}
	*/

	public String getResultField(JSONObject retObj,String name){
		try{
			if(retObj != null){
				return retObj.getString(name);
			}
			return null;
		}catch(JSONException ex){
			log(ex.getMessage());
			return null;
		}
	}
 }
