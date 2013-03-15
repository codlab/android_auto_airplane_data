package eu.codlab.airplane;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

public class ScreenStateReceiver extends BroadcastReceiver{
	private static Context _context;
	private static boolean _is_screen_on = true;
	private static boolean _is_calling = false;
	private static boolean _try_shut = false;
	private static boolean _is_init;
	private static ListenPhone _listener_phone;
	private final static int WIFI_AP_STATE_UNKNOWN=-1;
	private final static int WIFI_AP_STATE_ENABLING=2;
	private final static int WIFI_AP_STATE_ENABLED=3;
	private static WifiManager wifi;
	private static SharedPreferences _shared;

	public ScreenStateReceiver(){
		if(_context == null)
			_is_init = false;
	}

	public ScreenStateReceiver(Context context){
		this();
		_context = context;
		init(context);
		event(true);
	}


	/*public void reset(Context context){
		if(context == null && !isInit())
			return;
		else if(!isInit())
			init(context);
		if(_listener_phone != null)
			_listener_phone.reset();
	}*/

	private void setMobileDataEnabled(Context context, boolean enabled) throws ClassNotFoundException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

		final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final Class<?> conmanClass = Class.forName(conman.getClass().getName());
		final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
		iConnectivityManagerField.setAccessible(true);
		final Object iConnectivityManager = iConnectivityManagerField.get(conman);
		final Class<?> iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
		final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
		setMobileDataEnabledMethod.setAccessible(true);

		setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
	}

	public void allow3gData(Context context){
		try{
			setMobileDataEnabled(context, true);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void disallow3gData(Context context){
		try{
			setMobileDataEnabled(context, false);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @param context
	 * @param name
	 * @param value
	 * @param new_status indicate wether now 3g is available or not
	 */
	public void edit3gStat(Context context, String name, String value, String apn, boolean new_status){
		String columns[] = new String[] {"apn", "type","current"};
		String where = "_id = ?";
		String where2 = "_id = ?";
		String wargs[] = new String[] {name};
		String sortOrder = null;
		Cursor cur = context.getContentResolver().query(Uri.parse("content://telephony/carriers"), columns, where, wargs, sortOrder);
		if (cur != null) {
			if (cur.moveToFirst()) {
				ContentValues values = new ContentValues(1);
				String [] _values = value.split(",");
				String res ="";
				for(int i=0;i<_values.length;i++){
					if(new_status && _values[i].indexOf("1")>=0)
						res+=(i>0 ? ",":"")+_values[i].substring(1);
					else if(!new_status && _values[i].indexOf("1")<0)
						res+=(i>0 ? ",":"")+"1"+_values[i];
					else
						res+=(i>0 ? ",":"")+_values[i];
				}
				if(!new_status && apn.indexOf("no3g") < 0)
					apn+="no3g";
				else
					while(apn.indexOf("no3g")>=0)
						apn=apn.replace("no3g","");

				Log.d("SET APN", new_status + " " +apn+ " "+res);
				values.put("type", res);
				values.put("apn", apn);
				if (context.getContentResolver().update(Uri.parse("content://telephony/carriers"), values, where, wargs) == 1)
					Toast.makeText(context, "update", Toast.LENGTH_SHORT).show();
			}
			cur.close();
		}
	}
	public void parcours3g(Context context, boolean stat){
		try{
			String[] arrayOfString = new String[5];
			arrayOfString[0] = "_id";
			arrayOfString[1] = "apn";
			arrayOfString[2] = "name";
			arrayOfString[3] = "type";
			arrayOfString[4] = "current";

			String match ="";
			if(stat){
				match = "apn like '%no3g' and current is not null";
				Log.d("MATCH", match);
			}else{
				match = "current is not null";
				Log.d("MATCH", match);
			}	
			Cursor cursor = context.getContentResolver().query(Uri.parse("content://telephony/carriers"), arrayOfString, match, null, null);
			cursor.moveToFirst();
			if(!cursor.isLast() && cursor.getCount()>0)
				do{
					if(cursor.getString(cursor.getColumnIndex("type")).indexOf("default") >=0)
						edit3gStat(context, 
								cursor.getString(cursor.getColumnIndex("_id")), 
								cursor.getString(cursor.getColumnIndex("type")),
								cursor.getString(cursor.getColumnIndex("apn")),
								stat);
					//if()
					//	Log.d("EEEEEE", " "+cursor.getString(cursor.getColumnIndex("name"))+ " " +cursor.getString(cursor.getColumnIndex("current")));
				}while(cursor.moveToNext());
			cursor.close();

		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void allow3g(Context context){
		parcours3g(context, true);
	}

	public void disallow3g(Context context){
		parcours3g(context, false);
	}

	private void setAirPlane(boolean state){
		android.provider.Settings.System.putInt(_context.getContentResolver(),
				android.provider.Settings.System.AIRPLANE_MODE_ON,state ? 1: 0);
		Intent send = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		send.putExtra("state", 0);
		_context.sendBroadcast(send);
	}

	private boolean isInit(){
		return _is_init;
	}

	private void init(Context context){
		_context = context;
		_shared = _context.getSharedPreferences("AIRPLANEMODEAPP",0);
		_listener_phone = new ListenPhone(this, context);
		_is_init = true;
		wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}

	private boolean attemptAirPlane(){
		if(!_is_calling){
			setAirPlane(true);
			_try_shut = false;
			Log.d("ATTEMPTAIR PLANE","OK");
			return true;
		}
		Log.d("ATTEMPTAIR PLANE","NO > call pending");
		return false;
	}

	void callStop(){
		Log.d("CALL","OFF");
		_is_calling = false;
		if(_try_shut && _shared.getBoolean("AIRPLANE", false))
			_try_shut = attemptAirPlane();
	}

	void callStart(){
		Log.d("CALL","ON");
		_is_calling = true;
	}


	private boolean isWifiAPSet() {
		int state = WIFI_AP_STATE_UNKNOWN;
		try {
			Method method2 = wifi.getClass().getMethod("getWifiApState");
			state = (Integer) method2.invoke(wifi);
		} catch (Exception e) {}
		return state == WIFI_AP_STATE_ENABLED || state == WIFI_AP_STATE_ENABLING;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if(!isInit()){
			init(context);
		}

		if(intent.getAction().equals("android.intent.action.PHONE_STATE")){
			
		}else if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) && _shared.getBoolean("AUTO", false)){
			Intent i = new Intent(context, AirPlaneService.class);
			context.startService(i);
		}else{
			//we received a screen changed event
			//if we want to switch off network or not
			event(intent.getAction().equals(Intent.ACTION_SCREEN_ON));
		}

	}

	public void event(boolean state){
		Log.d("LOG","SCREEN");
		if(!this.isWifiAPSet()){
			if(_shared.getBoolean("AIRPLANE", false)){
				if (!state) {
					Log.d("AIRPLANE","OFF");

					//if we could not shut the air plane mode
					//cause we had a phone call in progress
					//then we wait until we receive a stopcall and that the screen is off
					_try_shut = attemptAirPlane() == false;


					_is_screen_on = false;
				} else if (state) {
					//intent.getAction().equals(Intent.ACTION_SCREEN_ON)
					Log.d("AIRPLANE","ON");
					setAirPlane(false);
					/*android.provider.Settings.System.putInt(context.getContentResolver(),
						android.provider.Settings.System.AIRPLANE_MODE_ON,0);
				Intent send = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
				send.putExtra("state", 0);
				context.sendBroadcast(send);*/
					_is_screen_on = true;
					_try_shut = false;
				}
			}

			//if we want to switch off data or on
			if(_shared.getBoolean("DATA3G", false)){
				if (!state) {
					//intent.getAction().equals(Intent.ACTION_SCREEN_OFF)
					Log.d("DATA 3G","OFF");
					this.disallow3gData(_context);
					_is_screen_on = false;
				} else if (state) {
					//intent.getAction().equals(Intent.ACTION_SCREEN_ON)
					Log.d("DATA 3G","ON");
					this.allow3gData(_context);
					_is_screen_on = true;
				}
			}

			//if we want to switch off 3g or on
			if(_shared.getBoolean("3G", false)){
				if (!state) {
					Log.d("3G","OFF");
					this.disallow3g(_context);
					_is_screen_on = false;
				} else if (state) {
					Log.d("3G","ON");
					this.allow3g(_context);
					_is_screen_on = true;
				}
			}
		}
	}

}
