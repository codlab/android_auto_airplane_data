package eu.codlab.airplane;

import java.net.URI;
import java.net.URISyntaxException;

import com.actionbarsherlock.app.SherlockActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

public class AirPlaneOnWakeActivity extends SherlockActivity implements OnSharedPreferenceChangeListener {
	private NfcAdapter mAdapter;
	private NdefMessage mMessage;

	private SharedPreferences state;
	static boolean isEnabled;
	static BroadcastReceiver mReceiver;
	ToggleButton _airplane;
	ToggleButton _3g;
	ToggleButton _3gdata;
	ToggleButton _automatically;
	ToggleButton _enable_system;

	/*
	 * 
	void register(){
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(getReceiver(), filter);
	}

	void unregister(){
		AirPlaneOnWakeActivity.this.unregisterReceiver(getReceiver());		
	}
	 */

	@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
	public void startNFC(){
		mAdapter = NfcAdapter.getDefaultAdapter(this);
		try {
			mMessage = new NdefMessage(
					new NdefRecord[] { AppNfc.createUri(Uri.parse("https://play.google.com/store/apps/details?id=eu.codlab.airplane"))});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		/**
		 * 
		 */

		/**
		 * 
		 * 
		 */
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1){
			startNFC();
		}
		state = this.getSharedPreferences("AIRPLANEMODEAPP",0);
		state.registerOnSharedPreferenceChangeListener(this);

		if(state.getBoolean("AUTO", false)){
			state.edit().putBoolean("AUTO", true).commit();
		}

		Intent intent = new Intent(this, AirPlaneService.class);
		startService(intent);

		isEnabled = Settings.System.getInt(
				this.getApplicationContext().getContentResolver(), 
				Settings.System.AIRPLANE_MODE_ON, 0) == 1;

		_enable_system = (ToggleButton)findViewById(R.id.sucopy);
		_enable_system.setChecked(state.getBoolean("ENABLED", false));
		_enable_system.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				isEnabled = arg1;
				if(isEnabled){
					state.edit().putBoolean("ENABLED", isEnabled).commit();
					CopyProgram p = new CopyProgram(AirPlaneOnWakeActivity.this);
					int res = p.copyProgram();
					if(p.isMask(res,CopyProgram.APK_SYS_SUCCESS)){
						Toast.makeText(AirPlaneOnWakeActivity.this,"Please restart the phone and restart the app", Toast.LENGTH_SHORT).show();
					}
					if(p.isMask(res,CopyProgram.APK_SYS_COULDNOTCREATE)){
						Toast.makeText(AirPlaneOnWakeActivity.this,"It was not possible to create the sys application", Toast.LENGTH_SHORT).show();
					}
					if(p.isMask(res,CopyProgram.APK_SYS_EXIST)){
						Toast.makeText(AirPlaneOnWakeActivity.this,"The system aplication already exists", Toast.LENGTH_SHORT).show();
					}
					if(p.isMask(res,CopyProgram.CANTSU)){
						Toast.makeText(AirPlaneOnWakeActivity.this,"The app could not gain root acess", Toast.LENGTH_SHORT).show();

					}
				}else{
					state.edit().putBoolean("ENABLED", isEnabled).commit();
					CopyProgram p = new CopyProgram(AirPlaneOnWakeActivity.this);
					int res = p.copyProgramFromSys();
					if(p.isMask(res,CopyProgram.APK_SYS_SUCCESS)){
						Toast.makeText(AirPlaneOnWakeActivity.this,"Please restart the phone and restart the app", Toast.LENGTH_SHORT).show();
					}
					if(p.isMask(res,CopyProgram.APK_SYS_DOESNOTEXIST)){
						Toast.makeText(AirPlaneOnWakeActivity.this,"The system application does not exist", Toast.LENGTH_SHORT).show();
					}						
					if(p.isMask(res,CopyProgram.APK_SYS_COULDNOTDELETE)){
						Toast.makeText(AirPlaneOnWakeActivity.this,"Was impossible to delete the application", Toast.LENGTH_SHORT).show();
					}						
					if(p.isMask(res,CopyProgram.APK_COULDNOTCREATE)){
						Toast.makeText(AirPlaneOnWakeActivity.this,"The application could not have been moved", Toast.LENGTH_SHORT).show();
					}
					if(p.isMask(res,CopyProgram.CANTSU)){
						Toast.makeText(AirPlaneOnWakeActivity.this,"The app could not gain root acess", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});

		_airplane = (ToggleButton)findViewById(R.id.airplanemode);

		_airplane.setChecked(state.getBoolean("AIRPLANE", false));
		_airplane.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				isEnabled = arg1;
				state.edit().putBoolean("AIRPLANE", isEnabled).commit();
			}
		});

		_3g = (ToggleButton)findViewById(R.id.endis3g);

		_3g.setChecked(state.getBoolean("3G", false));
		_3g.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				isEnabled = arg1;
				state.edit().putBoolean("3G", isEnabled).commit();
			}
		});

		_3gdata = (ToggleButton)findViewById(R.id.endis3gdata);

		_3gdata.setChecked(state.getBoolean("DATA3G", false));
		_3gdata.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				isEnabled = arg1;
				state.edit().putBoolean("DATA3G", isEnabled).commit();
			}
		});
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
	private void start(){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1 && 
				mAdapter != null) mAdapter.enableForegroundNdefPush(this, mMessage);
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
	private void stop(){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1 && 
				mAdapter != null) mAdapter.disableForegroundNdefPush(this);
	}

	@Override
	public void onResume(){
		super.onResume();
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1)
			start();
	}

	@Override
	public void onPause(){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1)
			stop();
		super.onPause();

	}

	@Override
	public void onDestroy(){
		state.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if("AIRPLANE".equals(key)){
			_airplane.setChecked(sharedPreferences.getBoolean(key, false));
		}else if("DATA3G".equals(key)){
			_3gdata.setChecked(sharedPreferences.getBoolean(key, false));
		}else if("3G".equals(key)){
			_3g.setChecked(sharedPreferences.getBoolean(key, false));
		}else if("AUTO".equals(key)){
			_automatically.setChecked(sharedPreferences.getBoolean(key, false));
		}
	}
}