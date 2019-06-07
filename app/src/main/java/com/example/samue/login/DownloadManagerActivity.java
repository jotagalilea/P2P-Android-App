package com.example.samue.login;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by jotagalilea on 12/03/2019.
 *
 * Actividad que agrupa las descargas.
 */

public class DownloadManagerActivity extends AppCompatActivity {

	private ArrayList<Download> al_downloads;
	private DownloadService downloadService;
	private ListView dl_listView;
	private DownloadListAdapter listAdapter;
	private Timer timer;
	private boolean serviceBound = false;
	private ServiceConnection serviceConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			DownloadService.DownloadBinder binder = (DownloadService.DownloadBinder) iBinder;
			downloadService = binder.getService();
			serviceBound = true;
			prepareActivity();
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			serviceBound = false;
			Log.e("ERROR EN DESCARGA", "SERVICIO DESCONECTADO INESPERADAMENTE");
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download_manager);
		Toolbar toolbar = findViewById(R.id.download_toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle("Descargas");

		Intent intent = getIntent();
		Intent serviceIntent = intent.getParcelableExtra("downloadServiceIntent");
		boolean bound = bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
		//System.out.print(bound);
	}


	private void prepareActivity(){
		if (serviceBound) {
			al_downloads = downloadService.getDownloads();
			listAdapter = new DownloadListAdapter(getApplicationContext(), al_downloads);
			dl_listView = findViewById(R.id.downloads_list);
			dl_listView.setAdapter(listAdapter);

			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					updateGUI();
				}
			}, 0, 1000);
		}
	}



	// Va actualizando las descargas y notificando al adapter.
	private void updateGUI(){
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				listAdapter.notifyDataSetChanged();
			}
		});
	}


}
