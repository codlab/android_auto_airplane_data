package eu.codlab.airplane;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;


public class ListenPhone extends PhoneStateListener {
	TelephonyManager _telephony;
	ScreenStateReceiver _parent;

	boolean _is_calling;
	private ListenPhone(){
		_parent = null;
		_telephony = null;
	}

	public ListenPhone(ScreenStateReceiver parent, Context context){
		this();
		Log.d("ListenPhone","CREATE");
		Log.d("ListenPhone","CREATE");
		Log.d("ListenPhone","CREATE");
		Log.d("ListenPhone","CREATE");
		Log.d("ListenPhone","CREATE");
		Log.d("ListenPhone","CREATE");
		Log.d("ListenPhone","CREATE");
		Log.d("ListenPhone","CREATE");
		_parent = parent;
		_telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);  
		_telephony.listen(this,PhoneStateListener.LISTEN_CALL_STATE);
		reset();
	}

	public void reset(){
		changeState(_telephony.getCallState());
	}

	public void changeState(int state){
		Log.d("CALL STATE","changeState");
		Log.d("CALL STATE","changeState");
		Log.d("CALL STATE","changeState");
		Log.d("CALL STATE","changeState");
		Log.d("CALL STATE","changeState"+state);
		Log.d("CALL STATE","changeState");
		Log.d("CALL STATE","changeState");
		Log.d("CALL STATE","changeState");
		Log.d("CALL STATE","changeState");
		Log.d("CALL STATE","changeState");
		switch(state){
		case TelephonyManager.CALL_STATE_IDLE:
			sendParentCallStop();
			break;
		case TelephonyManager.CALL_STATE_OFFHOOK:
		case TelephonyManager.CALL_STATE_RINGING:
			sendParentCallStart();
			break;
		default:
		}
	}
	
	@Override
	public void onCallStateChanged(int state, String number){
		changeState(state);
	}

	private void sendParentCallStop(){
		_parent.callStop();
	}

	private void sendParentCallStart(){
		_parent.callStart();
	}
}
