package eu.codlab.airplane;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.IBinder;
import android.util.Log;

public class AirPlaneService extends Service implements OnSharedPreferenceChangeListener{
	private SharedPreferences state;
	static boolean isEnabled;
	static BroadcastReceiver mReceiver;
	boolean _3g;
	boolean _3gdata;
	boolean _air;
	boolean _registered = false;

	BroadcastReceiver getReceiver(){
		if(mReceiver == null)
			mReceiver = new ScreenStateReceiver(this.getApplicationContext());
		return mReceiver;
	}

	void register(){
		if(!_registered){
			_registered = true;
			Log.d("register","ok");
			IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
			filter.addAction(Intent.ACTION_SCREEN_OFF);
			//getReceiver().reset(_context);
			registerReceiver(getReceiver(), filter);
		}
	}

	void unregister(){
		if(_registered){
			_registered = false;
			Log.d("register","off");
			try{
				this.unregisterReceiver(getReceiver());		
			}catch(Exception e){
				//e.printStackTrace();
			}
		}
	}

	void creation(){
		state = this.getSharedPreferences("AIRPLANEMODEAPP",0);
		state.registerOnSharedPreferenceChangeListener(this);
		_air = state.getBoolean("AIRPLANE", false);
		_3g = state.getBoolean("3G", false);
		_3gdata = state.getBoolean("DATA3G", false);
		if(_air || _3g || _3gdata)
			register();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate(){
		super.onCreate();
		creation();
	}

	@Override 
	public int onStartCommand(Intent intent, int flags, int startId) { 
		creation();
		return Service.START_STICKY; 
	} 

	@Override 
	public void onDestroy(){
		if(state != null)
			state.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();

	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if("AIRPLANE".equals(key)){
			_air = sharedPreferences.getBoolean(key, false);
		}else if("DATA3G".equals(key)){
			_3gdata = sharedPreferences.getBoolean(key, false);
		}else if("3G".equals(key)){
			_3g = sharedPreferences.getBoolean(key, false);
		}
		if(_air || _3g || _3gdata)
			register();
		else
			unregister();
	}
}
