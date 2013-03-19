package eu.codlab.airplanefree;

import com.actionbarsherlock.app.SherlockActivity;

import eu.codlab.airplane.AirPlaneService;
import eu.codlab.airplane.AppNfc;
import eu.codlab.airplane.CopyProgram;
import eu.codlab.airplanefree.R;
import eu.codlab.airplane.ShellCommand;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class AirPlaneOnWakeActivityFreeActivity extends SherlockActivity implements OnSharedPreferenceChangeListener {
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

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1){
			startNFC();
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		state = this.getSharedPreferences("AIRPLANEMODEAPP",0);
		state.registerOnSharedPreferenceChangeListener(this);
		Intent intent = new Intent(this, AirPlaneService.class);
		startService(intent);

		isEnabled = Settings.System.getInt(
				this.getApplicationContext().getContentResolver(), 
				Settings.System.AIRPLANE_MODE_ON, 0) == 1;

		_enable_system = (ToggleButton)findViewById(R.id.sucopy);


		//CopyProgram cp = new CopyProgram(AirPlaneOnWakeActivityFreeActivity.this);
		_enable_system.setChecked(false);//cp.existProgramSys());
		ShellCommand sh = new ShellCommand();

		_enable_system.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				_enable_system.setChecked(false);
				showPay();
			}
		});

		_automatically = (ToggleButton)findViewById(R.id.automatically);

		_automatically.setChecked(false);
		state.edit().putBoolean("AUTO", false).commit();
		_automatically.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				isEnabled = arg1;
				_automatically.setChecked(false);
				state.edit().putBoolean("AUTO", false).commit();
				showPay();
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

		_3g.setChecked(false);
		state.edit().putBoolean("3G", false).commit();
		_3g.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				isEnabled = arg1;
				_3g.setChecked(false);
				state.edit().putBoolean("3G", false).commit();
				showPay();
			}
		});

		Button b = (Button)findViewById(R.id.buymarket);
		b.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent goToMarket = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=eu.codlab.airplane"));
				startActivity(goToMarket);
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
	private void showPay(){
		Toast.makeText(this, "To use this feature, please consider to buy the paid version with everything unlocked", Toast.LENGTH_LONG).show();
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